package apigen.gen.tom.c;

import apigen.gen.StringConversions;
import apigen.gen.TypeConverter;
import apigen.gen.c.CTypeConversions;
import apigen.gen.tom.TomSignatureImplementation;

public class CTomSignatureImplementation implements TomSignatureImplementation {
	private static TypeConverter converter;
	private boolean jtype;
	private String prefix = "";

	public CTomSignatureImplementation(CTomGenerationParameters params) {
		this.prefix = params.getPrefix();
		this.jtype = params.isJtype();
	}

	static {
		converter = new TypeConverter(new CTypeConversions(""));
	}

	private String buildTypeName(String type) {
		return StringConversions.makeCapitalizedIdentifier(type);
	}

	private String buildAltTypeName(String type, String alt) {
		return StringConversions.capitalize(type + StringConversions.capitalize(alt));
	}

	public String IncludePrefix() {
		return "c/";
	}
	
	public String TypeName(String type) {
		return buildTypeName(type);
	}

	public String TypeImpl(String type) {
		return type;
	}

	public String TypeEquals(String type, String arg1, String arg2) {
		return prefix + "isEqual" + buildTypeName(type) + "(" + arg1 + "," + arg2 + ")";
	}

	public String TypeGetStamp() {
		return "";
	}
	
	public String TypeSetStamp(String type) {
		return "";
	}
	
	public String TypeGetImplementation(String arg) {
		return arg;
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
		return "is" + StringConversions.capitalize(buildAltTypeName(type, alt) + "(" + term + ")");
	}

	public String OperatorGetSlot(String term, String type, String slot) {
		return prefix + "get" + StringConversions.capitalize(buildAltTypeName(type, slot)) + "(" + term + ")";
	}

	public String OperatorMake(String type, String alt, String arguments) {
		return prefix + "make" + buildAltTypeName(type, alt) + arguments;
	}

	public String FieldName(String id) {
		return StringConversions.makeIdentifier(id);
	}

	public String FieldType(String type) {
		return buildTypeName(type);
	}

	public String ListHead(String type) {
		return prefix + "get" + StringConversions.capitalize(buildTypeName(type)) + "Head(l)";
	}

	public String ListTail(String type) {
		return prefix + "get" + StringConversions.capitalize(buildTypeName(type)) + "Tail(l)";
	}

	public String ListEmpty(String type) {
		return prefix + "is" + StringConversions.capitalize(buildTypeName(type)) + "Empty(l)";
	}

	public String ListIsList(String term, String type) {
		return prefix
			+ "is"
			+ buildAltTypeName(type, "empty")
			+ "("
			+ term
			+ ") ||"
			+ prefix
			+ "is"
			+ buildAltTypeName(type, "many")
			+ "("
			+ term
			+ ")";
	}

	public String ListmakeEmpty(String type) {
		return prefix + "make" + buildAltTypeName(type, "empty") + "()";
	}

	public String ListmakeInsert(String type, String eltType) {
		return prefix + "make" + buildAltTypeName(type, "many") + "(e, l)";
	}
}
