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
		String apiName = getGenerationParameters().getApiName();
		return StringConversions.decapitalize(apiName) + '.' + packageName(type);
	}

	public String getQualifiedClassName() {
		return qualifiedClassName(getJavaGenerationParameters(), type);
	}

	public static String qualifiedClassName(JavaGenerationParameters params, String type) {
		StringBuffer buf = new StringBuffer();
		buf.append(params.getPackageName());
		buf.append('.');
		buf.append(StringConversions.decapitalize(params.getApiName()));
		buf.append('.');
		buf.append(packageName(type));
		buf.append('.');
		buf.append(className(type));
		return buf.toString();
	}

	public static String qualifiedClassName(JavaGenerationParameters params, Type type) {
		return qualifiedClassName(params, type.getId());
	}

	public static String packageName(Type type) {
		return packageName(type.getId());
	}

	public static String packageName(String type) {
		return StringConversions.decapitalize(type);
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
		String superClassName = GenericConstructorGenerator.qualifiedClassName(params);

		println("abstract public class " + classImplName + " extends " + superClassName + " {");

		genConstructor(classImplName);
		genInitMethod();
		genInitHashcodeMethod();
		genIsEqual(className);
		genIsTypeMethod(type);
		genTypeDefaultProperties(type);
		genDefaultGetAndSetMethods(type);
		println("}");
		println();

	}

	protected void genInitMethod() {
		println("  public void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {");
		println("    super.init(hashCode, annos, fun, args);");
		println("  }");
		println();
	}

	protected void genInitHashcodeMethod() {
		println("  public void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {");
		println("  	super.initHashCode(annos, fun, args);");
		println("  }");
		println();
	}

	protected void genConstructor(String classImplName) {
		String factoryName = FactoryGenerator.qualifiedClassName(getJavaGenerationParameters());
		println("  protected " + classImplName + "(" + factoryName + " factory) {");
		println("     super(factory);");
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
		String class_name = TypeGenerator.className(type.getId());
		String field_name = StringConversions.makeCapitalizedIdentifier(field.getId());
		String field_id = getFieldId(field.getId());
		String field_type_id = TypeGenerator.className(field.getType());

		// getter
		println("  public " + field_type_id + " get" + field_name + "() {");
		println("     throw new UnsupportedOperationException(\"This " + class_name + " has no " + field_name + "\");");
		println("  }");
		println();

		// setter
		println("  public " + class_name + " set" + field_name + "(" + field_type_id + " " + field_id + ") {");
		println("     throw new IllegalArgumentException(\"Illegal argument: \" + " + field_id + ");");
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
		println("  public boolean has" + StringConversions.makeCapitalizedIdentifier(field.getId()) + "() {");
		println("    return false;");
		println("  }");
		println();
	}

	protected void genDefaultIsMethod(Alternative alt) {
		println("  public boolean is" + StringConversions.makeCapitalizedIdentifier(alt.getId()) + "() {");
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
