package apigen.gen.java;

import apigen.gen.TypeConversions;

public class JavaTypeConversions implements TypeConversions {

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

    /**
     * assumption: there is a variable "factory" that points to 
     * an ATermFactory present in the context
     */
    public String makeIntegerToATermConversion(String expression) {
        return "(aterm.ATerm) factory.makeInt(" + expression + ")";
    }

    /**
     * assumption: there is a variable "factory" that points to 
     * an ATermFactory present in the context
     */
    public String makeRealToATermConversion(String expression) {
        return "(aterm.ATerm) factory.makeReal(" + expression + ")";
    }

    /**
     * assumption: there is a variable "factory" that points to 
     * an ATermFactory present in the context
     */
    public String makeStringToATermConversion(String expression) {
        return "(aterm.ATerm) factory.makeAppl(factory.makeAFun(" + expression + ", 0, false))";
    }

    public String makeListToATermConversion(String expression) {
        return "(aterm.ATermList) " + expression;
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
}