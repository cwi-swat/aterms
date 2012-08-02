using System;
using JJTraveler;

namespace aterm
{
	/// <summary>
	/// Summary description for Visitor.
	/// </summary>
	/// 
	public class ATermFwd : ATermVisitor
	{
		private IVisitor visitor;
		
		public ATermFwd(IVisitor visitor) 
		{
			this.visitor = visitor;
		}

		public virtual IVisitable visit(IVisitable visitable)
		{
			if(visitable is ATermVisitable)
			{
				((ATermVisitable)visitable).accept(this);
				return visitable;
			}
			else
			{
				throw new VisitFailure();
			}
		}
		public virtual void visitATerm(ATerm arg) // throws VisitFailure 
		{
			visitor.visit(arg);
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
			visitor.visit(fun);
		}
	}
}
