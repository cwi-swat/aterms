using System;

namespace aterm
{
	/// <summary>
	/// This interface describes the functionality of an ATermReal
	/// </summary>
	public interface ATermReal : ATerm
	{
		/**
		 * Gets the real value from this term as a double.
		 *
		 * @return the real value from this term.
		 *
		 */
		double getReal();
	}
}
