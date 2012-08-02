using System;
using System.Collections;
using dotUnit.Framework;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for ListTest.
	/// </summary>
	public class ListTest : TestCase
	{
		private ATermFactory factory;
		public ListTest(string name) : base(name)
		{
		}
		public virtual void TestList() 
		{
			factory = Tester.theFactory;
			ATermList list = (ATermList) factory.parse("[1,2,3]");
			ATermList result = list.remove(factory.parse("2"));
			AssertTrue(result.Equals(factory.parse("[1,3]")));

			list = (ATermList) factory.parse("[1,2,3]");
			result = list.replace(factory.parse("99"), 1);
			AssertTrue(result.Equals(factory.parse("[1,99,3]")));

			list = factory.makeList();
			result = list.append(factory.parse("1"));
			AssertTrue(result.Equals(factory.parse("[1]")));

			list = (ATermList) factory.parse("[]");
			result = factory.makeList();
			AssertTrue(result.Equals(list));

//			Console.Out.WriteLine("pass: testList");
		}
	}
}
