using System;
using JJTraveler;
using aterm;

namespace ATermVisitTest
{
	/// <summary>
	/// Summary description for Class1.
	/// </summary>
	public class ATermLeavesCounter : ATermFwd // IVisitor
	{
		protected int count;
		public ATermLeavesCounter() : base(new Identity() ) 
		{
		}
		public int Count {get {return count;}}

		public override void visitAppl(ATermAppl a ) {}
		public override void visitList(ATermList l) {}

		public override void visitATerm(ATerm any)
		{
			count++;
		}
	}
}
