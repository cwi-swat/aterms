using System;

namespace aterm
{
	/// <summary>
	/// This interface describes the functionality of an ATermInt
	/// </summary>
	public interface ATermInt : ATerm
	{
		/**
	     * Gets the integer value from this term.
		 *
		 * @return the integer value from this term.
		 *
		 */
		int getInt();		
	}
}
