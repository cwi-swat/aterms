package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.ADT;
import apigen.adt.Type;
import apigen.gen.GenerationParameters;
import apigen.gen.StringConversions;

public class GenericConstructorGenerator extends JavaGenerator {
	private boolean visitable;
	private String apiName;
	private String className;
	private String factoryName;
	private ADT adt;

	public GenericConstructorGenerator(ADT adt, GenerationParameters params) {
		super(params);
		this.adt = adt;
		this.apiName = params.getApiName();
		this.visitable = params.isVisitable();
		this.className = className(params.getApiName());
		this.factoryName = FactoryGenerator.className(params);
	}

	public String getClassName() {
		return className;
	}

	public static String className(String name) {
		return StringConversions.makeCapitalizedIdentifier(name) + "Constructor";
	}
	
	public static String qualifiedClassName(GenerationParameters params) {
		StringBuffer buf = new StringBuffer();
		buf.append(params.getPackageName());
		buf.append('.');
		buf.append(StringConversions.decapitalize(params.getApiName()));
		buf.append('.');
		buf.append(StringConversions.makeCapitalizedIdentifier(params.getApiName()));
		buf.append("Constructor");
		return buf.toString();
	}

	protected void generate() {
		printPackageDecl();
		genGenericConstructorClass();
	}

	private void genGenericConstructorClass() {
		println("abstract public class " + className + " extends aterm.pure.ATermApplImpl {");
		println("  protected aterm.ATerm term = null;");
		println();
		println("  " + factoryName + " factory = null;");
		println();
		println("  public " + className + "(" + factoryName + " factory) {");
		println("    super(factory.getPureFactory());");
		println("    this.factory = factory;");
		println("  }");
		println();

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
		println("  public " + factoryName + " get" + factoryName + "() {");
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
		println("  abstract public void accept(jjtraveler.Visitor v) throws jjtraveler.VisitFailure;");
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
		return StringConversions.decapitalize(apiName);
	}

	public String getQualifiedClassName() {
		return getClassName();
	}
}