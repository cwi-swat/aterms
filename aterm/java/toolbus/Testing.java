package toolbus;

import java.io.*;

import aterm.*;

public class Testing
  extends TestTifs
{
  public static final void main(String[] args)
    throws IOException
  {
    ATermFactory factory = new aterm.pure.PureFactory();
    Testing tool = new Testing(factory);
    tool.init(args);
    tool.connect();
    tool.run();
  }

  public Testing(ATermFactory factory)
  {
    super(factory);
  }

  void listtest(ATermList l0)
  {
    info("listtest: " + l0);
  }

  void testit(String s0)
  {
    info("testit: " + s0);
  }

  void testit(String s0, int i1)
  {
    info("testit (s,i): " + s0 + "," + i1);
  }

  void testit(String s0, ATerm t1)
  {
    info("testit (s,t): " + s0 + "," + t1);
  }

  ATerm question(ATerm t0)
  {
    info("question: " + t0);
    //return factory.parse("snd-value(answer(f([4], 3.2){[label,val]}))");
    return factory.parse("snd-value(answer(f([4], 3.2)))");
  }

  void recAckEvent(ATerm t0)
  {
    info("recAckEvent: " + t0);
  }

  void recTerminate(ATerm t0)
  {
    info("recTerminate: " + t0);
  }
}

