package apigen.gen.java;

import java.util.List;

import apigen.adt.SeparatedListType;
import apigen.adt.api.Separators;

public class SeparatedListTypeImplGenerator extends ListTypeImplGenerator {
    protected Separators separators;

    public SeparatedListTypeImplGenerator(
        SeparatedListType type,
        String directory,
        String pkg,
        String apiName,
        List standardImports,
        boolean verbose) {
        super(type, directory, pkg, apiName, standardImports, verbose);

        this.separators = type.getSeparators();
    }

    protected void generate() {
        printPackageDecl();
        
        // TODO: test whether these import are needed!
        imports.add("java.io.InputStream");
        imports.add("java.io.IOException");

        printImports();
        println();
        genListTypeClassImpl();
    }

    protected String classModifier() {
       return "public";
    }
    private void genSeparatedListTypeClassImpl() {
        println(
            classModifier() + " class " + typeName + " extends aterm.pure.ATermListImpl");
        println("{");
        
        println("}");
    }

}
