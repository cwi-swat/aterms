package toolbus;

import java.io.*;

import aterm.*;

public class Testing
  implements TestingTif
{
  private ATermFactory factory;

  public static final void main(String[] args)
    throws IOException
  {
    ATermFactory factory = new aterm.pure.PureFactory();
    Testing testing = new Testing(factory);
    TestingBridge bridge = new TestingBridge(factory, testing);
    bridge.init(args);
    bridge.connect();
    bridge.run();
  }

  public Testing(ATermFactory factory)
  {
    this.factory = factory;
  }

  public void listtest(ATermList l0)
  {
    System.out.println("listtest: " + l0);
  }

  public void testit(String s0)
  {
    System.out.println("testit: " + s0);
  }

  public void testit(String s0, int i1)
  {
    System.out.println("testit (s,i): " + s0 + "," + i1);
  }

  public void testit(String s0, ATerm t1)
  {
    System.out.println("testit (s,t): " + s0 + "," + t1);
  }

  public ATerm question(ATerm t0)
  {
    System.out.println("question: " + t0);
    //return factory.parse("snd-value(answer(f([4], 3.2){[label,val]}))");
    return factory.parse("snd-value(answer(f([4], 3.2)))");
  }

  public void recAckEvent(ATerm t0)
  {
    System.out.println("recAckEvent: " + t0);
  }

  public void recTerminate(ATerm t0)
  {
    System.out.println("recTerminate: " + t0);
  }
}

