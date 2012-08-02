using System;
using JJTraveler;
using aterm;

namespace ATermVisitTest
{
	/// <summary>
	/// Summary description for ATermVisitTest.
	/// </summary>
	public class ATermVisitTest
	{
		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		[STAThread]
		static void Main(string[] args)
		{
			PureFactory factory = new PureFactory();
			ATerm t = factory.parse("f(g(1,h(2,3)),i(4,5),j(6,k(7,[8,9])))");
            ATermLeavesCounter v = new ATermLeavesCounter();
			TopDown td = new TopDown(v);
			td.visit(t);
			Console.Out.WriteLine(v.Count);
		}
		public ATermVisitTest()
		{
			//
			// TODO: Add constructor logic here
			//
		}
	}
}
