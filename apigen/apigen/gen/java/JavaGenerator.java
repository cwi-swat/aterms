package apigen.gen.java;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import apigen.adt.Alternative;
import apigen.adt.Field;
import apigen.adt.Type;
import apigen.gen.Generator;
import apigen.gen.StringConversions;
import apigen.gen.TypeConverter;

public abstract class JavaGenerator extends Generator {
    static protected TypeConverter converter;
    protected String pkg;
    List imports;

    /**
     * Load the Java type conversions
     */
    static {
        converter = new TypeConverter(new JavaTypeConversions());
    }

    /**
     * A java file generator
     * @param directory The path of the new java file
     * @param filename  The name of the new java class
     * @param pkg       The package that it should go into
     * @param standardImports The list of imports it should *at least* do
     * @param verbose   Print information on stderr while generating
     */
    protected JavaGenerator(
        String directory,
        String filename,
        String pkg,
        List standardImports,
        boolean verbose) {
        super(directory, filename, ".java", verbose, false);
        this.pkg = pkg;
        this.imports = new LinkedList(standardImports);
    }

    protected void printImports() {
        Iterator iter = imports.iterator();
        while (iter.hasNext()) {
            println("import " + (String) iter.next() + ";");
        }
    }

    protected void printPackageDecl() {
        if (pkg.length() > 0) {
            println("package " + pkg + ";");
            println();
        }
    }

    /**
     * Create a variable name from a field name
     */
    public static String getFieldId(String fieldId) {
        return "_" + StringConversions.makeIdentifier(fieldId);
    }

    public static String getFieldIndex(String fieldId) {
        return "index_" + StringConversions.makeIdentifier(fieldId);
    }

    /**
     * Print an actual argument list for one specific constructor. The field names
     * are used for the variable names of the argument positions. In case of a reserved
     * type the appropriate conversion is generated from target type to ATerm representation.
     */
    protected void printActualTypedArgumentList(Type type, Alternative alt) {
        Iterator fields = type.altFieldIterator(alt.getId());

        print(buildActualTypedAltArgumentList(fields));
    }

    protected String buildActualTypedAltArgumentList(Iterator fields) {
        String result = "";

        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String field_id = getFieldId(field.getId());
            String field_type = field.getType();

            if (field_type.equals("str")) {
                result += "makeAppl(makeAFun(" + field_id + ", 0, true))";
            } else if (field_type.equals("int")) {
                result += "makeInt(" + field_id + ")";
            } else if (field_type.equals("real")) {
                result += "makeReal(" + field_id + ")";
            } else {
                result += field_id;
            }

            if (fields.hasNext()) {
                result += ", ";
            }
        }

        return result;
    }

    protected String buildActualNullArgumentList(Iterator fields) {
        String result = "";

        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            result += "(" + TypeGenerator.className(field.getType()) + ") null";

            if (fields.hasNext()) {
                result += ", ";
            }
        }

        return result;
    }
    
    /**
     * Print a formal argument list for one specific constructor. The field types are
     * derived from the ADT, the field names are used for the formal parameter names.
     */
    protected void printFormalTypedAltArgumentList(Type type, Alternative alt) {
        Iterator fields = type.altFieldIterator(alt.getId());
        print(buildFormalTypedArgumentList(fields));
    }

    protected String buildFormalTypedArgumentList(Iterator fields) {
        String result = "";
        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String field_id = getFieldId(field.getId());
            result += TypeGenerator.className(field.getType()) + " " + field_id;

            if (fields.hasNext()) {
                result += ", ";
            }
        }

        return result;
    }

}
