package apigen.gen.java;

import java.util.Iterator;

import apigen.adt.Alternative;
import apigen.adt.Field;
import apigen.adt.Type;
import apigen.gen.GenerationParameters;
import apigen.gen.StringConversions;

public class TypeGenerator extends JavaGenerator {
	private Type type;
	private String superClassName;
	private String factoryName;
	private String apiName;

	protected TypeGenerator(GenerationParameters params, Type type) {
		super(params);
		this.type = type;
		this.apiName = params.getApiName();
		this.superClassName = GenericConstructorGenerator.className(params.getApiName());
		this.factoryName = FactoryGenerator.className(params.getApiName());
	}

	public String getClassName() {
		return className(type);
	}

	public String getPackageName() {
		return StringConversions.decapitalize(apiName) + '.' + packageName(type);
	}

	public String getQualifiedClassName() {
		return qualifiedClassName(type);
	}

	public static String qualifiedClassName(String type) {
		return packageName(type) + '.' + className(type);
	}

	public static String qualifiedClassName(Type type) {
		return qualifiedClassName(type.getId());
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
		String classImplName = className(type);
		String className = TypeGenerator.className(type.getId());

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
		println("  protected void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {");
		println("    super.init(hashCode, annos, fun, args);");
		println("  }");
	}

	protected void genInitHashcodeMethod() {
		println("  protected void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] i_args) {");
		println("  	super.initHashCode(annos, fun, i_args);");
		println("  }");
	}

	protected void genConstructor(String classImplName) {
		println("  protected " + classImplName + "(" + factoryName + " factory) {");
		println("     super(factory);");
		println("  }");
	}

	protected void genIsEqual(String class_name) {
		println("  public boolean isEqual(" + class_name + " peer) {");
		println("    return super.isEqual(peer);");
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
