package aterm;

/**
 * An ATermAppl represents a function application.
 * 
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 * @version 0.1, Fri Jan 28 10:19:58 MET 2000
 */
public interface ATermAppl extends ATerm {

  /**
   * Gets the AFun object that represents the function symbol of this application
   *
   * @return the function symbol of this application.
   *
   */
  public AFun getAFun();

  /**
   * Gets the function name of this application.
   *
   * @return the function name of this application.
   *
   */
  public String getName();

  /**
   * Gets the arguments of this application.
   *
   * @return a list containing all arguments of this application.
   */
  public ATermList getArguments();

  /**
   * Gets the arguments of this application as an array of ATerm objects.
   *
   * @return an array containing all arguments of this application.
   *
   */

  public ATerm[] getArgumentArray();

  /**
   * Gets a specific argument of this application.
   *
   * @param i the index of the argument to be retrieved.
   *
   * @return the ith argument of the application.
   */
  public ATerm getArgument(int i);

  /**
   * Sets a specific argument of this application.
   *
   * @param arg the new ith argument.
   * @param i the index of the argument to be set.
   *
   * @return a copy of this application with argument i replaced by arg.
   */
  public ATermAppl setArgument(ATerm arg, int i);

  /**
   * Checks if this application is quoted. A quoted application looks
   * like this: "foo", whereas an unquoted looks like this: foo.
   *
   * @return true if this application is quoted, false otherwise.
   */
  public boolean isQuoted();

  /**
   * Gets the arity of this application. Arity is the number
   * of arguments of a function application.
   *
   * @return the number of arguments of this application.
   */
  public int getArity();
}
