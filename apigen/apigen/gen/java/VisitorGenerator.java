
package apigen.gen.java;

import java.util.List;

import apigen.adt.ADT;
import apigen.gen.StringConversions;

public class VisitorGenerator extends JavaGenerator {
    private ADT adt;
    
	public VisitorGenerator(
	    ADT adt,
		String directory,
		String apiName,
		String pkg,
		List standardImports,
		boolean verbose,
		boolean folding) {
		super(directory, className(apiName), pkg, standardImports, verbose, folding);
		this.adt = adt;
	}

    public static String className(String apiName) {
    	return StringConversions.capitalize(apiName) + "Visitor";
    }
    
	protected void generate() {
//TODO: implement Visitor Generator
	}

}
