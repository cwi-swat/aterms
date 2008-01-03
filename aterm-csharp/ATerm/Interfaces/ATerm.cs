using System;
using System.Collections;
using JJTraveler;

namespace aterm
{
	public enum ATermType {		
		INT = 1,     /// A term of type INT
		REAL,        /// A term of type REAL
		APPL,        /// A term of type APPL (function application)
		LIST,        /// A term of type LIST
		PLACEHOLDER, /// A term of type PLACEHOLDER
		BLOB,        /// A term of type BLOB (Binary Large OBject)
		AFUN         /// A term of type AFUN (function symbol)
	};
	/// <summary>
	/// This is the base interface for all ATerm interfaces,
	/// which will ultimately be implemented by two separate
	/// ATerm Factories (a native and a pure one).
	/// 
	/// @author Hayco de Jong (jong@cwi.nl)
	/// @author Pieter Olivier (olivierp@cwi.nl)
	/// </summary>
	public interface ATerm : IVisitable, Identifiable
	{
		/**
		 * Gets the type of this term.
		 * 
		 * 
		 * @return the type of this ATerm.
		 * 
		 * @see #INT
		 * @see #REAL
		 * @see #APPL
		 * @see #LIST
		 * @see #PLACEHOLDER
		 * @see #BLOB
		 * @see #AFUN
		 */
		ATermType getType();

		/**
		 * Gets a hashcode value of this term.
		 * 
		 * @return the hashcode of this term.
		 *
		 */
		int GetHashCode();

		/**
		 * Matches this term against a String pattern. The pattern is
		 * parsed into a term, which this term is then matched against.
		 * 
		 * @param pattern the string pattern to match this term against.
		 * 
		 * @return a list containing the subterms matching the placeholders
		 * if the match succeeds, or null if the match fails.
		 *
		 * @throws ParseError if pattern cannot be parsed into a term.
		 * 
		 * @see #match(ATerm)
		 */
		ArrayList match(string pattern);

		/**
		 * Matches this term against a term pattern. A list containing
		 * the subterms matching the placeholders in the pattern is
		 * built as this term is matched against the pattern.
		 *
		 * @param pattern The term pattern to match this term against.
		 *
		 * @return a list containing the subterms matching the placeholders
		 * if the match succeeds, or null if the match fails.
		 *
		 */
		ArrayList match(ATerm pattern);

		/**
		 * Gets a annotation associated with specific label from this term.
		 * 
		 * 
		 * @param label the label of the desired annotation.
		 * 
		 * @return the annotation associated with label, or null if
		 * annotation with specified label does not exist.
		 * 
		 * @see #setAnnotation
		 */
		ATerm getAnnotation(ATerm label);

		/**
		 * Sets annotation of this term with given label. If no annotation
		 * with this label exists, a new annotation is created, otherwise
		 * the existing annotation is updated.
		 * 
		 * 
		 * @param label the label of the annotation.
		 * @param anno the annotation itself.
		 * 
		 * @return a new version of this term with the requested annotation.
		 * 
		 * @see #getAnnotation
		 */
		ATerm setAnnotation(ATerm label, ATerm anno);

		/**
		 * Removes a specific annotation from this term.
		 *
		 * 
		 * @param label the label of the annotation to be removed.
		 * 
		 * @return a new version of this term without the annotation.
		 * 
		 * @see #setAnnotation
		 */
		ATerm removeAnnotation(ATerm label);

		/**
		 * Gets all annotations of this term.
		 * 
		 * @return all annotations of this term
		 * 
		 * @see #setAnnotations
		 */
		ATermList getAnnotations();

		/**
		 * Sets all annotations of this term.
		 *
		 * 
		 * @param annos the annotations to set.
		 * 
		 * @return a new version of this term with the requested annotations.
		 * 
		 * @see #getAnnotations
		 */
		ATerm setAnnotations(ATermList annos);

		/**
		 * Removes all annotations of this term.
		 *
		 * 
		 * @return a new version of this term without annotations.
		 * 
		 * @see #setAnnotations
		 */
		ATerm removeAnnotations();

		/**
		 * Checks equality of this term against another term.
		 * This method exists to keep a tight relation to the C-library.
		 * Experienced Java programmers might feel more comfortable using
		 * the {@link #equals} method.
		 * 
		 * 
		 * @param term the term to check for equality.
		 * 
		 * @return true iff terms are equal (including
		 * any annotations they might have!), false otherwise.
		 *
		 * @see #equals(Object)
		 * 
		 */
		bool isEqual(ATerm term);

		/**
		 * Checks equality of this term against any java object.
		 * Note that for two terms to be equal, any annotations they
		 * might have must be equal as well.
		 * 
		 * @param obj the object to check for equality.
		 * 
		 * @return true iff term equals obj (including annotations).
		 * 
		 * @see #isEqual
		 */
		bool equals(Object obj);

		/**
		  * Write a term to a text file/stream.
		  *
		  * @param stream the stream to write to
		  */
		void writeToTextFile(System.IO.Stream stream); //		throws IOException;

		/**
		  * Write a term to a shared text file/stream.
		  * An efficient shared ASCII representation of this term is written to
		  * the stream.
		  *
		  * @param stream the stream to write this term to
		  */
		void writeToSharedTextFile(System.IO.Stream stream);   //		throws IOException;

		/**
		  * Create a new term based on this term as a pattern and a list of arguments.
		  *
		  * @param args the list of arguments used to fill up holes in the pattern
		  *
		  */
		ATerm make(ArrayList args);

		/**
		 * Retrieves the factory responsible for creating this ATerm.
		 *
		 * @return the factory that created this ATerm object.
		 */

		ATermFactory getFactory();

		/**
		 * Gets a string representation of this term.
		 * 
		 * 
		 * @return a string representation of this term.
		 * 
		 */
		string ToString();
	}
}
