using System;
using dotUnit.Framework;
using dotUnit.GUI;
using aterm;

namespace ATermTests
{
	/// <summary>
	/// Summary description for Class1.
	/// </summary>
	class Tester
	{
		public static  PureFactory theFactory = new PureFactory();

		public static TestSuite Suite
		{
			get
			{
				TestSuite suite = new TestSuite("ATerm Tests");
				suite.AddTestSuite(typeof(PrimesTest));
				suite.AddTestSuite(typeof(FibInterpretedTest));
				suite.AddTestSuite(typeof(MakeIntTest));
				suite.AddTestSuite(typeof(MakeRealTest));
				suite.AddTestSuite(typeof(MakeApplTest));
				suite.AddTestSuite(typeof(ParserTest));
				suite.AddTestSuite(typeof(FileParserTest));
				suite.AddTestSuite(typeof(MakeListTest));
				suite.AddTestSuite(typeof(PatternMatchTest));
				suite.AddTestSuite(typeof(PatternMakeTest));
				suite.AddTestSuite(typeof(MaxTermTest));
				suite.AddTestSuite(typeof(DictTest));
				suite.AddTestSuite(typeof(AnnosTest));
				suite.AddTestSuite(typeof(ListTest));
				suite.AddTestSuite(typeof(FilesTest));
				suite.AddTestSuite(typeof(MatchTest));
				suite.AddTestSuite(typeof(FibTest));
				return suite;
			}
		}
		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		[STAThread]
		static void Main(string[] args)
		{
			GUIRunner.Run(Suite);
		}
	}
}
