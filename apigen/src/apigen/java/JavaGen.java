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
  public static boolean verbose = false;

  private ATermFactory factory;

  private InputStream input;
  private String      basedir;
  private String      pkg;
  private List	      imports;

  private String class_name;
  private PrintStream stream;

  private Map specialChars;
  private char last_special_char;
  private String last_special_char_word;

  private int fold_level = 1;

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

    for (int i=0; i<SPECIAL_CHAR_WORDS.length; i++) {
      String word = SPECIAL_CHAR_WORDS[i];
      specialChars.put(new Character(word.charAt(0)), word.substring(1));
    }

    factory = new PureFactory();

    ATerm adt = factory.readFromFile(input);

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
    println("  private static boolean initialized = false;");
    println();
    genPatternAttrs(type);
    println();
    println("  ATerm term;");

    println();
    genFactoryMethod(type);
    genConstructor();
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
      println("  static ATerm pat" + buildId(alt.getId()) + ";");
    }
    printFoldClose();
  }

  //}}}
  //{{{ private void genFactoryMethod(Type type)

  private void genFactoryMethod(Type type)
  {
    String decl = "public static " + class_name + " fromTerm(ATerm term)";
    printFoldOpen(decl);
    println("  " + decl);
    println("  {");
    println("    List args;");
    println();
    printFoldOpen(2, "initialize patterns");
    println("    if (!initialized) {");
    println("      initialized = true;");
    genPatternInit(type);
    println("    }");
    printFoldClose(2);
    println();

    Iterator iter = type.alternativeIterator();
    while (iter.hasNext()) {
      Alternative alt = (Alternative)iter.next();
      String alt_id   = buildId(alt.getId());
      String alt_class = buildAltClassName(type, alt);
      println("    args = pat" + alt_id + ".match(term);");
      println("    if (args != null) {");
      println("      return new " + alt_class + "(term, args);");
      println("    }");
      println();
    }
    println("    throw new RuntimeException(\"illegal " + type.getId() + "\");");
    println("  }");
    printFoldClose();
  }

  //}}}
  //{{{ private void genPatternInit(Type type)

  private void genPatternInit(Type type)
  {
    Iterator iter = type.alternativeIterator();
    while (iter.hasNext()) {
      Alternative alt = (Alternative)iter.next();
      println("      pat" + buildId(alt.getId()) + " = term.getFactory().parse(\""
	      + escapeQuotes(alt.getPattern().toString()) + "\");");
    }
  }

  //}}}
  
  //{{{ private void genConstructor()

  private void genConstructor()
  {
    String decl = class_name + "(ATerm term)";
    printFoldOpen(decl);
    println("  " + decl);
    println("  {");
    println("    this.term = term;");
    println("  }");
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

    genAltConstructor(type, alt);

    println("}");
  }

  //}}}
  //{{{ private void genAttributes(Type type, Alternative alt)

  private void genAttributes(Type type, Alternative alt)
  {
    Iterator iter = type.altFieldIterator(alt.getId());
    while (iter.hasNext()) {
      Field field = (Field)iter.next();
      String field_name = buildId(field.getId());
      String field_type = buildId(field.getType());

      println("  private " + field_type + " " + field_name + ";");
    }
  }

  //}}}

  //{{{ private void genAltConstructor(Type type, Alternative alt)

  private void genAltConstructor(Type type, Alternative alt)
  {
    String alt_class = buildAltClassName(type, alt);
    String decl = alt_class + "(ATerm term, List args)";
    printFoldOpen(decl);
    println("  " + decl);
    println("  {");
    println("    super(term);");
    println();

    println("    Iterator iter = args.iterator();");
    Iterator iter = type.altFieldIterator(alt.getId());
    while (iter.hasNext()) {
      Field field = (Field)iter.next();
      String field_name = buildId(field.getId());
      String field_type = buildId(field.getType());
      println("    " + field_name + " = (" + field_type + ")iter.next();");
    }
    println("    if (iter.hasNext()) {");
    println("      throw new RuntimeException(\"too many elements?\");");
    println("    }");

    println("  }");
    printFoldClose();
  }

  //}}}

  //{{{ private String buildClassName(Type type)

  private String buildClassName(Type type)
  {
    return buildId(type.getId());
  }

  //}}}
  //{{{ private String buildAltClassName(Type type, Alternative alt)

  private String buildAltClassName(Type type, Alternative alt)
  {
    return buildId(type.getId()) + "_" + buildId(alt.getId());
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
