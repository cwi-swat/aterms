/*
 * Java version of the ATerm library
 * Copyright (C) 2002, CWI, LORIA-INRIA
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */

using System;
using System.Collections;
using System.IO;
using SharedObjects;

namespace aterm
{
	/// <summary>
	/// Summary description for ATermImpl.
	/// </summary>
	abstract public class ATermImpl : ATermVisitableImpl, ATerm, SharedObjectWithID
	{
		private ATermList annotations;
		internal PureFactory factory;
		private int _hashCode;
		private int uniqueId;
		public abstract ATermType getType();
		public abstract ATerm setAnnotations(ATermList annos); 
 
 
		internal ATermImpl(PureFactory factory) : base()
		{
			this.factory = factory;
		}

		public override int GetHashCode() 
		{
			return this._hashCode;
		}
  
		abstract public SharedObject duplicate();

		internal virtual void setHashCode(int hashcode) 
		{
			this._hashCode = hashcode;
		}

		internal virtual void internSetAnnotations(ATermList annos) 
		{
			this.annotations = annos;
		}

		internal virtual void init(int hashCode, ATermList annos) 
		{
			this._hashCode = hashCode;
			this.annotations = annos;
		}
  
		public virtual bool equivalent(SharedObject obj) 
		{
			try 
			{

				if( ((ATerm)obj).getType() == getType() ) 
				{
					return ((ATerm)obj).getAnnotations().equals(getAnnotations());
				}
				return false;
			} 
			catch (InvalidCastException) 
			{
				return false;
			}
		}

		public virtual ATermFactory getFactory() 
		{
			return factory;
		}
  
		internal virtual PureFactory getPureFactory() 
		{
			return (PureFactory) getFactory();
		}
  
		public virtual ATerm setAnnotation(ATerm label, ATerm anno) 
		{
			ATermList new_annos = annotations.dictPut(label, anno);
			ATerm result = setAnnotations(new_annos);

			return result;
		}

		public virtual ATerm removeAnnotation(ATerm label) 
		{
			return setAnnotations(annotations.dictRemove(label));
		}

		public virtual ATerm getAnnotation(ATerm label) 
		{
			return annotations.dictGet(label);
		}

		public virtual ATerm removeAnnotations() 
		{
			return setAnnotations(((PureFactory) getFactory()).getEmpty());
		}

		public virtual ATermList getAnnotations() 
		{
			return annotations;
		}

		public virtual ArrayList match(string pattern) // throws ParseError 
		{
			return match(factory.parsePattern(pattern));
		}

		public virtual ArrayList match(ATerm pattern) 
		{
			ArrayList list = new ArrayList();
			if (match(pattern, list)) 
			{
				return list;
			} 
			else 
			{
				return null;
			}
		}

		public virtual bool isEqual(ATerm term) 
		{
			if (term is ATermImpl) 
			{
				return this == term;
			}
			return factory.isDeepEqual(this, term);
		}

		public virtual bool equals(Object obj) 
		{
			if (obj is ATermImpl) 
			{
				return this == obj;
			}

			if (obj is ATerm) 
			{
				return factory.isDeepEqual(this, (ATerm) obj);
			}
			return false;
		}

		internal virtual bool match(ATerm pattern, ArrayList list) 
		{
			if (pattern.getType() == ATermType.PLACEHOLDER) 
			{
				ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
				if (type.getType() == ATermType.APPL) 
				{
					ATermAppl appl = (ATermAppl) type;
					AFun afun = appl.getAFun();
					if (afun.getName().Equals("term") && afun.getArity() == 0 && !afun.isQuoted()) 
					{
						list.Add(this);
						return true;
					}
				}
			}
			return false;
		}

		public virtual ATerm make(ArrayList list) 
		{
			return this;
		}

		public virtual void writeToTextFile(ATermWriter writer) // throws IOException 
		{
			try 
			{
				writer.visitChild(this);
				writer.getStream().Flush();
			} 
			catch (JJTraveler.VisitFailure e) 
			{
				throw new IOException(e.Message);
			}
		}

		public virtual void writeToSharedTextFile(Stream stream) // throws IOException 
		{
			ATermWriter writer = new ATermWriter(new BufferedStream(stream));
			writer.initializeSharing();
			stream.WriteByte((byte)'!');
			writeToTextFile(writer);
		}

		public virtual void writeToTextFile(Stream stream) // throws IOException 
		{
			ATermWriter writer = new ATermWriter(new BufferedStream(stream));
			writeToTextFile(writer);
		}

		public override string ToString() 
		{
			try 
			{
				MemoryStream stream = new MemoryStream();
				StringWriter s = new StringWriter();
				ATermWriter writer = new ATermWriter(stream);
				writeToTextFile(writer);
				foreach(byte b in stream.ToArray() )
				{
					s.Write((char)b);
				}

				return s.ToString();
			} 
			catch (IOException e) 
			{
				throw new Exception("IOException: " + e.Message);
			}
		}

		public override int getNrSubTerms() 
		{
			return 0;
		}

		public override ATerm getSubTerm(int index) 
		{
			throw new Exception("no children!");
		}

		public override ATerm setSubTerm(int index, ATerm t) 
		{
			throw new Exception("no children!");
		}

		public virtual int getUniqueIdentifier() 
		{
			return uniqueId;
		}
  
		public virtual void setUniqueIdentifier(int uniqueId) 
		{
			this.uniqueId = uniqueId;
		}
	}
}

