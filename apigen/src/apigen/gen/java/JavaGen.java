package apigen.gen.java;

//{{{ imports

import java.io.*;
import java.util.*;

// import aterm.pure.PureFactory;

import apigen.*;
import apigen.adt.*;
import apigen.gen.Generator;
import aterm.pure.PureFactory;

//}}}

public class JavaGen
extends Generator
{
  
  //{{{ private final static String[][] RESERVED_TYPES =

  protected String[][] RESERVED_TYPES =
  { { "int",  "Integer"    },
    { "real", "Double" },
    { "str",  "String" },
    { "term", "ATerm" }
  };

  //}}}

  public static boolean folding = false;
  public static boolean visitable = false;
  public static boolean jtom = false;

  private aterm.ATermFactory factory;

  private String basedir;
  private String pkg;
  private List	 imports;
  private String class_name;
  private String api_name;
  
  private InputStream input;
	private String path;

  //{{{ private static void usage()

  private static void usage()
  {
    System.err.println("usage: JavaGen [options]");
    System.err.println("options:");
    System.err.println("\t-verbose                  [off]");
    System.err.println("\t-package <package>        [\"\"]");
    System.err.println("\t-name <api name>          [improvised]");
    System.err.println("\t-basedir <basedir>        [\".\"]");
    System.err.println("\t-import <package>         (can be repeated)");
    System.err.println("\t-input <in>               [-]");
    System.err.println("\t-folding                  [off]");
    System.err.println("\t-visitable                [off]"); 
    System.err.println("\t-jtom                     [off]"); 
    System.exit(1);
  }

  //}}}

  //{{{ public final static void main(String[] args)

  public static void main(String[] args)
    throws IOException
  {
    String pkg = "";
    String input  = "-";
    String basedir = ".";
    String api_name = "";
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
      } else if ("-folding".startsWith(args[i])) {
        folding = true;
      } else if ("-visitable".startsWith(args[i])) {
        visitable = true;
      } else if ("-jtom".startsWith(args[i])) {
        jtom = true;
      } else if ("-name".startsWith(args[i])) {
        api_name = args[++i];
      } else {
        usage();
      }
    }

    if (input.equals("-")) {
      inputStream = System.in;
    } else {
      inputStream = new FileInputStream(input);
    }
    
    if (api_name.equals("")) {
      if (input.equals("-")) {
        System.err.println("Please give a name to the API");
        usage();
      }
      else {
        api_name = input.substring(0, input.lastIndexOf((int) '.'));
      }
    }
    
    JavaGen gen = new JavaGen(inputStream,api_name,basedir, pkg, imports);
  }

  //}}}

  //{{{ public JavaGen(InputStream input, String basedir, String pkg, List imports)

  public JavaGen(InputStream input, String api_name, String basedir, String pkg, List imports)
    throws IOException
  {
    this.api_name = api_name;
    this.input   = input;
    this.basedir = basedir;
    this.pkg	 = pkg;
    this.imports = imports;
    
    initializeConstants(RESERVED_TYPES);

    factory = new PureFactory();
    aterm.ATerm adt = factory.readFromFile(input);
    ADT api = new ADT(adt);
    
    genFactoryClassFile(api);
    genTypeClassFiles(api);
    
    if (jtom) {
      genTomSignatureFile(api);
    }
  }

	

  private void genFactoryClassFile(ADT api) throws IOException {
    class_name = capitalize(buildId(api_name)) + "Factory";
    
    createClassStream(class_name);
    
    genPackageDecl();
    
    List extra = new LinkedList();
    extra.add("aterm.pure.PureFactory");
    genImports(extra); 
    
    genFactoryClass(api);
    
    stream.close();
  }

	private void closeStream() {
		stream.close();
	}
  
  private void genFactoryClass(ADT api) throws IOException {  
    println("class " + class_name + " extends PureFactory");
    println("{");
    println("  public " + class_name + "()");
    println("  {");
    println("     super();");
    println("     initialize();");
    println("  }");
    println("");
    println("  private void initialize()");
    println("  {");
    
    genInitializeCalls(api);
    
    println("  }");
    println("}");
  }
  
  private void genInitializeCalls(ADT api)
  {
    Iterator types = api.typeIterator();
    while (types.hasNext()) {
      Type type = (Type)types.next();
      
      println("      " + buildClassName(type) + ".initialize(this);");
      
      Iterator alts = type.alternativeIterator();
      while (alts.hasNext()) {
        Alternative alt = (Alternative)alts.next();
        
        println("      " + buildAltClassName( type, alt) + ".initialize(this);");
      }
    }
  }
  
	private void genTypeClassFiles(ADT api) throws IOException {
		Iterator types = api.typeIterator();
		while (types.hasNext()) {
		  Type type = (Type)types.next();
		  genTypeClassFile(type);
		}
	}

  private void genTypeClassFile(Type type)
    throws IOException
  {
    String class_name = buildClassName(type);    
    createClassStream(class_name);
    PrintStream tmp;
    
    info("generating " + class_name);

    genPackageDecl();
    genImports();    
    println();
    
    genTypeClass(type); 
    stream.close();
       
    genAlternativesClassesFiles(type);
  }
    
	private void genPackageDecl() {
		if (pkg.length() > 0) {
		  println("package " + pkg + ";");
		  println();
		}
	}

	private void genAlternativesClassesFiles(Type type) {
		Iterator alt_iter = type.alternativeIterator();
		while (alt_iter.hasNext()) {
		  println();
		  Alternative alt = (Alternative)alt_iter.next();
		  genAlternativeClassFile(type, alt);
		}
	}
  
  private void genAlternativeClassFile(Type type, Alternative alt)
  {
    String class_name = buildAltClassName(type, alt);
    createClassStream(class_name);
    
    info("generating " + class_name);
    
    genPackageDecl();
    genImports();
      
    genAlternativeClass(type, alt);
    
    stream.close();
  }

  private void createClassStream(String class_name) {
    createFileStream(class_name, ".java"); 
  }
  
  private void createTomSignatureStream(String name) {
    createFileStream(name, ".t");
  }
  
  private void createFileStream(String name, String ext) {
    char sep = File.separatorChar;
    createStream(basedir + sep + pkg.replace('.', sep) + sep + name + ext);
  }
	

	private void genTypeClass(Type type) {
    String class_name = buildClassName(type);
    
		println("abstract public class " + class_name);
		println("{");
		println("  private static aterm.ATermFactory factory = null;");
		println("  protected aterm.ATerm term = null;");    
    println();
    printFoldOpen("initialize(aterm.ATermFactory f)");
		println("  static public void initialize(aterm.ATermFactory f)");
		println("  {");
		println("    factory = f;");
		println("  }");
    printFoldClose();
    printFoldOpen("toTerm()");
		println("  public aterm.ATerm toTerm()");
		println("  {");
		println("    return this.term;");
		println("  }");
    printFoldClose();
    printFoldOpen("toString()");
		println("  public String toString()");
		println("  {");
		println("    return this.term.toString();");
		println("  }");
    printFoldClose();
    printFoldOpen("fromString()");
		println("  static " + class_name + " fromString(String str)");
		println("  {");
		println("    aterm.ATerm trm = factory.parse(str);");
		println("    return fromTerm(trm);");
		println("  }");
    printFoldClose();
    printFoldOpen("fromTextFile()");
		println("  static " + class_name + " fromTextFile(InputStream stream) throws aterm.ParseError, IOException");
		println("  {");
		println("    aterm.ATerm trm = factory.readFromTextFile(stream);");
		println("    return fromTerm(trm);");
		println("  }");
    printFoldClose();
		printFoldOpen("equals(" + class_name + ")");
		println("  public boolean isEqual(" + class_name + " peer)");
		println("  {");
		println("    return term.isEqual(peer.toTerm());");
		println("  }");
    printFoldClose();
    printFoldOpen("fromTerm(aterm.ATerm trm)");
    println("  public static " + class_name + " fromTerm(aterm.ATerm trm)");
    println("  {");
    println("    " + class_name + " tmp;");
    genFromTermCalls(type);
    println();
    println("    throw new RuntimeException(\"This is not a " + class_name + ": \" + trm);" );
    println("  }");
    printFoldClose();
    println();
    genTypeDefaultProperties(type);
    genDefaultGetAndSetMethods(type);
		println();
    println("}");
    println();
		
	}

	private void genDefaultGetAndSetMethods(Type type) {
    Iterator fields = type.fieldIterator();
    
    printFoldOpen("default getters and setters");
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      genDefaultGetAndSetMethod(type, field);
    }
    printFoldClose();
	}


  private void genTypeDefaultProperties(Type type)
  {
    printFoldOpen("default isX and hasX properties");
    genDefaultIsMethods(type);
    genDefaultHasMethods(type);
    printFoldClose();
  }
   
  private void genOverrideProperties(Type type, Alternative alt)
  {
    printFoldOpen("isXXX and hasXXX properties");
    genOverrideIsMethod(alt);
    genOverrideHasMethods(type, alt);
    printFoldClose();
  }
  
  private void genDefaultIsMethods(Type type)
  {
    Iterator alts = type.alternativeIterator();
    while (alts.hasNext()) {
      Alternative alt = (Alternative) alts.next();    
      genDefaultIsMethod(alt);
    }
  }
    
  private void genDefaultIsMethod(Alternative alt) {   
      println("  public boolean is" + capitalize(buildId(alt.getId())) + "()");
      println("  {");
      println("    return false;");
      println("  }");
      println();
  }
  
  private void genOverrideIsMethod(Alternative alt) {
    println("  public boolean is" + capitalize(buildId(alt.getId())) + "()");
    println("  {");
    println("    return true;");
    println("  }");
    println();
  }
    
  private void genDefaultHasMethods(Type type)
  {
    Iterator fields = type.fieldIterator() ;
    
    while (fields.hasNext()) {
      Field field = (Field) fields.next();    
      genDefaultHasMethod(field);
    }
  }
  
  private void genDefaultHasMethod(Field field)
  {    
      println("  public boolean has" + capitalize(buildId(field.getId())) + "()");
      println("  {");
      println("    return false;");
      println("  }");
      println();
  }
  
  private void genOverrideHasMethods(Type type, Alternative alt)
  {
    Iterator fields = type.altFieldIterator(alt.getId());
    
    while (fields.hasNext()) {
      Field field = (Field) fields.next();   
      genOverrideHasMethod(field);
    }
  }
  
  private void genOverrideHasMethod(Field field)
  {
     println("  public boolean has" + capitalize(buildId(field.getId())) + "()");
     println("  {");
     println("    return true;");
     println("  }");
     println();
  }
  
  private void genFromTermCalls(Type type)
  {
    String class_name = buildClassName(type);
    Iterator alts = type.alternativeIterator();
    while (alts.hasNext()) {
      Alternative alt = (Alternative) alts.next();    
      String alt_class_name = buildAltClassName(type, alt);
      println("    if ((tmp = " + alt_class_name + ".fromTerm(trm)) != null) {");
      println("      return tmp;");
      println("    }");
      println();
    }
  }
  
  private void genImports() {
    genImports(new LinkedList());
  }
  
	private void genImports(List extra) {	
    printFoldOpen("imports");
    List all = new LinkedList(imports);
    all.addAll(extra);
    
		all.add("java.io.InputStream");
		all.add("java.io.OutputStream");
		all.add("java.io.IOException");
    all.add("aterm.ATerm");
		
		Iterator iter = all.iterator();
		while (iter.hasNext()) {
		  println("import " + (String)iter.next() + ";");
		}
    
    printFoldClose();
	}
  
  private void genAlternativeClass(Type type, Alternative alt)
  {
    String type_id = buildId(type.getId());
    String alt_class = buildAltClassName(type, alt);
    
    println("class " + alt_class);
    println("extends " + type_id);
    if (visitable) {
      println("implements Visitable");
    }
    println("{");
    println("  static private aterm.ATerm pattern = null;");
    println();
    
    genFieldMembers(type, alt);
    genAltConstructors(type, alt);
    genAltInitialize(type, alt);
    genAltFromTerm(type, alt);
    genOverrideProperties(type,alt);
    genAltGetAndSetMethods(type, alt);
    if (visitable) {
      genAltVisitableInterface(type, alt);
    }
    
    println("}");
  }
  
  // This is not well thought out yet!
	private void genAltVisitableInterface(Type type, Alternative alt)
  {
    printFoldOpen("visitable interface");

    println("  public void accept(Visitor v)");
    println("  {");
    println("    v.visit(this);");
    println("  }");
    println();
        
    println("  public Visitable getChildAt(int argnr)");
    println("  {");
    println("    switch(argnr) {");
    {
      Iterator fields = type.altFieldIterator(alt.getId());
      int argnr = 0;
      while (fields.hasNext()) {
        Field field = (Field) fields.next();
        if (!isReservedType(field.getType())) {
          println("      case " + argnr + ": return (Visitable) get" + capitalize(buildId(field.getId())) + "();");
        }
        argnr++;
      }
      println("      default: throw new RuntimeException(\"" + buildAltClassName(type, alt) + 
              " does not have an argument at position \" + argnr);");
    }
    println("    }");
    println("  }");
    println();
    println("  public Visitable setChildAt(int argnr, Visitable child)");
    println("  {");
    println("    switch(argnr) {");
    {
      Iterator fields = type.altFieldIterator(alt.getId());
      int argnr = 0;
      while (fields.hasNext()) {
        Field field = (Field) fields.next();
        if (!isReservedType(field.getType())) {
          println("      case " + argnr + ": return (Visitable) set" + capitalize(buildId(field.getId())) + 
                  "((" + buildClassName(field.getType()) + ") child );");
        }
        argnr++;
      }
      println("      default: throw new RuntimeException(\"" + buildAltClassName(type, alt) + 
              " does not have an argument at position \" + argnr);");
    }
    println("    }");
    println("  }");

    println();
    println("  public int getChildCount()");
    println("  {");
    println("    return " + countVisitableChildren(type, alt) + ";");
    println("  }");  
    
    printFoldClose(); 
  }

	private int countVisitableChildren(Type type, Alternative alt) {
		Iterator fields = type.altFieldIterator(alt.getId());
    int count = 0;
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      if (!isReservedType(field.getType())) {
        count++;
      }
    }
    return count;
	}

  
	private void genAltInitialize(Type type, Alternative alt) 
  {
    printFoldOpen("initialize(aterm.ATermFactory f");
    println("  static public void initialize(aterm.ATermFactory f)");
    println("  {");
    println("    pattern = f.parse(\"" + escapeQuotes(alt.buildMatchPattern().toString()) + "\");");
    println("  }");
    println();
    printFoldClose();
	}

	private void genAltGetAndSetMethods(Type type, Alternative alt) {
    printFoldOpen("getters and setters");
    
    Iterator fields = type.altFieldIterator( alt.getId());
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      genAltGetAndSetMethod(type, alt, field);
    }
    
    printFoldClose();
	}

	private void genAltGetAndSetMethod(Type type, Alternative alt, Field field) {
    String class_name = buildClassName(type);
    String alt_class_name = buildAltClassName(type, alt);
    String field_name = capitalize(buildId(field.getId()));
    String field_id = buildFieldId(field.getId());
    String field_type_id = buildClassName(field.getType());

    // getter    
    println("  public " + field_type_id + " get" + field_name + "()");
    println("  {");
    if (!isReservedType(field.getType())) {
      println("    if ( this." + field_id + " == null) {");
      println("      return " + field_type_id + ".fromTerm( term" + field_id + ");");
      println("    }");
      println("    else {");
      println("      return this." + field_id + ";");
      println("    }");
    }
    else {
      println("    return this." + field_id + ";");
    }

    println("  }");
    println();
    
    // setter
    println("  public " + class_name + " set" + field_name + "(" + field_type_id + " " + field_id + ")");
    println("  {");
    if (!isReservedType(field.getType())) {
      println("    aterm.ATerm term" + field_id + " = " + field_id + ".toTerm();");
      print  ("    return new " + alt_class_name + "("); makeActualTermAltArgumentList(type,alt); println(");");
    }
    else {
      print  ("    return new " + alt_class_name + "("); makeActualTermAltArgumentList(type,alt); println(");");
    } 
     
    
    println("  }");
    println();
	}
  
  private void genDefaultGetAndSetMethod(Type type, Field field) {
    String class_name = buildClassName(type);
    String field_name = capitalize(buildId(field.getId()));
    String field_id = buildFieldId(field.getId());
    String field_type_id = buildClassName(field.getType());
    
    // getter    
    println("  public " + field_type_id + " get" + field_name + "()");
    println("  {");
    println("     throw new RuntimeException(\"This " + class_name + " has no " + field_name + "\");"); 
    println("  }");
    println();
    
    // setter
    println("  public " + class_name + " set" + field_name + "(" + field_type_id + " " + field_id + ")");
    println("  {");
    println("     throw new RuntimeException(\"This " + class_name + " has no " + field_name + "\");");  
    println("  }");
    println();
  }

  private void genFieldMembers(Type type, Alternative alt)
  {
    printFoldOpen("private members");
    Iterator fields = type.altFieldIterator(alt.getId());
    int argnr = 0;
    
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      String field_class_name = buildClassName(field.getType());
      String field_id = buildFieldId(field.getId());
      
      println("  private " + field_class_name + " " + field_id + " = null;");
      
      if (!isReservedType(field.getType())) {
        println("  private aterm.ATerm term" + field_id + " = null;");
      }
      
      println();
      
      argnr++;
    }
    printFoldClose();
  }

  private void genAltConstructors(Type type, Alternative alt)
  {
    printFoldOpen("constructors");
    String alt_class = buildAltClassName(type, alt);
    Iterator fields = type.altFieldIterator(alt.getId());
    
    if (!fields.hasNext()) { // a constructor without children
      genConstantAltConstructor(type, alt);
    }
    else {
      genApplicationAltConstructors(type, alt); 
    }
    printFoldClose(); 
  }
  
  private void genConstantAltConstructor(Type type, Alternative alt)
  {
    println("  public " + buildAltClassName(type, alt) + "()");
    println("  {");
    println("    term = pattern;");
    println("  }");
    println();
  }
  
  private void genApplicationAltConstructors(Type type, Alternative alt)
  {
    String alt_class_name = buildAltClassName(type, alt);
    Iterator fields;
    int argnr;
    
    print  ("  public " + alt_class_name + "("); makeFormalTypedAltArgumentList(type, alt); println(")");
    println("  {");
    print  ("    make" + alt_class_name + "("); makeActualTypedArgumentList(type, alt); println(");");
    
    fields = type.altFieldIterator(alt.getId());
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      String field_id = buildFieldId(field.getId());
      println("    this." + field_id + " = " + field_id + ";");
    }
    println("  }");
    println();
    
    if (!allFieldsReservedTypes(type, alt)) {
      print  ("  private " + alt_class_name + "("); makeFormalUntypedAltArgumentList(type, alt); println(")");
      println("  {");
      print  ("    make" + alt_class_name + "("); makeActualUntypedAltArgumentList(type, alt); println(");");
      println("  }");  
      println();
    }
    
    print  ("  private void make" + alt_class_name + "("); makeFormalUntypedAltArgumentList(type, alt); println(")");
    println("  {");
    println("    java.util.List args = new java.util.LinkedList();");
    println();
    
    fields = type.altFieldIterator(alt.getId());   
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      String field_id = buildFieldId(field.getId());
      if (!isReservedType(field.getType())) {
        println("    this." + "term" + field_id + " = " + field_id + ";");
      }
      else {
        println("    this." + field_id + " = " + field_id + ";");
      }
      println("    args.add( " + field_id + ");");
    }
    println();
    
    println("    term = pattern.getFactory().make(pattern, args);");
    println("  }");
    println();
  }

	
	private boolean allFieldsReservedTypes(Type type, Alternative alt) {
    Iterator fields = type.altFieldIterator(alt.getId());
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      if (!isReservedType(field.getType())) {
        return false;
      }
    }
    
		return true;
	}

  
	private void makeActualTypedArgumentList(Type type, Alternative alt) {
		Iterator fields = type.altFieldIterator(alt.getId());
		int argnr = 0;
		while (fields.hasNext()) {
		  Field field = (Field) fields.next();
      String field_id = buildFieldId(field.getId());
      if (!isReservedType(field.getType())) {
        print(field_id + ".toTerm()");
      }
      else {
        print(field_id);
      }
		  argnr++;
		  
		  if (fields.hasNext()) {
		    print(", ");
		  }
		}
		
	}

  private void makeActualUntypedAltArgumentList(Type type, Alternative alt) {
    Iterator fields = type.altFieldIterator(alt.getId());
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      String field_id = buildFieldId(field.getId());
      print(field_id);
      
      if (fields.hasNext()) {
        print(", ");
      }
    }
    
  }
  
  private void makeActualTermAltArgumentList(Type type, Alternative alt) {
    Iterator fields = type.altFieldIterator(alt.getId());
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      String field_id = buildFieldId(field.getId());
      if (!isReservedType(field.getType())) {
        print("term" + field_id);
      }
      else {
        print(field_id);
      }
      
      if (fields.hasNext()) {
        print(", ");
      }
    }
    
  }
	private void makeFormalTypedAltArgumentList(Type type, Alternative alt) {
		Iterator fields = type.altFieldIterator(alt.getId());
		while (fields.hasNext()) {
		  Field field = (Field) fields.next();
      String field_id = buildFieldId(field.getId());
      print(buildClassName(buildClassName(field.getType())) + " " + field_id);
      
		  if (fields.hasNext()) {
		    print(", ");
		  }
		}
	}
  
  private void makeFormalUntypedAltArgumentList(Type type, Alternative alt) {
    Iterator fields = type.altFieldIterator(alt.getId());
    
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      String field_id = buildFieldId(field.getId());
      if (!isReservedType(field.getType())) {
        print("aterm.ATerm " + field_id);
      }
      else {
        print(buildClassName(field.getType()) + " " + field_id);
      }
     
      if (fields.hasNext()) {
        print(", ");
      }
    }
  }
    
  //}}}
  //{{{ private void genAltToTerm(Type type, Alternative alt)

  private void genAltFromTerm(Type type, Alternative alt)
  {
    String class_name = buildClassName(type);
    String alt_class_name = buildAltClassName(type, alt);
    Iterator fields;
    int argnr;
   
    printFoldOpen("fromTerm(ATerm trm)"); 
    println("  static public " + class_name + " fromTerm(aterm.ATerm trm)");
    println("  {");
    println("    java.util.List children = trm.match(pattern);");
    
    println("    if (children != null) {");
    
    fields = type.altFieldIterator(alt.getId());
    argnr = 0;
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      String field_id = buildFieldId(field.getId());
      if (!isReservedType(field.getType())) {
        println("      aterm.ATerm " + field_id + "= (aterm.ATerm) children.get(" + argnr + ");");
      }
      else {
        String field_class_name = buildClassName(field.getType());
        
        println("      " + field_class_name + " " + field_id + " = (" + 
                field_class_name + ") children.get(" + argnr + ");");
      }
      argnr++;
    }
    
    print  ("      return (" + class_name + ")" + 
    " new " + alt_class_name + "(");  makeActualUntypedAltArgumentList(type, alt) ; println(");");
    
    println("    }"); // endif
      
    println("    else {");
    println("      return null;");
    println("    }");
    println("  }");
    printFoldClose();
  }
 

  //}}}

  private void genTomSignatureFile(ADT api) {
    String filename = buildId(api_name);
    createTomSignatureStream(filename);
    
    info("generating " + filename + ".t");
    
    genTomBuiltinTypes();
    genTomTypes(api);
  }

	private void genTomBuiltinTypes() {
    println("%typeterm String {");
    println("  implement { String }");
    println("  get_fun_sym(t) { t }");
    println("  cmp_fun_sym(s1,s2) { s1.equals(s2) }");
    println("  get_subterm(t,n) { null }");
    println("}");
    println();
    println("%typeterm Integer {");
    println("  implement { Integer }");
    println("  get_fun_sym(t) { t }");
    println("  cmp_fun_sym(s1,s2) { s1.equals(s2) }");
    println("  get_subterm(t,n) { null }");
    println("}");
    println();
    println("%typeterm ATerm {");
    println("  implement { ATerm }");
    println("  get_fun_sym(t) { t }");
    println("  cmp_fun_sym(s1,s2) { s1 == s2 }");
    println("  get_subterm(t,n) { null }");
    println("}");
    println();
	}


  private void genTomTypes(ADT api) {
    Iterator types = api.typeIterator();
    
    while (types.hasNext()) {
      Type type = (Type) types.next(); 
      genTomType(type);
    }
  }

  private void genTomType(Type type) {
    String class_name = buildClassName(type);
    println("%typeterm " + class_name + " {");
    println("  implement { " + class_name + " }");
    println("  get_fun_sym(t) { null }");
    println("  cmp_fun_sym(s1,s2) { false }");
    println("  get_subterm(t,n) { null }");
    println("}");
    println();
    
    genTomAltOperators(type);
  }

  private void genTomAltOperators(Type type) {
    Iterator alts = type.alternativeIterator();
    
    while (alts.hasNext()) {
      Alternative alt = (Alternative) alts.next();
      genTomAltOperator(type, alt);
    }
  }

  private void genTomAltOperator(Type type, Alternative alt) {
    String class_name = buildClassName(type);
    String operator_name = capitalize(buildId(alt.getId()));
    String alt_class_name = buildAltClassName(type,alt);
      
    print  ("%op " + class_name + " " + alt_class_name); // operator_name if Tom can deal with it
    
    Iterator fields = type.altFieldIterator(alt.getId());
    if (fields.hasNext()) {
      print("(");
      while (fields.hasNext()) {
        Field field = (Field) fields.next();
        String field_id = buildId(field.getId());
        String field_class = buildClassName(field.getType());
        String field_type = field_class;
        print  (field_id + ":" + field_type);
      
        if (fields.hasNext()) {
          print (", ");
        }
      }
      print(")");
    }
    println(" {");
    println("  fsym { }");
    println("  is_fsym(t) { t.is" + operator_name + "() }");
    fields = type.altFieldIterator(alt.getId());
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      String field_id = buildId(field.getId());
      println("  get_slot(" + field_id + ",t) { t.get" + capitalize(field_id) +
              "() }");
    }
    println("}");
    println();    
  }

  //{{{ private String buildClassName(Type type)

  private String buildClassName(Type type)
  {
    String typeId = type.getId();

    return buildClassName(typeId);
  }

	private String buildClassName(String typeId) {
		String nativeType = (String)reservedTypes.get(typeId);
		
		if (nativeType != null) {
		  return nativeType;
		}
		
		return buildId(typeId);
	}

  //}}}
  //{{{ private String buildAltClassName(Type type, Alternative alt)

  private String buildAltClassName(Type type, Alternative alt)
  {
    return buildClassName(type.getId()) + "_" + capitalize(buildId(alt.getId()));
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
 
  //{{{ private void printFoldOpen(int fold_level, String comment)

  private void printFoldOpen(int fold_level, String comment)
  {    
    if (folding) {
      for (int i=0; i<fold_level; i++) {
        print("  ");
      }
      println("//{{" + "{ " + comment);
      println();
    }
  }

  //}}}
  //{{{ private void printFoldClose(int fold_level)

  private void printFoldClose(int fold_level)
  {
    if (folding) {
      for (int i=0; i<fold_level; i++) {
        print("  ");
      }
      println("//}}" + "}");
    }
    else {
      println();
    }
  }

  //}}}

  //{{{ private void printFoldOpen(String comment)

  protected void printFoldOpen(String comment)
  {
    printFoldOpen(1, comment);
  }

  //}}}
  //{{{ private void printFoldClose()

  protected void printFoldClose()
  {
    printFoldClose(1);
  }

  //}}}

}
