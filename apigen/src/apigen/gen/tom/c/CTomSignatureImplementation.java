package apigen.gen.tom.c;

import apigen.gen.StringConversions;
import apigen.gen.TypeConverter;
import apigen.gen.c.CTypeConversions;
import apigen.gen.tom.TomGenerationParameters;
import apigen.gen.tom.TomSignatureImplementation;

public class CTomSignatureImplementation implements TomSignatureImplementation {
	private static TypeConverter converter;
	private boolean jtype;
	private String prefix;

	public CTomSignatureImplementation(TomGenerationParameters params) {
		this.prefix = params.getPrefix();
		this.jtype = params.isJtype();
	}

	static {
		converter = new TypeConverter(new CTypeConversions());
	}

	private String buildTypeName(String type) {
		return StringConversions.makeCapitalizedIdentifier(type);
	}

	private String buildAltTypeName(String type, String alt) {
		return StringConversions.capitalize(type + StringConversions.capitalize(alt));
	}

	public String StringName() {
		return "Str";
	}

	public String StringImpl() {
		return converter.getStringType();
	}

	public String StringGetFunSym(String arg1) {
		return arg1;
	}

	public String StringGetSubTerm(String term, String n) {
		return "NULL";
	}

	public String StringCmpFunSym(String s1, String s2) {
		return "!strcmp(" + s1 + "," + s2 + ")";
	}

	public String StringEquals(String s1, String s2) {
		return StringCmpFunSym(s1, s2);
	}

	public String IntegerName() {
		return "Int";
	}

	public String IntegerImpl() {
		return converter.getIntegerType();
	}

	public String IntegerGetFunSym(String arg1) {
		return arg1;
	}

	public String IntegerGetSubTerm(String term, String n) {
		return "0";
	}

	public String IntegerCmpFunSym(String s1, String s2) {
		return "(" + s1 + "==" + s2 + ")";
	}

	public String IntegerEquals(String s1, String s2) {
		return IntegerCmpFunSym(s1, s2);
	}

	public String DoubleName() {
		return "Double";
	}

	public String DoubleImpl() {
		return converter.getRealType();
	}

	public String DoubleGetFunSym(String arg1) {
		return arg1;
	}

	public String DoubleCmpFunSym(String s1, String s2) {
		return "(" + s1 + "==" + s2 + ")";
	}

	public String DoubleGetSubTerm(String term, String n) {
		return "0.0";
	}

	public String DoubleEquals(String s1, String s2) {
		return DoubleCmpFunSym(s1, s2);
	}

	public String ATermName() {
		return "ATerm";
	}

	public String ATermImpl() {
		return converter.getTermType();
	}

	public String ATermGetFunSym(String arg) {
		return "((ATgetType(" + arg + ") == AT_APPL)?ATgetAFun((ATermAppl)" + arg + "):NULL)";
	}

	public String ATermCmpFunSym(String s1, String s2) {
		return "ATisEqualAFun(" + s1 + "," + s2 + ")";
	}

	public String ATermGetSubTerm(String term, String n) {
		return "ATgetArgument(((ATermAppl)" + term + ")," + n + ")";
	}

	public String ATermEquals(String s1, String s2) {
		return "ATisEqual(" + s1 + "," + s2 + ")";
	}

	public String ATermListName() {
		return "ATermList";
	}

	public String ATermListImpl() {
		return converter.getTermType();
	}

	public String ATermListGetFunSym(String arg) {
		return "((ATgetType(" + arg + ") == AT_LIST)?ATmakeAFun(\"conc\",1,ATfalse)):NULL)";
	}

	public String ATermListCmpFunSym(String s1, String s2) {
		return "ATisEqualAFun(" + s1 + "," + s2 + ")";
	}

	public String ATermListEquals(String s1, String s2) {
		return "ATisEqual(" + s1 + "," + s2 + ")";
	}

	public String TypeName(String type) {
		return buildTypeName(type);
	}

	public String TypeImpl(String type) {
		return type;
	}

	public String TypeGetFunSym(String arg) {
		return "NULL";
	}

	public String TypeCmpFunSym(String arg1, String arg2) {
		return "!(0==0)";
	}

	public String TypeGetSubTerm(String term, String n) {
		return "NULL";
	}

	public String TypeEquals(String type, String arg1, String arg2) {
		return prefix + "isEqual" + buildTypeName(type) + "(" + arg1 + "," + arg2 + ")";
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

	public String OperatorFSym(String type, String alt) {
		return "";
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
