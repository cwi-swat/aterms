package apigen.gen.c;

import apigen.gen.TypeConversions;

public class CTypeConversions implements TypeConversions {
    private String prefix;

    public CTypeConversions(String prefix) {
        this.prefix = prefix;
    }

    public String getIntegerType() {
        return "int";
    }

    public String getRealType() {
        return "float";
    }

    public String getTermType() {
        return "ATerm";
    }

    public String getListType() {
        return "ATermList";
    }

    public String getStringType() {
        return "char*";
    }

    public String getCharsType() {
        return "char*";
    }

    public String makeIntegerToATermConversion(String expression) {
        return "(ATerm) ATmakeInt(" + expression + ")";
    }

    public String makeRealToATermConversion(String expression) {
        return "(ATerm) ATmakeReal(" + expression + ")";
    }

    public String makeStringToATermConversion(String expression) {
        return "(ATerm) ATmakeAppl(ATmakeAFun(" + expression + ", 0, ATfalse))";
    }

    public String makeListToATermConversion(String expression) {
        return "(ATerm) " + expression;
    }

    public String makeCharsToATermConversion(String expression) {
        return prefix + "stringToChars(" + expression + ")";
    }

    public String makeATermToIntegerConversion(String expression) {
        return "ATgetInt((ATermInt) " + expression + ")";
    }

    public String makeATermToRealConversion(String expression) {
        return "ATgetReal((ATermReal) " + expression + ")";
    }

    public String makeATermToStringConversion(String expression) {
        return "ATgetName(ATgetAFun((ATermAppl) " + expression + "))";
    }

    public String makeATermToListConversion(String expression) {
        return "(ATermList) " + expression;
    }

    public String makeATermToCharsConversion(String expression) {
        return prefix + "charsToString((ATerm)" + expression + ")";
    }
}
