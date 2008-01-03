using System;
using System.Collections;
using dotUnit.Framework;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for PatternMakeTest.
	/// </summary>
	public class PatternMakeTest : TestCase
	{
		private ATermFactory factory;
		public PatternMakeTest(string name) : base(name)
		{
		}
		public virtual void TestPatternMake() 
		{
			factory = Tester.theFactory;
			ArrayList list = new ArrayList();
			ATerm result;

			list.Clear(); 
			result = factory.make("23", list);
//			Console.Out.WriteLine("\tresult = " + result);

			list.Clear(); 
			result = factory.make("3.14", list);
//			Console.Out.WriteLine("\tresult = " + result);

			list.Clear(); 
			result = factory.make("[1,2,3]", list);
//			Console.Out.WriteLine("\tresult = " + result);
    
			list.Clear(); 
			result = factory.make("GL(\"toto\")", list);
//			Console.Out.WriteLine("\tresult = " + result);

			list.Clear(); list.Add(1);
			result = factory.make("<int>", list);
//			Console.Out.WriteLine("\tresult = " + result);

			list.Clear(); list.Add(3.14);
			result = factory.make("<real>", list);
//			Console.Out.WriteLine("\tresult = " + result);

			list.Clear(); list.Add(factory.parse("f(a,b,c)"));
			result = factory.make("<term>", list);
//			Console.Out.WriteLine("\tresult = " + result);
    

			list.Clear();
			list.Add(factory.parse("f(a,b,c)"));
			list.Add(3);
			list.Add(factory.parse("<abc>"));
			result = factory.make("[<term>,2,<int>,3.14,<placeholder>]", list);
//			Console.Out.WriteLine("\tresult = " + result);

			list.Clear();
			list.Add(factory.parse("b"));
			list.Add(4);
			result = factory.make("f(1,<term>,c,<int>)", list);
//			Console.Out.WriteLine("\tresult = " + result);

			list.Clear();
			list.Add(factory.parse("b"));
			list.Add(4);
			result = factory.make("f(1,g(<term>),c,h(<int>))", list);
//			Console.Out.WriteLine("\tresult = " + result);

			//Ts[8] = factory.parse();
			list.Clear();
			list.Add(factory.parse("1"));
			list.Add(factory.parse("[]"));
			result = factory.make("[<term>,<list>]", list);
//			Console.Out.WriteLine("\tresult = " + result);
			AssertTrue("make-1a",((ATermList)result).getFirst() == factory.parse("1") );
			AssertTrue("make-1b",((ATermList)result).getLength() == 1);

    
			/*
		  list.add(new Integer(1));
		  test(factory.make("<int>", list).Equals(T[0]), "make-1");

		  list.clear(); list.add(T[3]);
		  test(factory.make("<term>", list).Equals(T[3]), "make-2");

		  list.clear(); list.add( "b");
		  test(factory.make("<appl>", list).Equals(T[4]), "make-3");

		  list.clear(); list.add(new Double(3.14));
		  test(factory.make("<real>", list).Equals(
			factory.makeReal(3.14)), "make-4");

		  list.clear(); list.add(factory.makeAppl(
			factory.makeAFun("real",0,false)));
		  test(factory.make("<placeholder>", list).Equals(
			factory.parse("<real>")), "make-5");

		  list.clear(); list.add(T[7]);
		  test(factory.make("[<list>]", list).Equals(T[7]), "make-6");

		  list.clear();
		  list.add(T[3]);
		  list.add("b");
		  list.add(factory.makeList(T[5], factory.makeList()));
		  test(factory.make("f(<term>,<appl>,<list>)", list).Equals(T[6]), "make-7");

		  list.clear();
		  list.add("f");
		  list.add(new Integer(2));
		  test(factory.make("<appl(1,<int>,3)>", list).Equals(T[8]), "make-8");
			*/

//			Console.Out.WriteLine("pass: testPatternMake");
		}
		
	}
}
