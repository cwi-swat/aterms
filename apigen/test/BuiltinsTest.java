package test;

import test.builtins.*;

public class BuiltinsTest
{

  private BuiltinsFactory factory;
  static private String directory;

  public BuiltinsTest(BuiltinsFactory factory) {
    this.factory = factory;
  }

  public void run() throws java.io.IOException {
  
      T t = factory.makeT_Trm(factory.parse("one"));
      testAssert(t.toString().equals("term(one)"), "make term test");
      
      D d = factory.makeD_Double(new Double(1.0));
      testAssert(d.toString().equals("double(1.0)"),"make double test");
      
      I i = factory.makeI_Integer(new Integer(1));
      testAssert(i.toString().equals("int(1)"),"make int test");
      
      L l = factory.makeL_Lst((aterm.ATermList) factory.parse("[one]"));
      testAssert(l.toString().equals("list([one])"), "make list test");
      
      S s = factory.makeS_String("one");
      testAssert(s.toString().equals("str(\"one\")"), "make str test");
  }

  public final static void main(String[] args) throws jjtraveler.VisitFailure,
  java.io.IOException
  {
    BuiltinsTest test = new BuiltinsTest (new BuiltinsFactory()); 
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
