package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.Alternative;
import apigen.adt.Field;
import apigen.adt.Type;
import apigen.gen.Generator;
import apigen.gen.StringConversions;

public abstract class JavaGenerator extends Generator {
	protected String pkg;
	List imports;

	protected JavaGenerator(
		String directory,
		String filename,
		String pkg,
		List standardImports,
		boolean verbose,
		boolean folding) {
		super(directory, filename, ".java", verbose, folding);
		this.pkg = pkg;
	    this.imports = standardImports;
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

	public static String getClassImplName(String type) {
		return getClassName(type) + "Impl";
	}

	public static String getClassName(String typeId) {
		return StringConversions.makeIdentifier(JavaTypeConversions.getType(typeId));
	}

	public static String getTypeId(String typeId) {
		return StringConversions.makeIdentifier(JavaTypeConversions.getType(typeId));
	}

	public static String getFieldId(String fieldId) {
		return "_" + StringConversions.makeIdentifier(fieldId);
	}

	public static String getFieldIndex(String fieldId) {
		return "index_" + StringConversions.makeIdentifier(fieldId);
	}

	protected void printActualTypedArgumentList(Type type, Alternative alt) {
		Iterator fields = type.altFieldIterator(alt.getId());
		int argnr = 0;
		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			String field_id = getFieldId(field.getId());
			String field_type = field.getType();

			if (field_type.equals("str")) {
				print("makeAppl(makeAFun(" + field_id + ", 0, true))");
			} else if (field_type.equals("int")) {
				print("makeInt(" + field_id + ".intValue())");
			} else if (field_type.equals("real")) {
				print("makeReal(" + field_id + ".doubleValue())");
			} else {
				print(field_id);
			}

			if (fields.hasNext()) {
				print(", ");
			}
		}
	}

	protected void printFormalTypedAltArgumentList(Type type, Alternative alt) {
		Iterator fields = type.altFieldIterator(alt.getId());
		while (fields.hasNext()) {
		  Field field = (Field) fields.next();
	      String field_id = getFieldId(field.getId());
	      print(getClassName(getClassName(field.getType())) + " " + field_id);
	
		  if (fields.hasNext()) {
		    print(", ");
		  }
		}
	}

}
