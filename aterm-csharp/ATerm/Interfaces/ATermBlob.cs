using System;

namespace aterm
{
	/// <summary>
	/// This interface describes the functionality of an ATermBlob
	/// (Binary Large OBject).
	/// </summary>
	public interface ATermBlob : ATerm
	{
		/**
		 * Gets the size (in bytes) of the data in this blob.
		 *
		 * @return the size of the data in this blob.
		 */
		int getBlobSize();

		/**
		 * Gets the data in this blob.
		 *
		 * @return the data in this blob.
		 *
		 */
		byte[] getBlobData();
		
	}
}
