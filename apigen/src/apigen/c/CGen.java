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

  //{{{ private static void usage()

  private static void usage()
  {
    System.err.println("usage: CGen -prefix <prefix> -input <in> -output <out>");
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
  //{{{ private void genInitFunction(API api)

  private void genInitFunction(API api)
  {
    String decl = "void " + prefix + "init" + capOutput + "Api(void)";
    header.println(decl + ";");

    printFoldOpen(source, decl);
    source.println(decl);
    source.println("{");
    source.println("  init_" + output + "_dict();");
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
      String id = buildTypeName(type);

      //{{{ makeXXXFromTerm(ATerm t)

      String decl = id + " make" + id + "FromTerm(ATerm t)";
      header.println(decl + ";");
      printFoldOpen(source, decl);
      source.println(decl);
      source.println("{");
      source.println("  return (" + id + ")t;");
      source.println("}");
      printFoldClose(source);

      //}}}
      //{{{ makeTermFromXxx(Xxx arg)

      decl = "ATerm makeTermFrom" + id + "(" + id + " arg)";
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
      String id = buildTypeName(type);

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
	decl.append(id + " make" + id + capitalize(alt.getId()) + "(");
	fields = alt_fields.iterator();
	boolean first = true;
	while (fields.hasNext()) {
	  Field field = (Field)fields.next();
	  if (first) {
	    first = false;
	  } else {
	    decl.append(", ");
	  }
	  decl.append(prefix + field.getType() + " " + field.getId());
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
	source.print("  return (" + id + ")ATmakeTerm(pattern" + id + 
		       capitalize(alt.getId()));
	fields = alt_fields.iterator();
	while (fields.hasNext()) {
	  Field field = (Field)fields.next();
	  source.print(", " + field.getId());
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
      printFoldOpen(header, type.getId() + " accessor prototypes");
      printFoldOpen(source, type.getId() + " accessor implementations");

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
    String id = prefix + type.getId();
    String decl = "ATbool isValid" + id + "(" + id + " arg)";

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
      source.println("if (is" + id + capitalize(alt.getId()) + "(arg)) {");
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
    String id = prefix + type.getId();
    String decl = "ATbool is" + id + capitalize(alt.getId()) + "(" + id + " arg)";

    header.println(decl + ";");

    printFoldOpen(source, decl);
    source.println(decl);
    source.println("{");
    source.print("  return ATmatchTerm(arg, pattern" + id + capitalize(alt.getId()));
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
    String id = prefix + type.getId();
    String decl = "ATbool has" + id + capitalize(field.getId()) + "(" + id + " arg)";

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
      source.println("if (is" + id + capitalize(loc.getAltId()) + "(arg)) {");
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
    String id = prefix + type.getId();
    String fieldType = prefix + field.getType();
    String decl = fieldType + " get" + id + capitalize(field.getId())
      + "(" + id + " arg)";

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
      source.println("if (is" + id + capitalize(loc.getAltId()) + "(arg)) {");
      source.print("    return (" + fieldType + ")");
      Iterator steps = loc.stepIterator();
      genGetterSteps(steps, "arg");
      source.println(";");
      source.println("  }");
    }
    source.println();
    source.println("  ATabort(\"" + id + " has no " + capitalize(field.getId())
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
	  source.print("ATgetArgument((ATermAppl)");
	  break;
	case Step.ELEM:
	  source.print("ATelementAt((ATermList)");
	  break;
	case Step.TAIL:
	  source.print("ATgetTail((ATermList)");
	  break;
      }
      genGetterSteps(steps, arg);
      source.print(", " + step.getIndex() + ")");
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
    String id = prefix + type.getId();
    String fieldType = prefix + field.getType();
    String decl = id + " set" + id + capitalize(field.getId())
      + "(" + id + " arg, " + fieldType + " " + field.getId() + ")";

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
      source.println("if (is" + id + capitalize(loc.getAltId()) + "(arg)) {");
      source.print("    return (" + id + ")");
      Iterator steps = loc.stepIterator();
      genSetterSteps(steps, new LinkedList(), field.getId());
      source.println(";");
      source.println("  }");
    }
    source.println();
    source.println("  ATabort(\"" + id + " has no " + capitalize(field.getId())
		   + ": %t\\n\", arg);");
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
    return prefix + type.getId();
  }

  //}}}

  //{{{ private ATerm buildDictionary(API api)

  private ATerm buildDictionary(API api)
  {
    ATermList entries = factory.makeList();

    Iterator types = api.typeIterator();
    while (types.hasNext()) {
      Type type = (Type)types.next();
      String id = prefix + type.getId();
      Iterator alts = type.alternativeIterator();
      while (alts.hasNext()) {
	Alternative alt = (Alternative)alts.next();
	ATerm entry = factory.make("[<appl>,<term>]",
				   "pattern" + id + capitalize(alt.getId()),
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
