package aterm;

import java.io.*;

/**
  * ParseError is thrown when a an error occurs during the
  * parsing of a term.
  * Note that ParseError is a RuntimeException,
  * so it can be ignored when a parse error can only be caused
  * by a bug in your program.
  * @see java.lang.RuntimeException
  */

public class ParseError extends RuntimeException
{
  private ATermChannel channel;
  private char last;
  private String error;

  protected ParseError(ATermChannel c, int lst, String err) {
    super(explanation(c, (char)lst, err));
    channel = c;
    last = (char)lst;
    error = err;
  }

  protected ParseError(String msg) {
    super(msg);
  }

  static private String explanation(ATermChannel c, char lst, String err) {
    StringBuffer str = new StringBuffer();

    str.append(err);
    if(c == null)
      str.append(" at the end of the input.");
    else {
      str.append("\ninput left (max 40 chars.): ");
      int ch;
      int i = 40;
      str.append('\'');
      if(lst != (char)-1)
        str.append(lst);
      try {
        while((ch = c.read()) != -1 && i-- > 0) {
          str.append((int)ch);
        }
      } catch (IOException e) {
        str.append("*read error*");
      }
      str.append('\'');
    }
    str.append('\n');
    return str.toString();
  }
}
