using System;

namespace aterm
{
	/// <summary>
	/// This interface describes the functionality of an ATermPlaceholder
	/// </summary>
	public interface ATermPlaceholder : ATerm
	{
	
		/**
		 * Gets the type of this placeholder.
		 *
		 * @return the type of this placeholder.
		 *
		 */
		ATerm getPlaceholder();
	}
}