package apigen.c;

//{{{ imports

import java.io.*;
import java.util.*;

import aterm.*;
import aterm.pure.PureFactory;

import apigen.*;

//}}}

public class CGen
{
  private final static String[] SPECIAL_CHAR_WORDS =
  { "[BracketOpen", "]BracketClose",
    "{BraceOpen",   "}BraceClose",
    "(ParenOpen",   ")ParenClose",
    "|Bar",         "&Amp",
    "+Plus",	    ",Comma",
  };
  public static boolean verbose = false;

  private ATermFactory factory;

  private InputStream input;
  private String      output;
  private String      capOutput;
  private String      prefix;
  private String      prologue;
  private String      macro;

  private PrintStream source;
  private PrintStream header;

  private Map specialChars;
  private char last_special_char;
  private String last_special_char_word;

  //{{{ private static void usage()

  private static void usage()
  {
    System.err.println("usage: CGen [options]");
    System.err.println("options:");
    System.err.println("\t-prefix <prefix>          [\"\"]");
    System.err.println("\t-input <in>               [-]");
    System.err.println("\t-output <out>");
    System.err.println("\t-prologue <prologue>");
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
    String prefix = "";
    String input  = "-";
    String output = null;
    String prologue = null;
    InputStream inputStream;

    if (args.length == 0) {
      usage();
    }

    for (int i=0; i<args.length; i++) {
      if ("-help".startsWith(args[i])) {
	usage();
      } else if ("-verbose".startsWith(args[i])) {
	verbose = true;
      } else if ("-prefix".startsWith(args[i])) {
	prefix = args[++i];
      } else if ("-output".startsWith(args[i])) {
	output = args[++i];
      } else if ("-prologue".startsWith(args[i])) {
	prologue = args[++i];
      } else if ("-input".startsWith(args[i])) {
	input = args[++i];
      } else {
	usage();
      }
    }

    if (input.equals("-")) {
      inputStream = System.in;
      if (output == null) {
	usage();
      }
    } else {
      inputStream = new FileInputStream(input);
      if (output == null) {
	int extIndex = input.lastIndexOf((int)'.');
	output = input.substring(0, extIndex);
      }
    }
    CGen gen = new CGen(inputStream, output, prefix, prologue);
  }

  //}}}

  //{{{ public CGen(InputStream input, String output, String prefix, String prologue)

  public CGen(InputStream input, String output, String prefix, String prologue)
    throws IOException
  {
    this.input     = input;
    this.output    = output;
    this.capOutput = capitalize(output);
    this.prefix    = prefix;
    this.prologue  = prologue;
    specialChars   = new HashMap();

    for (int i=0; i<SPECIAL_CHAR_WORDS.length; i++) {
      String word = SPECIAL_CHAR_WORDS[i];
      specialChars.put(new Character(word.charAt(0)), word.substring(1));
    }

    factory = new PureFactory();

    ATerm adt = factory.readFromFile(input);

    API api = new API(adt);

    String header_name = output + ".h";
    header = new PrintStream(new FileOutputStream(header_name));

    String source_name = output + ".c";
    source = new PrintStream(new FileOutputStream(source_name));

    genPrologue(api);
    genTypes(api);
    genInitFunction(api);
    genTermConversions(api);
    genConstructors(api);
    genAccessors(api);
    genEpilogue(api);

    ATerm dict = buildDictionary(api);
    OutputStream dict_out = new FileOutputStream(output + ".dict");
    dict.writeToTextFile(dict_out);
  }

  //}}}

  //{{{ private void genPrologue(API api)

  private void genPrologue(API api)
    throws IOException
  {
    /* Header stuff */
    macro = "_" + output.toUpperCase() + "_H";
    StringBuffer buf = new StringBuffer();
    for (int i=0; i<macro.length(); i++) {
      if (Character.isLetterOrDigit(macro.charAt(i))) {
	buf.append(macro.charAt(i));
      } else {
	buf.append('_');
      }
    }
    macro = buf.toString();

    header.println("#ifndef " + macro);
    header.println("#define " + macro);

    //{{{ Output includes

    header.println();
    printFoldOpen(header, "includes");
    header.println("#include <aterm1.h>");
    header.println("#include \"" + output + "_dict.h\"");
    printFoldClose(header);
    header.println();

    //}}}
    //{{{ Copy prologue file verbatim

    if (prologue != null) {
      printFoldOpen(header, "prologue");
      InputStream stream = new FileInputStream(prologue);
      while (true) {
	int b = stream.read();
	if (b == -1) {
	  break;
	}
	header.write(b);
      }
      printFoldClose(header);
    }

    //}}}

    /* Source stuff */
    source.println("#include <aterm2.h>");
    source.println("#include <deprecated.h>");
    source.println("#include \"" + output + ".h\"");
    source.println();
  }

  //}}}
  //{{{ private void genTypes(API api)

  private void genTypes(API api)
  {
    Iterator types = api.typeIterator();
    printFoldOpen(header, "typedefs");
    printFoldOpen(source, "typedefs");
    while (types.hasNext()) {
      Type type = (Type)types.next();
      String id = buildTypeName(type);
      header.println("typedef struct _" + id + " *" + id + ";");
      source.println("typedef struct ATerm _" + id + ";");
    }
    printFoldClose(source);
    printFoldClose(header);
    source.println();
    header.println();
  }

  //}}}
  //{{{ private String buildDictInitFunc(String name)

  private String buildDictInitFunc(String name)
  {
    StringBuffer buf = new StringBuffer();
    for (int i=0; i<name.length(); i++) {
      char c = name.charAt(i);
      if (Character.isLetterOrDigit(c)) {
	buf.append(c);
      } else {
	buf.append('_');
      }
    }
    return buf.toString();
  }

  //}}}
  //{{{ private void genInitFunction(API api)

  private void genInitFunction(API api)
  {
    String decl = "void " + prefix
      + "init" + buildId(capOutput) + "Api(void)";
    header.println(decl + ";");

    printFoldOpen(source, decl);
    source.println(decl);
    source.println("{");
    String dictInitfunc = buildDictInitFunc(output);
    source.println("  init_" + dictInitfunc + "_dict();");
    source.println("}");
    printFoldClose(source);
    source.println();
    header.println();
  }

  //}}}
  //{{{ private void genTermConversions(API api)

  private void genTermConversions(API api)
  {
    Iterator types = api.typeIterator();
    while (types.hasNext()) {
      Type type = (Type)types.next();
      String type_id = buildId(type.getId());
      String type_name = buildTypeName(type);

      //{{{ PPPmakeXXXFromTerm(ATerm t)

      String decl =
	type_name + " " + prefix + "make" + type_id + "FromTerm(ATerm t)";
      header.println(decl + ";");
      printFoldOpen(source, decl);
      source.println(decl);
      source.println("{");
      source.println("  return (" + type_name + ")t;");
      source.println("}");
      printFoldClose(source);

      //}}}
      //{{{ PPPmakeTermFromXxx(Xxx arg)

      decl = "ATerm " + prefix + "makeTermFrom" + type_id
	+ "(" + type_name + " arg)";
      header.println(decl + ";");
      printFoldOpen(source, decl);
      source.println(decl);
      source.println("{");
      source.println("  return (ATerm)arg;");
      source.println("}");
      printFoldClose(source);

      //}}}
    }
    source.println();
    header.println();
  }

  //}}}
  //{{{ private void genConstructors(API api)

  private void genConstructors(API api)
  {
    Iterator types = api.typeIterator();
    while (types.hasNext()) {
      Type type = (Type)types.next();
      String type_id = buildId(type.getId());
      String type_name = buildTypeName(type);

      Iterator alts = type.alternativeIterator();
      while (alts.hasNext()) {
	Alternative alt = (Alternative)alts.next();
	//{{{ Find fields in this alternative

	List alt_fields = new LinkedList();
        Iterator fields = type.altFieldIterator(alt.getId());
	while (fields.hasNext()) {
	  Field field = (Field)fields.next();
	  alt_fields.add(field);
	}

	//}}}
	//{{{ Build declaration

	StringBuffer decl = new StringBuffer();
	decl.append(type_name + " " + prefix + "make" + type_id
		    + capitalize(buildId(alt.getId())) + "(");
	fields = alt_fields.iterator();
	boolean first = true;
	while (fields.hasNext()) {
	  Field field = (Field)fields.next();
	  if (first) {
	    first = false;
	  } else {
	    decl.append(", ");
	  }
	  decl.append(buildTypeName(field.getType()) + " " + buildId(field.getId()));
	}
	decl.append(")");


	//}}}
	//{{{ Generate declaration
	
	header.println(decl + ";");

	//}}}
	//{{{ Generate implementation

	printFoldOpen(source, decl.toString());
	source.println(decl);
	source.println("{");
	source.print("  return (" + type_name + ")ATmakeTerm("
		     + prefix + "pattern" + type_id
		     + capitalize(buildId(alt.getId())));
	fields = alt_fields.iterator();
	while (fields.hasNext()) {
	  Field field = (Field)fields.next();
	  source.print(", " + buildId(field.getId()));
	}
	source.println(");");
	source.println("}");
	printFoldClose(source);

	//}}}
      }
    }
    source.println();
    header.println();
  }

  //}}}
  //{{{ private void genAccessors(API api)

  private void genAccessors(API api)
  {
    Iterator types = api.typeIterator();
    while (types.hasNext()) {
      Type type = (Type)types.next();
      String type_name = buildTypeName(type);
      printFoldOpen(header, type_name + " accessor prototypes");
      printFoldOpen(source, type_name + " accessor implementations");

      genTypeIsValid(type);

      Iterator alts = type.alternativeIterator();
      while (alts.hasNext()) {
	Alternative alt = (Alternative)alts.next();
	genIsAlt(type, alt);
      }

      Iterator fields = type.fieldIterator();
      while (fields.hasNext()) {
	Field field = (Field)fields.next();
	genHasField(type, field);
	genGetField(type, field);
	genSetField(type, field);
      }

      printFoldClose(source);
      printFoldClose(header);
    }
  }

  //}}}
  //{{{ private void genTypeIsValid(Type type)

  private void genTypeIsValid(Type type)
  {
    String type_id = buildId(type.getId());
    String type_name = buildTypeName(type);
    String decl = "ATbool " + prefix + "isValid" + type_id
      + "(" + type_name + " arg)";

    printFoldOpen(source, decl);

    header.println(decl + ";");
    source.println(decl);
    source.println("{");
    Iterator alts = type.alternativeIterator();
    boolean first = true;
    while (alts.hasNext()) {
      Alternative alt = (Alternative)alts.next();
      source.print("  ");
      if (first) {
	first = false;
      } else {
	source.print("else ");
      }
      source.println("if (" + prefix + "is" + type_id 
		     + capitalize(buildId(alt.getId())) + "(arg)) {");
      source.println("    return ATtrue;");
      source.println("  }");
    }
    source.println("  return ATfalse;");
    source.println("}");

    printFoldClose(source);
  }

  //}}}
  //{{{ private void genIsAlt(Type type, Alternative alt)

  private void genIsAlt(Type type, Alternative alt)
  {
    String type_id = buildId(type.getId());
    String type_name = buildTypeName(type);
    String decl = "ATbool " + prefix + "is" + type_id
      + capitalize(buildId(alt.getId())) + "(" + type_name + " arg)";

    header.println(decl + ";");

    printFoldOpen(source, decl);
    source.println(decl);
    source.println("{");
    source.print("  return ATmatchTerm((ATerm)arg, " + prefix + "pattern"
                 + type_id + capitalize(buildId(alt.getId())));
    Iterator fields = type.altFieldIterator(alt.getId());
    while (fields.hasNext()) {
      fields.next();
      source.print(", NULL");
    }
    source.println(");");
    source.println("}");

    printFoldClose(source);
  }

  //}}}
  //{{{ private void genHasField(Type type, Field field)

  private void genHasField(Type type, Field field)
  {
    String type_id = buildId(type.getId());
    String type_name = buildTypeName(type);
    String decl = "ATbool " + prefix + "has" + type_id
      + capitalize(buildId(field.getId())) + "(" + type_name + " arg)";

    header.println(decl + ";");

    printFoldOpen(source, decl);
    source.println(decl);
    source.println("{");
    Iterator locs = field.locationIterator();
    boolean first = true;
    while (locs.hasNext()) {
      Location loc = (Location)locs.next();
      source.print("  ");
      if (first) {
	first = false;
      }
      else {
	source.print("else ");
      }
      source.println("if (" + prefix + "is" + type_id
		     + capitalize(buildId(loc.getAltId())) + "(arg)) {");
      source.println("    return ATtrue;");
      source.println("  }");
    }
    source.println("  return ATfalse;");
    source.println("}");
    printFoldClose(source);
  }

  //}}}

  //{{{ private void genGetField(Type type, Field field)

  private void genGetField(Type type, Field field)
  {
    String type_id = buildId(type.getId());
    String type_name = buildTypeName(type);
    String field_type_name = buildTypeName(field.getType());
    String fieldId = capitalize(buildId(field.getId()));
    String decl = field_type_name + " " + prefix + "get" + type_id
      + fieldId + "(" + type_name + " arg)";

    header.println(decl + ";");

    printFoldOpen(source, decl);
    source.println(decl);
    source.println("{");
    Iterator locs = field.locationIterator();
    boolean first = true;
    while (locs.hasNext()) {
      Location loc = (Location)locs.next();
      source.print("  ");
      if (first) {
	first = false;
      }
      else {
	source.print("else ");
      }
      source.println("if (" + prefix + "is" + type_id
		     + capitalize(buildId(loc.getAltId())) + "(arg)) {");
      source.print("    return (" + field_type_name + ")");
      Iterator steps = loc.stepIterator();
      genGetterSteps(steps, "arg");
      source.println(";");
      source.println("  }");
    }
    source.println();
    source.println("  ATabort(\"" + type_id + " has no " + fieldId
		   + ": %t\\n\", arg);");
    source.println("  return NULL;");

    source.println("}");
    printFoldClose(source);
  }

  //}}}
  //{{{ private void genGetterSteps(Iterator steps, String arg)

  private void genGetterSteps(Iterator steps, String arg)
  {
    if (steps.hasNext()) {
      Step step = (Step)steps.next();
      switch (step.getType()) {
	case Step.ARG:
	  genGetterSteps(steps, "ATgetArgument((ATermAppl)" + arg
			 + ", " + step.getIndex() + ")");
	  break;
	case Step.ELEM:
	  genGetterSteps(steps, "ATelementAt((ATermList)" + arg
			 + ", " + step.getIndex() + ")");
	  break;
	case Step.TAIL:
	  genGetterSteps(steps, "ATgetTail((ATermList)" + arg
			 + ", " + step.getIndex() + ")");
	  break;
      }
    }
    else {
      source.print(arg);
    }
  }

  //}}}
  //{{{ private void genEpilogue(API api)

  private void genEpilogue(API api)
  {
    header.println();
    header.println("#endif /* " + macro + " */");
    header.close();
  }

  //}}}

  //{{{ private void genSetField(Type type, Field field)

  private void genSetField(Type type, Field field)
  {
    String type_id    = buildId(type.getId());
    String type_name  = buildTypeName(type);
    String field_id   = buildId(field.getId());
    String field_type_name = buildTypeName(field.getType());
    String decl = type_name + " "
      + prefix + "set" + type_id + capitalize(field_id)
      + "(" + type_name + " arg, " + field_type_name + " " + field_id + ")";

    header.println(decl + ";");

    printFoldOpen(source, decl);
    source.println(decl);
    source.println("{");
    Iterator locs = field.locationIterator();
    boolean first = true;
    while (locs.hasNext()) {
      Location loc = (Location)locs.next();
      source.print("  ");
      if (first) {
	first = false;
      }
      else {
	source.print("else ");
      }
      source.println("if (" + prefix + "is" + type_id
		     + capitalize(buildId(loc.getAltId())) + "(arg)) {");
      source.print("    return (" + type_name + ")");
      Iterator steps = loc.stepIterator();
      genSetterSteps(steps, new LinkedList(), buildId(field.getId()));
      source.println(";");
      source.println("  }");
    }
    source.println();
    source.println("  ATabort(\"" + type_id + " has no "
		   + capitalize(field_id) + ": %t\\n\", arg);");
    source.println("  return NULL;");

    source.println("}");
    printFoldClose(source);
  }

  //}}}
  //{{{ private void genSetterSteps(Iterator steps, List parentPath, String arg)

  private void genSetterSteps(Iterator steps, List parentPath, String arg)
  {
    if (steps.hasNext()) {
      Step step = (Step)steps.next();
      switch (step.getType()) {
	case Step.ARG:
	  source.print("ATsetArgument((ATermAppl)");
	  break;
	case Step.ELEM:
	  source.print("ATreplace((ATermList)");
	  break;
	case Step.TAIL:
	  source.print("ATreplaceTail((ATermList)");
	  break;
      }
      genGetterSteps(parentPath.iterator(), "arg");
      if (step.getType() == Step.TAIL) {
        source.print(", (ATermList)");
      } else {
        source.print(", (ATerm)");
      }
      parentPath.add(step);
      genSetterSteps(steps, parentPath, arg);
      source.print(", " + step.getIndex() + ")");
    }
    else {
      source.print(arg);
    }
  }

  //}}}

  //{{{ private void printFoldOpen(PrintStream out, String comment)

  private void printFoldOpen(PrintStream out, String comment)
  {
    out.println("/*{{" + "{  " + comment + " */");
    out.println();
  }

  //}}}
  //{{{ private void printFoldClose(PrintStream out)

  private void printFoldClose(PrintStream out)
  {
    out.println();
    out.println("/*}}" + "}  */");
  }

  //}}}
  //{{{ private String buildTypeName(Type type)

  private String buildTypeName(Type type)
  {
    return prefix + buildId(type.getId());
  }

  //}}}
  //{{{ private String buildTypeName(String typeId)

  private String buildTypeName(String typeId)
  {
    return prefix + buildId(typeId);
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
  //{{{ private ATerm buildDictionary(API api)

  private ATerm buildDictionary(API api)
  {
    ATermList entries = factory.makeList();

    Iterator types = api.typeIterator();
    while (types.hasNext()) {
      Type type = (Type)types.next();
      String id = buildId(type.getId());
      Iterator alts = type.alternativeIterator();
      while (alts.hasNext()) {
	Alternative alt = (Alternative)alts.next();
	ATerm entry = factory.make("[<appl>,<term>]",
				   prefix + "pattern" + id
				   + buildId(capitalize(alt.getId())),
				   buildDictPattern(alt.getPattern()));
	entries = factory.makeList(entry, entries);
      }
    }

    return factory.make("[afuns([]),terms(<term>)]", entries);
  }

  //}}}
  //{{{ private ATerm buildDictPattern(ATerm t)

  private ATerm buildDictPattern(ATerm t)
  {
    switch (t.getType()) {
      case ATerm.APPL:
	{
	  ATermAppl appl = (ATermAppl)t;
	  AFun fun = appl.getAFun();
	  ATerm[] newargs = new ATerm[fun.getArity()];
	  for (int i=0; i<fun.getArity(); i++) {
	    newargs[i] = buildDictPattern(appl.getArgument(i));
	  }
	  return factory.makeAppl(fun, newargs);
	}
	
      case ATerm.LIST:
	{
	  ATermList list = (ATermList)t;
	  ATerm[] elems = new ATerm[list.getLength()];
	  int i = 0;
	  while (!list.isEmpty()) {
	    elems[i++] = buildDictPattern(list.getFirst());
	    list = list.getNext();
	  }
	  for (i=elems.length-1; i>=0; i--) {
	    list = list.insert(elems[i]);
	  }
	  return list;
	}

      case ATerm.PLACEHOLDER:
	{
	  ATerm ph = ((ATermPlaceholder)t).getPlaceholder();
	  if (ph.getType() == ATerm.LIST) {
	    return factory.parse("<list>");
	  } else {
	    return factory.parse("<term>");
	  }
	}

      default:
	return t;
    }
  }

  //}}}
}
