package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.Type;

public class VisitorGenerator extends JavaGenerator {
	private ADT adt;
    private String apiName; 
    private String className;

	public VisitorGenerator(
		ADT adt,
		String directory,
		String apiName,
		String pkg,
		List standardImports,
		boolean verbose,
		boolean folding) {
		super(directory, className(apiName), pkg, standardImports, verbose);
		this.adt = adt;
        this.apiName = apiName;
        className = className(apiName);
        
	}

	public static String className(String apiName) {
		return  "Visitor";
	}

	protected void generate() {
		printPackageDecl();
	
		println("public abstract class " + className + " extends jjtraveler.VoidVisitor");
		println("{");
		genVisits(adt);
		println("}");
	}
    
	protected void genVisits(ADT adt) {
		Iterator types = adt.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			Iterator alts = type.alternativeIterator();
			while (alts.hasNext()) {
				Alternative alt = (Alternative) alts.next();
				genVisit(type, alt);
			}
		}
	}

	private void genVisit(Type type, Alternative alt) {
		String classImplName = AlternativeImplGenerator.className(type, alt);
        String className = AlternativeGenerator.className(type, alt);
        
		println("public abstract void visit_" + className + "(" + classImplName + " arg ) throws jjtraveler.VisitFailure;");
	}

}
