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
  //{ public void testAll()

  public void testAll()
  {
    testMakeInt();
  }

  //}
}
