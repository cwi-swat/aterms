package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.Field;
import apigen.adt.SeparatedListType;
import apigen.adt.api.Separators;
import apigen.gen.StringConversions;

public class SeparatedListTypeImplGenerator extends ListTypeImplGenerator {
    protected Separators separators;
    SeparatedListType listType;

    public SeparatedListTypeImplGenerator(
        SeparatedListType type,
        String directory,
        String pkg,
        String apiName,
        List standardImports,
        boolean verbose) {
        super(type, directory, pkg, apiName, standardImports, verbose);

        this.listType = type;
        this.separators = type.getSeparators();
    }

    protected void generate() {
        printPackageDecl();
        printImports();
        println();
        genSeparatedListTypeClassImpl();
    }

    protected String classModifier() {
        return "public";
    }
    private void genSeparatedListTypeClassImpl() {
        println(
            classModifier() + " class " + typeName + " extends aterm.pure.ATermListImpl");
        println("{");
        genSeparatorFields();
        genSeparatorsGettersAndSetters();
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
        genOverrideInsertMethod();
        genReverseMethod();
        println("}");
    }

    
    private void genReverseMethod() {
        String className = TypeGenerator.className(type);
        println("  public aterm.ATermList reverse() {");
        println("    return reverse" + className + "();");
        println("  }");
        println("  public " + className + " reverse" + className + "() {");
        println("    return " + factoryGetter() + ".reverse((" + className + ")this);");
        println("  }");
        
    }

    
    protected void genToTerm() {
        String factoryName = FactoryGenerator.className(apiName);

        println("  public aterm.ATerm toTerm() {");
        println("    if (term == null) {");
        println("      term = get" + factoryName + "().toTerm(this);");
        println("    }");
        println("    return term;");
        println("  }");
        println();
    }
    protected void genOverrideInsertMethod() {
        println("  public aterm.ATermList insert(aterm.ATerm head) {");
        println(
            "    throw new java.lang.UnsupportedOperationException(\"Insert is not supported for separated lists ("
                + type.getId()
                + "), please use a make method.\");");
        println("  }");
    }

    protected void genInitMethod() {
        println(
            "  protected void init (int hashCode, aterm.ATermList annos, aterm.ATerm first, "
                + buildFormalSeparatorArguments(listType)
                + "aterm.ATermList next) {");
        println("    super.init(hashCode, annos, first, next);");
        genSeparatorInitAssigmnents(listType);
        println("  }");
    }

    protected void genInitHashcodeMethod() {
        println(
            "  protected void initHashCode(aterm.ATermList annos, aterm.ATerm first, "
                + buildFormalSeparatorArguments(listType)
                + "aterm.ATermList next) {");
        println("    super.initHashCode(annos, first, next);");
        genSeparatorInitAssigmnents(listType);
        println("  }");
    }

    private void genSeparatorInitAssigmnents(SeparatedListType type) {
        Iterator fields = type.separatorFieldIterator();
        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String fieldId = JavaGenerator.getFieldId(field.getId());
            println("    this." + fieldId + " = " + fieldId + ";");
        }
    }

    private String buildFormalSeparatorArguments(SeparatedListType type) {
        String result = buildFormalTypedArgumentList(type.separatorFieldIterator());

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

    private void genSeparatorGetterAndSetter(Field field) {
        String fieldName = StringConversions.makeCapitalizedIdentifier(field.getId());
        String fieldClass = TypeGenerator.className(field.getType());
        String fieldId = JavaGenerator.getFieldId(field.getId());
        String className = TypeGenerator.className(type);

        if (converter.isReserved(field.getType())) {
            // TODO: find a way to reuse generation of getters in AlternativeImplGenerator
            throw new UnsupportedOperationException("separators with builtin types not yet supported");
        }

        println("  public " + fieldClass + " get" + fieldName + "() {");
        println("    if (!isEmpty() && !isSingle()) {");
        println("      return " + fieldId + ";");
        println("    }");
        println(
            "    throw new UnsupportedOperationException(\"This "
                + className
                + " does not have a "
                + field.getId()
                + ":\" + this);"); 
        println("  }");

        println(
            "  public " + className + " set" + fieldName + "(" + fieldClass + " arg) {");
        println("    if (!isEmpty() && !isSingle()) {");
        String arglist = buildActualSeparatorArguments(listType);
        arglist = arglist.replaceAll(fieldId, "arg");
        println(
            "      return get"
                + FactoryGenerator.className(apiName)
                + "().make"
                + className
                + "(getHead(), "
                + arglist
                + "getTail());");
        println("    }");
        println(
            "    throw new RuntimeException(\"This "
                + className
                + " does not have a "
                + fieldId
                + ".\");");
        println("  }");
    }

    protected void genPredicates() {
        genIsEmpty(type.getId());
        genIsMany();
        genIsSingle();
    }

    private void genSeparatorFields() {
        Iterator fields = listType.separatorFieldIterator();

        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            genSeparatorField(field);
        }
    }

    protected void genEquivalentMethod() {
        String className = TypeGenerator.className(type);

        println("  public boolean equivalent(shared.SharedObject peer) {");
        println("    if (peer instanceof " + className + ") {");
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

    }

    protected void genDuplicateMethod() {
        String className = TypeGenerator.className(type);
        println("  public shared.SharedObject duplicate() {");
        println("    " + className + " clone = new " + className + "(factory);");
        println(
            "    clone.init(hashCode(), getAnnotations(), getFirst(), "
                + buildActualSeparatorArguments(listType)
                + "getNext());");
        println("    return clone;");
        println("  }");
    }

    private String buildActualSeparatorArguments(SeparatedListType type) {
        Iterator fields = type.separatorFieldIterator();
        String result = "";

        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String fieldId = JavaGenerator.getFieldId(field.getId());
            result += fieldId + ", ";
        }

        return result;
    }

    protected void genSeparatorFieldsEquivalentConjunction() {
        Iterator fields = listType.separatorFieldIterator();
        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String fieldId = JavaGenerator.getFieldId(field.getId());
            print(
                " && "
                    + fieldId
                    + ".equivalent((("
                    + TypeGenerator.className(type)
                    + ")peer)."
                    + fieldId
                    + ")");
        }
    }

    private void genSeparatorField(Field field) {
        String fieldClass = TypeGenerator.className(field.getType());
        String fieldId = JavaGenerator.getFieldId(field.getId());
        println("  protected " + fieldClass + " " + fieldId + " = null;");
    }

}
