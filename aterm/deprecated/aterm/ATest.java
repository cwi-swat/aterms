package aterm;

import aterm.util.Writer;
import aterm.util.*;
import java.util.*;
import java.io.*;

class TestFailedException extends Exception
{
  private String func;
  public TestFailedException(String function)	{ func = function; }
  public String explain() {
    return func;
  }
}

public class ATest
{
  private World world;

  //{ public static void main(String[] args)

  public static void main(String[] args)
    throws IOException, TestFailedException
  {
    ATest test = new ATest();

    try {
      test.testATerm();
      test.testATermParser();
      test.testATermList();
      test.testATermPatternMatch();
      test.testATermPatternMake();
      test.testAnno();
    } catch (TestFailedException e) {
      System.out.println("test failed: " + e.explain());
    }
    
/*
    for(int i=0; i<15; i++) {
      System.out.println("table size: " + ATerm.the_world.tableSize());
      System.out.println("garbage collect " + i);
      System.gc();
      System.out.println("finalization" + i);
      System.runFinalization();
    }
    
    if(ATerm.the_world.tableSize() != 0) {
      System.out.println("terms left:");
      Enumeration enum = ATerm.the_world.tableElements();
      while(enum.hasMoreElements()) {
	ATerm t = (ATerm)enum.nextElement();
	t.print(new PrintWriter(System.out));
	System.out.println(" (" + t.refCount() + ", " + t.getType() + ")");
      }
    }
*/
  }

//}

  //{ public ATest()

  /**
    * Execute tests.
    */

  public ATest()
  {
    //ATerm.init();
    world = ATerm.the_world;
  }

  //}
  //{ protected  void test(boolean cond, String id)

  protected  void test(boolean cond, String id)
    throws TestFailedException
  {
    if(cond)
      System.out.println("test " + id + " ok!");
    else
      throw new TestFailedException(id);
  }

  //}
  //{ protected  void testATerm()

  protected  void testATerm()
    throws IOException
  {
    ATerm[] T;
    T = new ATerm[10];
    
    System.out.println("testing ATerm classes");
    T[0] = world.makeInt(5);
    T[1] = world.makeReal(3.14);
    T[2] = world.makeAppl("abc", world.empty, true, world.empty);
    ATermList list = world.makeList(T[0], 
												 world.makeList(T[1], 
                             world.makeList(T[2])));
    T[3] = world.makeAppl("abc", list);
    T[4] = world.makeAppl("abc\002def", world.empty);
		T[5] = world.parse("f(00000004:1234,xyz,[1,2,3])");
		T[6] = world.parse("00000000:");

    for(int i=0; i<7; i++)
      System.out.println("term " + i + ": " + T[i]);
  }

  //}
  //{ protected  void testATermParser()

  protected  void testATermParser()
    throws IOException, ParseError
  {
    ATerm[] T = new ATerm[10];
    
    System.out.println("testing world class");
    T[0] = world.parse("g");
    T[1] = world.parse("fgh(1,2,<3>)");
    T[2] = world.parse("[1,3.5,4e6,123.21E-3,-12]");

    for(int i=0; i<3; i++)
      System.out.println("term " + i + ": " + T[i]);
  }

  //}
  //{ protected  void testATermList()

  protected  void testATermList()
    throws ParseError, TestFailedException 
  {
    ATerm[] T = new ATerm[10];
    ATermList[] Ts = new ATermList[10];

    System.out.println("testing ATermList class");
    T[0] = world.parse("[0,1,2,3,4,5,4,3,2,1]");
    Ts[0] = (ATermList)T[0];
    T[1] = world.parse("[]");
    Ts[1] = world.empty;
    T[2] = world.parse("[1,2,3]");
    Ts[2] = (ATermList)T[2];
    T[3] = world.parse("[4,5,6]");
    Ts[3] = (ATermList)T[3];
    T[4] = world.parse("[1,2,3,4,5,6]");
    Ts[4] = (ATermList)T[4];

    T[5] = world.parse("[1,2,3,4,5,6,7]");
    Ts[5] = (ATermList)T[5];

    //{ test length

    test(Ts[0].getLength() == 10, "length-1");

    //}
    //{ test search

    test(Ts[0].indexOf(world.makeInt(2), 0) == 2, "indexOf-1");
    test(Ts[0].indexOf(world.makeInt(10), 0) == -1, "indexOf-2");
    test(Ts[0].indexOf(world.makeInt(0), 0) == 0, "indexOf-3");
    test(Ts[0].indexOf(world.makeInt(5), 0) == 5, "indexOf-4");

    //}
    //{ test lastIndexOf

    test(Ts[0].lastIndexOf(world.makeInt(1), -1) == 9, "lastIndexOf-1");
    test(Ts[0].lastIndexOf(world.makeInt(0), -1) == 0, "lastIndexOf-2");
    test(Ts[0].lastIndexOf(world.makeInt(10), -1) == -1, "lastIndexOf-3");

    //}
    //{ test concat

    test(Ts[2].concat(Ts[3]).equals(Ts[4]), "concat-1");
    test(Ts[0].concat(Ts[1]).equals(Ts[0]), "concat-2");

    //}
    //{ test append

    test(Ts[4].append(world.makeInt(7)).equals(Ts[5]), "append-1");

    //}
  }

  //}
  //{ protected  void testATermPatternMatch()

  protected  void testATermPatternMatch()
    throws ParseError, TestFailedException
  {
    ATerm[] T = new ATerm[10];
    Vector result;
    
    T[0] = world.parse("f(1,2,3)");
    T[1] = world.parse("[1,2,3]");
    T[2] = world.parse("f(a,\"abc\",2.3,<abc>)");
    
    test(T[0].match("f(1,2,3)") != null, "match-1");
    test(T[1].match("f(1,2,3)") == null, "match-2");
    result = T[0].match("f(1,<int>,3)");
    test(result != null && result.size() == 1 && 
				 result.elementAt(0).equals(new Integer(2)), "match-3");

    result = T[2].match("f(<appl>,<str>,<real>,<placeholder>)");
    test(result != null && result.size() == 4, "match-4a");
    test(result.elementAt(0).equals(world.parse("a")), "match-4b");
    test(result.elementAt(1).equals("abc"), "match-4c");
    test(result.elementAt(2).equals(new Double(2.3)), "match-4d");
    test(result.elementAt(3).equals(world.parse("<abc>")), "match-4e");

    result = T[1].match("<list>");
    test(result != null && result.size() == 1 && 
				 result.elementAt(0).equals(T[1]), "match-6a");
    
    result = T[1].match("[<int>,<list>]");
    test(result != null && result.size() == 2 && 
				 result.elementAt(0).equals(new Integer(1)), "match-6b");
    test(result.elementAt(1).equals((ATermList)world.parse("[2,3]")),
				 "match-6c");

    result = T[0].match("<fun(<int>,<list>)>");
    test(result != null && result.size() == 3, "match-7a");
    test(result.elementAt(0).equals("f"), "match-7b");
    test(result.elementAt(1).equals(new Integer(1)), "match-7c");
    test(result.elementAt(2).equals((ATermList)world.parse("[2,3]")),
				 "match-7d");

    result = T[0].match("<f>");
    test(result != null && result.size()==1 && 
				 result.elementAt(0).equals(T[0]), "match-8");

    result = T[0].match("<f(1,2,<int>)>");
    test(result != null && result.size() == 2, "match-9a");
    test(result.elementAt(0).equals(T[0]), "match9b");
    test(result.elementAt(1).equals(new Integer(3)), "match-9b");
  }

  //}
  //{ protected  void testATermPatternMake()

  protected  void testATermPatternMake()
    throws ParseError, TestFailedException
  {
    ATerm[] T = new ATerm[10];

    T[0] = world.makeInt(1);
    T[1] = world.makeInt(2);
    T[2] = world.makeInt(3);
    T[3] = world.makeAppl("a", world.empty);
    T[4] = world.makeAppl("b", world.empty);
    T[5] = world.makeAppl("c", world.empty);
    T[6] = world.parse("f(a,b,c)");
    T[7] = world.parse("[1,2,3]");
    T[8] = world.parse("f(1,2,3)");

    test(ATerm.make("<int>", new Integer(1)).equals(T[0]), "make-1");
    test(ATerm.make("<appl>", T[3]).equals(T[3]), "make-2");
    test(ATerm.make("<fun>", "b").equals(T[4]), "make-3");
    test(ATerm.make("<real>", new Double(3.14)).equals(world.makeReal(3.14)),
				 "make-4");
    test(ATerm.make("<placeholder>", world.makeAppl("real", world.empty))
				 .equals(world.parse("<real>")), "make-5");
    test(ATerm.make("<list>", T[7]).equals(T[7]), "make-6");
    test(ATerm.make("f(<appl>,<fun>,<terms>)", T[3], "b", 
										world.makeList(T[5], world.empty)).equals(T[6]), "make-7");

    test(ATerm.make("<fun(1,<int>,3)>", "f", new Integer(2)).equals(T[8]), "make-8");
  }

  //}
  //{ protected  void testAnno()

  /**
    * Test annotations.
    */

  protected  void testAnno()
    throws ParseError, TestFailedException
  {
    ATermAppl T[] = new ATermAppl[5];
		ATerm key = world.parse("color");

    T[0] = (ATermAppl)world.parse("f(1,2,3)");
    T[1] = (ATermAppl)world.parse("red");
    T[0] = (ATermAppl)T[0].setAnnotation(key, T[1]);
    test(T[0].getAnnotation(key).equals(T[1]), "anno-1");
    test(T[1].getAnnotation(key) == null, "anno-2");
    T[2] = (ATermAppl)T[1].setAnnotation(key, T[1]);
    test(T[2].getAnnotation(key).equals(T[1]), "anno-3");
  }

  //}
}
