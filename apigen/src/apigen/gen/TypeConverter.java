package apigen.gen;

import java.util.HashMap;
import java.util.Map;

public class TypeConverter implements TypeConversions {
    public static final String LIST_TYPE = "list";
    public static final String TERM_TYPE = "term";
    public static final String STR_TYPE = "str";
    public static final String REAL_TYPE = "real";
    public static final String INT_TYPE = "int";
    private Map reservedTypes;
    private TypeConversions converter;

    /**
     * Create a new TypeConverter
     * 
     * @param conv
     *            The mapping of builtin (ATerm) types to target language types
     */
    public TypeConverter(TypeConversions conv) {
        reservedTypes = new HashMap();

        reservedTypes.put(INT_TYPE, conv.getIntegerType());
        reservedTypes.put(REAL_TYPE, conv.getRealType());
        reservedTypes.put(STR_TYPE, conv.getStringType());
        reservedTypes.put(TERM_TYPE, conv.getTermType());
        reservedTypes.put(LIST_TYPE, conv.getListType());

        this.converter = conv;
    }

    /**
     * Tests whether a type-name is reserved
     *  
     */
    public boolean isReserved(String t) {
        return reservedTypes.containsKey(t);
    }

    /**
     * Transforms reserved type to their target implementation and leaves other
     * types alone.
     *  
     */
    public String getType(String t) {
        if (isReserved(t)) {
            return (String) reservedTypes.get(t);
        } else {
            return t;
        }
    }

    /**
     * Returns the implementation type of: int
     */
    public String getIntegerType() {
        return (String) reservedTypes.get(INT_TYPE);
    }

    /**
     * Returns the implementation type of: real
     */
    public String getRealType() {
        return (String) reservedTypes.get(REAL_TYPE);
    }

    /**
     * Returns the implementation type of: str
     */
    public String getStringType() {
        return (String) reservedTypes.get(STR_TYPE);
    }

    /**
     * Returns the implementation type of: term
     */
    public String getTermType() {
        return (String) reservedTypes.get(TERM_TYPE);
    }

    /**
     * Returns the implementation type of: list
     */
    public String getListType() {
        return (String) reservedTypes.get(LIST_TYPE);
    }

    /**
     * Builds conversion code from builtin types to ATerms
     * 
     * @param type the type of the expression to be converted
     * @param expression the expression to be converted
     * @return an implementation of a conversion
     */
    public String makeToATermConversion(String type, String expression) {
        if (INT_TYPE.equals(type)) {
            return makeIntegerToATermConversion(expression);
        } else if (REAL_TYPE.equals(type)) {
            return makeRealToATermConversion(expression);
        } else if (STR_TYPE.equals(type)) {
            return makeStringToATermConversion(expression);
        } else if (LIST_TYPE.equals(type)) {
            return makeListToATermConversion(expression);
        } else if (TERM_TYPE.equals(type)) {
            return expression;
        } else {
            return expression;
        }
    }

    public String makeListToATermConversion(String expression) {
        return converter.makeListToATermConversion(expression);
    }

    public String makeStringToATermConversion(String expression) {
        return converter.makeStringToATermConversion(expression);
    }

    public String makeIntegerToATermConversion(String expression) {
        return converter.makeIntegerToATermConversion(expression);
    }

    public String makeRealToATermConversion(String expression) {
        return converter.makeRealToATermConversion(expression);
    }

    public String makeATermToStringConversion(String expression) {
        return converter.makeATermToStringConversion(expression);
    }

    public String makeATermToIntegerConversion(String expression) {
        return converter.makeATermToIntegerConversion(expression);
    }

    public String makeATermToRealConversion(String expression) {
        return converter.makeATermToRealConversion(expression);
    }

    /**
     * Builds conversion code from ATerms to builtin types
     * 
     * @param type the type of the resulting expression
     * @param expression the expression to be converted
     * @return an implementation of a conversion
     */
    public String makeATermToBuiltinConversion(String type, String expression) {
        if (INT_TYPE.equals(type)) {
            return makeATermToIntegerConversion(expression);
        } else if (REAL_TYPE.equals(type)) {
            return makeATermToRealConversion(expression);
        } else if (STR_TYPE.equals(type)) {
            return makeATermToStringConversion(expression);
        } else if (LIST_TYPE.equals(type)) {
            return makeATermToListConversion(expression);
        } else if (TERM_TYPE.equals(type)){
            return expression;
        } else {
            return expression;
        }
    }

    public String makeATermToListConversion(String expression) {
        return converter.makeATermToListConversion(expression);
    }

    public String makeBuiltinToATermConversion(String type, String expression) {
        if (INT_TYPE.equals(type)) {
            return makeIntegerToATermConversion(expression);
        } else if (REAL_TYPE.equals(type)) {
            return makeRealToATermConversion(expression);
        } else if (STR_TYPE.equals(type)) {
            return makeStringToATermConversion(expression);
        } else if (LIST_TYPE.equals(type)) {
            return makeListToATermConversion(expression);
        } else if (TERM_TYPE.equals(type)) {
            return expression;
        } else {
            return expression;
        }
    }

    
}
