using System;
using JJTraveler;

namespace aterm
{
	/// <summary>
	/// Summary description for Visitor.
	/// </summary>
	/// 
	public interface ATermVisitor : IVisitor
	{
		void visitATerm(ATerm arg);

		void visitInt(ATermInt arg);

		void visitReal(ATermReal arg);

		void visitAppl(ATermAppl arg);

		void visitList(ATermList arg);

		void visitPlaceholder(ATermPlaceholder arg);

		void visitBlob(ATermBlob arg);

		void visitAFun(AFun fun);

	}
#if false
	public class ATermVisitor : VoidVisitor
	{
		public override void voidVisit(IVisitable any) // throws VisitFailure 
		{
			if (any is ATermVisitable) 
			{
				((ATermVisitable) any).accept(this);
			}
			else 
			{
				throw new VisitFailure();
			}
		}

		public virtual void visitATerm(ATerm arg) // throws VisitFailure 
		{
			// do nothing
		}

		public virtual void visitInt(ATermInt arg) // throws VisitFailure 
		{
			visitATerm(arg);
		}

		public virtual void visitReal(ATermReal arg) // throws VisitFailure 
		{
			visitATerm(arg);
		}

		public virtual void visitAppl(ATermAppl arg) // throws VisitFailure 
		{
			visitATerm(arg);
		}

		public virtual void visitList(ATermList arg) // throws VisitFailure 
		{
			visitATerm(arg);
		}

		public virtual void visitPlaceholder(ATermPlaceholder arg) // throws VisitFailure 
		{
			visitATerm(arg);
		}

		public virtual void visitBlob(ATermBlob arg) // throws VisitFailure 
		{
			visitATerm(arg);
		}

		public virtual void visitAFun(AFun fun) // throws VisitFailure 
		{
			// do nothing
		}
	}
#endif
}
