
package apigen.gen.java;

import java.util.List;

public class VisitorGenerator extends JavaGenerator {

	public VisitorGenerator(
		String directory,
		String filename,
		String pkg,
		List standardImports,
		boolean verbose,
		boolean folding) {
		super(directory, filename, pkg, standardImports, verbose, folding);
		//TODO
	}

    public static String className(String apiName) {
    	return apiName + "Visitor";
    }
    
	protected void generate() {
//TODO
	}

}
