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
			"abstract public class "
				+ class_impl_name
				+ " extends "
				+ GenericConstructorGenerator.className(apiName));
		println("{");

		genConstructor(class_impl_name);
		genInitMethod();
		genInitHashcodeMethod();
		genIsEqual(class_name);
		genIsTypeMethod(type);
		genTypeDefaultProperties(type);
		genDefaultGetAndSetMethods(type);
		println("}");
		println();

	}

	private void genInitMethod() {
		println("  protected void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {");
		println("    super.init(hashCode, annos, fun, args);");
		println("  }");
	}

	private void genInitHashcodeMethod() {
		println("  protected void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] i_args) {");
		println("  	super.initHashCode(annos, fun, i_args);");
		println("  }");
	}
	protected void genConstructor(String class_impl_name) {
		println(
			"  "
				+ class_impl_name
				+ "("
				+ FactoryGenerator.className(apiName)
				+ " factory) {");
		println("     super(factory);");
		println("  }");
	}

	private void genIsEqual(String class_name) {
		println("  public boolean isEqual(" + class_name + " peer)");
		println("  {");
		println("    return term.isEqual(peer.toTerm());");
		println("  }");
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
		String field_name =
			StringConversions.makeCapitalizedIdentifier(field.getId());
		String field_id = getFieldId(field.getId());
		String field_type_id = TypeGenerator.className(field.getType());

		// getter    
		println("  public " + field_type_id + " get" + field_name + "()");
		println("  {");
		println(
			"     throw new RuntimeException(\"This "
				+ class_name
				+ " has no "
				+ field_name
				+ "\");");
		println("  }");
		println();

		// setter
		println(
			"  public "
				+ class_name
				+ " set"
				+ field_name
				+ "("
				+ field_type_id
				+ " "
				+ field_id
				+ ")");
		println("  {");
		println(
			"     throw new RuntimeException(\"This "
				+ class_name
				+ " has no "
				+ field_name
				+ "\");");
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
		println(
			"  public boolean has"
				+ StringConversions.makeCapitalizedIdentifier(field.getId())
				+ "()");
		println("  {");
		println("    return false;");
		println("  }");
		println();
	}

	protected void genDefaultIsMethod(Alternative alt) {
		println(
			"  public boolean is"
				+ StringConversions.makeCapitalizedIdentifier(alt.getId())
				+ "()");
		println("  {");
		println("    return false;");
		println("  }");
		println();
	}

	protected void genIsTypeMethod(Type type) {
		println(
			"  public boolean isSort"
				+ TypeGenerator.className(type)
				+ "()  {");
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
