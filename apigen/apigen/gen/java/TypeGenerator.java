
package apigen.gen.java;

import java.io.File;
import java.util.List;

import apigen.adt.Type;
import apigen.gen.StringConversions;


public class TypeGenerator extends JavaGenerator {
    private Type type;
    private String className;
    private String apiName;
    
	protected TypeGenerator(
	    Type type,
		String directory,
		String pkg,
		String apiName, 
		List standardImports,
		boolean verbose,
		boolean folding) {
		super(directory, className(type), pkg, standardImports, verbose);
		this.type = type;
		this.apiName = apiName;
		className = className(type);
	}

  public static String className(String type) {
  	if (converter.isReserved(type)) {
  		return converter.getType(type);
  	}
  	else {
  		return StringConversions.makeIdentifier(converter.getType(type));
  	}
  }
    
  public static String className(Type type) {
    return className(type.getId());
  }
		
    public void run() {
    	if (!new File(getPath(directory,className,".java")).exists()) {
    		super.run();
    	}
    	else {
    		info("preserving " + className);
    	}
    }
    
	protected void generate() {
		printPackageDecl();
	    printTypeClass(type); 
	 }
	 
	protected String classModifier() {
		return "public abstract";
	}
	
	protected void printTypeClass(Type type) {
		println(classModifier() + " class " + className);
		println("extends " + TypeImplGenerator.className(type));
		println("{");
		println("  " + className + "(" + FactoryGenerator.className(apiName) + " factory) {");
		println("    super(factory);");
		println("  }");
		println();
		println("}");
	  }
}
