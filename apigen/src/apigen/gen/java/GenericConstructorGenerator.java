package apigen.gen.java;

import java.util.Iterator;
import java.util.LinkedList;

import apigen.adt.ADT;
import apigen.adt.Type;
import apigen.gen.StringConversions;

public class GenericConstructorGenerator extends JavaGenerator {
	private boolean visitable;
	private String className;
	private String factoryName;
	private ADT api;

	public GenericConstructorGenerator(
		ADT api,
		String directory,
		String apiName,
		String pkg,
		boolean verbose,
		boolean visitable) {

		super(directory, className(apiName), pkg, new LinkedList(), verbose);

		this.visitable = visitable;
		className = className(apiName);
		factoryName = FactoryGenerator.className(apiName);
		this.api = api;
	}

	public static String className(String apiName) {
		return StringConversions.makeCapitalizedIdentifier(apiName)
			+ "Constructor";
	}

	protected void generate() {
		printPackageDecl();
		genGenericConstructorClass();
	}

	private void genGenericConstructorClass() {
		println("abstract public class " + className);
		println("extends aterm.pure.ATermApplImpl");
		println("implements aterm.ATerm");
		println("{");
		println("  protected aterm.ATerm term = null;");
		println();
		println("  " + factoryName + " factory = null;");
		println();
		println("  public " + className + "(" + factoryName + " factory) {");
		println("    super(factory);");
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
		println("  protected void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {");
		println("    super.init(hashCode, annos, fun, args);");
		println("  }");
	}

	private void genInitHashcodeMethod() {
		println("  protected void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] i_args) {");
		println("  	super.initHashCode(annos, fun, i_args);");
		println("  }");
	}

	private void genGetFactoryMethod() {
		println("  public " + factoryName + " get" + factoryName + "() {");
		println("    return factory;");
		println("  }");
	}

	private void genSetTermMethod() {
		println("  protected void setTerm(aterm.ATerm term) {");
		println("   this.term = term;");
		println("  }");
	}

	private void genToStringMethod() {
		println("  public String toString() {");
		println("    return toTerm().toString();");
		println("  }");
	}

	private void genToTermMethod() {
		println("  abstract public aterm.ATerm toTerm();");
	}

	private void genAccept() {
		println("  abstract public void accept(Visitor v) throws jjtraveler.VisitFailure;");
	}

	private void genDefaultTypePredicates() {
		Iterator types = api.typeIterator();

		while (types.hasNext()) {
			Type type = (Type) types.next();

			genDefaultTypePredicate(type);
		}
	}

	private void genDefaultTypePredicate(Type type) {
		println(
			"  public boolean isSort" + TypeGenerator.className(type) + "() {");
		println("    return false;");
		println("  }");
		println();
	}
}
