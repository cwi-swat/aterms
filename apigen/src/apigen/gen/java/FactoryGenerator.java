
package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.*;
import apigen.gen.StringConversions;

public class FactoryGenerator extends JavaGenerator {
    private String className;
    private ADT adt;
    
	public FactoryGenerator(ADT adt, String directory, String apiName, String pkg, List standardImports, 
	                                    boolean verbose, boolean folding) {
		super(directory,getFactoryClassName(apiName),pkg,standardImports,verbose,folding);
		this.className = getFactoryClassName(apiName);
		this.adt = adt;
	}

	public static String getFactoryClassName(String apiName) {
		return  apiName + "Factory";
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
		   String altClassName = AlternativeGenerator.getAltClassName(type.getId(),alt.getId());
		   String typeClassName = getClassName(type.getId());
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
		   String altClassName = AlternativeGenerator.getAltClassName(type.getId(),alt.getId());
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
      
		 println("    " + getClassName(type.getId()) + ".initialize(this);");
      
		 Iterator alts = type.alternativeIterator();
		 while (alts.hasNext()) {
		   Alternative alt = (Alternative)alts.next();
		   String altClassName = AlternativeGenerator.getAltClassName(type.getId(),alt.getId());
		   String protoVar = "proto" + altClassName;
		   String funVar = "fun" + altClassName;

		   println();        
		   println("    " + altClassName + ".initializePattern();");
		   println("    " + funVar + " = makeAFun(\"" + 
				   altClassName + "\", " + type.getAltArity(alt) + ", false);");
		   println("    " + protoVar + " = new " + altClassName + "();");      
		 }
		 println();
	   }
    
	   Iterator bottoms = api.bottomTypeIterator();
    
	   while (bottoms.hasNext()) {
		 String type = (String) bottoms.next();
      
		 if (!JavaTypeConversions.isReserved(type)) {
		   println("    " + StringConversions.makeCapitalizedIdentifier(type) + ".initialize(this);");
		 }
	   }
	 }
}
