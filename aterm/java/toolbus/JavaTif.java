package toolbus;

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;
import aterm.*;

public class JavaTif
{
  private ATermList tifs = null;
  private ATermFactory factory;
  private Hashtable doEvents = null;
  private Hashtable evalEvents = null;
  private Hashtable otherEvents = null;
  private boolean found_one = false;

  private String package_name;
  private String tool_interface;
  private String tool_class;
  private String tool_bridge;

  //{{{ static void usage()

  static void usage()
  {
    System.err.println("usage: javatif -tool <tool> -tifs <tifs> " +
		       "[-class <class>] [-package <package>]");
    System.exit(0);
  }

  //}}}
  //{{{ public static void main(String[] args)

  public static void main(String[] args)
    throws IOException
    {
      String tool = null, tifsfile = null;
      String package_name = null, tool_interface = null, tool_class = null;
      String tool_bridge = null;
      String output = null;

      for(int i=0; i<args.length; i++) {
	if (args[i].equals("-h")) {
	  usage();
	} else if (args[i].equals("-tool")) {
	  tool = args[++i];
	} else if (args[i].equals("-tifs")) {
	  tifsfile = args[++i];
	} else if (args[i].equals("-package")) {
	  package_name = args[++i];
	} else if (args[i].equals("-tool-interface")) {
	  tool_interface = args[++i];
	} else if (args[i].equals("-tool-class")) {
	  tool_class = args[++i];
	} else if (args[i].equals("-tool-bridge")) {
	  tool_bridge = args[++i];
	}
      }

      if (tool == null || tifsfile == null) {
	usage();
      } else {
	String tool_name = JavaTif.capitalize(tool, true);
	if (tool_interface == null) {
	  tool_interface = tool_name + "Tif";
	}
	if (tool_class == null) {
	  tool_class = tool_name + "Tool";
	}
	if (tool_bridge == null) {
	  tool_bridge = tool_name + "Bridge";
	}

	JavaTif gen = new JavaTif(package_name, tool_interface,
				  tool_class, tool_bridge);
	gen.readTifs(tifsfile);
	gen.selectTifs(tool);
	if (!gen.found_one) {
	  System.err.println("warning: no tifs found for tool " + tool);
	}
	gen.genTif();
	gen.genTool();
	gen.genBridge();
      }
    }

  //}}}

  //{{{ public JavaTif(pkg_name, tool_interface, tool_class, tool_bridge)

  public JavaTif(String pkg_name, String tool_interface,
		 String tool_class, String tool_bridge)
  {
    doEvents = new Hashtable();
    evalEvents = new Hashtable();
    otherEvents = new Hashtable();
    factory = new aterm.pure.PureFactory();
    
    this.package_name = pkg_name;
    this.tool_interface = tool_interface;
    this.tool_class = tool_class;
    this.tool_bridge = tool_bridge;
  }

  //}}}

  //{{{ static public String capitalize(String str, boolean firstCap)

  static public String capitalize(String str, boolean firstCap)
  {
    StringBuffer name = new StringBuffer();
    for(int i=0; i<str.length(); i++) {
      if (str.charAt(i) == '-')
	firstCap = true;
      else {
	if (firstCap) {
	  name.append(Character.toUpperCase(str.charAt(i)));
	  firstCap = false;
	} else
	  name.append(str.charAt(i));
      }
    }
    return name.toString();
  }

  //}}}
  //{{{ public void readTifs(String tifsfile)

  public void readTifs(String tifsfile)
    throws IOException
    {
      tifs = factory.makeList();
      ATermAppl appl = null;

      FileInputStream s = new FileInputStream(tifsfile);

      do {
	if (appl != null && appl.getName().startsWith("rec-")) {
	  tifs = factory.makeList(appl, tifs);
	}
	appl = (ATermAppl)AbstractTool.readTerm(s, factory);
      } while(!appl.getName().equals("end-of-tifs"));
    }

  //}}}
  //{{{ public void selectTifs(String tool)

  public void selectTifs(String tool)
    throws IOException
    {
      ATermList list = tifs;
      AFun fun = factory.makeAFun(tool, 0, false);
      ATerm T = factory.makePlaceholder(factory.makeAppl(fun));

      while(!list.isEmpty()) {
	ATermAppl appl = (ATermAppl)list.getFirst();
	if (!appl.getArguments().isEmpty() && appl.getName().startsWith("rec-") &&
	   appl.getArguments().getFirst().equals(T)) {
	  found_one = true;
	  if (appl.getName().equals("rec-do")) {
	    appl = (ATermAppl)appl.getArguments().getNext().getFirst();
	    appl = normalize(appl);
	    SpecOrderVector v = (SpecOrderVector)doEvents.get(appl.getName());
	    if (v == null) {
	      v = new SpecOrderVector();
	      doEvents.put(appl.getName(), v);
	    }
	    v.insert(appl);
	  } else if (appl.getName().equals("rec-eval")) {
	    appl = (ATermAppl)appl.getArguments().getNext().getFirst();
	    appl = normalize(appl);
	    SpecOrderVector v = (SpecOrderVector)evalEvents.get(appl.getName());
	    if (v == null) {
	      v = new SpecOrderVector();
	      evalEvents.put(appl.getName(), v);
	    }
	    v.insert(appl);
	  } else {
	    // Dump first argument (<tool>)
	    AFun oldfun = appl.getAFun();
	    AFun newfun = factory.makeAFun(oldfun.getName(), oldfun.getArity()-1,
					   oldfun.isQuoted());
	    appl = factory.makeAppl(newfun, appl.getArguments().getNext());
	    appl = normalize(appl);
	    SpecOrderVector v = (SpecOrderVector)otherEvents.get(appl.getName());
	    if (v == null) {
	      v = new SpecOrderVector();
	      otherEvents.put(appl.getName(), v);
	    }
	    v.insert(appl);	  
	  }
	}
	list = list.getNext();
      }
    }

  //}}}

  //{{{ public void genTif()

  public void genTif()
    throws IOException
  {
    System.out.println("generating file " + tool_interface + ".java");

    PrintWriter out =
      new PrintWriter(new FileOutputStream(tool_interface + ".java"));
    genTifHeader(out);
    genMethods(out, false);
    out.println("}");
    out.close();
  }

  //}}}
  //{{{ private void genTifHeader(PrintWriter out)

  private void genTifHeader(PrintWriter out)
    throws IOException
    {
      out.println("// Java tool interface " + tool_interface);
      out.println("// This file is generated automatically, please do not edit!");
      out.print("// generation time: ");
      out.println(DateFormat.getDateTimeInstance().format(new Date()));

      out.println();
      if (package_name != null) {
	out.println("package " + package_name + ";");
      }
      out.println();
      out.println("import aterm.*;");
      out.println("import toolbus.*;");
      out.println();
      out.println("public interface " + tool_interface);
      out.println("{");
    }

  //}}}

  //{{{ public void genTool()

  public void genTool()
    throws IOException
    {
      System.out.println("generating file " + tool_class + ".java");

      PrintWriter out = new PrintWriter(new FileOutputStream(tool_class
							     + ".java"));
      genHeader(out);

      genSigTable(out);
      genPatternAttribs(out);
      out.println();

      genConstructor(out);
      out.println();

      genInitSigTable(out);
      genInitPatterns(out);
      out.println();

      genHandler(out);
      genCheckInputSignature(out);
      genNotInInputSignature(out);
      out.println("}");
      out.close();
    }

  //}}}
  //{{{ private void genSigTable(PrintWriter out)

  private void genSigTable(PrintWriter out)
  {
    out.println("  // This table will hold the complete input signature");
    out.println("  private Map sigTable = new HashMap();");
    out.println();
  }

  //}}}
  //{{{ private void genHeader(PrintWriter out)

  private void genHeader(PrintWriter out)
    throws IOException
    {
      out.println("// Java tool interface class " + tool_class);
      out.println("// This file is generated automatically, please do not edit!");
      out.print("// generation time: ");
      out.println(DateFormat.getDateTimeInstance().format(new Date()));

      out.println();
      if (package_name != null) {
	out.println("package " + package_name + ";");
      }
      out.println();
      out.println("import aterm.*;");
      out.println("import toolbus.*;");
      out.println("import java.net.*;");
      out.println("import java.util.*;");
      out.println();
      out.println("abstract public class " + tool_class);
      out.println("  extends AbstractTool");
      out.println("  implements " + tool_interface);
      out.println("{");
    }

  //}}}
  //{{{ private void genPatternAttribs(PrintWriter out)

  private void genPatternAttribs(PrintWriter out)
    throws IOException
    {
      printFoldOpen(out, "Patterns that are used to match against incoming terms");
      Enumeration en = doEvents.keys();
      while(en.hasMoreElements()) {
	String key = (String)en.nextElement();
	SpecOrderVector v = (SpecOrderVector)doEvents.get(key);
	v.genPatternAttribs(out, capitalize(key, false));
      }

      en = evalEvents.keys();
      while(en.hasMoreElements()) {
	String key = (String)en.nextElement();
	SpecOrderVector v = (SpecOrderVector)evalEvents.get(key);
	v.genPatternAttribs(out, capitalize(key, false));
      }

      en = otherEvents.keys();
      while(en.hasMoreElements()) {
	String key = (String)en.nextElement();
	SpecOrderVector v = (SpecOrderVector)otherEvents.get(key);
	v.genPatternAttribs(out, capitalize(key, false));
      }
      printFoldClose(out);
    }

  //}}}
  //{{{ private void genConstructor(PrintWriter out)

  private void genConstructor(PrintWriter out)
    throws IOException
    {
      String decl = "protected " + tool_class + "(ATermFactory factory)";
      printFoldOpen(out, decl);
      out.println("  // Mimic the constructor from the AbstractTool class");
      out.println("  " + decl);
      out.println("  {");
      out.println("    super(factory);");
      out.println("    initSigTable();");
      out.println("    initPatterns();");
      out.println("  }");
      printFoldClose(out);
    }

  //}}}
  //{{{ private void genInitSigTable(PrintWriter out)

  private void genInitSigTable(PrintWriter out)
  {
    String decl = "private void initSigTable()";
    printFoldOpen(out, decl);
    out.println("  // This method initializes the table with input signatures");
    out.println("  " + decl);
    out.println("  {");
    ATermList sigs = tifs;
    while(!sigs.isEmpty()) {
      ATerm sig = sigs.getFirst();
      sigs = sigs.getNext();
      out.print("    sigTable.put(factory.parse(\"");
      out.print(sig.toString());
      out.println("\"), new Boolean(true));");
    }
    out.println("  }");
    printFoldClose(out);
  }

  //}}}
  //{{{ private void genInitPatterns(PrintWriter out)

  private void genInitPatterns(PrintWriter out)
    throws IOException
    {
      String decl = "private void initPatterns()";
      printFoldOpen(out, decl);
      out.println("  // Initialize the patterns that are used to match against incoming terms");
      out.println("  " + decl);
      out.println("  {");
      Enumeration e = doEvents.keys();
      while(e.hasMoreElements()) {
	String key = (String)e.nextElement();
	SpecOrderVector v = (SpecOrderVector)doEvents.get(key);
	v.genPatterns(out, capitalize(key, false), "rec-do");
      }

      e = evalEvents.keys();
      while(e.hasMoreElements()) {
	String key = (String)e.nextElement();
	SpecOrderVector v = (SpecOrderVector)evalEvents.get(key);
	v.genPatterns(out, capitalize(key, false), "rec-eval");
      }

      e = otherEvents.keys();
      while(e.hasMoreElements()) {
	String key = (String)e.nextElement();
	SpecOrderVector v = (SpecOrderVector)otherEvents.get(key);
	v.genPatterns(out, capitalize(key, false), null);
      }
      out.println("  }");
      printFoldClose(out);
    }

  //}}}
  //{{{ private void genHandler(PrintWriter out)

  private void genHandler(PrintWriter out)
  {
    String decl = "public ATerm handler(ATerm term)";
    printFoldOpen(out, decl);
    out.println("  // The generic handler calls the specific handlers");
    out.println("  " + decl);
    out.println("  {");
    out.println("    List result;");
    out.println();

    Enumeration e = doEvents.keys();
    while(e.hasMoreElements()) {
      String key = (String)e.nextElement();
      SpecOrderVector v = (SpecOrderVector)doEvents.get(key);
      v.genCalls(out, capitalize(key, false), false);
    }

    e = evalEvents.keys();
    while(e.hasMoreElements()) {
      String key = (String)e.nextElement();
      SpecOrderVector v = (SpecOrderVector)evalEvents.get(key);
      v.genCalls(out, capitalize(key, false), true);
    }

    e = otherEvents.keys();
    while(e.hasMoreElements()) {
      String key = (String)e.nextElement();
      SpecOrderVector v = (SpecOrderVector)otherEvents.get(key);
      v.genCalls(out, capitalize(key, false), false);
    }
    out.println();
    out.println("      notInInputSignature(term);");
    out.println("    return null;");
    out.println("  }");
    printFoldClose(out);
  }

  //}}}
  //{{{ private void genMethods(PrintWriter out, boolean gen_impl)

  private void genMethods(PrintWriter out, boolean gen_impl)
  {
    Enumeration en = doEvents.keys();
    while(en.hasMoreElements()) {
      String key = (String)en.nextElement();
      SpecOrderVector v = (SpecOrderVector)doEvents.get(key);
      v.genMethods(out, capitalize(key, false), false, gen_impl);
    }

    en = evalEvents.keys();
    while(en.hasMoreElements()) {
      String key = (String)en.nextElement();
      SpecOrderVector v = (SpecOrderVector)evalEvents.get(key);
      v.genMethods(out, capitalize(key, false), true, gen_impl);
    }

    en = otherEvents.keys();
    while(en.hasMoreElements()) {
      String key = (String)en.nextElement();
      SpecOrderVector v = (SpecOrderVector)otherEvents.get(key);
      v.genMethods(out, capitalize(key, false), false, gen_impl);
    }
  }

  //}}}
  //{{{ private void genCheckInputSignature(PrintWriter out)

  private void genCheckInputSignature(PrintWriter out)
  {
    String decl = "public void checkInputSignature(ATermList sigs)";
    printFoldOpen(out, decl);
    out.println("  // Check the input signature");
    out.println("  " + decl);
    out.println("  {");
    out.println("    while(!sigs.isEmpty()) {");
    out.println("      ATermAppl sig = (ATermAppl)sigs.getFirst();");
    out.println("      sigs = sigs.getNext();");
    out.println("      if (!sigTable.containsKey(sig)) {");
    out.println("        // Sorry, but the term is not in the input signature!");
    out.println("        notInInputSignature(sig);");
    out.println("      }");
    out.println("    }");
    out.println("  }");
    printFoldClose(out);
  }

  //}}}
  //{{{ private void genNotInInputSignature(PrintWriter out)

  private void genNotInInputSignature(PrintWriter out)
  {
    String decl = "void notInInputSignature(ATerm t)";
    printFoldOpen(out, decl);
    out.println("  // This function is called when an input term");
    out.println("  // was not in the input signature.");
    out.println("  " + decl);
    out.println("  {");
    out.println("    throw new RuntimeException("
		+ "\"term not in input signature: \"+t);");
    out.println("  }");
    printFoldClose(out);
  }

  //}}}

  //{{{ private void genBridge()

  private void genBridge()
    throws IOException
  {
    System.out.println("generating file " + tool_bridge + ".java");

    PrintWriter out =
      new PrintWriter(new FileOutputStream(tool_bridge + ".java"));
    genBridgeHeader(out);

    out.println("  private " + tool_interface + " tool;");
    out.println();
    String decl = "public " + tool_bridge + "(ATermFactory factory, "
      + tool_interface + " tool)";
    printFoldOpen(out, decl);
    out.println("  " + decl);
    out.println("  {");
    out.println("    super(factory);");
    out.println("    this.tool = tool;"); 
    out.println("  }");
    printFoldClose(out);
    out.println();

    genMethods(out, true);
    out.println("}");
    out.close();
  }

  //}}}
  //{{{ private void genBridgeHeader(PrintWriter out)

  private void genBridgeHeader(PrintWriter out)
    throws IOException
    {
      out.println("// Java tool bridge " + tool_bridge);
      out.println("// This file is generated automatically, please do not edit!");
      out.print("// generation time: ");
      out.println(DateFormat.getDateTimeInstance().format(new Date()));

      out.println();
      if (package_name != null) {
	out.println("package " + package_name + ";");
      }
      out.println();
      out.println("import aterm.*;");
      out.println("import toolbus.*;");
      out.println();
      out.println("public class " + tool_bridge);
      out.println("  extends " + tool_class);
      out.println("{");
    }

  //}}}

  //{{{ private ATermAppl normalize(ATermAppl term)

  private ATermAppl normalize(ATermAppl appl)
  {
    ATermList args = appl.getArguments();
    if (!args.isEmpty()) {
      int len = args.getLength();
      ATerm[] newargs = new ATerm[len];
      String type = null;

      for (int i=0; i<len; i++) {
	ATerm arg = args.getFirst();
	args = args.getNext();
	switch (arg.getType()) {
	  case ATerm.APPL:
	    type = "<term>";
	    break;
	  case ATerm.INT:
	    type = "<int>";
	    break;
	  case ATerm.REAL:
	    type = "<real>";
	    break;
	  case ATerm.PLACEHOLDER:
	    type = arg.toString();
	    if (!type.equals("<int>") && !type.equals("<str>")
		&& !type.equals("<real>")) {
	      type = "<term>";
	    }
	    //newargs[i] = arg;
	    break;
	}
	if (newargs[i] == null) {
	  newargs[i] = factory.parse(type);
	}
      }
      args = factory.makeList();
      for (int i = len-1; i >= 0; i--) {
	args = factory.makeList(newargs[i], args);
      }
    }
    return factory.makeAppl(appl.getAFun(), args);    
  }

  //}}}
  //{{{ static void printFoldOpen(PrintWriter out, String comment)

  static void printFoldOpen(PrintWriter out, String comment)
  {
    out.println("  //{{" + "{  " + comment);
    out.println();
  }

  //}}}
  //{{{ static void printFoldClose(PrintWriter out)

  static void printFoldClose(PrintWriter out)
  {
    out.println();
    out.println("  //}}" + "}");
  }

  //}}}
}


class SpecOrderVector extends Vector
{
  //{{{ public void insert(ATermAppl appl)

  public void insert(ATermAppl appl)
  {
    for(int i=0; i<size(); i++) {
      if (moreSpecific(appl, (ATermAppl)elementAt(i))) {
	insertElementAt(appl, i);
	return;
      }
    }
    addElement(appl);
  }

  //}}}
  //{{{ private boolean moreSpecific(ATerm a, ATerm b)

  private boolean moreSpecific(ATerm a, ATerm b)
  {
    if (a == b)
      return true;
    if (a.equals(b))
      return true;
    if (a.getType() > b.getType())
      return true;
    if (a.getType() < b.getType())
      return false;
    switch(a.getType()) {
      case ATerm.APPL:
	ATermAppl appl1 = (ATermAppl)a;
	ATermAppl appl2 = (ATermAppl)b;
	if (appl1.getName().equals(appl2.getName()))
	  return moreSpecific(appl1.getArguments(), appl2.getArguments());
	if (moreSpecific(appl1.getName(), appl2.getName()))
	  return true;
	return false;
      case ATerm.PLACEHOLDER:
	ATermPlaceholder p1 = (ATermPlaceholder)a;
	ATermPlaceholder p2 = (ATermPlaceholder)b;
	return moreSpecific(p1.getPlaceholder(), p2.getPlaceholder());
      case ATerm.LIST:
	ATermList terms1 = (ATermList)a;
	ATermList terms2 = (ATermList)b;
	if (terms1.isEmpty())
	  return true;
	if (terms2.isEmpty())
	  return false;
	if (terms1.getFirst().equals(terms2.getFirst()))
	  return moreSpecific(terms1.getNext(), terms2.getNext());
	return moreSpecific(terms1.getFirst(), terms2.getFirst());
    }
    return false; // compiler!
  }

  //}}}
  //{{{ private boolean moreSpecific(String a, String b)

  private boolean moreSpecific(String a, String b)
  {
    int i;
    for(i=0; i<a.length(); i++) {
      if (i > b.length())
	return true;
      if (a.charAt(i) < b.charAt(i))
	return true;
    }
    return false;
  }

  //}}}
  //{{{ public void print(PrintWriter out)

  public void print(PrintWriter out)
  {
    for(int i=0; i<size(); i++) {
      out.println(elementAt(i).toString());
    }
  }

  //}}}
  //{{{ public void genPatterns(PrintWriter out, String base)

  /**
    Generate pattern declarations for the functions in this vector.
   */
  public void genPatterns(PrintWriter out, String base, String func)
    throws IOException
    {
      for (int i=0; i<size(); i++) {
	out.print("    P" + base + i + " = factory.parse(\"");
	if (func != null) {
	  out.print(func + "(");
	}
	out.print(elementAt(i).toString());
	if (func != null) {
	  out.print(")");
	}
	out.println("\");");
      }
    }

  //}}}
  //{{{ public void genPatternAttribs(PrintWriter out, String base)

  /**
    Generate pattern attributes for the functions in this vector.
   */
  public void genPatternAttribs(PrintWriter out, String base)
    throws IOException
    {
      for(int i=0; i<size(); i++)
	out.println("  private ATerm P" + base + i + ";");
    }

  //}}}
  //{{{ public void genCalls(PrintWriter out, String base, boolean ret)

  public void genCalls(PrintWriter out, String base, boolean ret)
  {
    for (int i=0; i<size(); i++) {
      ATermAppl appl = ((ATermAppl)elementAt(i));
      out.println("    result = term.match(P" + base + i + ");");
      out.println("    if (result != null) {");
      if (ret) {
	out.print("      return " + base + "(");
      }
      else {
	out.print("      " + base + "(");
      }

      genArgs(out, appl.getArguments());
      out.println(");");
      if (!ret) {
	out.println("      return null;");
      }
      out.println("    }");
    }
  }

  //}}}
  //{{{ private static void genArgs(PrintWriter out, ATermList args)

  private static void genArgs(PrintWriter out, ATermList args)
  {
    int idx = 0;

    while(!args.isEmpty()) {
      ATermPlaceholder ph = (ATermPlaceholder)args.getFirst();
      String fun = ((ATermAppl)ph.getPlaceholder()).getName();
      args = args.getNext();
      if (fun.equals("int"))
	out.print("((Integer)result.get(" + idx + ")).intValue()");
      else if (fun.equals("real"))
	out.print("((Double)result.get(" + idx + ")).doubleValue()");
      else if (fun.equals("term"))
	out.print("(ATerm)result.get(" + idx + ")");
      else if (fun.equals("appl"))
	out.print("(ATermAppl)result.get(" + idx + ")");
      else if (fun.equals("list"))
	out.print("(ATermList)result.get(" + idx + ")");
      else if (fun.equals("str"))
	out.print("(String)result.get(" + idx + ")");
      else
	out.print("(ATermAppl)result.get(" + idx + ")");
      if (!args.isEmpty())
	out.print(", ");
      idx++;
    }
  }

  //}}}
  //{{{ public void genMethods(PrintWriter out, String base, ret, gen_impl)

  public void genMethods(PrintWriter out, String base, boolean ret,
			 boolean gen_impl)
  {
    for(int i=0; i<size(); i++) {
      ATermAppl appl = ((ATermAppl)elementAt(i));

      String decl;
     
      if (ret) {
        decl = "public ATerm " + base + "(";
      }
      else {
	decl = "public void " + base + "(";
      }


      decl += buildFormals(appl.getArguments());
      decl += ")";

      if (gen_impl) {
	JavaTif.printFoldOpen(out, decl);
      }

      out.print("  " + decl);

      if (gen_impl) {
	out.println();
	out.println("  {");
	out.println("    if (tool != null) {");
	out.print(  "      " + (ret ? "return " : "") + "tool." + base + "(");
	genActuals(out, appl.getArguments());
	out.println(");");
	out.println("    }");
	out.println("    else {");
	out.print(  "      throw new UnsupportedOperationException(");
	out.println("\"method `" + base + "' not supported.\");");
	out.println("    }");
	out.println("  }");
	JavaTif.printFoldClose(out);
      } else {
	out.println(";");
      }
    }
  }

  //}}}
  //{{{ private static String buildFormals(ATermList args)

  private static String buildFormals(ATermList args)
  {
    int idx = 0;
    StringBuffer result = new StringBuffer();

    while (!args.isEmpty()) {
      ATermPlaceholder ph = (ATermPlaceholder)args.getFirst();
      String fun = ((ATermAppl)ph.getPlaceholder()).getName();
      args = args.getNext();
      if (fun.equals("int")) {
	result.append("int i" + idx);
      }
      else if (fun.equals("real")) {
	result.append("double d" + idx);
      }
      else if (fun.equals("term")) {
	result.append("ATerm t" + idx);
      }
      else if (fun.equals("appl")) {
	result.append("ATermAppl a" + idx);
      }
      else if (fun.equals("list")) {
	result.append("ATermList l" + idx);
      }
      else if (fun.equals("str")) {
	result.append("String s" + idx);
      }
      else {
	result.append("ATermAppl a" + idx);
      }

      if (!args.isEmpty()) {
	result.append(", ");
      }
      idx++;
    }

    return result.toString();
  }

  //}}}
  //{{{ private static void genActuals(PrintWriter out, ATermList args)

  private static void genActuals(PrintWriter out, ATermList args)
  {
    int idx = 0;

    while (!args.isEmpty()) {
      ATermPlaceholder ph = (ATermPlaceholder)args.getFirst();
      String fun = ((ATermAppl)ph.getPlaceholder()).getName();
      args = args.getNext();
      if (fun.equals("int")) {
	out.print("i" + idx);
      }
      else if (fun.equals("real")) {
	out.print("d" + idx);
      }
      else if (fun.equals("term")) {
	out.print("t" + idx);
      }
      else if (fun.equals("appl")) {
	out.print("a" + idx);
      }
      else if (fun.equals("list")) {
	out.print("l" + idx);
      }
      else if (fun.equals("str")) {
	out.print("s" + idx);
      }
      else {
	out.print("a" + idx);
      }

      if (!args.isEmpty()) {
	out.print(", ");
      }
      idx++;
    }
  }

  //}}}
}


