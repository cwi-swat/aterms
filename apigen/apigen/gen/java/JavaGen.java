package apigen.gen.java;

//{{{ imports

import java.io.*;
import java.util.*;

// import aterm.pure.PureFactory;

import apigen.*;
import apigen.adt.*;
import apigen.gen.Generator;
import aterm.ParseError;
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
  private String api_factory;
  private String api_constructor;
  
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
    initializeConstants(RESERVED_TYPES);
        
    this.api_name = api_name;
    this.api_factory = capitalize(buildId(api_name)) + "Factory";
    this.api_constructor = capitalize(buildId(api_name)) + "Constructor";
    this.input   = input;
    this.basedir = basedir;
    this.pkg	 = pkg;
    this.imports = imports;
    


    factory = new PureFactory();
    try {
      aterm.ATerm adt = factory.readFromFile(input);
      ADT api = new ADT(adt);
    
      genFactoryClassFile(api);
      genGenericConstructorClassFile();
      genTypeClassFiles(api);
    
      if (jtom) {
        genTomSignatureFile(api);
      }
    }
    catch (ParseError exc) {
      System.err.println("A parse error occurred in the ADT file:");
      System.err.println(exc);
    }
  }

	

  private void genFactoryClassFile(ADT api) throws IOException {
    class_name = api_factory;
    
    info("generating " + class_name);
    
    createClassStream(class_name);
    
    genPackageDecl();
    
    List extra = new LinkedList();
    extra.add("aterm.pure.PureFactory");
    extra.addAll(imports);
    genImports(extra); 
    
    genFactoryClass(api);
    
    stream.close();
  }

  private void genGenericConstructorClassFile()
  {
    class_name = api_constructor;
    
    createClassStream(class_name);
    info("generating " + class_name);
    
    genPackageDecl();
    genGenericConstructorClass();
    
    stream.close();
  }
  
  private void genGenericConstructorClass()
  { 
    println("abstract public class " + class_name);
    println("extends aterm.pure.ATermApplImpl");
    println("implements aterm.ATerm");
    println("{");
    println("  protected aterm.ATerm term = null;");
    println();
    println("  abstract protected aterm.ATerm getPattern();");
    println();
    println("  public aterm.ATerm toTerm() {");
    println("    if(term == null) {");
    println("      java.util.List args = new java.util.LinkedList();");
    println("      for(int i = 0; i<getArity() ; i++) {");
    println("        args.add(((" + class_name + ") getArgument(i)).toTerm());");
    println("      }");
    println("      setTerm(getFactory().make(getPattern(), args));");
    println("    }");
    println("    return term;");
    println("  }");
    println();
    println("  public String toString() {");
    println("    return toTerm().toString();");
    println("  }");
    println();
    println("  protected void setTerm(aterm.ATerm term) {");
    println("   this.term = term;");
    println("  }");
    println();
    println("  protected " + api_factory + " get" + api_factory + "() {");
    println("    return (" + api_factory + ") getFactory();");
    println("  }");
    println();
    println("  static protected " + api_factory + " getStatic" + api_factory + "() {");
    println("    return (" + api_factory + ") getStaticFactory();");
    println("  }");
    println("}");
  }
 
	private void closeStream() {
		stream.close();
	}
  
  private void genFactoryClass(ADT api) throws IOException {  
    println("public class " + class_name + " extends PureFactory");
    println("{");
    genFactoryPrivateMembers(api); 
    println("  public " + class_name + "()");
    println("  {");
    println("     super();");
    println("     initialize();");
    println("  }");
    println("");
    println("  public " + class_name + "(int logSize)");
    println("  {");
    println("     super(logSize);");
    println("     initialize();");
    println("  }");
    println("");
    println("  private void initialize()");
    println("  {");    
    genFactoryInitialization(api);
    println("  }");
    println();
    genFactoryMakeMethods(api);
    println("}");
  }

	private void genFactoryPrivateMembers(ADT api) {
    printFoldOpen("private members");
    Iterator types = api.typeIterator();
    while (types.hasNext()) {
      Type type = (Type) types.next();
      Iterator alts = type.alternativeIterator();
      while (alts.hasNext()) {
        Alternative alt = (Alternative) alts.next();
        String altClassName = buildAltClassName(type,alt);
        String typeClassName = buildClassName(type.getId());
        String protoVar = "proto" + altClassName;
        String funVar = "fun" + altClassName;
        
        println("  private aterm.AFun " + funVar + ";");
        println("  private " + typeClassName + " " + protoVar + ";");
      }
    }
    printFoldClose();
	}

	private void genFactoryMakeMethods(ADT api) {
    Iterator types = api.typeIterator();
    while (types.hasNext()) {
      Type type = (Type) types.next();
      Iterator alts = type.alternativeIterator();
      while (alts.hasNext()) {
        Alternative alt = (Alternative) alts.next();
        String altClassName = buildAltClassName(type,alt);
        String typeClassName = buildClassName(type.getId());
        String protoVar = "proto" + altClassName;
        String funVar = "fun" + altClassName;
        
        printFoldOpen("make " + altClassName);
        println("  protected " + typeClassName + " make" + altClassName + 
                "(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {");
        println("    synchronized (" + protoVar + ") {");
        println("      " + protoVar + ".initHashCode(annos,fun,args);");
        println("      return (" + typeClassName + ") build(" + protoVar + ");");
        println("    }");
        println("  }");
        println();
        print  ("  public " + typeClassName + " make" + altClassName + "(");
        makeFormalTypedAltArgumentList(type, alt);
        println(") {");
        print  ("    aterm.ATerm[] args = new aterm.ATerm[] {");
        makeActualTypedArgumentList(type,alt);
        println("};");
        println("    return make" + altClassName + "( " + funVar + ", args, empty);"); 
        println("  }");
        println();
      }
    }
	}
 
  private void genFactoryInitialization(ADT api)
  {
    Iterator types = api.typeIterator();
    while (types.hasNext()) {
      Type type = (Type)types.next();
      
      println("    " + buildClassName(type) + ".initialize(this);");
      
      Iterator alts = type.alternativeIterator();
      while (alts.hasNext()) {
        Alternative alt = (Alternative)alts.next();
        String altClassName = buildAltClassName(type,alt);
        String protoVar = "proto" + altClassName;
        String funVar = "fun" + altClassName;

        println();        
        println("    " + altClassName + ".initializePattern();");
        println("    " + funVar + " = makeAFun(\"" + 
                altClassName + "\", " + computeAltArity(type,alt) + ", false);");
        println("    " + protoVar + " = new " + altClassName + "();");      
      }
      println();
    }
    
    Iterator bottoms = api.bottomTypeIterator();
    
    while (bottoms.hasNext()) {
      String type = (String) bottoms.next();
      
      if (!isReservedType(type)) {
        println("    " + capitalize(buildId(type)) + ".initialize(this);");
      }
    }
  }

	private int computeAltArity(Type type, Alternative alt) {
    // There must be a better way to do this, it can be computed when building
    // the ADT datastructures
		Iterator fields = type.altFieldIterator(alt.getId());
    int arity = 0;
    
    for(arity = 0; fields.hasNext(); fields.next()) {
      arity++;
    }
    
    return arity;
  }

  
	private void genTypeClassFiles(ADT api) throws IOException {
		Iterator types = api.typeIterator();
		while (types.hasNext()) {
		  Type type = (Type)types.next();
      genTypeClassImplFile(type);
		  genTypeClassFile(type);
		}
	}

  private void genTypeClassImplFile(Type type)
    throws IOException
  {
    List extra = new LinkedList();
    String class_name = buildClassImplName(type);    

    createClassStream(class_name);
    
    info("generating " + class_name);

    genPackageDecl();
    extra.add("java.io.InputStream");
    extra.add("java.io.IOException");
    extra.addAll(imports);
    genImports(extra);
    println();
    
    genTypeClassImpl(type); 
    stream.close();
       
    genAlternativesClassesFiles(type);
  }
    
  private void genTypeClassFile(Type type)
    throws IOException
  {
    List imports = new LinkedList();
    String class_name = buildClassName(type); 
    String file = basedir + File.separatorChar + class_name + ".java";
    
    if (!new File(file).exists()) {   
      createClassStream(class_name);
    
      info("generating " + class_name);

      genPackageDecl();
      genTypeClass(type); 
      stream.close();
    }
    else {
      info("preserving " + class_name);
    }
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
      genAlternativeClassImplFile(type, alt);
		  genAlternativeClassFile(type, alt);
		}
	}
  
  private void genAlternativeClassImplFile(Type type, Alternative alt)
  {
    String class_name = buildAltClassImplName(type, alt);
    createClassStream(class_name);
    
    info("generating " + class_name);
    
    genPackageDecl();
    genImports(imports);
    genAlternativeClassImpl(type, alt);
    
    stream.close();
  }

  private void genAlternativeClassFile(Type type, Alternative alt)
  {
    String class_name = buildAltClassName(type,alt);
    String file = basedir + File.separatorChar + class_name + ".java";
    
    if (!new File(file).exists()) {
      createClassStream(class_name);
    
      info("generating " + class_name);
    
      genPackageDecl();
      genAlternativeClass(type, alt);
    
      stream.close();
    }
    else {
      info("preserving " + class_name);
    }
  }

	private void genAlternativeClass(Type type, Alternative alt) {
    String alt_class = buildAltClassName(type, alt);
    String alt_impl_class = buildAltClassImplName(type,alt);
    
    println("public class " + alt_class);
    println("extends " + alt_impl_class);
    println("{");
    println();
    println("}");
	}

  
  private void createClassStream(String class_name) {
    createFileStream(class_name, ".java"); 
  }
  
  private void createTomSignatureStream(String name) {
    createFileStream(name, ".t");
  }
  
  private void createFileStream(String name, String ext) {
    char sep = File.separatorChar;
    File base = new File(basedir);
    
    if (!base.exists()) {
      if(!base.mkdirs()) {
        throw new RuntimeException("could not create output directory " + basedir);
      }
    }
    else if (!base.isDirectory()) {
      throw new RuntimeException(basedir + " is not a directory");
    }
    
    createStream(basedir + sep + name + ext);
  }
	
  private void genTypeClass(Type type) {
    String class_name = buildClassName(type);
    String class_impl_name = buildClassImplName(type);
    
    println("abstract public class " + class_name);
    println("extends " + class_impl_name);
    println("{");
    println();
    println("}");
  }
  
	private void genTypeClassImpl(Type type) {
    String class_impl_name = buildClassImplName(type);
    String class_name = buildClassName(type);
    String get_factory = "getStatic" + api_factory + "()";
       
		println("abstract public class " + class_impl_name + " extends " + api_constructor);
		println("{");

    printFoldOpen("fromString()");
		println("  static " + class_name + " fromString(String str)");
		println("  {");
		println("    aterm.ATerm trm = " + get_factory + ".parse(str);");
		println("    return fromTerm(trm);");
		println("  }");
    printFoldClose();
    printFoldOpen("fromTextFile()");
		println("  static " + class_name + " fromTextFile(InputStream stream) " +
            "throws aterm.ParseError, IOException");
		println("  {");
		println("    aterm.ATerm trm = " + get_factory + ".readFromTextFile(stream);");
		println("    return fromTerm(trm);");
		println("  }");
    printFoldClose();
		printFoldOpen("isEqual(" + class_name + ")");
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
  
  private void genImports(List imports) {	
    printFoldOpen("imports");
    Iterator iter = imports.iterator();
    while (iter.hasNext()) {
      println("import " + (String)iter.next() + ";");
    }
    printFoldClose();
  }
  
  private void genAlternativeClassImpl(Type type, Alternative alt)
  {
    String type_id = buildId(type.getId());
    String alt_class = buildAltClassImplName(type, alt);
    
    println("public class " + alt_class);
    println("extends " + type_id);
    if (visitable) {
      println("implements Visitable");
    }
    println("{");
    println("  static private aterm.ATerm pattern = null;");
    println();
    println("  protected aterm.ATerm getPattern() {");
    println("    return pattern;");
    println("  }");
    
    genAltFieldIndexMembers(type,alt);
    genAltClone(type, alt);
    genAltMake(type, alt);
    genAltInitialize(type, alt);
    genAltFromTerm(type, alt);
    genAltToTerm(type,alt);
    genOverrideProperties(type,alt);
    genAltGetAndSetMethods(type, alt);
    genAltHashFunction(type, alt);
    if (visitable) {
      genAltVisitableInterface(type, alt);
    }
    
    println("}");
  }

	private void genAltHashFunction(Type type, Alternative alt) 
  {       
    if (!hasReservedTypeFields(type,alt)) {
      int arity = computeAltArity(type, alt);
      String goldenratio = "0x9e3779b9";
      int changingArg = guessChangingArgument(type,alt);
      String initval;
    
      if (changingArg > 0) {
        initval = "getArgument(" + changingArg + ").hashCode()";
      }
      else {
        initval = "0";
      }
    
      println("  protected int hashFunction() {");
      println("    int c = " + initval + " + (getAnnotations().hashCode()<<8);");
      println("    int a = " + goldenratio + ";");
      println("    int b = " + goldenratio + ";");
      
      for (int i = arity - 1; i >= 0; i--) {
        int shift = (i%4) * 8;
        println("    " + "aaaabbbbcccc".toCharArray()[i%12] + 
                " += (getArgument(" + i + ").hashCode() << " + 
                shift + ");");
      }
  
      println();
      println("    a -= b; a -= c; a ^= (c >> 13);");
      println("    b -= c; b -= a; b ^= (a << 8);");
      println("    c -= a; c -= b; c ^= (b >> 13);");
      println("    a -= b; a -= c; a ^= (c >> 12);");
      println("    b -= c; b -= a; b ^= (a << 16);");
      println("    c -= a; c -= b; c ^= (b >> 5);");
      println("    a -= b; a -= c; a ^= (c >> 3);");
      println("    b -= c; b -= a; b ^= (a << 10);");
      println("    c -= a; c -= b; c ^= (b >> 15);");
      println();
      println("    return c;");
      println("  }");
    }
	}

	private int guessChangingArgument(Type type, Alternative alt) {
    Iterator fields = type.altFieldIterator(alt.getId());
    
    /* if an argument has the same type as the result type, there
     * exists a chance of building a tower of this constructor where
     * only this argument changes. Therefore, this argument must be
     * very important in the computation of the hash code in order to
     * avoid collissions
     */
    for(int i = 0; fields.hasNext(); i++) {
      Field field = (Field) fields.next();
      
      if (field.getType().equals(type.getId())) {
        return i;
      }
    }
    
		return -1;
	}



	private void genAltToTerm(Type type, Alternative alt) {
    if (hasReservedTypeFields(type, alt)) {
      println("  public aterm.ATerm toTerm() {");
      println("    if(term == null) {");
      println("      java.util.List args = new java.util.LinkedList();");
      Iterator fields = type.altFieldIterator(alt.getId());
      for (int i = 0; fields.hasNext(); i++) {
        Field field = (Field) fields.next();
        String field_type = field.getType();
        
        if (field_type.equals("str")) {
          println("      args.add(((aterm.ATermAppl) getArgument(" + i + ")).getAFun().getName());");
        }
        else if (field_type.equals("int")) {
          println("      args.add(new Integer(((aterm.ATermInt) getArgument(" + i + ")).getInt()));");
        }
        else if (field_type.equals("real")) {
          println("      args.add(new Double (((aterm.ATermReal) getArgument(" + i + ")).getReal()));");
        }
        else if (field_type.equals("term")) {
          println("      args.add((aterm.ATerm) getArgument(" + i + "));");
        }
        else {
          println("      args.add(((" + api_constructor + ") getArgument(" + i + ")).toTerm());");
        }
      }
      println("      setTerm(getFactory().make(getPattern(), args));");
      println("    }");
      println("    return term;");
      println("  }");
      println();
    }
	}

	private boolean hasReservedTypeFields(Type type, Alternative alt) {
		return computeAltArityNotReserved(type,alt) < computeAltArity(type,alt);
	}


	private void genAltMake(Type type, Alternative alt) {
    String altClassName = buildAltClassName(type,alt);
    
    println("  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] i_args," +
            " aterm.ATermList annos) {");
    println("    return get" + api_factory + "().make" + altClassName +
            "(fun, i_args, annos);");
    println("  }");
	}


	private void genAltClone(Type type, Alternative alt) {
    String altClassName = buildAltClassName(type,alt);
    println("  public Object clone() {");
    println("    " + altClassName + " clone = new " + altClassName + "();");
    println("     clone.init(hashCode(), getAnnotations(), getAFun(), " +
            "getArgumentArray());");
    println("    return clone;");
    println("  }");
    println();
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
    println("    return " + computeAltArityNotReserved(type, alt) + ";");
    println("  }");  
    
    printFoldClose(); 
  }

	private int computeAltArityNotReserved(Type type, Alternative alt) {
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
    printFoldOpen("initializePattern()");
    println("  static public void initializePattern()");
    println("  {");
    println("    pattern = getStaticFactory().parse(\"" + escapeQuotes(alt.buildMatchPattern().toString()) + "\");");
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
    String field_type = field.getType();
    String field_class = buildClassName(field_type);
    String field_index = buildFieldIndex(field.getId());

    // getter    
    println("  public " + field_class + " get" + field_name + "()");
    println("  {");
    
    if (field_type.equals("str")) {
      println("   return ((aterm.ATermAppl) this.getArgument(" + field_index + ")).getAFun().getName();");
    }
    else if (field_type.equals("int")) {
      println("   return new Integer(((aterm.ATermInt) this.getArgument(" + field_index + ")).getInt());");
    }
    else if (field_type.equals("double")) {
      println("   return new Double(((aterm.ATermReal) this.getArgument(" + field_index + ")).getReal());");
    }
    else if (field_type.equals("term")) {
      println("   return this.getArgument(" + field_index + ");");    
    }
    else {
      println("    return (" + field_class + ") this.getArgument(" + field_index + ") ;");
    }
    
    println("  }");
    println();

    // setter    
    println("  public " + class_name + " set" + field_name + "(" +
               field_class + " " + field_id + ")");
    println("  {");
    
    print("    return (" + class_name + ") this.setArgument(");
    
    if (field_type.equals("str")) {
      print("getFactory().makeAppl(getFactory().makeAFun(" + field_id + ", 0, true))");
    }
    else if (field_type.equals("int")) {
      print("getFactory().makeInt(" + field_id + ".intValue())");
    }
    else if (field_type.equals("double")) {
      print("getFactory().makeReal(" + field_id + ".doubleValue())");
    }
    else {
      print(field_id);  
    }
    
    println(", " + field_index + ");");
    
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

  private void genAltFieldIndexMembers(Type type, Alternative alt)
  {
    printFoldOpen("field indexes");
    Iterator fields = type.altFieldIterator(alt.getId());
    int argnr = 0;
    
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      String field_class_name = buildClassName(field.getType());
      String field_id = buildFieldIndex(field.getId());
      
      println("  private static int " + field_id + " = " + argnr + ";");
      argnr++;
    }
    printFoldClose();
    
  }
  	
	private void makeActualTypedArgumentList(Type type, Alternative alt) {
		Iterator fields = type.altFieldIterator(alt.getId());
		int argnr = 0;
		while (fields.hasNext()) {
		  Field field = (Field) fields.next();
      String field_id = buildFieldId(field.getId());
      String field_type = field.getType();
      
      if (field_type.equals("str")) {
        print("makeAppl(makeAFun(" + field_id + ", 0, true))");
      }
      else if (field_type.equals("int")) {
        print("makeInt(" + field_id + ".intValue())"); 
      }
      else if (field_type.equals("real")) {
        print ("makeReal(" + field_id + ".doubleValue())");
      }
      else {
        print (field_id);
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
    print  ("      " + class_name + " tmp = getStatic" + api_factory + 
            "().make" + alt_class_name + "(");

    fields = type.altFieldIterator(alt.getId());
    argnr = 0;            
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      String field_type = field.getType();
      String field_class = buildClassName(field_type);
      
      if (field_type.equals("str")) {
        print("(String) children.get(" + argnr + ")");
      }
      else if (field_type.equals("int")) {
        print("(Integer) children.get(" + argnr + ")");
      }
      else if (field_type.equals("real")) {
        print("(Double) children.get(" + argnr + ")");
      }
      else if (field_type.equals("term")) {
        print("(aterm.ATerm) children.get(" + argnr + ")");
      }
      else {
        print(field_class + ".fromTerm( (aterm.ATerm) children.get(" + argnr + "))");
      }
      
      if (fields.hasNext()) {
        print(", ");
      }
      argnr++;
    }
    println(");");
    println("      tmp.setTerm(trm);");
    println("      return tmp;");
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
    println("%typeterm Double {");
    println("  implement { Double }");
    println("  get_fun_sym(t) { t }");
    println("  cmp_fun_sym(s1,s2) { s1.equals(s2) }");
    println("  get_subterm(t,n) { null }");
    println("}");
    println();
    println("%typeterm ATerm {");
    println("  implement { ATerm }");
    println("  get_fun_sym(t) { ((t instanceof ATermAppl)?((ATermAppl)t).getAFun():null) }");
    println("  cmp_fun_sym(s1,s2) { s1 == s2 }");
    println("  get_subterm(t,n) { (((ATermAppl)t).getArgument(n)) }");
    println("}");
    println();
   
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
      
    print  ("%op " + class_name + " " + operator_name); // operator_name if Tom can deal with it
    
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

      /*
       * The following cast is not necessary.
       * It could be replaced by subject = "t"
       * This is to be compatible when the strictType option is not set
       */
    String subject = "((" + class_name + ")t)";
      //println("  is_fsym(t) { " + subject + ".is" + operator_name + "() }");
    println("  is_fsym(t) { (t instanceof " + class_name  + ")?" + subject + ".is" + operator_name + "():false }");
    
    fields = type.altFieldIterator(alt.getId());
    while (fields.hasNext()) {
      Field field = (Field) fields.next();
      String field_id = buildId(field.getId());
      println("  get_slot(" + field_id + ",t) { " + subject + ".get" + capitalize(field_id) +
              "() }");
    }
 

    String arg = "(";
    int arity = computeAltArity(type,alt);
    for(int i = 0; i < arity; i++) {
      arg += ("t" + i);
      if (i < arity - 1) {
        arg += ", ";
      }
    }
    arg += ")";
    println("  make" + arg + " { get" + api_factory + "().make" + alt_class_name + 
            arg + "}");
    
    println("}");
    println();    
  }

  //{{{ private String buildClassName(Type type)

  private String buildClassName(Type type)
  {
    String typeId = type.getId();

    return buildClassName(typeId);
  }
  
  private String buildClassImplName(Type type)
  {
    return buildClassName(type) + "Impl";
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
  
  private String buildAltClassImplName(Type type, Alternative alt)
  {
    return buildAltClassName(type,alt) + "Impl";
  }
  
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
  
  private String buildFieldIndex(String fieldId)
  {
    return "index_" + buildId(fieldId);
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
