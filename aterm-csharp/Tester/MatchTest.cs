using System;
using System.Collections;
using dotUnit.Framework;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for MatchTest.
	/// </summary>
	public class MatchTest : TestCase
	{
		private ATermFactory factory;
		public MatchTest(string name) : base(name)
		{
		}
		public virtual void TestMatch() 
		{
			factory = Tester.theFactory;

			ATerm t = factory.parse("node(\"Pico-eval\",box,182,21,62,26)");
			ArrayList result = t.match("node(<str>,<fun>,<int>,<int>,<int>,<int>)");
			AssertTrue(result != null);

			t = factory.parse("f(1,2,3)"); 
			result = t.match("f(1,2,3)");
			AssertTrue(result != null);

//			Console.Out.WriteLine("pass: testMatch");
		}
	}
}
