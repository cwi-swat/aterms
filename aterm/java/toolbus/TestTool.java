package toolbus;

import aterm.*;
import aterm.pure.*;
import java.net.*;
import java.io.*;

public class TestTool extends AbstractTool
{
  public static final void main(String[] args)
    throws UnknownHostException
  {
    TestTool tool = new TestTool(args);
    tool.run();
  }

  //{{{ public TestTool()

  public TestTool()
  { 
    super(new PureFactory());
  }

  //}}}
  //{{{ public TestTool(String[] args)

  public TestTool(String[] args)
    throws UnknownHostException
  {
    this();
    init(args);
  }

  //}}}

  public void run()
  {
    try {
      System.out.println("connecting to ToolBus...");
      connect();
      System.out.println("connected!");
      super.run();
    } catch (IOException e) {
      System.err.println("IOException caught: " + e.toString());
    }
  }

  public ATerm handler(ATerm term) 
  {
    System.out.println("handler called: " + term);
    return null;
  }

  public void checkInputSignature(ATermList sig)
  {
    System.out.println("checkInputSignature called: " + sig);
  }
}

