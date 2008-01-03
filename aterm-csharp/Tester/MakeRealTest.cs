using System;
using System.Collections;
using aterm;
using dotUnit.Framework;

namespace ATermTests
{
	/// <summary>
	/// Summary description for MakeRealTest.
	/// </summary>
	public class MakeRealTest : TestCase
	{
		private ATermFactory factory;
		public MakeRealTest(string name) : base(name)
		{
		}
		public virtual void TestMakeReal() 
		{
			factory = Tester.theFactory;

			ATermReal[] term = new ATermReal[2];

			term[0] = factory.makeReal(Math.PI);
			term[1] = factory.makeReal(Math.PI);

			AssertTrue(term[0].getType() == ATermType.REAL);
			AssertTrue(term[0].getReal() == Math.PI);
			AssertTrue(term[0] == term[1]);

			ArrayList result;

			result = term[0].match("<real>");
			AssertTrue(result != null);
			AssertTrue(result.Count == 1);
			AssertTrue(result[0].Equals(Math.PI));

//			Console.Out.WriteLine("pass: testMakeReal");
		}
	}
}
