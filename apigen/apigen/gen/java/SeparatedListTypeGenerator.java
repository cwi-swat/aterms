package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.Field;
import apigen.adt.SeparatedListType;
import apigen.gen.StringConversions;
import apigen.gen.TypeConverter;

public class SeparatedListTypeGenerator extends ListTypeGenerator {
    private SeparatedListType listType;

    public SeparatedListTypeGenerator(
        JavaGenerationParameters params,
        SeparatedListType type) {
        super(params, type);
        this.listType = type;
    }

    protected void generate() {
        printPackageDecl();
        printImports();
        genSeparatedListTypeClass();
    }

    private void genSeparatedListTypeClass() {
        println("public class " + getTypeName() + " extends aterm.pure.ATermListImpl {");
        genSeparatorFields();
        genSeparatorsGettersAndSetters();
        genInitMethod();
        genInitHashcodeMethod();
        genConstructor(getTypeName());
        genGetFactoryMethod();
        genTermField();
        genToTerm();
        genToString();
        genGetters();
        genPredicates();
        genSharedObjectInterface();
        genGetEmptyMethod();
        genOverrideInsertMethod();
        genReverseMethods();
        genConcatMethods();
        println("}");
    }

    private void genReverseMethods() {
        genOverrideReverseMethod();
        genTypedReverseMethod();
    }

    private void genTypedReverseMethod() {
        println("  public " + getTypeName() + " reverse" + getTypeName() + "() {");
        println(
            "    return " + factoryGetter() + ".reverse((" + getTypeName() + ")this);");
        println("  }");
        println();
    }

    private void genOverrideReverseMethod() {
        println("  public aterm.ATermList reverse() {");
        println("    return reverse" + getTypeName() + "();");
        println("  }");
        println();
    }

    private void genConcatMethods() {
        genOverrideConcatMethod();
        genTypedConcatMethod();
    }

    private void genTypedConcatMethod() {
        String formalSepArgs = buildFormalSeparatorArguments(listType);
        String actualSepArgs = buildActualSeparatorArguments(listType);

        String typeName = getTypeName();
        println(
            "  public " + typeName + " concat(" + formalSepArgs + typeName + " peer) {");
        println(
            "    return "
                + factoryGetter()
                + ".concat(this, "
                + actualSepArgs
                + "peer);");
        println("  }");
        println();
    }

    private void genOverrideConcatMethod() {
        println("  public aterm.ATermList concat(aterm.ATermList peer) {");
        println("    throw new java.lang.UnsupportedOperationException(");
        println(
            "      \"Cannot concat ATermList to "
                + getTypeName()
                + ", use typed concat method with separators.\");");
        println("  }");
        println();
    }

    protected void genToTerm() {
        println("  public aterm.ATerm toTerm() {");
        println("    if (term == null) {");
        println("      term = " + factoryGetter() + ".toTerm(this);");
        println("    }");
        println("    return term;");
        println("  }");
        println();
    }

    protected void genOverrideInsertMethod() {
        println("  public aterm.ATermList insert(aterm.ATerm head) {");
        println("    throw new java.lang.UnsupportedOperationException(");
        println("      \"Cannot insert ATerm in separated list ("
                + listType.getId()
                + "), use a factory make method.\");");
        println("  }");
        println();
    }

    protected void genInitMethod() {
        println(
            "  public void init(int hashCode, aterm.ATermList annos, aterm.ATerm first, "
                + buildFormalSeparatorArguments(listType)
                + "aterm.ATermList next) {");
        println("    super.init(hashCode, annos, first, next);");
        genSeparatorInitAssignments(listType);
        println("  }");
        println();
    }

    protected void genInitHashcodeMethod() {
        println(
            "  public void initHashCode(aterm.ATermList annos, aterm.ATerm first, "
                + buildFormalSeparatorArguments(listType)
                + "aterm.ATermList next) {");
        println("    super.initHashCode(annos, first, next);");
        genSeparatorInitAssignments(listType);
        println("  }");
        println();
    }

    private void genSeparatorInitAssignments(SeparatedListType type) {
        Iterator fields = type.separatorFieldIterator();
        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String fieldId = JavaGenerator.getFieldId(field.getId());
            println("    this." + fieldId + " = " + fieldId + ";");
        }
    }

    private String buildFormalSeparatorArguments(SeparatedListType type) {
        String result = buildFormalTypedArgumentList(type.separatorFieldIterator());
        //TODO: remove terminating ","!!
        if (result.length() > 0) {
            result += ", ";
        }

        return result;
    }

    private void genSeparatorsGettersAndSetters() {
        Iterator fields = listType.separatorFieldIterator();
        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            genSeparatorGetterAndSetter(field);
        }
    }

    private String buildSeparatorFieldGetter(Field field) {
        String fieldName = StringConversions.makeCapitalizedIdentifier(field.getId());
        return "get" + fieldName + "()";
    }

    private void genSeparatorGetterAndSetter(Field field) {
        String fieldName = StringConversions.makeCapitalizedIdentifier(field.getId());
        String fieldClass =
            TypeGenerator.qualifiedClassName(
                getJavaGenerationParameters(),
                field.getType());
        String fieldId = JavaGenerator.getFieldId(field.getId());
        String fieldGetter = buildSeparatorFieldGetter(field);

        // TODO: find a way to reuse generation of getters in
        // AlternativeGenerator for lists of builtins

        println("  public " + fieldClass + " " + fieldGetter + " {");
        println("    if (!isEmpty() && !isSingle()) {");
        println("      return " + fieldId + ";");
        println("    }");
        println(
            "    throw new UnsupportedOperationException(\"This "
                + getClassName()
                + " does not have a "
                + field.getId()
                + ":\" + this);");
        println("  }");
        println();

        println(
            "  public "
                + getClassName()
                + " set"
                + fieldName
                + "("
                + fieldClass
                + " arg) {");
        println("    if (!isEmpty() && !isSingle()) {");
        String arglist = buildActualSeparatorArguments(listType);
        arglist = arglist.replaceAll(fieldId, "arg");
        println(
            "      return "
                + factoryGetter()
                + ".make"
                + getClassName()
                + "(getHead(), "
                + arglist
                + "getTail());");
        println("    }");
        println(
            "    throw new RuntimeException(\"This "
                + getClassName()
                + " does not have a "
                + fieldId
                + ".\");");
        println("  }");
        println();
    }

    protected void genPredicates() {
        genIsEmpty(listType.getId());
        genIsMany();
        genIsSingle();
    }

    private void genSeparatorFields() {
        Iterator fields = listType.separatorFieldIterator();

        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            genSeparatorField(field);
        }
        println();
    }

    protected void genEquivalentMethod() {
        println("  public boolean equivalent(shared.SharedObject object) {");
        println("    if (object instanceof " + getClassName() + ") {");
        println("      " + getClassName() + " peer = (" + getClassName() + ") object;");
        println("      if (isEmpty() || isSingle()) {");
        println("        return super.equivalent(peer); ");
        println("      }");
        print("      return super.equivalent(peer) ");
        genSeparatorFieldsEquivalentConjunction();
        println(";");
        println("    }");
        println("    else {");
        println("      return false;");
        println("    }");
        println("  }");
        println();

    }

    protected void genDuplicateMethod() {
        println("  public shared.SharedObject duplicate() {");
        println(
            "    " + getClassName() + " clone = new " + getClassName() + "(factory);");
        println(
            "    clone.init(hashCode(), getAnnotations(), getFirst(), "
                + buildActualSeparatorArguments(listType)
                + "getNext());");
        println("    return clone;");
        println("  }");
        println();
    }

    private String buildActualSeparatorArguments(SeparatedListType type) {
        Iterator fields = type.separatorFieldIterator();
        String result = "";

        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String fieldId = JavaGenerator.getFieldId(field.getId());
            // TODO: remove terminating ", "!
            result += fieldId + ", ";
        }

        return result;
    }

    private void genSeparatorFieldsEquivalentConjunction() {
        Iterator fields = listType.separatorFieldIterator();
        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String fieldId = JavaGenerator.getFieldId(field.getId());
            TypeConverter converter = getConverter();
            String fieldType = field.getType();
            String fieldGetter = buildSeparatorFieldGetter(field);
            String equivalenceTest =
                EquivalentBuilder.buildEquivalent(
                    fieldType,
                    fieldId,
                    "peer." + fieldGetter);
            print(" && " + equivalenceTest);
        }
    }

    private void genSeparatorField(Field field) {
        String fieldClass =
            TypeGenerator.qualifiedClassName(
                getJavaGenerationParameters(),
                field.getType());
        String fieldId = JavaGenerator.getFieldId(field.getId());
        println("  private " + fieldClass + " " + fieldId + ";");
    }

}
