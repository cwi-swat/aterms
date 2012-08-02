using System;
using System.Collections;
using dotUnit.Framework;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for MakeIntTest.
	/// </summary>
	public class MakeIntTest : TestCase
	{
		private ATermFactory factory;

		public MakeIntTest(string name) : base(name)
		{

		}
		public virtual void TestMakeInt() 
		{
			factory = Tester.theFactory;
			ATermInt[] term = new ATermInt[2];

			term[0] = factory.makeInt(3);
			term[1] = factory.makeInt(3);

			AssertTrue(term[0].getType() == ATermType.INT);
			AssertTrue(term[0].getInt() == 3);
			AssertTrue(term[0] == term[1]);

			AssertTrue(term[0].ToString().Equals("3"));

			ArrayList result;

			result = term[0].match("3");
			AssertTrue(result != null);
			AssertTrue(result.Count == 0);

			result = term[0].match("<int>");
			AssertTrue(result != null);
			AssertTrue(result.Count == 1);
			AssertTrue(result[0].Equals(3));
    
//			Console.Out.WriteLine("pass: testMakeInt");
		}
	}
}
