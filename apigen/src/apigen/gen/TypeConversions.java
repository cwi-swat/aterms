package apigen.gen;

public interface TypeConversions {
    /**
     * @return The target implementation type of the builtin ATerm type: int
     */
    public String getIntegerType();

    /**
     * @return The target implementation type of the builtin ATerm type: real
     */
    public String getRealType();

    /**
     * @return The target implementation type of the builtin ATerm type: term
     */
    public String getTermType();

    /**
     * @return The target implementation type of the builtin ATerm type: str
     */
    public String getStringType();

    /**
     * @return The target implementation type of the builtin ATermList type
     */
    public String getListType();

    /**
     * @return The target implementation type of the builtin Chars type
     */
    public String getCharsType();
    
    /**
     * Build a conversion from a Integer to ATerm
     * 
     * @param expr  The expression that is input to the conversion
     * @return An implementation of a conversion from Integer to ATerm
     *
     */
    public String makeIntegerToATermConversion(String expression);

    /**
     * Build a conversion from a Real to ATerm
     * 
     * @param expr  The expression that is input to the conversion
     * @return An implementation of a conversion from Real to ATerm
     *
     */
    public String makeRealToATermConversion(String expression);

    /**
     * Build a conversion from an String to an ATerm
     * 
     * @param expr The expression that is input to the conversion
     * @return An implementation of a conversion from String to ATerm
     *
     */
    public String makeStringToATermConversion(String expression);

    /**
      * Build a conversion from a List to an ATerm (List is the builtin
      * ATerm datatype <list>)
      * 
      * @param expr The expression that is input to the conversion
      * @return An implementation of a conversion from a List to ATerm
      *
      */
    public String makeListToATermConversion(String expression);

    /**
     * Build a conversion from a Chars to an ATerm. A Chars is an
     * ATermList of integers representing characters in a string.
     * 
     * @see makeATermToStringConversion 
     * 
     * @param expr The expression that is input to the conversion
     * @return An implementation of a conversion from a Chars to ATerm
     *
     */
    public String makeCharsToATermConversion(String expression);
   
    
    /**
     * Build a conversion from a ATerm to Integer
     * 
     * @param expr  The expression that is input to the conversion
     * @return An implementation of a conversion from ATerm To Integer
     *
     */
    public String makeATermToIntegerConversion(String expression);

    /**
     * Build a conversion from a ATerm to Real
     * 
     * @param expr  The expression that is input to the conversion
     * @return An implementation of a conversion from ATerm to Real
     *
     */
    public String makeATermToRealConversion(String expression);

    /**
     * Build a conversion from an ATerm to an String
     * 
     * @param expr The expression that is input to the conversion
     * @return An implementation of a conversion from ATerm to String
     *
     */
    public String makeATermToStringConversion(String expression);

    /**
      * Build a conversion from an ATerm to a List (List is the builtin
      * ATerm datatype <list>)
      * 
      * @param expr The expression that is input to the conversion
      * @return An implementation of a conversion from a ATerm to a List
      *
      */
    public String makeATermToListConversion(String expression);

    
    /**
     * Build a conversion from an ATerm to a Chars. A Chars is an
     * ATermList of integers representing characters in a string.
     * 
     * @see makeATermToStringConversion 
     * 
     * @param expr The expression that is input to the conversion
     * @return An implementation of a conversion from a ATerm  to a Chars
     *
     */
    public String makeATermToCharsConversion(String expression);
}
