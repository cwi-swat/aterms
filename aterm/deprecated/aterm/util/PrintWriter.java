package aterm.util;

import java.io.*;

public class PrintWriter extends Writer
{
  public PrintWriter(Writer w) { super(w.getPrintStream()); }
  public PrintWriter(OutputStream o) { super(new PrintStream(o)); }
  public void print(int val)   { getPrintStream().print(""+val); }
  public void println(int val)   { getPrintStream().println(""+val); }
  public void print(char val)   { getPrintStream().print(""+val); }
  public void println(char val)   { getPrintStream().println(""+val); }
  public void print(Double val){ getPrintStream().print(""+val); }
  public void println(Double val){ getPrintStream().println(""+val); }
  public void print(String s)  { getPrintStream().print(s); }
  public void println(String s)  { getPrintStream().println(s); }
  public void flush() { try { super.flush(); } catch (IOException e) {} }
}






