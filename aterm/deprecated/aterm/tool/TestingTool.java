
package aterm.tool;
import aterm.*;
import aterm.tool.*;
import aterm.tool.ToolException;
import java.net.*;
import java.io.*;

public class TestingTool extends Testing
{
  public TestingTool() throws UnknownHostException { super("testing"); }
  public TestingTool(String args[]) throws UnknownHostException {super(args);}
	
  void testit(String s0)
  {
    System.out.println("testit called: s0=" + s0);
  }

  void testit(String s0, int i1)
  {
    System.out.println("testit-2 called: s0=" + s0 + "i1=" + i1);
  }

  void testit(String s0, ATermAppl a1)
  {
    System.out.print("testit-3 called: s0=" + s0 + "a1=");
    a1.println(System.out);
  }

  ATerm question(ATermAppl a0)
  {
    ATerm R = null;
    System.out.print("question called: a0=");
    a0.println(System.out);
    try {
      R = ATerm.the_world.parse("snd-value(answer(f(4, 3.2)))");
    } catch (ParseError e) { System.err.println("parse failure!"); }
    return R;
  }

  void recTerminate(ATerm t0)
  {
    System.out.print("recTerminate called: ");
    t0.println(System.out);
  }

  void recAckEvent(ATerm t0)
  {
    System.out.print("recAckEvent called: ");
    t0.println(System.out);
  }

  public static void main(String[] args) 
    throws UnknownHostException, IOException, ToolException
  {
    TestingTool T = new TestingTool(args);
    T.connect();
    T.run();
  }
}

