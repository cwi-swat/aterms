using System;
using System.Collections;
using dotUnit.Framework;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for MakeApplTest.
	/// </summary>
	public class MakeApplTest : TestCase
	{
		private ATermFactory factory;

		public MakeApplTest(string name) : base(name)
		{
		}
		public virtual void testMakeAppl() 
		{
			factory = Tester.theFactory;

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
			apples[4] = factory.makeAppl(symmies[2], new ATerm[] {(ATerm)apples[0],
																	 (ATerm)apples[0], 
																	 (ATerm)apples[1],
																	 (ATerm)apples[0], 
																	 (ATerm)apples[0],
																	 (ATerm)apples[1]});
			apples[5] = factory.makeAppl(symmies[3], new ATerm[] {(ATerm)apples[0],
																	 (ATerm)apples[1],
																	 (ATerm)apples[0],
																	 (ATerm)apples[1],
																	 (ATerm)apples[0],
																	 (ATerm)apples[1],
																	 (ATerm)apples[0],
																	 (ATerm)apples[1],
																	 (ATerm)apples[0],
																	 (ATerm)apples[1]});
			apples[6] = (ATermAppl)apples[2].setArgument((ATerm)apples[0], 0);
			ATerm[] args = { apples[0], apples[1], apples[0], apples[1], apples[0], apples[1], apples[0], apples[1], apples[0], apples[1] };
			apples[7] = factory.makeAppl(symmies[3], args);

			AssertTrue(factory.makeAppl(symmies[0]) == apples[0]);
			AssertTrue(factory.makeAppl(symmies[1], (ATerm) apples[0]) == apples[1]);

			AssertTrue(apples[6].isEqual(apples[1]));
			AssertTrue(apples[1].isEqual(apples[3]));
			AssertTrue(!apples[2].isEqual(apples[1]));
			AssertTrue(!apples[2].isEqual(apples[6]));
			AssertTrue(!apples[1].isEqual(apples[2]));
			AssertTrue(!apples[2].isEqual(apples[3]));
			AssertTrue(!apples[0].isEqual(apples[1]));

			//			Console.Out.WriteLine("pass: testMakeAppl");
		}


	}
}
