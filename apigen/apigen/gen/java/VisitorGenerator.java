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
					genVisit(type, alt);
				}
			}
		}
	}

	private void genListVisit(Type type) {
		String className = TypeGenerator.className(type);
		String classImplName = ListTypeGenerator.className(type);
		genVisitDecl(classImplName, className);
	}

	private void genVisit(Type type, Alternative alt) {
		String classImplName = AlternativeGenerator.className(type, alt);
		String className = AlternativeGenerator.className(type, alt);
		genVisitDecl(classImplName, className);
	}

	private void genVisitDecl(String classImplName, String className) {
		println(
			"public abstract void visit_" + className + "(" + classImplName + " arg ) throws jjtraveler.VisitFailure;");
	}

}
