package aterm.test;

import aterm.*;
import aterm.pure.*;

import java.util.*;

public class Test
{
  ATermFactory factory;

  //{ public final static void main(String[] args)

  public final static void main(String[] args)
  {
    Test pureSuite = new Test(new PureFactory());
    pureSuite.testAll();
    //Test nativeSuite = new Test(new NativeFactory());
    //nativeSuite.testAll();
  }

  //}
  //{ public Test(ATermFactory factory)

  public Test(ATermFactory factory)
  {
    this.factory = factory;
  }

  //}

  //{ void assert(boolean condition)

  void assert(boolean condition)
  {
    if(!condition) {
      throw new RuntimeException("assertion failed.");
    }
  }

  //}

  //{ public void testMakeInt()

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

  //}
  //{ public void testMakeReal()

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

  //}
  //{ public void testMakeAppl()

  public void testMakeAppl()
  {
    AFun symmies[4];
    ATermAppl apples[16];

    symmies[0] = factory.makeAFun("f0", 0, false);
    symmies[1] = factory.makeAFun("f1", 1, false);
    symmies[2] = factory.makeAFun("f6", 6, false);
    symmies[3] = factory.makeAFun("f10", 10, false);

    apples[0] = ATmakeAppl0(symmies[0]);
    apples[1] = ATmakeAppl1(symmies[1], (ATerm)apples[0]);
    apples[2] = ATmakeAppl1(symmies[1], (ATerm)apples[1]);
    apples[3] = ATmakeAppl1(symmies[1], (ATerm)apples[0]);
    apples[4] = ATmakeAppl6(symmies[2], (ATerm)apples[0], (ATerm)apples[0], 
			    (ATerm)apples[1], (ATerm)apples[0], 
			    (ATerm)apples[0], (ATerm)apples[1]);
    apples[5] = ATmakeAppl(symmies[3], apples[0], apples[1], apples[0],
			   apples[1], apples[0], apples[1], apples[0],
			   apples[1], apples[0], apples[1]);
    apples[6] = ATsetArgument(apples[2], (ATerm)apples[0], 0);

    assert(ATisEqual(apples[6], apples[1]));
    assert(ATisEqual(apples[1], apples[3]));
    assert(!ATisEqual(apples[2], apples[1]));
    assert(!ATisEqual(apples[2], apples[6]));
    assert(!ATisEqual(apples[1], apples[2]));
    assert(!ATisEqual(apples[2], apples[3]));
    assert(!ATisEqual(apples[0], apples[1]));

    ATprintf("application tests ok.\n");
  }

  //}
  //{ public void testAll()

  public void testAll()
  {
    testMakeInt();
    testMakeReal();
    testMakeAppl();
    /*testMakeList();
    testMakePlaceholder();
    testMakeBlob();
    */
  }

  //}
}
