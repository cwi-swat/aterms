
package apigen.gen;

public interface TypeConversions {
	/**
	 * 
	 * @return The target implementation type of the builtin ATerm type: int
	 */
  public String IntegerType();
  
  /**
   * 
   * @return The target implementation type of the builtin ATerm type: real
   */
  public String RealType();
  
  /**
   * 
   * @return The target implementation type of the builtin ATerm type: term
   */
  public String TermType();
  
  /**
   * 
   * @return The target implementation type of the builtin ATerm type: str
   */
  public String StringType();

    /**
   * 
   * @return The target implementation type of the builtin ATermList type: list
   */
  public String ListType();
}
