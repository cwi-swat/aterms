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

        // TODO: test whether these import are needed!
        imports.add("java.io.InputStream");
        imports.add("java.io.IOException");

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
        println("}");
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
        println("  }");
    }

    private void genSeparatorInitAssigmnents(SeparatedListType type) {
        Iterator fields = type.separatorFieldIterator();
        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String fieldId = AlternativeImplGenerator.getFieldId(field.getId());
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
        String fieldId = AlternativeGenerator.getFieldId(field.getId());
        String className = TypeGenerator.className(type);

        if (converter.isReserved(field.getType())) {
            // TODO: find a way to reuse generation of getters in AlternativeImplGenerator
            throw new UnsupportedOperationException("separators with builtin types not yet supported");
        }

        println("  public " + fieldClass + " get" + fieldName + "() {");
        println("    return " + fieldId + ";");
        println("  }");

        // TODO : implement separator setters
        println(
            "  public " + className + " set" + fieldName + "(" + fieldClass + " arg) {");
        println("    return (" + className + ") null; // not yet implemented");
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
            String fieldId = AlternativeImplGenerator.getFieldId(field.getId());
            result += fieldId + ", ";
        }

        return result;
    }

    protected void genSeparatorFieldsEquivalentConjunction() {
        Iterator fields = listType.separatorFieldIterator();
        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String fieldId = AlternativeImplGenerator.getFieldId(field.getId());
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
        String fieldId = AlternativeImplGenerator.getFieldId(field.getId());
        println("  protected " + fieldClass + " " + fieldId + " = null;");
    }

}
