using System;
using System.Collections;
using dotUnit.Framework;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for MakeListTest.
	/// </summary>
	public class MakeListTest : TestCase
	{
		private ATermFactory factory;
		public MakeListTest(string name) : base(name)
		{
		}
		public virtual void TestMakeList() 
		{
			factory = Tester.theFactory;
			ATerm[] T = new ATerm[10];
			ATermList[] Ts = new ATermList[10];

			//Console.Out.WriteLine("testing ATermList class");
			T[0] = factory.parse("[0,1,2,3,4,5,4,3,2,1]");
			Ts[0] = (ATermList)T[0];
			T[1] = factory.parse("[]");
			Ts[1] = factory.makeList();
			T[2] = factory.parse("[1,2,3]");
			Ts[2] = (ATermList)T[2];
			T[3] = factory.parse("[4,5,6]");
			Ts[3] = (ATermList)T[3];
			T[4] = factory.parse("[1,2,3,4,5,6]");
			Ts[4] = (ATermList)T[4];

			//    T[5] = factory.parse("[1 , 2 , 3 , 4,5,6,7]");
			T[5] = factory.parse("[1,2,3,4,5,6,7]");
			Ts[5] = (ATermList)T[5];

			//T[6] = factory.parse("f(abc{[label,val]})");

			// test length
			AssertTrue("length-1",Ts[0].getLength() == 10 );

			// test search
			AssertTrue("indexOf-1",Ts[0].indexOf(factory.makeInt(2), 0) == 2 );
			AssertTrue("indexOf-2",Ts[0].indexOf(factory.makeInt(10), 0) == -1 );
			AssertTrue("indexOf-3",Ts[0].indexOf(factory.makeInt(0), 0) == 0 );
			AssertTrue("indexOf-4",Ts[0].indexOf(factory.makeInt(5), 0) == 5 );

			// test lastIndexOf
    
			AssertTrue("lastIndexOf-1",Ts[0].lastIndexOf(factory.makeInt(1), -1) == 9 );
			AssertTrue("lastIndexOf-2",Ts[0].lastIndexOf(factory.makeInt(0), -1) == 0 );
			AssertTrue("lastIndexOf-3",Ts[0].lastIndexOf(factory.makeInt(10), -1) == -1 );

			// test concat
			AssertTrue("concat-1",Ts[2].concat(Ts[3]).Equals(Ts[4]) );
			AssertTrue("concat-2",Ts[0].concat(Ts[1]).Equals(Ts[0]) );

			// test append
			AssertTrue("append-1",Ts[4].append(factory.makeInt(7)).Equals(Ts[5]) );

			// test insert
			Ts[7] = Ts[3].insert(factory.parse("3"));
			Ts[7] = Ts[7].insert(factory.parse("2"));
			Ts[7] = Ts[7].insert(factory.parse("1"));
			AssertTrue("insert-1",Ts[7].Equals(Ts[4]) );
    
			AssertTrue("insert-2",Ts[1].insert(factory.parse("1")).Equals(factory.parse("[1]")) );

			AssertTrue("insert-3",Ts[4].insertAt(factory.parse("7"), Ts[4].getLength()).Equals(Ts[5]) );

			// Test prefix/last
			AssertTrue("prefix-1",Ts[5].getPrefix().Equals(Ts[4]) );
			AssertTrue("last-1",Ts[5].getLast().Equals(factory.parse("7")) );

//			Console.Out.WriteLine("pass: testMakeList");
		}
	
	}
}
