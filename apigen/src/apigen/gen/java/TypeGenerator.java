package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.Alternative;
import apigen.adt.Field;
import apigen.adt.Type;
import apigen.gen.StringConversions;

public class TypeGenerator extends JavaGenerator {
	private Type type;

	protected TypeGenerator(JavaGenerationParameters params, Type type) {
		super(params);
		this.type = type;
	}

	public String getClassName() {
		return className(type);
	}

	public String getPackageName() {
		String apiName = getGenerationParameters().getApiExtName(type);
		return (apiName + '.' + packageName()).toLowerCase();
	}

	public String getQualifiedClassName() {
		return qualifiedClassName(getJavaGenerationParameters(), type);
	}

	public static String qualifiedClassName(JavaGenerationParameters params,
			String type) {
		if (getConverter().isReserved(type)) {
			return getConverter().getType(type);
		}
		StringBuffer buf = new StringBuffer();
		String pkg = params.getPackageName();

		if (pkg != null) {
			buf.append(pkg);
			buf.append('.');
		}
		String extName = params.getApiExtName(getConverter(), type);
		buf.append(extName.toLowerCase());
		buf.append('.');
		buf.append(packageName());
		buf.append('.');
		buf.append(className(type));
		return buf.toString();
	}

	public String qualifiedClassName(JavaGenerationParameters params, Type type) {
		return qualifiedClassName(params, type.getId());
	}

	public static String packageName() {
		return "types";
	}

	public static String className(Type type) {
		return className(type.getId());
	}

	public static String className(String type) {
		String className = getConverter().getType(type);

		if (!getConverter().isReserved(type)) {
			className = StringConversions.makeIdentifier(className);
		}

		return className;
	}

	protected void generate() {
		printPackageDecl();
		printImports();
		genTypeClassImpl(type);
	}

	protected void genTypeClassImpl(Type type) {
		JavaGenerationParameters params = getJavaGenerationParameters();
		String classImplName = className(type);
		String className = TypeGenerator.className(type.getId());
		String superClassName = AbstractTypeGenerator.qualifiedClassName(
				params, type.getModuleName());

		println("abstract public class " + classImplName + " extends "
				+ superClassName + " {");

		genConstructor(classImplName);
		genIsEqual(className);
		genIsTypeMethod(type);
		genTypeDefaultProperties(type);
		genDefaultGetAndSetMethods(type);
		println("}");
		println();

	}

	protected void genConstructor(String classImplName) {
		String factoryName = FactoryGenerator.qualifiedClassName(
				getJavaGenerationParameters(), type.getModuleName());
		println("  public "
				+ classImplName
				+ "("
				+ factoryName
				+ " factory, aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {");
		println("     super(factory, annos, fun, args);");
		println("  }");
		println();
	}

	protected void genIsEqual(String class_name) {
		println("  public boolean isEqual(" + class_name + " peer) {");
		println("    return super.isEqual(peer);");
		println("  }");
		println();
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
		JavaGenerationParameters params = getJavaGenerationParameters();
		String className = TypeGenerator.className(type.getId());
		String fieldName = StringConversions.makeCapitalizedIdentifier(field
				.getId());
		String fieldId = getFieldId(field.getId());
		String fieldTypeId = qualifiedClassName(params, field.getType());

		// getter
		println("  public " + fieldTypeId + " get" + fieldName + "() {");
		println("     throw new UnsupportedOperationException(\"This "
				+ className + " has no " + fieldName + "\");");
		println("  }");
		println();

		// setter
		println("  public " + className + " set" + fieldName + "("
				+ fieldTypeId + " " + fieldId + ") {");
		println("     throw new IllegalArgumentException(\"Illegal argument: \" + "
				+ fieldId + ");");
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
		println("  public boolean has"
				+ StringConversions.makeCapitalizedIdentifier(field.getId())
				+ "() {");
		println("    return false;");
		println("  }");
		println();
	}

	protected void genDefaultIsMethod(Alternative alt) {
		println("  public boolean is"
				+ StringConversions.makeCapitalizedIdentifier(alt.getId())
				+ "() {");
		println("    return false;");
		println("  }");
		println();
	}

	protected void genIsTypeMethod(Type type) {
		println("  public boolean isSort" + TypeGenerator.className(type)
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
