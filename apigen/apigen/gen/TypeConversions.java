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
}
