using System;

namespace aterm 
{
	/// <summary>
	/// Summary description for AFun.
	/// </summary>
	public interface AFun : ATerm
	{
		/**
		 * Gets the name of the function symbol
		 *
		 * @return the name of this function symbol.
		 */
		string getName();

		/**
		 * Gets the arity of this application. Arity is the number
		 * of arguments of a function application.
		 *
		 * @return the number of arguments that applications of this
		 * function symbol have.
		 */
		int getArity();

		/**
		 * Checks if this application is quoted. A quoted application looks
		 * like this: "foo", whereas an unquoted looks like this: foo.
		 *
		 * @return true if this application is quoted, false otherwise.
		 */
		bool isQuoted();
		
	}
}