package apigen.gen.java;

import apigen.gen.TomSignatureGenerator;

public class JavaTomSignatureGenerator extends TomSignatureGenerator {
	private String api_name = "";

	private String buildTypeName(String type) {
		return capitalize(buildId(type));
	}

	private String buildAltTypeName(String type, String alt) {
		return capitalize(type + "_" + capitalize(alt));
	}

	protected String StringImpl() {
		return "String";
	}

	protected String StringGetFunSym(String arg1) {
		return arg1;
	}

	protected String StringGetSubTerm(String term, String n) {
		return "null";
	}

	protected String StringCmpFunSym(String s1, String s2) {
		return s1 + ".equals(" + s2 + ")";
	}

	protected String StringEquals(String s1, String s2) {
		return StringCmpFunSym(s1, s2);
	}

	protected String IntegerImpl() {
		return "Integer";
	}

	protected String IntegerGetFunSym(String arg1) {
		return arg1;
	}

	protected String IntegerGetSubTerm(String term, String n) {
		return "null";
	}

	protected String IntegerCmpFunSym(String s1, String s2) {
		return s1 + ".equals(" + s2 + ")";
	}

	protected String IntegerEquals(String s1, String s2) {
		return IntegerCmpFunSym(s1, s2);
	}

	protected String DoubleImpl() {
		return "Double";
	}

	protected String DoubleGetFunSym(String arg1) {
		return arg1;
	}

	protected String DoubleCmpFunSym(String s1, String s2) {
		return s1 + ".equals(" + s2 + ")";
	}

	protected String DoubleGetSubTerm(String term, String n) {
		return "null";
	}

	protected String DoubleEquals(String s1, String s2) {
		return DoubleCmpFunSym(s1, s2);
	}

	protected String ATermImpl() {
		return "ATerm";
	}

	protected String ATermGetFunSym(String arg) {
		return "(("
			+ arg
			+ "instanceof ATermAppl)?((ATermAppl)"
			+ arg
			+ ").getAFun():null)";
	}

	protected String ATermCmpFunSym(String s1, String s2) {
		return s1 + "==" + s2;
	}

	protected String ATermGetSubTerm(String term, String n) {
		return "(((ATermAppl)" + term + ").getArgument(" + n + "))";
	}

	protected String ATermEquals(String s1, String s2) {
		return s1 + ".equals(" + s2 + ")";
	}

	protected String TypeName(String type) {
		return buildTypeName(type);
	}

	protected String TypeImpl(String type) {
		return type;
	}

	protected String TypeGetFunSym(String arg) {
		return "null";
	}

	protected String TypeCmpFunSym(String arg1, String arg2) {
		return "false";
	}

	protected String TypeGetSubTerm(String term, String n) {
		return "null";
	}

	protected String TypeEquals(String arg1, String arg2) {
		return arg1 + ".equals(" + arg2 + ")";
	}

	protected String OperatorName(String id) {
		return buildId(id);
	}

	protected String OperatorType(String type, String id) {
		return buildAltTypeName(type, id);
	}

	protected String OperatorFSym(String type, String alt) {
		return "";
	}

	protected String OperatorIsFSym(String term, String type, String alt) {
		return "("
			+ term
			+ "!= null) &&"
			+ term
			+ ".is"
			+ capitalize(buildAltTypeName(type, alt) + "()");
	}

	protected String OperatorGetSlot(String term, String slot) {
		return term + ".get" + capitalize(slot) + "()";
	}

	protected String OperatorMake(String type, String alt, String arguments) {
		return "get"
			+ capitalize(buildId(api_name))
			+ "Factory"
			+ "().make"
			+ buildAltTypeName(type, alt)
			+ arguments;
	}

	protected String FieldName(String id) {
		return buildId(id);
	}

	protected String FieldType(String type) {
		return buildTypeName(type);
	}
}
