package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.gen.GenerationParameters;

public class ForwardGenerator extends JavaGenerator {
	private static final String CLASS_NAME = "Fwd";

	private ADT adt;
	private String constructor;

	public ForwardGenerator(ADT adt, GenerationParameters params) {
		super(params);
		setDirectory(params.getBaseDir());
		setFileName(getClassName());
		this.adt = adt;
		this.constructor = GenericConstructorGenerator.className(params.getApiName());
	}

	public String getClassName() {
		return className();
	}

	public static String className() {
		return CLASS_NAME;
	}

	protected void genVisits(ADT adt) {
		Iterator types = adt.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			Iterator alts = type.alternativeIterator();

			if (type instanceof ListType) {
				genListVisit(type);
			}
			else {
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
		genVisitMethod(className, classImplName);
	}

	private void genVisitMethod(String className, String classImplName) {
		println("public void visit_" + className + "(" + classImplName + " arg) throws jjtraveler.VisitFailure {");
		println("  any.visit(arg);");
		println("}");
	}

	private void genVisit(Type type, Alternative alt) {
		String className = AlternativeGenerator.className(type, alt);
		String classImplName = AlternativeGenerator.className(type, alt);

		genVisitMethod(className, classImplName);
	}

	protected void generate() {
		printPackageDecl();

		println("public class Fwd extends Visitor implements jjtraveler.Visitor");
		println("{");
		genConstructor();
		genVoidVisit();
		genVisits(adt);
		println("}");
	}

	private void genConstructor() {
		println("private jjtraveler.Visitor any;");
		println("public Fwd (jjtraveler.Visitor v) {");
		println("this.any = v;");
		println("}");
	}

	private void genVoidVisit() {
		println("  public void voidVisit(jjtraveler.Visitable v) throws jjtraveler.VisitFailure {");
		println("    if (v instanceof " + constructor + ") {");
		println("        ((" + constructor + ") v).accept(this);");
		println("    } else {");
		println("      any.visit(v);");
		println("    }");
		println("  }");
	}
}
