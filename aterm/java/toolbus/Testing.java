package toolbus;

import java.io.*;

import aterm.*;

public class Testing
  implements TestingTif
{
  private ATermFactory factory;
  private TestingBridge bridge;

  public static final void main(String[] args)
    throws IOException
  {
    Testing testing = new Testing(args);
  }

  public Testing(String[] args)
    throws IOException
  {
    factory = new aterm.pure.PureFactory();
    bridge = new TestingBridge(factory, this);
    bridge.init(args);
    bridge.connect();
    bridge.run();
  }

  public void listtest(ATerm l0)
  {
    System.out.println("listtest: " + l0);
    bridge.sendEvent(factory.parse("test-event(1{[key,val]})"));
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

  public void activeDisconnect()
  {
    try {
      bridge.sendTerm(factory.parse("snd-disconnect"));
    } catch (IOException e) {
      System.err.println("IOException received: " + e.getMessage());
    }
    System.exit(0);
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

