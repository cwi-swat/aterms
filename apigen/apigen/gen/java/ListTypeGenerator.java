package apigen.gen.java;

import sun.io.Converters;
import apigen.adt.ListType;
import apigen.adt.Type;

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

        if (!type.getElementType().equals("term")) {
            genInsertMethod();
        }
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

    private void genGetFactoryMethod() {
        println("  public " + factory + " " + buildFactoryGetter() + " {");
        println("    return factory;");
        println("}");
        println();
    }

    protected void genConstructor(String className) {
        println("  public " + className + "(" + factory + " factory) {");
        println("     super(factory.getPureFactory());");
        println("     this.factory = factory;");
        println("  }");
        println();
    }

    private void genFactoryField() {
        println("  private " + factory + " factory = null;");
    }

    private void genInsertMethod() {
        println("  public " + typeName + " insert(" + elementTypeName + " head) {");
        println(
            "    return "
                + buildFactoryGetter()
                + ".make"
                + typeName
                + "(head, ("
                + typeName
                + ") this);");
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
        println(
            "    return (aterm.ATermList)"
                + buildFactoryGetter()
                + ".make"
                + className
                + "();");
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
        println("    " + className + " clone = new " + className + "(factory);");
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
        return AbstractTypeGenerator.getFactoryMethodName(getGenerationParameters())
            + "()";
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
        String convertedValue =
            getConverter().makeATermToBuiltinConversion(elementType, "getFirst()");
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
        println(
            "    aterm.ATermFactory factory = " + getFactoryMethodName + ".getPureFactory();");
        println("    if (this.term == null) {");
        println("      " + className + " reversed = (" + className + ")this.reverse();");
        println("      aterm.ATermList tmp = factory.makeList();");
        println("      for (; !reversed.isEmpty(); reversed = reversed.getTail()) {");
        
        String head = "reversed.getHead()";
        String termHead;
        if (!getConverter().isReserved(elementType)) {
            termHead = head + ".toTerm()";
        } else {
            termHead = getConverter().makeBuiltinToATermConversion(elementType, head);
        }
        println("        aterm.ATerm elem = " + termHead + ";");

        println("        tmp = factory.makeList(elem, tmp);");
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