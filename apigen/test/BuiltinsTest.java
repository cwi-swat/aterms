package test;

import test.builtins.*;
import aterm.pure.PureFactory;

public class BuiltinsTest
{

  private BuiltinsFactory factory;
  static private String directory;

  public BuiltinsTest(BuiltinsFactory factory) {
    this.factory = factory;
  }

  public void run() throws java.io.IOException {
  
      D t = factory.makeD_Trm(factory.getPureFactory().parse("one"));
      testAssert(t.toString().equals("term(one)"), "make term test");
      
      D d = factory.makeD_Double(1.0);
      testAssert(d.toString().equals("double(1.0)"),"make double test");
      testAssert(d.getNumber() == 1.0, "get double test");
      testAssert(d.setNumber(2.0).getNumber() == 2.0, "set double test");
      
      D i = factory.makeD_Integer(1);
      testAssert(i.toString().equals("int(1)"),"make int test");
      testAssert(i.getInteger() == 1, "get integer test");
      testAssert(i.setInteger(2).getInteger() == 2, "set integer test");
      
      D l = factory.makeD_Lst((aterm.ATermList) factory.getPureFactory().parse("[one]"));
      testAssert(l.toString().equals("list([one])"), "make list test");
      
      D s = factory.makeD_String("one");
      testAssert(s.toString().equals("str(\"one\")"), "make str test");
  }

  public final static void main(String[] args) throws jjtraveler.VisitFailure,
  java.io.IOException
  {
    BuiltinsTest test = new BuiltinsTest (new BuiltinsFactory(new PureFactory())); 
    directory = args[0];

    test.run();
    return;
  }

  void testAssert(boolean b, String name) {
    if (!b) {
      throw new RuntimeException("Test " + name + " failed!");
    }
  }
}
