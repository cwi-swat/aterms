package aterm.util;
import java.io.*;

public class CharArrayWriter extends Writer
{
  ByteArrayOutputStream bastream;
  public CharArrayWriter() 
  {
    bastream = new ByteArrayOutputStream();
    setStream(new PrintStream(bastream));
  }
  public String toString()
  { 
    return bastream.toString();
  }
}
