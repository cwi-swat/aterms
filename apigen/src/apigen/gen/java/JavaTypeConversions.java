package apigen.gen.java;

import apigen.gen.TypeConversions;

public class JavaTypeConversions implements TypeConversions {
    private String atermFactory;
    private String factory;

    public JavaTypeConversions(String atermFactory, String factory) {
        this.atermFactory = atermFactory;
        this.factory = factory;
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
    
    
    public String makeIntegerToATermConversion(String expression) {
        return "(aterm.ATerm) " + atermFactory + ".makeInt(" + expression + ")";
    }

    
    public String makeRealToATermConversion(String expression) {
        return "(aterm.ATerm) " + atermFactory + ".makeReal(" + expression + ")";
    }

    
    public String makeStringToATermConversion(String expression) {
        return "(aterm.ATerm) " + atermFactory + ".makeAppl(factory.makeAFun(" + expression + ", 0, false))";
    }

    public String makeListToATermConversion(String expression) {
        return "(aterm.ATermList) " + expression;
    }

    public String makeCharsToATermConversion(String expression) {
        return "stringToChars(" + expression + ")";
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
}