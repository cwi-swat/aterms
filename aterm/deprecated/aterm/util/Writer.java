package aterm.util;

import java.io.*;

public class Writer
{
  PrintStream pstream;

  protected Writer() { pstream = null; }
  protected void setStream(PrintStream ps) { pstream = ps; }
  public Writer(PrintStream ps) { pstream = ps; }
  public PrintStream getPrintStream() { return pstream; }
  public void flush() throws IOException { getPrintStream().flush(); }
  public void close() { getPrintStream().close(); }
  public void write(int i) throws IOException { getPrintStream().write(i); }
}
