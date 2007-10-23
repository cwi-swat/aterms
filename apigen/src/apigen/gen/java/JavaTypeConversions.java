package apigen.gen.java;

import apigen.gen.TypeConversions;

public class JavaTypeConversions implements TypeConversions {
    private String factory;

    public JavaTypeConversions(String atermFactory) {
        this.factory = atermFactory;
    }
    
    public String getIntegerType() {
        return "int";
    }

    public String getRealType() {
        return "double";
    }

    public String getTermType() {
        return "aterm.ATerm";
    }

    public String getListType() {
        return "aterm.ATermList";
    }

    public String getStringType() {
        return "String";
    }
    
    public String getCharsType() {
        return "String";
    }

    public String getCharType() {
        return "char";
    }
    
    
    public String makeIntegerToATermConversion(String expression) {
        return factory + ".makeInt(" + expression + ")";
    }

    
    public String makeRealToATermConversion(String expression) {
        return factory + ".makeReal(" + expression + ")";
    }

    
    public String makeStringToATermConversion(String expression) {
        return factory + ".makeAppl(factory.makeAFun(" + expression + ", 0, true))";
    }

    public String makeListToATermConversion(String expression) {
        return "(aterm.ATermList) " + expression;
    }

    public String makeCharsToATermConversion(String expression) {
        return "stringToChars(" + expression + ")";
    }

    public String makeCharToATermConversion(String expression) {
        return "byteToChar(" + expression + ")";
    }
    
    public String makeATermToIntegerConversion(String expression) {
        return "((aterm.ATermInt) " + expression + ").getInt()";
    }

    public String makeATermToRealConversion(String expression) {
        return "((aterm.ATermReal) " + expression + ").getReal()";
    }

    public String makeATermToStringConversion(String expression) {
        return "((aterm.ATermAppl) " + expression + ").getAFun().getName()";
    }

    public String makeATermToListConversion(String expression) {
        return "(aterm.ATermList) " + expression;
    }

    public String makeATermToCharsConversion(String expression) {
        return "charsToString(" + expression + ")";
    }

    public String makeATermToCharConversion(String expression) {
        return "charToByte(" + expression + ")";
    }
}
