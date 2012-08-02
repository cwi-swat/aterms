using System;

namespace aterm
{
	/// <summary>
	/// Summary description for ATermVisitableImpl.
	/// </summary>
	public abstract class ATermVisitableImpl : ATermVisitable
	{
		abstract public int getNrSubTerms();
		abstract public ATerm getSubTerm(int index);
		abstract public ATerm setSubTerm(int index, ATerm t);
		abstract public void accept(ATermVisitor visitor);

		public virtual int getChildCount()
		{
			return getNrSubTerms();
		}

		public virtual JJTraveler.IVisitable getChildAt(int index)
		{
			return getSubTerm(index);
		}

		public virtual JJTraveler.IVisitable setChildAt(int index, JJTraveler.IVisitable v)
		{
			return setSubTerm(index, (ATerm) v);
		}
		
	}
}
