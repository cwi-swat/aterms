package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.Type;
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
		super(directory, className(apiName), pkg, standardImports, verbose);
		this.adt = adt;
	}

	public static String className(String apiName) {
		return StringConversions.capitalize(apiName) + "Visitor";
	}

	protected void generate() {
		printPackageDecl();
		printImports();

		println("public abstract class Visitor extends jjtraveler.VoidVisitor implements jjtraveler.Visitor);");
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
		String className = AlternativeGenerator.className(type, alt);
		println("public abstract void visit_" + className + "(" + className + " arg ) throws jjtraveler.VisitFailure;");
	}

	private void genVoidVisit() {
		println("public void voidVisit(jjtraveler.Visitor v) {");
		println(
            "if (any instanceof Visitable) {" +            "      ((Visitable) any).accept(this);" +            "    } else {" +            "      throw new jjtraveler.VisitFailure();" +            "    }" +            "  }");
		println("}");
	}

}
