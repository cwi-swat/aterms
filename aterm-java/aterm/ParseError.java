package aterm;

/**
 * A ParseError is thrown when an error occurs during the
 * parsing of a term.
 * Note that ParseError is a RuntimeException, so it can
 * be ignored if a parse error can only occur by a bug in
 * your program.
 * 
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 */
public class ParseError extends RuntimeException {

  /**
   * Constructs a ParseError given a description of the error
   *
   * @param msg the error message describing the parse error.
   */
  public ParseError(String msg) {
    super(msg);
  }

}
