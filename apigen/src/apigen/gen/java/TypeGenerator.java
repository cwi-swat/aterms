
package apigen.gen.java;

import java.io.File;
import java.util.List;

import apigen.adt.Type;
import apigen.gen.StringConversions;


public class TypeGenerator extends JavaGenerator {
    private Type type;
    private String className;
    
	protected TypeGenerator(
	    Type type,
		String directory,
		String pkg,
		List standardImports,
		boolean verbose,
		boolean folding) {
		super(directory, className(type), pkg, standardImports, verbose, folding);
		this.type = type;
		className = className(type);
	}

    public static String className(String type) {
    	return StringConversions.makeIdentifier(converter.getType(type));
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
	 
	private void printTypeClass(Type type) {
		println("abstract public class " + className);
		println("extends " + TypeImplGenerator.className(type));
		println("{");
		println();
		println("}");
	  }
}
