package apigen.java;

//{{{ imports

import java.io.*;
import java.util.*;

import aterm.*;
import aterm.pure.PureFactory;

import apigen.*;

//}}}

public class JavaGen
{
  //{{{ private final static String[] SPECIAL_CHAR_WORDS =

  private final static String[] SPECIAL_CHAR_WORDS =
  { "[BracketOpen", "]BracketClose",
    "{BraceOpen",   "}BraceClose",
    "(ParenOpen",   ")ParenClose",
    "<LessThan",    ">GreaterThan",
    "|Bar",         "&Amp",
    "+Plus",	    ",Comma",
    ".Period",	    "~Tilde",
    ":Colon",	    ";SemiColon",
    "=Equals",	    "#Hash",
    "/Slash",	    "\\Backslash",
  };

  //}}}
  //{{{ private final static String[][] RESERVED_TYPES =

  private final static String[][] RESERVED_TYPES =
  { { "int",  "int"    },
    { "real", "double" },
    { "str",  "String" }
  };

  //}}}

  public static boolean verbose = false;

  private aterm.ATermFactory factory;

  private String basedir;
  private String pkg;
  private List	 imports;
  private String class_name;

  private InputStream input;
  private PrintStream stream;

  private Map    specialChars;
  private char   last_special_char;
  private String last_special_char_word;

  private Map    reservedTypes;

  //{{{ private static void usage()

  private static void usage()
  {
    System.err.println("usage: JavaGen [options]");
    System.err.println("options:");
    System.err.println("\t-verbose                  [off]");
    System.err.println("\t-package <package>        [\"\"]");
    System.err.println("\t-basedir <basedir>        [\".\"]");
    System.err.println("\t-import <package>         (can be repeated)");
    System.err.println("\t-input <in>               [-]");
    System.exit(1);
  }

  //}}}
  //{{{ public static String capitalize(String s)

  public static String capitalize(String s)
  {
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  //}}}

  //{{{ public final static void main(String[] args)

  public final static void main(String[] args)
    throws IOException
  {
    String pkg = "";
    String input  = "-";
    String basedir = ".";
    List imports = new LinkedList();
    InputStream inputStream;

    if (args.length == 0) {
      usage();
    }

    for (int i=0; i<args.length; i++) {
      if ("-help".startsWith(args[i])) {
	usage();
      } else if ("-verbose".startsWith(args[i])) {
	verbose = true;
      } else if ("-package".startsWith(args[i])) {
	pkg = args[++i];
      } else if ("-basedir".startsWith(args[i])) {
	basedir = args[++i];
      } else if ("-import".startsWith(args[i])) {
	imports.add(args[++i]);
      } else if ("-input".startsWith(args[i])) {
	input = args[++i];
      } else {
	usage();
      }
    }

    if (input.equals("-")) {
      inputStream = System.in;
    } else {
      inputStream = new FileInputStream(input);
    }
    JavaGen gen = new JavaGen(inputStream, basedir, pkg, imports);
  }

  //}}}

  //{{{ public JavaGen(InputStream input, String basedir, String pkg, List imports)

  public JavaGen(InputStream input, String basedir, String pkg, List imports)
    throws IOException
  {
    this.input   = input;
    this.basedir = basedir;
    this.pkg	 = pkg;
    this.imports = imports;
    specialChars = new HashMap();
    reservedTypes = new HashMap();

    for (int i=0; i<SPECIAL_CHAR_WORDS.length; i++) {
      String word = SPECIAL_CHAR_WORDS[i];
      specialChars.put(new Character(word.charAt(0)), word.substring(1));
    }

    for (int i=0; i<RESERVED_TYPES.length; i++) {
      reservedTypes.put(RESERVED_TYPES[i][0], RESERVED_TYPES[i][1]);
    }

    factory = new PureFactory();

    aterm.ATerm adt = factory.readFromFile(input);

    API api = new API(adt);

    Iterator types = api.typeIterator();
    while (types.hasNext()) {
      Type type = (Type)types.next();
      genTypeClasses(type);
    }
  }

  //}}}

  //{{{ private void genTypeClasses(Type type)

  private void genTypeClasses(Type type)
    throws IOException
  {
    class_name = buildClassName(type);
    char sep = File.separatorChar;
    String path = basedir + sep + pkg.replace('.', sep) + sep + class_name + ".java";
    stream = new PrintStream(new FileOutputStream(path));

    info("generating " + path);

    println("package " + pkg + ";");
    println();

    printFoldOpen(0, "imports");
    println("import java.util.*;");
    println("import aterm.*;");
    println();
    Iterator iter = imports.iterator();
    while (iter.hasNext()) {
      println("import " + (String)iter.next() + ";");
    }
    printFoldClose(0);
    println();

    println("abstract public class " + class_name);
    println("{");
    println("  static boolean initialized = false;");
    println();
    genPatternAttrs(type);
    println();
    println("  aterm.ATerm term;");

    println();
    genFactoryMethod(type);
    genToTerm();
    genPatternInit(type);
    genAltFactoryMethods(type);
    genConstructor();
    genEquals();
    genAccessors(type);
    println("}");

    Iterator alt_iter = type.alternativeIterator();
    while (alt_iter.hasNext()) {
      println();
      Alternative alt = (Alternative)alt_iter.next();
      genAltClass(type, alt);
    }
  }

  //}}}
  //{{{ private void genPatternAttrs(Type type)

  private void genPatternAttrs(Type type)
  {
    printFoldOpen("pattern attributes");
    Iterator iter = type.alternativeIterator();
    while (iter.hasNext()) {
      Alternative alt = (Alternative)iter.next();
      println("  static aterm.ATerm pat" + buildId(alt.getId()) + ";");
    }
    printFoldClose();
  }

  //}}}
  //{{{ private void genFactoryMethod(Type type)

  private void genFactoryMethod(Type type)
  {
    String decl = "public static " + class_name + " fromTerm(aterm.ATerm term)";
    printFoldOpen(decl);
    println("  " + decl);
    println("  {");
    println("    List args;");
    println();
    printFoldOpen(2, "initialize patterns");
    println("    if (!initialized) {");
    println("      initializePatterns(term.getFactory());");
    println("    }");
    printFoldClose(2);
    println();

    Iterator iter = type.alternativeIterator();
    while (iter.hasNext()) {
      Alternative alt = (Alternative)iter.next();
      String alt_id   = buildId(alt.getId());
      String alt_class = buildAltClassName(type, alt);
      println("    args = term.match(pat" + alt_id + ");");
      println("    if (args != null) {");
      println("      return new " + alt_class + "(term, args);");
      println("    }");
      println();
    }
    println("    throw new RuntimeException(\"illegal " + type.getId() + "\");");
    println("  }");
    printFoldClose();
    println();
  }

  //}}}
  //{{{ private void genAltFactoryMethods(Type type)

  private void genAltFactoryMethods(Type type)
  {
    String type_id = buildId(type.getId());
    Iterator alts = type.alternativeIterator();
    while (alts.hasNext()) {
      Alternative alt = (Alternative)alts.next();
      String method = buildId("make-" + alt.getId());
      String formals = buildAltFormals(type, alt);
      String decl = "public static " + type_id + " "
	+ method + "(" + formals + ")";
      String actuals = buildAltActuals(type, alt);
      String alt_class = buildAltClassName(type, alt);
      printFoldOpen(decl);
      println("  " + decl);
      println("  {");
      println("    return new " + alt_class + "(" + actuals + ");");
      println("  }");
      printFoldClose();
    }
  }

  //}}}
  //{{{ private void genPatternInit(Type type)

  private void genPatternInit(Type type)
  {
    String decl = "static void initializePatterns(ATermFactory factory)";
    printFoldOpen("  " + decl);
    println("  " + decl);
    println("  {");
    Iterator iter = type.alternativeIterator();
    while (iter.hasNext()) {
      Alternative alt = (Alternative)iter.next();
      println("    pat" + buildId(alt.getId()) + " = factory.parse(\""
	      + escapeQuotes(alt.buildMatchPattern().toString()) + "\");");
    }
    println("    initialized = true;");
    println("  }");
    printFoldClose();
  }

  //}}}

  //{{{ private String buildAltFormals(Type type, Alternative alt)

  private String buildAltFormals(Type type, Alternative alt)
  {
    StringBuffer buf = new StringBuffer();
    
    Iterator fields = type.altFieldIterator(alt.getId());
    while (fields.hasNext()) {
      Field field = (Field)fields.next();
      String field_name = buildFieldId(field.getId());
      String field_type = buildTypeId(field.getType());
      buf.append(field_type + " " + field_name);
      if (fields.hasNext()) {
	buf.append(", ");
      }
    }

    return buf.toString();
  }

  //}}}
  //{{{ private String buildAltActuals(Type type, Alternative alt)

  private String buildAltActuals(Type type, Alternative alt)
  {
    StringBuffer buf = new StringBuffer();
    
    Iterator fields = type.altFieldIterator(alt.getId());
    while (fields.hasNext()) {
      Field field = (Field)fields.next();
      String field_name = buildFieldId(field.getId());
      buf.append(field_name);
      if (fields.hasNext()) {
	buf.append(", ");
      }
    }

    return buf.toString();
  }

  //}}}
  
  //{{{ private void genConstructor()

  private void genConstructor()
  {
    String decl = class_name + "(aterm.ATerm term)";
    printFoldOpen(decl);
    println("  " + decl);
    println("  {");
    println("    this.term = term;");
    println("  }");
    printFoldClose();
    println();
  }

  //}}}
  //{{{ private void genEquals()

  private void genEquals()
  {
    String decl = "public boolean isEqual(" + class_name + " peer)";

    printFoldOpen(decl);
    println("  " + decl);
    println("  {");
    println("    return toTerm().equals(peer.toTerm());");
    println("  }");
    printFoldClose();

    decl = "public boolean equals(Object peer)";
    printFoldOpen(decl);
    println("  " + decl);
    println("  {");
    println("    if (peer instanceof " + class_name + ") {");
    println("      return isEqual((" + class_name + ")peer);");
    println("    }");
    println();
    println("    return false;");
    println("  }");
    printFoldClose();
  }

  //}}}

  //{{{ private void genToTerm()

  private void genToTerm()
  {
    String decl = "abstract public aterm.ATerm toTerm(aterm.ATermFactory factory)";
    println("  " + decl + ";");

    decl = "public aterm.ATerm toTerm()";
    printFoldOpen(decl);
    println("  " + decl);
    println("  {");
    println("    return toTerm(null);");
    println("  }");
    printFoldClose();
    println();

    /*
    decl = "aterm.ATerm getTerm()";
    printFoldOpen(decl);
    println("  " + decl);
    println("  {");
    println("    return term;");
    println("  }");
    printFoldClose();
    println();
    */
  }

  //}}}

  //{{{ private void genAccessors(Type type)

  private void genAccessors(Type type)
  {
    printFoldOpen("abstract public boolean isXXX() methods");
    Iterator alts = type.alternativeIterator();
    while (alts.hasNext()) {
      Alternative alt = (Alternative)alts.next();
      String methodName = buildId("is-" + alt.getId());
      println("  abstract public boolean " + methodName + "();");
    }
    printFoldClose();

    printFoldOpen("abstract public boolean hasXXX() methods");
    Iterator fields = type.fieldIterator();
    while (fields.hasNext()) {
      Field field = (Field)fields.next();
      String methodName = buildId("has-" + field.getId());
      println("  abstract public boolean " + methodName + "();");
    }
    printFoldClose();

    printFoldOpen("abstract public XXX getXXX() methods");
    fields = type.fieldIterator();
    while (fields.hasNext()) {
      Field field = (Field)fields.next();
      String methodName = buildId("get-" + field.getId());
      String methodType = buildTypeId(field.getType());
      println("  abstract public " + methodType + " " + methodName + "();");
    }
    printFoldClose();

    printFoldOpen("abstract public " + class_name + " setXXX() methods");
    fields = type.fieldIterator();
    while (fields.hasNext()) {
      Field field = (Field)fields.next();
      String methodName = buildId("set-" + field.getId());
      String argName = buildFieldId(field.getId());
      String argType = buildTypeId(field.getType());
      println("  abstract public " + class_name + " " + methodName
	      + "(" + argType + " " + argName +");");
    }
    printFoldClose();
  }

  //}}}

  //{{{ private void genAltClass(Type type, Alternative alt)

  private void genAltClass(Type type, Alternative alt)
  {
    String type_id = buildId(type.getId());
    String alt_class = buildAltClassName(type, alt);
    println("class " + alt_class);
    println("  extends " + type_id);
    println("{");
    genAttributes(type, alt);
    println();

    genAltConstructors(type, alt);
    genAltToTerm(type, alt);
    genAltIsMethods(type, alt);
    genAltHasMethods(type, alt);
    genAltGetMethods(type, alt);
    genAltSetMethods(type, alt);

    println("}");
  }

  //}}}
  //{{{ private void genAttributes(Type type, Alternative alt)

  private void genAttributes(Type type, Alternative alt)
  {
    Iterator iter = type.altFieldIterator(alt.getId());
    while (iter.hasNext()) {
      Field field = (Field)iter.next();
      String field_name = buildFieldId(field.getId());
      String field_type = buildTypeId(field.getType());

      println("  private " + field_type + " " + field_name + ";");
    }
  }

  //}}}

  //{{{ private void genAltConstructors(Type type, Alternative alt)

  private void genAltConstructors(Type type, Alternative alt)
  {
    String alt_class = buildAltClassName(type, alt);
    String decl = alt_class + "(aterm.ATerm term, List args)";
    printFoldOpen(decl);
    println("  " + decl);
    println("  {");
    println("    super(term);");
    println();

    println("    Iterator iter = args.iterator();");
    Iterator fields = type.altFieldIterator(alt.getId());
    while (fields.hasNext()) {
      Field field = (Field)fields.next();
      String field_name = buildFieldId(field.getId());
      String field_type = buildTypeId(field.getType());
      if (field_type.equals("int")) {
	println("    this." + field_name
		+ " = ((Integer)iter.next()).intValue();");
      } else if (field_type.equals("real")) {
	println("    this." + field_name
		+ " = ((Double)iter.next()).doubleValue();");
      } else {
	println("    this." + field_name + " = (" + field_type + ")iter.next();");
      }
    }
    println("    if (iter.hasNext()) {");
    println("      throw new RuntimeException(\"too many elements?\");");
    println("    }");

    println("  }");
    printFoldClose();

    decl = alt_class + "(" + buildAltFormals(type, alt) + ")";

    printFoldOpen(decl);
    println("  " + decl);
    println("  {");
    println("    super(null);");
    println();

    fields = type.altFieldIterator(alt.getId());
    while (fields.hasNext()) {
      Field field = (Field)fields.next();
      String field_name = buildFieldId(field.getId());
      println("    this." + field_name + " = " + field_name + ";");
    }

    println("  }");
    printFoldClose();
    println();
  }

  //}}}
  //{{{ private void genAltToTerm(Type type, Alternative alt)

  private void genAltToTerm(Type type, Alternative alt)
  {
    String decl = "public aterm.ATerm toTerm(aterm.ATermFactory factory)";
    printFoldOpen(decl);
    println("  " + decl);
    println("  {");
    println("    if (super.term == null) {");
    println("      if (!initialized) {");
    println("        initializePatterns(factory);");
    println("      }");
    println("      List args = new LinkedList();");
    Iterator iter = type.altFieldIterator(alt.getId());
    while (iter.hasNext()) {
      Field field = (Field)iter.next();
      String field_name = buildFieldId(field.getId());
      String field_type = field.getType();
      if (field_type.equals("int")) {
	println("      args.add(new Integer(this." + field_name + "));");
      } else if (field_type.equals("real")) {
	println("      args.add(new Double(this." + field_name + "));");
      } else {
	println("      args.add(this." + field_name + ");");
      }
    }
    println();
    String alt_id = buildId(alt.getId());
    println("      super.term = factory.make(pat" + alt_id + ", args);"); 
    println("      return super.term;");
    println("    }");
    println();

    println("    if (term.getFactory() == factory || factory == null) {");
    println("      return super.term;");
    println("    } else {");
    println("      return factory.importTerm(super.term);");
    println("    }");
    println("  }");
    printFoldClose();
  }

  //}}}
  //{{{ private void genAltIsMethods(Type type, Alternative alt)

  private void genAltIsMethods(Type type, Alternative alt)
  {
    printFoldOpen("public boolean isXXX() methods");

    Iterator alts = type.alternativeIterator();
    while (alts.hasNext()) {
      Alternative curAlt = (Alternative)alts.next();
      String decl = "public boolean " + buildId("is-" + curAlt.getId()) + "()";
      printFoldOpen(decl);
      println("  " + decl);
      println("  {");
      if (curAlt == alt) {
	println("    return true;");
      }
      else {
	println("    return false;");
      }
      println("  }");
      printFoldClose();
    }
    
    printFoldClose();
  }

  //}}}
  //{{{ private void genAltHasMethods(Type type, Alternative alt)

  private void genAltHasMethods(Type type, Alternative alt)
  {
    printFoldOpen("public boolean hasXXX() methods");

    String altId = alt.getId();
    Iterator fields = type.fieldIterator();
    while (fields.hasNext()) {
      Field field = (Field)fields.next();
      String fieldId = buildId("has-" + field.getId());
      String decl = "public boolean " + fieldId + "()";
      printFoldOpen(decl);
      println("  " + decl);
      println("  {");
      if (field.hasAltId(altId)) {
	println("    return true;");
      }
      else {
	println("    return false;");
      }
      println("  }");
      printFoldClose();
    }

    printFoldClose();
  }

  //}}}
  //{{{ private void genAltGetMethods(Type type, Alternative alt)

  private void genAltGetMethods(Type type, Alternative alt)
  {
    printFoldOpen("public getXXX() methods");

    String altId = alt.getId();
    Iterator fields = type.fieldIterator();
    while (fields.hasNext()) {
      Field field = (Field)fields.next();
      String fieldId = buildFieldId(field.getId());
      String methodName = buildId("get-" + field.getId());
      String methodType = buildTypeId(field.getType());
      String decl = "public " + methodType + " " + methodName + "()";
      printFoldOpen(decl);
      println("  " + decl);
      println("  {");
      if (field.hasAltId(altId)) {
	println("    return " + fieldId + ";");
      }
      else {
	println("    throw new RuntimeException(\"" + altId + " has no "
		+ fieldId + "\");");
      }
      println("  }");
      printFoldClose();
    }

    printFoldClose();
  }

  //}}}
  //{{{ private void genAltSetMethods(Type type, Alternative alt)

  private void genAltSetMethods(Type type, Alternative alt)
  {
    printFoldOpen("public setXXX() methods");

    String altId = alt.getId();
    Iterator fields = type.fieldIterator();
    while (fields.hasNext()) {
      Field field = (Field)fields.next();
      String methodName = buildId("set-" + field.getId());
      String argType = buildTypeId(field.getType());
      String argName = buildFieldId(field.getId());
      String decl = "public " + class_name + " " + methodName
	+ "(" + argType + " " + argName + ")";
      printFoldOpen(decl);
      println("  " + decl);
      println("  {");
      if (field.hasAltId(altId)) {
	String alt_class = buildAltClassName(type, alt);
	String actuals = buildAltActuals(type, alt);
	println("    return new " + alt_class + "(" + actuals + ");");
      }
      else {
	println("    throw new RuntimeException(\"" + altId + " has no "
		+ argName + "\");");
      }
      println("  }");
      printFoldClose();
    }

    printFoldClose();
  }

  //}}}

  //{{{ private String buildClassName(Type type)

  private String buildClassName(Type type)
  {
    String typeId = type.getId();

    String nativeType = (String)reservedTypes.get(typeId);

    if (nativeType != null) {
      return nativeType;
    }

    return buildId(type.getId());
  }

  //}}}
  //{{{ private String buildAltClassName(Type type, Alternative alt)

  private String buildAltClassName(Type type, Alternative alt)
  {
    return buildId(type.getId()) + "_" + buildId(alt.getId());
  }

  //}}}
  //{{{ private String buildTypeId(String typeId)

  private String buildTypeId(String typeId)
  {
    String nativeType = (String)reservedTypes.get(typeId);

    if (nativeType != null) {
      return nativeType;
    }

    return buildId(typeId);
  }

  //}}}
  //{{{ private String buildTypeId(String typeId)

  private String buildFieldId(String fieldId)
  {
    return "_" + buildId(fieldId);
  }

  //}}}
  //{{{ private String buildId(String id)

  private String buildId(String id)
  {
    StringBuffer buf = new StringBuffer();
    boolean cap_next = false;

    for (int i=0; i<id.length(); i++) {
      char c = id.charAt(i);
      if (isSpecialChar(c)) {
	buf.append(getSpecialCharWord(c));
	cap_next = true;
      } else {
	switch (c) {
	  case '-':
	    cap_next = true;
	    break;

	  default:
	    if (cap_next) {
	      buf.append(Character.toUpperCase(c));
	      cap_next = false;
	    } else {
	      buf.append(c);
	    }
	    break;
	}
      }
    }

    return buf.toString();
  }

  //}}}
  //{{{ private boolean isSpecialChar(char c)

  private boolean isSpecialChar(char c)
  {
    return getSpecialCharWord(c) != null;
  }

  //}}}
  //{{{ private String getSpecialCharWord(char c)

  private String getSpecialCharWord(char c)
  {
    if (c == last_special_char) {
      return last_special_char_word;
    }

    last_special_char = c;
    last_special_char_word = (String)specialChars.get(new Character(c));

    return last_special_char_word;
  }

  //}}}

  //{{{ private boolean isReservedType(ATerm t)

  private boolean isReservedType(ATerm t)
  {
    return reservedTypes.containsKey(t.toString());
  }

  //}}}

  //{{{ private String escapeQuotes(String s)

  private String escapeQuotes(String s)
  {
    StringBuffer buf = new StringBuffer(s.length()*2);
    for (int i=0; i<s.length(); i++) {
      char c = s.charAt(i);
      if (c == '"' || c == '\\') {
	buf.append('\\');
      }
      buf.append(c);
    }
    return buf.toString();
  }

  //}}}

  //{{{ private void printFoldOpen(int fold_level, String comment)

  private void printFoldOpen(int fold_level, String comment)
  {
    for (int i=0; i<fold_level; i++) {
      print("  ");
    }

    println("//{{" + "{ " + comment);
    println();
  }

  //}}}
  //{{{ private void printFoldClose(int fold_level)

  private void printFoldClose(int fold_level)
  {
    println();

    for (int i=0; i<fold_level; i++) {
      print("  ");
    }

    println("//}}" + "}");
  }

  //}}}

  //{{{ private void printFoldOpen(String comment)

  private void printFoldOpen(String comment)
  {
    printFoldOpen(1, comment);
  }

  //}}}
  //{{{ private void printFoldClose()

  private void printFoldClose()
  {
    printFoldClose(1);
  }

  //}}}

  //{{{ private void println()

  private void println()
  {
    stream.println();
  }

  //}}}
  //{{{ private void println(String msg)

  private void println(String msg)
  {
    stream.println(msg);
  }

  //}}}
  //{{{ private void print(String msg)

  private void print(String msg)
  {
    stream.print(msg);
  }

  //}}}

  //{{{ private void info(String msg)

  private void info(String msg)
  {
    if (verbose) {
      System.err.println(msg);
    }
  }

  //}}}
}
