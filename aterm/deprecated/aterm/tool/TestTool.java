package aterm.tool;

import aterm.*;
import java.net.*;
import java.io.*;

public class TestTool extends Tool
{
  public TestTool() throws UnknownHostException { 
    super("testing");
  }

  public TestTool(String[] args) throws UnknownHostException {
    super(args);
  }

  public void run() {
    try {
      System.out.println("connecting to ToolBus...");
      connect();
      System.out.println("connected!");
      super.run();
    } catch (IOException e) {
      System.err.println("IOException caught: " + e.toString());
    } catch (ToolException e) {
      System.err.println("ToolException caught: " + e.toString());
    }
  }

  protected void idle() {
    System.out.println("idle called");
    super.idle();
  }

  protected ATerm handler(ATerm term) 
	{
    System.out.println("handler called: ");
    term.print(System.out);
    System.out.println("");
    return null;
  }

  protected void checkInputSignature(ATermList sig) {
    System.out.println("checkInputSignature called: ");
    sig.print(System.out);
    System.out.println("");
  }
}

