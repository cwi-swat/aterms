package apigen.gen.java;

import apigen.adt.ListType;
import apigen.adt.Type;
import apigen.gen.StringConversions;

public class ListTypeGenerator extends TypeGenerator {
	private ListType type;
	private String typeName;
	private String elementTypeName;
	private String factory;
	private String elementType;

	public ListTypeGenerator(JavaGenerationParameters params, ListType type) {
		super(params, type);
		this.type = type;
		this.typeName = TypeGenerator.className(type);
		this.elementType = type.getElementType();
		this.elementTypeName = TypeGenerator.qualifiedClassName(params, elementType);
		this.factory = FactoryGenerator.qualifiedClassName(params,type.getModuleName());
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

	private void genListTypeClass() {
		println("public class " + typeName + " extends aterm.pure.ATermListImpl {");

		genFactoryField();
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
		genInsertMethods();
		genReverseMethods();
		genConcatMethods();
		genAppendMethods();
		genElementAtMethod();
		println("}");
	}

	private void genElementAtMethod() {
		String elementName = StringConversions.capitalize(elementType);
		String converted = getConverter().makeATermToBuiltinConversion(elementType, "elementAt(index)");
		println("  public " + elementTypeName + " get" + elementName + "At(int index) {");
		println("    return (" + elementTypeName + ") " + converted + ";");
		println("  }");
		println();
	}

	private void genAppendMethods() {
		if (!type.getElementType().equals("term")) {
			genAppendMethod();
		}
		genOverrideAppendMethod();
	}

	private void genOverrideAppendMethod() {
		String elemName = "elem";
		String elem = getConverter().makeATermToBuiltinConversion(elementType, elemName);
		println("  public aterm.ATermList append(aterm.ATerm " + elemName + ") {");
		println("    return append((" + elementTypeName + ") " + elem + ");");
		println("  }");
		println();
	}

	private void genAppendMethod() {
		println("  public " + typeName + " append(" + elementTypeName + " elem) {");
		println("    return " + buildFactoryGetter() + ".append(this, elem);");
		println("  }");
		println();
	}

	private void genConcatMethods() {
		genConcatMethod();
		genOverrideConcatMethod();
	}

	private void genOverrideConcatMethod() {
		println("  public aterm.ATermList concat(aterm.ATermList peer) {");
		println("    return concat((" + typeName + ") peer);");
		println("  }");
		println();
	}

	private void genConcatMethod() {
		println("  public " + typeName + " concat(" + typeName + " peer) {");
		println("    return " + buildFactoryGetter() + ".concat(this, peer);");
		println("  }");
		println();
	}

	private void genReverseMethods() {
		genReverseMethod();
		genOverrideReverseMethod();
	}

	private void genOverrideReverseMethod() {
		println("  public aterm.ATermList reverse() {");
		println("    return reverse" + typeName + "();");
		println("  }");
		println();
	}

	private void genReverseMethod() {
		println("  public " + typeName + " reverse" + typeName + "() {");
		println("    return " + buildFactoryGetter() + ".reverse(this);");
		println("  }");
		println();
	}

	private void genInsertMethods() {
		if (!type.getElementType().equals("term")) {
			genInsertMethod();
		}
		genOverrideInsertMethod();
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

	private void genGetFactoryMethod() {
		println("  public " + factory + " " + buildFactoryGetter() + " {");
		println("    return localFactory;");
		println("}");
		println();
	}

	protected void genConstructor(String className) {
		println("  public " + className + "(" + factory + " localFactory) {");
		println("     super(localFactory.getPureFactory());");
		println("     this.localFactory = localFactory;");
		println("  }");
		println();
	}

	private void genFactoryField() {
		println("  private " + factory + " localFactory = null;");
	}

	private void genInsertMethod() {
		println("  public " + typeName + " insert(" + elementTypeName + " head) {");
		println("    return " + buildFactoryGetter() + ".make" + typeName + "(head, (" + typeName + ") this);");
		println("  }");
		println();
	}

	private void genOverrideInsertMethod() {
		String head = getConverter().makeATermToBuiltinConversion(elementType, "head");
		println("  public aterm.ATermList insert(aterm.ATerm head) {");
		println("    return insert((" + elementTypeName + ") " + head + ");");
		println("  }");
		println();
	}

	private void genGetEmptyMethod() {
		String className = TypeGenerator.className(type);
		println("  public aterm.ATermList getEmpty() {");
		println("    return (aterm.ATermList)" + buildFactoryGetter() + ".make" + className + "();");
		println("  }");
		println();
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
		println();
	}

	private void genHasHeadMethod() {
		println("  public boolean hasHead() {");
		println("    return !isEmpty();");
		println("  }");
		println();
	}

	private void genSharedObjectInterface() {
		genEquivalentMethod();
		genDuplicateMethod();
	}

	private void genDuplicateMethod() {
		String className = TypeGenerator.className(type);
		println("  public shared.SharedObject duplicate() {");
		println("    " + className + " clone = new " + className + "(localFactory);");
		println("    clone.init(hashCode(), getAnnotations(), getFirst(), getNext());");
		println("    return clone;");
		println("  }");
		println();
	}

	private void genEquivalentMethod() {
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

	private String buildFactoryGetter() {
		return AbstractTypeGenerator.getFactoryMethodName(getGenerationParameters(),type.getModuleName()) + "()";
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
		println();
	}

	private void genGetHead() {
		println("  public " + elementTypeName + " getHead() {");
		String convertedValue = getConverter().makeATermToBuiltinConversion(elementType, "getFirst()");
		println("    return (" + elementTypeName + ")" + convertedValue + ";");
		println("  }");
		println();
	}

	private void genIsAlternativeMethods() {
		String className = TypeGenerator.className(type);
		genIsEmpty(className);
		genIsMany();
		genIsSingle();
	}

	private void genIsMany() {
		println("  public boolean isMany() {");
		println("    return !isEmpty();");
		println("  }");
		println();
	}

	private void genIsEmpty(String className) {
		println("  public boolean isEmpty() {");
		println("    return this == " + buildFactoryGetter() + ".make" + className + "();");
		println("  }");
		println();
	}

	private void genIsSingle() {
		println("  public boolean isSingle() {");
		println("    return !isEmpty() && getNext().isEmpty();");
		println("  }");
		println();
	}

	private void genToTerm() {
		String getFactoryMethodName = buildFactoryGetter();
		String className = TypeGenerator.className(type);

		println("  public aterm.ATerm toTerm() {");
		println("    aterm.ATermFactory atermFactory = " + getFactoryMethodName + ".getPureFactory();");
		println("    if (this.term == null) {");
		println("      " + className + " reversed = (" + className + ")this.reverse();");
		println("      aterm.ATermList tmp = atermFactory.makeList();");
		println("      for (; !reversed.isEmpty(); reversed = reversed.getTail()) {");

		String head = "reversed.getHead()";
		String termHead;
		if (!getConverter().isReserved(elementType)) {
			termHead = head + ".toTerm()";
		}
		else {
			termHead = getConverter().makeBuiltinToATermConversion(elementType, head);
		}
		println("        aterm.ATerm elem = " + termHead + ";");

		println("        tmp = atermFactory.makeList(elem, tmp);");
		println("      }");
		println("      this.term = tmp;");
		println("    }");
		println("    return this.term;");
		println("  }");
		println();
	}

	private void genTermField() {
		println("  protected aterm.ATerm term = null;");
	}

	private void genToString() {
		println("  public String toString() {");
		println("    return toTerm().toString();");
		println("  }");
		println();
	}
}
