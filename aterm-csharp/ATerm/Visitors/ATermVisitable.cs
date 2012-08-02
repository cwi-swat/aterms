using System;
using JJTraveler;

namespace aterm
{
	/// <summary>
	/// Summary description for Visitor.
	/// </summary>
	/// 
	public interface ATermVisitable : IVisitable
	{
		void accept(ATermVisitor visitor);
	}
}
