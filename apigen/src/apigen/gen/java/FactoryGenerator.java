
package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.*;
import apigen.gen.StringConversions;

public class FactoryGenerator extends JavaGenerator {
    private String className;
    private ADT adt;
    private boolean jtype;
  
	public FactoryGenerator(ADT adt, String directory, String apiName, String pkg, List standardImports, 
	                                    boolean verbose, boolean folding) {
		super(directory,className(apiName),pkg,standardImports,verbose);
		this.className = className(apiName);
		this.adt = adt;
    }

	public static String className(String apiName) {
		return  StringConversions.makeCapitalizedIdentifier(apiName) + "Factory";
	}
	
	protected void generate() {
	   printPackageDecl();
    
       imports.add("aterm.pure.PureFactory");
	   printImports(); 
    
	   genFactoryClass(adt);
	 }

	 private void genFactoryClass(ADT api)  {  
	   println("public class " + className + " extends PureFactory");
	   println("{");
	   genFactoryPrivateMembers(api); 
	   println("  public " + className + "()");
	   println("  {");
	   println("     super();");
	   println("     initialize();");
	   println("  }");
	   println("");
	   println("  public " + className + "(int logSize)");
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
	   
	   Iterator types = api.typeIterator();
	   while (types.hasNext()) {
		 Type type = (Type) types.next();
		 Iterator alts = type.alternativeIterator();
		 while (alts.hasNext()) {
		   Alternative alt = (Alternative) alts.next();
		   String altClassName = AlternativeGenerator.className(type,alt);
		   String typeClassName = TypeGenerator.className(type.getId());
		   String protoVar = "proto" + altClassName;
		   String funVar = "fun" + altClassName;
        
		   println("  private aterm.AFun " + funVar + ";");
		   println("  private " + typeClassName + " " + protoVar + ";");
		 }
	   }
	   }
	   
	   private void genFactoryMakeMethods(ADT api) {
	   Iterator types = api.typeIterator();
	   while (types.hasNext()) {
		 Type type = (Type) types.next();
		 Iterator alts = type.alternativeIterator();
		 while (alts.hasNext()) {
		   Alternative alt = (Alternative) alts.next();
		   String altClassName = AlternativeGenerator.className(type,alt);
		   String protoVar = "proto" + altClassName;
		   String funVar = "fun" + altClassName;
        
		   println("  protected " + altClassName + " make" + altClassName + 
				   "(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {");
		   println("    synchronized (" + protoVar + ") {");
		   println("      " + protoVar + ".initHashCode(annos,fun,args);");
		   println("      return (" + altClassName + ") build(" + protoVar + ");");
		   println("    }");
		   println("  }");
		   println();
		   print  ("  public " + altClassName + " make" + altClassName + "(");
		   printFormalTypedAltArgumentList(type, alt);
		   println(") {");
		   print  ("    aterm.ATerm[] args = new aterm.ATerm[] {");
		   printActualTypedArgumentList(type,alt);
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
      
		 println("    " + TypeGenerator.className(type) + ".initialize(this);");
      
		 Iterator alts = type.alternativeIterator();
		 while (alts.hasNext()) {
		   Alternative alt = (Alternative)alts.next();
		   String altClassName = AlternativeGenerator.className(type,alt);
		   String protoVar = "proto" + altClassName;
		   String funVar = "fun" + altClassName;
           String afunName = alt.getId();
                   
		   println();        
		   println("    " + altClassName + ".initializePattern();");
		   println("    " + funVar + " = makeAFun(\"" + 
				   afunName + "\", " + type.getAltArity(alt) + ", false);");
		   println("    " + protoVar + " = new " + altClassName + "();");      
		 }
		 println();
	   }
    
	   Iterator bottoms = api.bottomTypeIterator();
    
	   while (bottoms.hasNext()) {
		 String type = (String) bottoms.next();
      
		 if (!converter.isReserved(type)) {
		   println("    " + StringConversions.makeCapitalizedIdentifier(type) + ".initialize(this);");
		 }
	   }
	 }
}
