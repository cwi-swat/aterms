package apigen.gen.java;

import apigen.adt.ListType;
import apigen.adt.Type;

public class ListTypeGenerator extends TypeGenerator {
	private ListType type;
	private String typeName;
	private String elementTypeName;
	private String factory;

	public ListTypeGenerator(JavaGenerationParameters params, ListType type) {
		super(params, type);
		this.type = type;
		this.typeName = TypeGenerator.className(type);
		this.elementTypeName = TypeGenerator.qualifiedClassName(params, type.getElementType());
		this.factory = FactoryGenerator.qualifiedClassName(params);
	}

	public String getTypeName() {
		return typeName;
	}

	public String getFactory() {
		return factory;
	}

	protected void generate() {
		printPackageDecl();
		printImports();
		genListTypeClass();
	}

	public static String className(Type type) {
		return TypeGenerator.className(type);
	}

	protected void genListTypeClass() {
		println("public class " + typeName + " extends aterm.pure.ATermListImpl {");

		genInitMethod();
		genInitHashcodeMethod();
		genConstructor(className(type));
		genGetFactoryMethod();
		genTermField();
		genToTerm();
		genToString();
		genGetters();
		genPredicates();
		genSharedObjectInterface();
		genGetEmptyMethod();
		genInsertMethod();
		genOverrideInsertMethod();
		println("}");
	}

	protected void genInitMethod() {
		println("  public void init(int hashCode, aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {");
		println("    super.init(hashCode, annos, first, next);");
		println("  }");
		println();
	}

	protected void genInitHashcodeMethod() {
		println("  public void initHashCode(aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {");
		println("    super.initHashCode(annos, first, next);");
		println("  }");
		println();
	}

	protected void genGetFactoryMethod() {
		println("  public " + factory + " " + factoryGetter() + " {");
		println("    return factory;");
		println("}");
		println();
	}

	protected void genConstructor(String className) {
		println("  private " + factory + " factory = null;");
		println("  public " + className + "(" + factory + " factory) {");
		println("     super(factory.getPureFactory());");
		println("     this.factory = factory;");
		println("  }");
		println();
	}

	protected void genInsertMethod() {
		println("  public " + typeName + " insert(" + elementTypeName + " head) {");
		println("    return " + factoryGetter() + ".make" + typeName + "(head, (" + typeName + ") this);");
		println("  }");
		println();
	}

	protected void genOverrideInsertMethod() {
		println("  public aterm.ATermList insert(aterm.ATerm head) {");
		println("    return insert((" + elementTypeName + ") head);");
		println("  }");
		println();
	}

	protected void genGetEmptyMethod() {
		String className = TypeGenerator.className(type);
		println("  public aterm.ATermList getEmpty() {");
		println("    return (aterm.ATermList)" + factoryGetter() + ".make" + className + "();");
		println("  }");
		println();
	}

	protected void genPredicates() {
		genIsTypeMethod(type);
		genIsAlternativeMethods();
		genHasPredicates();
	}

	protected void genHasPredicates() {
		genHasHeadMethod();
		genHasTailMethod();
	}

	protected void genHasTailMethod() {
		println("  public boolean hasTail() {");
		println("    return !isEmpty();");
		println("  }");
		println();
	}

	protected void genHasHeadMethod() {
		println("  public boolean hasHead() {");
		println("    return !isEmpty();");
		println("  }");
		println();
	}

	protected void genSharedObjectInterface() {
		genEquivalentMethod();
		genDuplicateMethod();
	}

	protected void genDuplicateMethod() {
		String className = TypeGenerator.className(type);
		println("  public shared.SharedObject duplicate() {");
		println("    " + className + " clone = new " + className + "(factory);");
		println("    clone.init(hashCode(), getAnnotations(), getFirst(), getNext());");
		println("    return clone;");
		println("  }");
		println();
	}

	protected void genEquivalentMethod() {
		String className = TypeGenerator.className(type);

		println("  public boolean equivalent(shared.SharedObject peer) {");
		println("    if (peer instanceof " + className + ") {");
		println("      return super.equivalent(peer);");
		println("    }");
		println("    else {");
		println("      return false;");
		println("    }");
		println("  }");
		println();
	}

	protected String factoryGetter() {
		return "get" + FactoryGenerator.className() + "()";
	}

	protected void genGetters() {
		genGetHead();
		genGetTail();
	}

	protected void genGetTail() {
		String className = TypeGenerator.className(type);

		println("  public " + className + " getTail() {");
		println("    return (" + className + ") getNext();");
		println("  }");
		println();
	}

	protected void genGetHead() {
		println("  public " + elementTypeName + " getHead() {");
		println("    return (" + elementTypeName + ") getFirst();");
		println("  }");
		println();
	}

	protected void genIsAlternativeMethods() {
		String className = TypeGenerator.className(type);
		genIsEmpty(className);
		genIsMany();
		genIsSingle();
	}

	protected void genIsMany() {
		println("  public boolean isMany() {");
		println("    return !isEmpty();");
		println("  }");
		println();
	}

	protected void genIsEmpty(String className) {
		println("  public boolean isEmpty() {");
		String getFactoryMethodName = "get" + FactoryGenerator.className();
		println("    return this == " +  getFactoryMethodName+ "().make" + className + "();");
		println("  }");
		println();
	}

	protected void genIsSingle() {
		println("  public boolean isSingle() {");
		println("    return !isEmpty() && getNext().isEmpty();");
		println("  }");
		println();
	}
	
	protected void genToTerm() {
		String get_factory = factoryGetter();
		String className = TypeGenerator.className(type);

		println("  public aterm.ATerm toTerm() {");
		println("    if (this.term == null) {");
		println("      " + className + " reversed = (" + className + ")this.reverse();");
		println("      aterm.ATermList tmp = " + get_factory + ".getPureFactory().makeList();");
		println("      for (; !reversed.isEmpty(); reversed = reversed.getTail()) {");
		println("         aterm.ATerm elem = reversed.getHead().toTerm();");
		println("         tmp = " + get_factory + ".getPureFactory().makeList(elem, tmp);");
		println("      }");
		println("      this.term = tmp;");
		println("    }");
		println("    return this.term;");
		println("  }");
		println();
	}

	protected void genTermField() {
		println("  protected aterm.ATerm term = null;");
	}

	protected void genToString() {
		println("  public String toString() {");
		println("    return toTerm().toString();");
		println("  }");
		println();
	}
}