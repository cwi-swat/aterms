package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Alternative;
import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.gen.GenerationParameters;
import apigen.gen.StringConversions;

public class VisitorGenerator extends JavaGenerator {
	private static final String CLASS_NAME = "Visitor";

	private ADT adt;

	public VisitorGenerator(ADT adt, JavaGenerationParameters params) {
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
		genVisitDecl(ListTypeGenerator.className(type), "");
	}

	private void genVisit(Type type, Alternative alt) {
		String methodName = FactoryGenerator.concatTypeAlt(type, alt);
		String paramName = AlternativeGenerator.qualifiedClassName(getJavaGenerationParameters(), type, alt);
		genVisitDecl(methodName, paramName);
	}

	private void genVisitDecl(String methodName, String paramName) {
		println("  public abstract void visit_" + methodName + "(" + paramName + " arg) throws jjtraveler.VisitFailure;");
	}
	
	public static String packageName(GenerationParameters params) {
		return StringConversions.decapitalize(params.getApiName());
	}
	
	public static String qualifiedClassName(JavaGenerationParameters params) {
		StringBuffer buf = new StringBuffer();
		buf.append(params.getPackageName());
		buf.append('.');
		buf.append(packageName(params));
		return buf.toString();
	}

	public String getPackageName() {
		return packageName(getGenerationParameters());
	}

	public String getQualifiedClassName() {
		return getClassName();
	}

}
