package aterm;

/**
 * An AFun represents a function symbol
 *
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 */
public interface AFun extends ATerm
{
  /**
   * Gets the name of the function symbol
   *
   * @return the name of this function symbol.
   */
  String getName();

  /**
   * Gets the arity of this application. Arity is the number
   * of arguments of a function application.
   *
   * @return the number of arguments that applications of this
   * function symbol have.
   */
  public int getArity();

  /**
   * Checks if this application is quoted. A quoted application looks
   * like this: "foo", whereas an unquoted looks like this: foo.
   *
   * @return true if this application is quoted, false otherwise.
   */
  public boolean isQuoted();
}
