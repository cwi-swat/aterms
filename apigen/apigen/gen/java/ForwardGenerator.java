
package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.Type;
import apigen.gen.StringConversions;

public class ForwardGenerator extends JavaGenerator {
    private ADT adt;
    
	public ForwardGenerator(
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
		  return StringConversions.capitalize(apiName + "Fwd");
	}
	 
	protected void foreachType(ADT adt) {
		Iterator types = adt.typeIterator();
		while(types.hasNext()) {
			Type type = (Type) types.next();
			//TODO: implement Forward generator
		}
	}

	protected void generate() {
		printPackageDecl();
		printImports();
		foreachType(adt);
	}

}
