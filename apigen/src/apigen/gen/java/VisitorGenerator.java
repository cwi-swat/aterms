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
		genVoidVisit();
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

	private void genVoidVisit() {
        String visitable = GenericConstructorGenerator.className(apiName);
        
        //TODO Make the visitor in the else branch a variation point, by adding an instance variable
        //in the visitable class which can be set via its constructor method (and per default Failure)
        
		println("  public void voidVisit(jjtraveler.Visitable any) throws jjtraveler.VisitFailure {");
		println("    if (any instanceof " + visitable + ") {");        println("        ((" + visitable +") any).jjtAccept(this);");        println("    } else {");        println("      throw new jjtraveler.VisitFailure();");        println("    }");        println("  }");
	}
}
