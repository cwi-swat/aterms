package apigen.gen.java;

import apigen.gen.StringConversions;
import apigen.gen.TomSignatureImplementation;
import apigen.gen.TypeConverter;

public class JavaTomSignatureImplementation implements TomSignatureImplementation {
	private static TypeConverter converter;

	static {
		converter = new TypeConverter(new JavaTypeConversions());
	}

	private String api_name;

	public JavaTomSignatureImplementation(String api_name) {
		this.api_name = api_name;
	}

	private String buildAltTypeName(String type, String alt) {
		return StringConversions.capitalize(type + "_" + StringConversions.capitalize(alt));
	}

	public String StringName() {
		return "String";
	}

	public String StringImpl() {
		return converter.StringType();
	}

	public String StringGetFunSym(String arg1) {
		return arg1;
	}

	public String StringGetSubTerm(String term, String n) {
		return "null";
	}

	public String StringCmpFunSym(String s1, String s2) {
		return s1 + ".equals(" + s2 + ")";
	}

	public String StringEquals(String s1, String s2) {
		return StringCmpFunSym(s1, s2);
	}

	public String IntegerName() {
		return "Integer";
	}

	public String IntegerImpl() {
		return converter.IntegerType();
	}

	public String IntegerGetFunSym(String arg1) {
		return arg1;
	}

	public String IntegerGetSubTerm(String term, String n) {
		return "null";
	}

	public String IntegerCmpFunSym(String s1, String s2) {
		return s1 + ".equals(" + s2 + ")";
	}

	public String IntegerEquals(String s1, String s2) {
		return IntegerCmpFunSym(s1, s2);
	}

	public String DoubleName() {
		return "Double";
	}

	public String DoubleImpl() {
		return converter.RealType();
	}

	public String DoubleGetFunSym(String arg1) {
		return arg1;
	}

	public String DoubleCmpFunSym(String s1, String s2) {
		return s1 + ".equals(" + s2 + ")";
	}

	public String DoubleGetSubTerm(String term, String n) {
		return "null";
	}

	public String DoubleEquals(String s1, String s2) {
		return DoubleCmpFunSym(s1, s2);
	}

	public String ATermName() {
		return "ATerm";
	}

	public String ATermImpl() {
		return converter.TermType();
	}

	public String ATermGetFunSym(String arg) {
		return "((" + arg + " instanceof ATermAppl)?((ATermAppl)" + arg + ").getAFun():null)";
	}

	public String ATermCmpFunSym(String s1, String s2) {
		return s1 + "==" + s2;
	}

	public String ATermGetSubTerm(String term, String n) {
		return "(((ATermAppl)" + term + ").getArgument(" + n + "))";
	}

	public String ATermEquals(String s1, String s2) {
		return s1 + ".equals(" + s2 + ")";
	}

	public String TypeName(String type) {
		return StringConversions.makeCapitalizedIdentifier(converter.getType(type));
	}

	public String TypeImpl(String type) {
		return type;
	}

	public String TypeGetFunSym(String arg) {
		return "null";
	}

	public String TypeCmpFunSym(String arg1, String arg2) {
		return "false";
	}

	public String TypeGetSubTerm(String term, String n) {
		return "null";
	}

	public String TypeEquals(String type, String arg1, String arg2) {
		return arg1 + ".equals(" + arg2 + ")";
	}

	public String OperatorName(String id) {
		return StringConversions.makeIdentifier(id);
	}

	public String OperatorType(String type, String id) {
		return buildAltTypeName(type, id);
	}

	public String OperatorFSym(String type, String alt) {
		return "";
	}

	public String OperatorIsFSym(String term, String type, String alt) {
		return "(" + term + "!= null) &&" + term + ".is" + StringConversions.makeCapitalizedIdentifier(alt) + "()";
	}

	public String OperatorGetSlot(String term, String type, String slot) {
		return term + ".get" + StringConversions.capitalize(slot) + "()";
	}

	public String OperatorMake(String type, String alt, String arguments) {
		return "get"
			+ StringConversions.makeCapitalizedIdentifier(api_name)
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
}
