
package apigen.gen.java;

import java.util.List;

import apigen.adt.ADT;

public class VisitorGenerator extends JavaGenerator {
    private ADT adt;
    
	public VisitorGenerator(
	    ADT adt,
		String directory,
		String filename,
		String pkg,
		List standardImports,
		boolean verbose,
		boolean folding) {
		super(directory, filename, pkg, standardImports, verbose, folding);
		this.adt = adt;
	}

    public static String className(String apiName) {
    	return apiName + "Visitor";
    }
    
	protected void generate() {
//TODO: implement Visitor Generator
	}

}
