package apigen.gen.java;

import java.util.List;

import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.gen.StringConversions;

public class ListTypeImplGenerator extends TypeImplGenerator {
	protected String typeId;
	protected String typeName;
	protected String elementTypeId;
	protected String elementTypeName;

	public ListTypeImplGenerator(
		ListType type,
		String directory,
		String pkg,
		String apiName,
		List standardImports,
		boolean verbose) {
		super(type, directory, pkg, apiName, standardImports, verbose);

		this.typeName = className(type);
		this.typeId = StringConversions.makeCapitalizedIdentifier(type.getId());
		this.elementTypeName = TypeGenerator.className(type.getElementType());
		this.elementTypeId =
			StringConversions.makeCapitalizedIdentifier(type.getElementType());
	}

	protected void generate() {
		printPackageDecl();
		imports.add("java.io.InputStream");
		imports.add("java.io.IOException");

		printImports();
		println();
		genListTypeClassImpl();
	}

	public static String className(Type type) {
		return TypeImplGenerator.className(type);
	}

	protected String classModifier() {
		return "abstract public";
	}

	private void genListTypeClassImpl() {
		println(
			classModifier()
				+ " class "
				+ typeName
				+ " extends aterm.pure.ATermListImpl");
		println("{");
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
		println("}");
	}

	private void genGetFactoryMethod() {
		println("  public " + FactoryGenerator.className(apiName) + " " + factoryGetter() + "{");
		println("    return factory;");
		println("}");
	}

	protected void genConstructor(String class_impl_name) {
		println(
			"  protected "
				+ FactoryGenerator.className(apiName)
				+ " factory = null;");
		println(
			"  "
				+ class_impl_name
				+ "("
				+ FactoryGenerator.className(apiName)
				+ " factory) {");
		println("     super(factory);");
		println("     this.factory = factory;");
		println("  }");
	}

	private void genInsertMethod() {
		String className = TypeGenerator.className(type);
		println("  public aterm.ATermList insert(aterm.ATerm head) {");
		println(
			"    return (aterm.ATermList)"
				+ factoryGetter()
				+ ".make"
				+ className
				+ "(("
				+ elementTypeName
				+ ") head, ("
				+ className
				+ ") this);");
		println("  }");
		println("");
		println(
			"  public "
				+ className
				+ " insert("
				+ elementTypeName
				+ " head) {");
		println(
			"    return "
				+ factoryGetter()
				+ ".make"
				+ className
				+ "(head, ("
				+ className
				+ ") this);");
		println("  }");
	}

	private void genGetEmptyMethod() {
		String className = TypeGenerator.className(type);
		println("  public aterm.ATermList getEmpty() {");
		println(
			"    return (aterm.ATermList)"
				+ factoryGetter()
				+ ".make"
				+ className
				+ "();");
		println("  }");
		println("");
	}
	
	private void genPredicates() {
		genIsTypeMethod(type);
		genIsAlternativeMethods();
		genHasPredicates();
	}

	private void genHasPredicates() {
		genHasHeadMethod();
		genHasTailMethod();
	}

	private void genHasTailMethod() {
		println("  public boolean hasTail() {");
		println("    return !isEmpty();");
		println("  }");
	}

	private void genHasHeadMethod() {
		println("  public boolean hasHead() {");
		println("    return !isEmpty();");
		println("  }");
	}

	private void genSharedObjectInterface() {
		genEquivalentMethod();
		genDuplicateMethod();
	}

	private void genDuplicateMethod() {
		String className = TypeGenerator.className(type);
		println("  public shared.SharedObject duplicate() {");
		println(
			"	 " + className + " clone = new " + className + "(factory);");
		println("	 clone.init(hashCode(), getAnnotations(), getFirst(), getNext());");
		println("	 return clone;");
		println("  }");
	}

	private void genEquivalentMethod() {
		String className = TypeGenerator.className(type);

		println("  public boolean equivalent(shared.SharedObject peer) {");
		println("	 if (peer instanceof " + className + ") {");
		println("	 	return super.equivalent(peer);");
		println("	 }");
		println("	 else {");
		println("      return false;");
		println("	 }");
		println("  }");
	}

	private String factoryGetter() {
		return "get" + FactoryGenerator.className(apiName) + "()";
	}

	private void genGetters() {
		genGetHead();
		genGetTail();
	}

	private void genGetTail() {
		String className = TypeGenerator.className(type);

		println("  public " + className + " getTail() {");
		println("    return (" + className + ") getNext();");
		println("  }");
	}

	private void genGetHead() {
		println("  public " + elementTypeName + " getHead() {");
		println("    return (" + elementTypeName + ") getFirst();");
		println("  }");
	}

	private void genIsAlternativeMethods() {
		String className = TypeGenerator.className(type);
		println("  public boolean isEmpty() {");
		println(
			"    return this == "
				+ FactoryGenerator.className(apiName)
				+ ".empty"
				+ className
				+ ";");
		println("  }");
		println("  public boolean isMany() {");
		println("    return !isEmpty();");
		println("  }");
	}

	private void genToTerm() {
		String get_factory = factoryGetter();
		String className = TypeGenerator.className(type);

		println("  public aterm.ATerm toTerm()");
		println("  {");
		println("    if (this.term == null) {");
		println(
			"      "
				+ className
				+ " reversed = ("
				+ className
				+ ")this.reverse();");
		println("      aterm.ATermList tmp = " + get_factory + ".makeList();");
		println("      for (; !reversed.isEmpty(); reversed = reversed.getTail()) {");
		println("         aterm.ATerm elem = reversed.getHead().toTerm();");
		println("         tmp = " + get_factory + ".makeList(elem, tmp);");
		println("      }");
		println("      this.term = tmp;");
		println("    }");
		println("    return this.term;");
		println("  }");
	}

	private void genTermField() {
		println("  protected aterm.ATerm term = null;");
	}

	private void genToString() {
		println("  public String toString() {");
		println("    return toTerm().toString();");
		println("  }");
	}

}
