package apigen.gen.java;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import apigen.adt.Alternative;
import apigen.adt.Field;
import apigen.adt.Type;
import apigen.gen.Generator;
import apigen.gen.StringConversions;
import apigen.gen.TypeConverter;

public abstract class JavaGenerator extends Generator {
    private static TypeConverter converter = new TypeConverter(new JavaTypeConversions("factory","getFactory()"));

    private String basePackageName;
    private List imports;

    public static TypeConverter getConverter() {
        return converter;
    }

    protected JavaGenerator(JavaGenerationParameters params) {
        super(params);
        this.basePackageName = params.getPackageName();
        this.imports = params.getImports();
        setExtension(".java");
        setDirectory(
            buildDirectoryName(params.getOutputDirectory(), params.getPackageName()));
    }

    public JavaGenerationParameters getJavaGenerationParameters() {
        return (JavaGenerationParameters) getGenerationParameters();
    }

    public String getDirectory() {
        return super.getDirectory()
            + File.separatorChar
            + getPackageName().replace('.', File.separatorChar);
    }

    public String getFileName() {
        return getClassName();
    }

    private String buildDirectoryName(String baseDir, String pkgName) {
        StringBuffer buf = new StringBuffer();
        buf.append(baseDir);
        buf.append(File.separatorChar);
        buf.append(pkgName.replace('.', File.separatorChar));
        return buf.toString();
    }

    abstract public String getClassName();
    abstract public String getPackageName();
    abstract public String getQualifiedClassName();

    protected void printImports() {
        if (imports.size() > 0) {
            Iterator iter = imports.iterator();
            while (iter.hasNext()) {
                println("import " + (String) iter.next() + ";");
            }
            println();
        }
    }

    protected void printPackageDecl() {
        print("package ");
        if (basePackageName != null) {
            print(basePackageName + '.');
        }
        println(getPackageName() + ';');
        println();
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
     * Print an actual argument list for one specific constructor. The field
     * names are used for the variable names of the argument positions. In case
     * of a reserved type the appropriate conversion is generated from target
     * type to ATerm representation.
     */
    protected void printActualTypedArgumentList(Type type, Alternative alt) {
        Iterator fields = type.altFieldIterator(alt.getId());

        print(buildActualTypedAltArgumentList(fields, true));
    }

    protected String buildActualTypedAltArgumentList(Iterator fields, boolean convertBuiltins) {
        StringBuffer buf = new StringBuffer();

        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String field_id = getFieldId(field.getId());
            String field_type = field.getType();
            
            if (convertBuiltins) {
               field_id = getConverter().makeBuiltinToATermConversion(field_type, field_id);
            }

            buf.append(field_id);

            if (fields.hasNext()) {
                buf.append(", ");
            }
        }

        return buf.toString();
    }

    protected String buildActualTypedAltArgumentListNoConversion(Iterator fields) {
        StringBuffer buf = new StringBuffer();

        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String field_id = getFieldId(field.getId());
            String field_type = field.getType();

            buf.append(field_id);

            if (fields.hasNext()) {
                buf.append(", ");
            }
        }

        return buf.toString();
    }

    protected String buildActualNullArgumentList(Iterator fields) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        StringBuffer buf = new StringBuffer();

        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            buf.append('(');
            buf.append(TypeGenerator.qualifiedClassName(params, field.getType()));
            buf.append(") null");

            if (fields.hasNext()) {
                buf.append(", ");
            }
        }

        return buf.toString();
    }

    /**
     * Print a formal argument list for one specific constructor. The field
     * types are derived from the ADT, the field names are used for the formal
     * parameter names.
     */
    protected void printFormalTypedAltArgumentList(Type type, Alternative alt) {
        Iterator fields = type.altFieldIterator(alt.getId());
        print(buildFormalTypedArgumentList(fields));
    }

    protected String buildFormalTypedArgumentList(Iterator fields) {
        JavaGenerationParameters params = getJavaGenerationParameters();
        StringBuffer buf = new StringBuffer();

        while (fields.hasNext()) {
            Field field = (Field) fields.next();
            String type = field.getType();
            buf.append(TypeGenerator.qualifiedClassName(params, type));
            buf.append(' ');
            buf.append(getFieldId(field.getId()));

            if (fields.hasNext()) {
                buf.append(", ");
            }
        }

        return buf.toString();
    }

}
