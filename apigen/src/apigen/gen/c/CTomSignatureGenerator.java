package apigen.gen.c;

import apigen.gen.TomSignatureGenerator;

public class CTomSignatureGenerator extends TomSignatureGenerator {
	
	protected void initTypeConverter() {
		typeConverter = new CTypeConverter();
	}
	
	private String buildTypeName(String type) {
		return converter.makeCapitalizedIdentifier(type);
	}

	private String buildAltTypeName(String type, String alt) {
		return converter.capitalize(type + converter.capitalize(alt));
	}

	protected String StringImpl() {
		return "char*";
	}

	protected String StringGetFunSym(String arg1) {
		return arg1;
	}

	protected String StringGetSubTerm(String term, String n) {
		return "NULL";
	}

	protected String StringCmpFunSym(String s1, String s2) {
		return "!strcmp(" + s1 + "," + s2 + ")";
	}

	protected String StringEquals(String s1, String s2) {
		return StringCmpFunSym(s1, s2);
	}

	protected String IntegerImpl() {
		return "int";
	}

	protected String IntegerGetFunSym(String arg1) {
		return arg1;
	}

	protected String IntegerGetSubTerm(String term, String n) {
		return "0";
	}

	protected String IntegerCmpFunSym(String s1, String s2) {
		return "(" + s1 + "==" + s2 + ")";
	}

	protected String IntegerEquals(String s1, String s2) {
		return IntegerCmpFunSym(s1, s2);
	}

	protected String DoubleImpl() {
		return "float";
	}

	protected String DoubleGetFunSym(String arg1) {
		return arg1;
	}

	protected String DoubleCmpFunSym(String s1, String s2) {
		return "(" + s1 + "==" + s2 + ")";
	}

	protected String DoubleGetSubTerm(String term, String n) {
		return "0.0";
	}

	protected String DoubleEquals(String s1, String s2) {
		return DoubleCmpFunSym(s1, s2);
	}

	protected String ATermImpl() {
		return "ATerm";
	}

		protected String ATermGetFunSym(String arg) {
		return "((ATgetType("
			+ arg + ") == AT_APPL)?ATgetAFun((ATermAppl)"
			+ arg
			+ "):NULL)";
	}

	protected String ATermCmpFunSym(String s1, String s2) {
		return "ATisEqualAFun(" + s1 + "," + s2 + ")";
	}

	protected String ATermGetSubTerm(String term, String n) {
		return "ATgetArgument(((ATermAppl)" + term + ")," + n + ")";
	}

	protected String ATermEquals(String s1, String s2) {
		return "ATisEqual(" + s1 + "," + s2 + ")";
	}

	protected String TypeName(String type) {
		return buildTypeName(type);
	}

	protected String TypeImpl(String type) {
		return type;
	}

	protected String TypeGetFunSym(String arg) {
		return "NULL";
	}

	protected String TypeCmpFunSym(String arg1, String arg2) {
		return "!(0==0)";
	}

	protected String TypeGetSubTerm(String term, String n) {
		return "NULL";
	}

	protected String TypeEquals(String type, String arg1, String arg2) {
		return "isEqual" + buildTypeName(type)
                  + "(" + arg1 + "," + arg2 + ")";
	}

	protected String OperatorName(String id) {
		return converter.makeIdentifier(id);
	}

	protected String OperatorType(String type, String id) {
		return buildAltTypeName(type, id);
	}

	protected String OperatorFSym(String type, String alt) {
		return "";
	}

	protected String OperatorIsFSym(String term, String type, String alt) {
                return "is"
                  + converter.capitalize(buildAltTypeName(type, alt)
                  + "(" + term + ")");
	}

	protected String OperatorGetSlot(String term, String type, String slot) {
		return "get" + converter.capitalize(buildAltTypeName(type,slot)) + "(" + term + ")";
	}

	protected String OperatorMake(String type, String alt, String arguments) {
		return "make"
			+ buildAltTypeName(type, alt)
			+ arguments;
	}

	protected String FieldName(String id) {
		return converter.makeIdentifier(id);
	}

	protected String FieldType(String type) {
		return buildTypeName(type);
	}
}
