package apigen.gen.java;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import apigen.adt.Alternative;
import apigen.adt.Field;
import apigen.adt.Type;
import apigen.gen.GenerationParameters;
import apigen.gen.Generator;
import apigen.gen.StringConversions;
import apigen.gen.TypeConverter;

public abstract class JavaGenerator extends Generator {
	private static TypeConverter converter = new TypeConverter(new JavaTypeConversions());

	private String packageName;
	private GenerationParameters params;

	public static TypeConverter getConverter() {
		return converter;
	}

	protected JavaGenerator(GenerationParameters params) {
		super();
		this.params = params;
		this.packageName = params.getPackageName();
		setDirectory(buildDirectoryName(params.getBaseDir(), params.getPackageName(), params.getApiName()));
		setFileName(getClassName());
		setExtension(".java");
	}

	private String buildDirectoryName(String baseDir, String pkgName, String apiName) {
		StringBuffer buf = new StringBuffer();
		buf.append(baseDir);
		buf.append(File.separatorChar);
		buf.append(pkgName.replace('.', File.separatorChar));
		buf.append(File.separatorChar);
		buf.append(apiName);
		System.err.println("buildDirectoryName: " + buf.toString());
		return buf.toString();
	}

	abstract public String getClassName();

	protected void printImports() {
		List imports = params.getImports();
		if (imports.size() > 0) {
			Iterator iter = imports.iterator();
			while (iter.hasNext()) {
				println("import " + (String) iter.next() + ";");
			}
			println();
		}
	}

	protected void printPackageDecl() {
		if (packageName.length() > 0) {
			println("package " + packageName + ';');
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
	 * Print an actual argument list for one specific constructor. The field
	 * names are used for the variable names of the argument positions. In case
	 * of a reserved type the appropriate conversion is generated from target
	 * type to ATerm representation.
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
				result += "factory.makeAppl(factory.makeAFun(" + field_id + ", 0, true))";
			}
			else if (field_type.equals("int")) {
				result += "factory.makeInt(" + field_id + ")";
			}
			else if (field_type.equals("real")) {
				result += "factory.makeReal(" + field_id + ")";
			}
			else {
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
	 * Print a formal argument list for one specific constructor. The field
	 * types are derived from the ADT, the field names are used for the formal
	 * parameter names.
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
