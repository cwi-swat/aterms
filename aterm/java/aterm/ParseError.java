package aterm;

import java.io.*;

/**
 * A ParseError is thrown when an error occurs during the
 * parsing of a term.
 * Note that ParseError is a RuntimeException, so it can
 * be ignored if a parse error can only occur by a bug in
 * your program.
 * 
 * @author Hayco de Jong (jong@cwi.nl)
 * @author Pieter Olivier (olivierp@cwi.nl)
 * @version 0.1, Thu Jan 27 15:45:52 MET 2000
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
