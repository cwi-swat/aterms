package apigen.gen.java;

import java.util.Iterator;
import java.util.List;

import apigen.adt.*;
import apigen.gen.StringConversions;

public class TypeImplGenerator extends JavaGenerator {
	protected Type type;
	protected String apiName;

	protected TypeImplGenerator(
		Type type,
		String directory,
		String pkg,
		String apiName,
		List standardImports,
		boolean verbose) {
		super(directory, className(type), pkg, standardImports, verbose);
		this.type = type;
		this.apiName = apiName;
	}

	public static String className(Type type) {
		return TypeGenerator.className(type.getId()) + "Impl";
	}

	protected void generate() {
		printPackageDecl();

		imports.add("java.io.InputStream");
		imports.add("java.io.IOException");

		printImports();
		println();

		genTypeClassImpl(type);
	}

	protected void genTypeClassImpl(Type type) {
		String class_impl_name = className(type);
		String class_name = TypeGenerator.className(type.getId());

		println(
			"abstract public class " + class_impl_name + " extends " + GenericConstructorGenerator.className(apiName));
		println("{");
		genFromString(class_name);
		genFromTextFile(class_name);
		genIsEqual(class_name);
		genFromTerm(type, class_name);
		genIsTypeMethod(type);
		genTypeDefaultProperties(type);
		genDefaultGetAndSetMethods(type);
		println("}");
		println();

	}

	private void genIsEqual(String class_name) {
		println("  public boolean isEqual(" + class_name + " peer)");
		println("  {");
		println("    return term.isEqual(peer.toTerm());");
		println("  }");
	}

	private void genFromTerm(Type type, String class_name) {
		println("  public static " + class_name + " fromTerm(aterm.ATerm trm)");
		println("  {");
		println("    " + class_name + " tmp;");
		genFromTermCalls(type);
		println();
		println("    throw new RuntimeException(\"This is not a " + class_name + ": \" + trm);");
		println("  }");
	}

	protected void genFromTextFile(String class_name) {
		String get_factory = "getStatic" + FactoryGenerator.className(apiName) + "()";
		println(
			"  public static "
				+ class_name
				+ " fromTextFile(InputStream stream) "
				+ "throws aterm.ParseError, IOException");
		println("  {");
		println("    aterm.ATerm trm = " + get_factory + ".readFromTextFile(stream);");
		println("    return fromTerm(trm);");
		println("  }");
	}

	protected void genFromString(String class_name) {
		String get_factory = "getStatic" + FactoryGenerator.className(apiName) + "()";
		println("  public static " + class_name + " fromString(String str)");
		println("  {");
		println("    aterm.ATerm trm = " + get_factory + ".parse(str);");
		println("    return fromTerm(trm);");
		println("  }");
	}

	protected void genFromTermCalls(Type type) {
		Iterator alts = type.alternativeIterator();
		while (alts.hasNext()) {
			Alternative alt = (Alternative) alts.next();
			String alt_class_name = AlternativeGenerator.className(type, alt);
			println("    if ((tmp = " + alt_class_name + ".fromTerm(trm)) != null) {");
			println("      return tmp;");
			println("    }");
			println();
		}
	}

	protected void genDefaultGetAndSetMethods(Type type) {
		Iterator fields = type.fieldIterator();

		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			genDefaultGetAndSetMethod(type, field);
		}
	}

	protected void genTypeDefaultProperties(Type type) {
		genDefaultIsMethods(type);
		genDefaultHasMethods(type);
	}

	protected void genDefaultGetAndSetMethod(Type type, Field field) {
		String class_name = TypeGenerator.className(type.getId());
		String field_name = StringConversions.makeCapitalizedIdentifier(field.getId());
		String field_id = getFieldId(field.getId());
		String field_type_id = TypeGenerator.className(field.getType());

		// getter    
		println("  public " + field_type_id + " get" + field_name + "()");
		println("  {");
		println("     throw new RuntimeException(\"This " + class_name + " has no " + field_name + "\");");
		println("  }");
		println();

		// setter
		println("  public " + class_name + " set" + field_name + "(" + field_type_id + " " + field_id + ")");
		println("  {");
		println("     throw new RuntimeException(\"This " + class_name + " has no " + field_name + "\");");
		println("  }");
		println();
	}

	protected void genDefaultHasMethods(Type type) {
		Iterator fields = type.fieldIterator();

		while (fields.hasNext()) {
			Field field = (Field) fields.next();
			genDefaultHasMethod(field);
		}
	}

	protected void genDefaultHasMethod(Field field) {
		println("  public boolean has" + StringConversions.makeCapitalizedIdentifier(field.getId()) + "()");
		println("  {");
		println("    return false;");
		println("  }");
		println();
	}

	protected void genDefaultIsMethod(Alternative alt) {
		println("  public boolean is" + StringConversions.makeCapitalizedIdentifier(alt.getId()) + "()");
		println("  {");
		println("    return false;");
		println("  }");
		println();
	}

	protected void genIsTypeMethod(Type type) {
		println("  public boolean isSort" + TypeGenerator.className(type) + "()  {");
		println("    return true;");
		println("  }");
		println();
	}

	protected void genDefaultIsMethods(Type type) {
		Iterator alts = type.alternativeIterator();
		while (alts.hasNext()) {
			Alternative alt = (Alternative) alts.next();
			genDefaultIsMethod(alt);
		}
	}
}
