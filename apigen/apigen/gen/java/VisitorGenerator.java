package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.gen.GenerationParameters;

public class VisitorGenerator extends JavaGenerator {
	private static final String CLASS_NAME = "Visitor";

	private ADT adt;

	public VisitorGenerator(ADT adt, GenerationParameters params) {
		super(params);
		this.adt = adt;
	}

	public String getClassName() {
		return className();
	}

	public static String className() {
		return CLASS_NAME;
	}

	protected void generate() {
		printPackageDecl();

		println("public abstract class " + getClassName() + " extends jjtraveler.VoidVisitor {");
		genVisits(adt);
		println("}");
	}

	protected void genVisits(ADT adt) {
		Iterator types = adt.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();

			if (type instanceof ListType) {
				genListVisit(type);
			}
			else {
				Iterator alts = type.alternativeIterator();
				while (alts.hasNext()) {
					Alternative alt = (Alternative) alts.next();
					genVisit(alt);
				}
			}
		}
	}

	private void genListVisit(Type type) {
		genVisitDecl(ListTypeGenerator.className(type));
	}

	private void genVisit(Alternative alt) {
		genVisitDecl(AlternativeGenerator.className(alt));
	}

	private void genVisitDecl(String className) {
		println(
			"public abstract void visit_" + className + "(" + className + " arg ) throws jjtraveler.VisitFailure;");
	}

	public String getPackageName() {
		return "";
	}

	public String getQualifiedClassName() {
		return getClassName();
	}

}
