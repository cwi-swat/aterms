package apigen.gen.tom.java;

import apigen.adt.api.types.Module;
import apigen.gen.StringConversions;
import apigen.gen.TypeConverter;
import apigen.gen.java.FactoryGenerator;
import apigen.gen.java.JavaTypeConversions;
import apigen.gen.tom.TomSignatureImplementation;
import apigen.gen.tom.TomTypeConversions;

public class JavaTomSignatureImplementation implements
		TomSignatureImplementation {
	private static TypeConverter javaConverter = new TypeConverter(
			new JavaTypeConversions("factory"));
	private static TypeConverter tomConverter = new TypeConverter(
			new TomTypeConversions());
	private boolean jtype = false;

	private String fullFactoryName;

	public JavaTomSignatureImplementation(JavaTomGenerationParameters params,
			Module module) {
		String moduleName = module.getModulename().getName();
		jtype = params.isJtype();
		fullFactoryName = "";
		if (params.getPackageName() != null) {
			fullFactoryName += params.getPackageName() + ".";
		}
		fullFactoryName += params.getApiExtName(module).toLowerCase() + ".";
		fullFactoryName += FactoryGenerator.className(moduleName);
	}

	private String buildAltTypeName(String type, String alt) {
		if (tomConverter.isReserved(type)) {
			return tomConverter.getType(type);
		}

		return StringConversions.capitalize(type + "_"
				+ StringConversions.capitalize(alt));
	}

	public String FieldName(String id) {
		return StringConversions.makeIdentifier(id);
	}

	public String FieldType(String type) {
		return TypeName(type);
	}

	public String IncludePrefix() {
		return "";
	}

	public String ListEmpty(String type) {
		return "l.isEmpty()";
	}

	public String ListHead(String type) {
		return "l.getHead()";
	}

	public String ListIsList(String term, String type) {
		return "(" + term + "!= null) && " + term + ".isSort"
				+ StringConversions.makeCapitalizedIdentifier(type) + "()";
	}

	public String ListmakeEmpty(String type) {
		return fullFactoryName
				+ ".getInstance(aterm.pure.SingletonFactory.getInstance())"
				+ ".make" + StringConversions.makeCapitalizedIdentifier(type)
				+ "()";
	}

	public String ListmakeInsert(String type, String eltType) {
		return fullFactoryName
				+ ".getInstance(aterm.pure.SingletonFactory.getInstance())"
				+ ".make" + StringConversions.makeCapitalizedIdentifier(type)
				+ "(e,l)";
	}

	public String ListTail(String type) {
		return "l.getTail()";
	}

	public String OperatorGetSlot(String term, String type, String slot) {
		return term + ".get" + StringConversions.capitalize(slot) + "()";
	}

	public String OperatorIsFSym(String term, String type, String alt) {
		return "(" + term + "!= null) && " + term + ".is"
				+ StringConversions.makeCapitalizedIdentifier(alt) + "()";
	}

	public String OperatorMake(String type, String alt, String arguments) {
		return fullFactoryName
				+ ".getInstance(aterm.pure.SingletonFactory.getInstance())"
				+ ".make" + buildAltTypeName(type, alt) + arguments;
	}

	public String OperatorName(String type, String id) {
		if (jtype) {
			return StringConversions.makeIdentifier(type) + "_"
					+ StringConversions.makeIdentifier(id);
		}
		return StringConversions.makeIdentifier(id);
	}

	public String OperatorType(String type, String id) {
		return buildAltTypeName(type, id);
	}

	public String TypeEquals(String type, String arg1, String arg2) {
		return arg1 + ".equals(" + arg2 + ")";
	}

	public String TypeGetImplementation(String arg) {
		return arg;
	}

	public String TypeGetStamp() {
		String stamp = "aterm.pure.SingletonFactory.getInstance().makeList()";
		return "if(t.getAnnotation(" + stamp + ") == " + stamp
				+ ")  return; else throw new RuntimeException(\"bad stamp\")";
	}

	public String TypeImpl(String type) {
		return type;
	}

	public String TypeName(String type) {
		if (tomConverter.isReserved(type)) {
			return tomConverter.getType(type);
		}

		return StringConversions.makeCapitalizedIdentifier(tomConverter
				.getType(type));
	}

	public String TypeSetStamp(String type) {
		String stamp = "aterm.pure.SingletonFactory.getInstance().makeList()";
		return "(" + type + ")t.setAnnotation(" + stamp + "," + stamp + ")";
	}
}
