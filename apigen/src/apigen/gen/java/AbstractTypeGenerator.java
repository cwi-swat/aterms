package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Type;
import apigen.gen.GenerationParameters;
import apigen.gen.StringConversions;

public class AbstractTypeGenerator extends JavaGenerator {
	private boolean visitable;
	private String apiName;
	private String factoryClassName;
	private ADT adt;

	public AbstractTypeGenerator(ADT adt, JavaGenerationParameters params) {
		super(params);
		this.adt = adt;
		this.apiName = params.getApiName();
		this.factoryClassName = FactoryGenerator.qualifiedClassName(getJavaGenerationParameters());
		this.visitable = params.isVisitable();
	}

	public String getClassName() {
		return className();
	}

	public static String className() {
		return "AbstractType";
	}

	public static String qualifiedClassName(JavaGenerationParameters params) {
		StringBuffer buf = new StringBuffer();
		buf.append(params.getPackageName());
		buf.append('.');
		buf.append(params.getApiName().toLowerCase());
		buf.append('.');
		buf.append(className());
		return buf.toString();
	}

	protected void generate() {
		printPackageDecl();
		genAbstractTypeClass();
	}

	private void genAbstractTypeClass() {
		println("abstract public class " + getClassName() + " extends aterm.pure.ATermApplImpl {");
		genClassVariables();
		genConstructor();
		genInitMethod();
		genInitHashcodeMethod();
		genToTermMethod();
		genToStringMethod();
		genSetTermMethod();
		genGetFactoryMethod();
		genDefaultTypePredicates();

		if (visitable) {
			genAccept();
		}
		println("}");
	}

	private void genClassVariables() {
		println("  protected aterm.ATerm term;");
		println();
		println("  " + factoryClassName + " factory;");
		println();
	}

	private void genConstructor() {
		println("  public " + getClassName() + "(" + factoryClassName + " factory) {");
		println("    super(factory.getPureFactory());");
		println("    this.factory = factory;");
		println("  }");
		println();
	}

	private void genInitMethod() {
		println("  public void init(int hashCode, aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {");
		println("    super.init(hashCode, annos, fun, args);");
		println("  }");
		println();
	}

	private void genInitHashcodeMethod() {
		println("  public void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {");
		println("    super.initHashCode(annos, fun, args);");
		println("  }");
		println();
	}

	private void genGetFactoryMethod() {
		GenerationParameters params = getGenerationParameters();
		println("  public " + factoryClassName + ' ' + getFactoryMethodName(params) + "() {");
		println("    return factory;");
		println("  }");
		println();
	}

	private void genSetTermMethod() {
		println("  protected void setTerm(aterm.ATerm term) {");
		println("    this.term = term;");
		println("  }");
		println();
	}

	private void genToStringMethod() {
		println("  public String toString() {");
		println("    return toTerm().toString();");
		println("  }");
		println();
	}

	private void genToTermMethod() {
		println("  abstract public aterm.ATerm toTerm();");
		println();
	}

	private void genAccept() {
		String visitorPackage = VisitorGenerator.qualifiedClassName(getJavaGenerationParameters());
		println("  abstract public void accept(" + visitorPackage + ".Visitor v) throws jjtraveler.VisitFailure;");
		println();
	}

	private void genDefaultTypePredicates() {
		Iterator types = adt.typeIterator();
		while (types.hasNext()) {
			Type type = (Type) types.next();
			genDefaultTypePredicate(type);
		}
	}

	private void genDefaultTypePredicate(Type type) {
		println("  public boolean isSort" + TypeGenerator.className(type) + "() {");
		println("    return false;");
		println("  }");
		println();
	}

	public String getPackageName() {
		return apiName.toLowerCase();
	}

	public String getQualifiedClassName() {
		return getClassName();
	}

	public static String getFactoryMethodName(GenerationParameters params) {
		String apiName = StringConversions.capitalize(params.getApiName());
		return "get" + apiName + FactoryGenerator.className();
	}
}