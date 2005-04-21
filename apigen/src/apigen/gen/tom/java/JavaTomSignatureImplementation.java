package apigen.gen.tom.java;

import apigen.adt.api.types.Module;
import apigen.gen.StringConversions;
import apigen.gen.TypeConverter;
import apigen.gen.java.JavaTypeConversions;
import apigen.gen.tom.TomSignatureImplementation;
import apigen.gen.tom.TomTypeConversions;

public class JavaTomSignatureImplementation implements TomSignatureImplementation {
	private static TypeConverter javaConverter = new TypeConverter(new JavaTypeConversions("factory"));
	private static TypeConverter tomConverter = new TypeConverter(new TomTypeConversions());
	private boolean jtype = false;

	private String apiName;

	public JavaTomSignatureImplementation(JavaTomGenerationParameters params, Module module) {
		//this.apiName = params.getApiName();
		String moduleName = module.getModulename().getName();
		this.apiName = (moduleName.equals("")?params.getApiName():moduleName);
		this.jtype = params.isJtype();
	}

	private String buildAltTypeName(String type, String alt) {
		if (tomConverter.isReserved(type)) {
			return tomConverter.getType(type);
		}

		return StringConversions.capitalize(type + "_" + StringConversions.capitalize(alt));
	}
	
	public String TypeName(String type) {
		if (tomConverter.isReserved(type)) {
			return tomConverter.getType(type);
		}

		return StringConversions.makeCapitalizedIdentifier(tomConverter.getType(type));
	}

	public String TypeImpl(String type) {
		return type;
	}

	public String TypeEquals(String type, String arg1, String arg2) {
		return arg1 + ".equals(" + arg2 + ")";
	}

	public String OperatorName(String type, String id) {
		if (jtype) {
			return StringConversions.makeIdentifier(type) + "_" + StringConversions.makeIdentifier(id);
		}
		else {
			return StringConversions.makeIdentifier(id);
		}
	}

	public String OperatorType(String type, String id) {
		return buildAltTypeName(type, id);
	}

	public String OperatorIsFSym(String term, String type, String alt) {
		return "(" + term + "!= null) && " + term + ".is" + StringConversions.makeCapitalizedIdentifier(alt) + "()";
	}

	public String OperatorGetSlot(String term, String type, String slot) {
		return term + ".get" + StringConversions.capitalize(slot) + "()";
	}

	public String OperatorMake(String type, String alt, String arguments) {
		return "get"
			+ StringConversions.makeCapitalizedIdentifier(apiName)
			+ "Factory"
			+ "().make"
			+ buildAltTypeName(type, alt)
			+ arguments;
	}

	public String FieldName(String id) {
		return StringConversions.makeIdentifier(id);
	}

	public String FieldType(String type) {
		return TypeName(type);
	}

	public String ListHead(String type) {
		return "l.getHead()";
	}

	public String ListTail(String type) {
		return "l.getTail()";
	}

	public String ListEmpty(String type) {
		return "l.isEmpty()";
	}

	public String ListIsList(String term, String type) {
		return "("
			+ term
			+ "!= null) && "
			+ term
			+ ".isSort"
			+ StringConversions.makeCapitalizedIdentifier(type)
			+ "()";
	}

	public String ListmakeEmpty(String type) {
		return "get"
			+ StringConversions.makeCapitalizedIdentifier(apiName)
			+ "Factory().make"
			+ StringConversions.makeCapitalizedIdentifier(type)
			+ "()";
	}

	public String ListmakeInsert(String type, String eltType) {
		return "get"
			+ StringConversions.makeCapitalizedIdentifier(apiName)
			+ "Factory().make"
			+ StringConversions.makeCapitalizedIdentifier(type)
			+ "(e,l)";
	}
}
