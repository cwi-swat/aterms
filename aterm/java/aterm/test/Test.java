package aterm.test;

import aterm.*;
import aterm.pure.*;

import java.util.*;
import java.io.*;

public class Test
{
  ATermFactory factory;

  //{{{ public final static void main(String[] args)

  public final static void main(String[] args)
  {
    Test pureSuite = new Test(new PureFactory());
    pureSuite.testAll();
    //Test nativeSuite = new Test(new NativeFactory());
    //nativeSuite.testAll();
  }

  //}}}
  //{{{ public Test(ATermFactory factory)

  public Test(ATermFactory factory)
  {
    this.factory = factory;
  }

  //}}}

  //{{{ void assert(boolean condition)

  void assert(boolean condition)
  {
    if(!condition) {
      throw new RuntimeException("assertion failed.");
    }
  }

  //}}}

  //{{{ public void testMakeInt()

  public void testMakeInt()
  {
    ATermInt[] term = new ATermInt[2];

    term[0] = factory.makeInt(3);
    term[1] = factory.makeInt(3);

    assert(term[0].getType() == ATerm.INT);
    assert(term[0].getInt() == 3);
    assert(term[0] == term[1]);

    assert(term[0].toString().equals("3"));

    List result;

    result = term[0].match("3");
    assert(result != null);
    assert(result.size() == 0);

    result = term[0].match("<int>");
    assert(result != null);
    assert(result.size() == 1);

    System.out.println("pass: testMakeInt");
  }

  //}}}
  //{{{ public void testMakeReal()

  public void testMakeReal()
  {
    ATermReal[] term = new ATermReal[2];

    term[0] = factory.makeReal(Math.PI);
    term[1] = factory.makeReal(Math.PI);

    assert(term[0].getType() == ATerm.REAL);
    assert(term[0].getReal() == Math.PI);
    assert(term[0] == term[1]);

    List result;

    result = term[0].match("<real>");
    assert(result != null);
    assert(result.size() == 1);
    assert(result.get(0).equals(new Double(Math.PI)));

    System.out.println("pass: testMakeReal");
  }

  //}}}
  //{{{ public void testMakeAppl()

  public void testMakeAppl()
  {
    AFun[] symmies = new AFun[4];
    ATermAppl[] apples = new ATermAppl[16];

    symmies[0] = factory.makeAFun("f0", 0, false);
    symmies[1] = factory.makeAFun("f1", 1, false);
    symmies[2] = factory.makeAFun("f6", 6, false);
    symmies[3] = factory.makeAFun("f10", 10, false);

    apples[0] = factory.makeAppl(symmies[0]);
    apples[1] = factory.makeAppl(symmies[1], (ATerm)apples[0]);
    apples[2] = factory.makeAppl(symmies[1], (ATerm)apples[1]);
    apples[3] = factory.makeAppl(symmies[1], (ATerm)apples[0]);
    apples[4] = factory.makeAppl(symmies[2], (ATerm)apples[0], (ATerm)apples[0], 
				 (ATerm)apples[1], (ATerm)apples[0], 
				 (ATerm)apples[0], (ATerm)apples[1]);
    ATerm[] args = { apples[0], apples[1], apples[0],
		     apples[1], apples[0], apples[1], apples[0],
		     apples[1], apples[0], apples[1] };
    apples[5] = factory.makeAppl(symmies[3], args);
    apples[6] = apples[2].setArgument((ATerm)apples[0], 0);

    assert(apples[6].equals(apples[1]));
    assert(apples[1].equals(apples[3]));
    assert(!apples[2].equals(apples[1]));
    assert(!apples[2].equals(apples[6]));
    assert(!apples[1].equals(apples[2]));
    assert(!apples[2].equals(apples[3]));
    assert(!apples[0].equals(apples[1]));

    System.out.println("application tests ok.\n");
  }

  //}}}
  //{{{ public void testDict()

  public void testDict()
  {
    ATermList dict = factory.makeList();
    ATerm key, value;

    for (int i=0; i<5; i++) {
      key = factory.parse("key" + i);
      value = factory.parse("value" + i);
      dict = dict.dictPut(key, value);
    }

    key = factory.parse("key3");
    value = factory.parse("value3");
    assert(dict.dictGet(key).equals(value));
  }

  //}}}
  //{{{ public void testAnnos()

  public void testAnnos()
  {
    ATerm t, key, value;
    ATermList annos;

    t = factory.parse("f");
    for (int i=0; i<5; i++) {
      key = factory.parse("key" + i);
      value = factory.parse("value" + i);
      t = t.setAnnotation(key, value);
    }

    key = factory.parse("key3");
    value = factory.parse("value3");
    assert(t.getAnnotation(key).equals(value));
    t = t.removeAnnotation(key);
    assert(t.getAnnotation(key) == null);
  }

  //}}}
  //{{{ public void testParser()

  public void testParser()
  {
    ATerm t = factory.parse("f");
    t = factory.parse("f(1)");
    t = factory.parse("f(1,2)");
    t = factory.parse("[]");
    t = factory.parse("[1]");
    t = factory.parse("[1,2]");
    t = factory.parse("<x>");
    t = factory.parse("3.14");
    t = factory.parse("f(\"x y z\"(),<abc(31)>,[])");
    t = factory.parse("home([<name(\"\",String)>,<phone(\"\",PhoneNumber)>])");
    t = factory.parse("[ a , b ]");
    t = factory.parse("f(a){[x,y],[1,2]}");
  }

  //}}}
  //{{{ public void testList()

  public void testList()
  {
    ATermList list = (ATermList)factory.parse("[1,2,3]");
    ATermList result = list.remove(factory.parse("2"));
    assert(result.equals(factory.parse("[1,3]")));
    
    list = (ATermList)factory.parse("[1,2,3]");
    result = list.replace(factory.parse("99"), 1);
    assert(result.equals(factory.parse("[1,99,3]")));
  }

  //}}}

  
  //{{{ public void testAll()

  public void testAll()
  {
    testMakeInt();
    testMakeReal();
    testMakeAppl();
    testDict();
    testAnnos();
    testParser();
    testList();
    /*testMakeList();
    testMakePlaceholder();
    testMakeBlob();
    */
  }

  //}}}
}
