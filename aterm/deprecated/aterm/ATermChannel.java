package aterm;

import java.io.*;

abstract public class ATermChannel
{
  protected int lastChar;

  abstract public int read()
    throws IOException;

  public char last()
  { 
    return (char)lastChar; 
  }

  public char readNext() 
	throws IOException
  {
    do {
      read();
    } while(lastChar == ' ' || lastChar == '\t' || lastChar == '\n');
    return (char)lastChar;
  }

  public void skipWhitespace() 
	throws IOException
  {
    while(lastChar == ' ' || lastChar == '\t' || lastChar == '\n')
      read();
  }

  public int readOct()
	throws IOException, ParseError
  {
    int val = Character.digit((char)lastChar, 8);
    val += Character.digit((char)read(), 8);
    if(val < 0)
      throw new ParseError(this, lastChar, "octal must have 3 digits.");
    val += Character.digit((char)read(), 8);
    if(val < 0)
      throw new ParseError(this, lastChar, "octal must have 3 digits");
    return val;
  }

  // reset is called before a new term is being read.
  public void reset() throws IOException
  { 
  }
}


