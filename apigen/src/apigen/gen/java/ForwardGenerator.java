package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.gen.StringConversions;

public class ForwardGenerator extends JavaGenerator {
	private static final String CLASS_NAME = "Fwd";

	private ADT adt;
	private String constructor;

	public ForwardGenerator(ADT adt, JavaGenerationParameters params) {
		super(params);
		this.adt = adt;
		this.constructor = GenericConstructorGenerator.qualifiedClassName(params);
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
		genVisitMethod(ListTypeGenerator.className(type), "");
	}

	private void genVisitMethod(String methodName, String paramType) {
		println("  public void " + methodName + "(" + paramType + " arg) throws jjtraveler.VisitFailure {");
		println("    any.visit(arg);");
		println("  }");
		println();
	}

	private void genVisit(Type type, Alternative alt) {
		String methodName = "visit_" + FactoryGenerator.concatTypeAlt(type, alt);
		String paramType = AlternativeGenerator.qualifiedClassName(getJavaGenerationParameters(), type, alt);

		genVisitMethod(methodName, paramType);
	}

	protected void generate() {
		printPackageDecl();

		println("public class Fwd extends Visitor implements jjtraveler.Visitor {");
		genConstructor();
		genVoidVisit();
		genVisits(adt);
		println("}");
	}

	private void genConstructor() {
		println("  private jjtraveler.Visitor any;");
		println();
		println("  public Fwd(jjtraveler.Visitor v) {");
		println("    this.any = v;");
		println("  }");
		println();
	}

	private void genVoidVisit() {
		println("  public void voidVisit(jjtraveler.Visitable v) throws jjtraveler.VisitFailure {");
		println("    if (v instanceof " + constructor + ") {");
		println("        ((" + constructor + ") v).accept(this);");
		println("    } else {");
		println("      any.visit(v);");
		println("    }");
		println("  }");
		println();
	}

	public String getPackageName() {
		String apiName = getGenerationParameters().getApiName();
		return StringConversions.decapitalize(apiName);
	}

	public String getQualifiedClassName() {
		return getClassName();
	}
}
