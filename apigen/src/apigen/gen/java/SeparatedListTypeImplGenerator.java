package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.Field;
import apigen.adt.SeparatedListType;
import apigen.adt.api.Separators;

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
        genSeparatorGettersAndSetters();
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

    private void genSeparatorsGettersAndSetters() {
        Iterator fields = listType.separatorFieldIterator();
        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            genSeparatorGetterAndSetter(field);
        }
    }

    private void genSeparatorGetterAndSetter(Field field) {
        String fieldName = field.getId()
        println("  public get" + )
        
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
        // TODO: generate isSingle
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
        println("    clone.init(hashCode(), getAnnotations(), getFirst(), getNext());");
        genCloneSetSeparatorFields();
        println("    return clone;");
        println("  }");
    }

    protected void genCloneSetSeparatorFields() {
        Iterator fields = listType.separatorFieldIterator();
        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            genCloneSetSeparatorField(field);
        }
    }

    protected void genCloneSetSeparatorField(Field field) {
        String fieldId = AlternativeImplGenerator.getFieldId(field.getId());
        println("    clone." + fieldId + " = " + fieldId + ";");
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
