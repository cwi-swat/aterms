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
using System.Text;
using SharedObjects;

namespace aterm
{
	/// <summary>
	/// Summary description for AFunImpl.
	/// </summary>
	public class AFunImpl : ATermImpl, AFun
	{
		public AFunImpl(PureFactory factory) : base(factory)
		{
		}

		internal string name;
		internal int arity;
		internal bool _isQuoted;

		internal virtual void init(int hashCode,string name,int arity,bool isQuoted)
		{
			base.init(hashCode, null);
			this.name = String.Intern(name);
			this.arity = arity;
			this._isQuoted = isQuoted;
		}

		public virtual void initHashCode(string name, int arity, bool isQuoted) 
		{
			this.name = String.Intern(name);
			this.arity = arity;
			this._isQuoted = isQuoted;
			this.setHashCode(this.hashFunction());
		}

		public override SharedObject duplicate() 
		{
			AFunImpl clone = new AFunImpl(factory);
			clone.init(GetHashCode(), name, arity, _isQuoted);
			return clone;
		}

		public override bool equivalent(SharedObject obj) 
		{
			try 
			{
				AFun peer = (AFun) obj;
				return peer.getName() == name
					&& peer.getArity() == arity
					&& peer.isQuoted() == _isQuoted;
			} 
			catch (InvalidCastException) 
			{
				return false;
			}
		}

		public override ATermType getType() 
		{
			return ATermType.AFUN;
		}

		public virtual string getName() 
					   {
						   return name;
					   }

		public virtual int getArity() 
		{
			return arity;
		}

		public virtual bool isQuoted() 
		{
			return _isQuoted;
		}

		public override ATerm getAnnotation(ATerm key) 
		{
			throw new  MissingMemberException(); // UnsupportedOperationException();
		}

		public override ATermList getAnnotations() 
		{
			throw new  MissingMemberException(); // UnsupportedOperationException();
		}

		public override ATerm setAnnotations(ATermList annos) 
		{
			throw new  MissingMemberException(); // UnsupportedOperationException();
		}

		public override string ToString() 
		{
			StringBuilder result = new StringBuilder(name.Length);

			if (_isQuoted) 
			{
				result.Append('"');
			}

			for (int i = 0; i < name.Length; i++) 
			{
				char c = name[i];
				switch (c) 
				{
					case '\n' :
						result.Append('\\');
						result.Append('n');
						break;
					case '\t' :
						result.Append('\\');
						result.Append('t');
						break;
					case '\b' :
						result.Append('\\');
						result.Append('b');
						break;
					case '\r' :
						result.Append('\\');
						result.Append('r');
						break;
					case '\f' :
						result.Append('\\');
						result.Append('f');
						break;
					case '\\' :
						result.Append('\\');
						result.Append('\\');
						break;
					case '\'' :
						result.Append('\\');
						result.Append('\'');
						break;
					case '\"' :
						result.Append('\\');
						result.Append('\"');
						break;

					case '!' : goto case'/';
					case '@' : goto case'/';
					case '#' : goto case'/';
					case '$' : goto case'/';
					case '%' : goto case'/';
					case '^' : goto case'/';
					case '&' : goto case'/';
					case '*' : goto case'/';
					case '(' : goto case'/';
					case ')' : goto case'/';
					case '-' : goto case'/';
					case '_' : goto case'/';
					case '+' : goto case'/';
					case '=' : goto case'/';
					case '|' : goto case'/';
					case '~' : goto case'/';
					case '{' : goto case'/';
					case '}' : goto case'/';
					case '[' : goto case'/';
					case ']' : goto case'/';
					case ';' : goto case'/';
					case ':' : goto case'/';
					case '<' : goto case'/';
					case '>' : goto case'/';
					case ',' : goto case'/';
					case '.' : goto case'/';
					case '?' : goto case'/';
					case ' ' : goto case'/';
					case '/' :
						result.Append(c);
						break;

					default :
						if (Char.IsLetterOrDigit(c)) 
						{
							result.Append(c);
						} 
						else 
						{
							result.Append('\\');
							result.Append((char) ((int) '0' + (int) c / 64));
							c = (char) (c % 64);
							result.Append((char) ((int) '0' + (int) c / 8));
							c = (char) (c % 8);
							result.Append((char) ((int) '0' + (int) c));
						};
						break;
				}
			}

			if (_isQuoted) 
			{
				result.Append('"');
			}

			return result.ToString();
		}

		public override void accept(ATermVisitor v) // throws VisitFailure 
		{
			v.visitAFun(this);
		}

		private int hashFunction() 
		{
			int a, b, c;
			/* Set up the internal state */
			a = b = unchecked((int)0x9e3779b9); /* the golden ratio; an arbitrary value */
			/*------------------------------------- handle the last 11 bytes */
			int len = name.Length;
			if (len >= 12) 
			{
				return hashFunction2();
			}
			c = (_isQuoted) ? 7 * arity + 1 : arity + 1;
			c += len;
			switch (len) 
			{
				case 11 :
					c += (name[10] << 24);
					goto case 10;
				case 10 :
					c += (name[9] << 16);
					goto case 9;
				case 9 :
					c += (name[8] << 8);
					goto case 8;
					/* the first byte of c is reserved for the length */
				case 8 :
					b += (name[7] << 24);
					goto case 7;
				case 7 :
					b += (name[6] << 16);
					goto case 6;
				case 6 :
					b += (name[5] << 8);
					goto case 5;
				case 5 :
					b += name[4];
					goto case 4;
				case 4 :
					a += (name[3] << 24);
					goto case 3;
				case 3 :
					a += (name[2] << 16);
					goto case 2;
				case 2 :
					a += (name[1] << 8);
					goto case 1;
				case 1 :
					a += name[0];
					/* case 0: nothing left to add */
					break;
			}
			//mix(a,b,c);

			a -= b;
			a -= c;
			a ^= (c >> 13);
			b -= c;
			b -= a;
			b ^= (a << 8);
			c -= a;
			c -= b;
			c ^= (b >> 13);
			a -= b;
			a -= c;
			a ^= (c >> 12);
			b -= c;
			b -= a;
			b ^= (a << 16);
			c -= a;
			c -= b;
			c ^= (b >> 5);
			a -= b;
			a -= c;
			a ^= (c >> 3);
			b -= c;
			b -= a;
			b ^= (a << 10);
			c -= a;
			c -= b;
			c ^= (b >> 15);

			return c;
		}

		private int hashFunction2() 
		{
			int offset = 0;
			int count = name.Length;
			char[] source = new char[count];

			offset = 0;
			name.CopyTo(0,source, 0,  count);
			int a, b, c;
			/* Set up the internal state */
			int len = count;
			a = b = unchecked((int)0x9e3779b9); /* the golden ratio; an arbitrary value */
			c = (_isQuoted) ? 7 * (arity + 1) : arity + 1; // to avoid collison 
			/*------------------------------------- handle the last 11 bytes */
			int k = offset;

			while (len >= 12) 
			{
				a
					+= (source[k
					+ 0]
					+ (source[k + 1] << 8)
					+ (source[k + 2] << 16)
					+ (source[k + 3] << 24));
				b
					+= (source[k
					+ 4]
					+ (source[k + 5] << 8)
					+ (source[k + 6] << 16)
					+ (source[k + 7] << 24));
				c
					+= (source[k
					+ 8]
					+ (source[k + 9] << 8)
					+ (source[k + 10] << 16)
					+ (source[k + 11] << 24));
				//mix(a,b,c);
				a -= b;
				a -= c;
				a ^= (c >> 13);
				b -= c;
				b -= a;
				b ^= (a << 8);
				c -= a;
				c -= b;
				c ^= (b >> 13);
				a -= b;
				a -= c;
				a ^= (c >> 12);
				b -= c;
				b -= a;
				b ^= (a << 16);
				c -= a;
				c -= b;
				c ^= (b >> 5);
				a -= b;
				a -= c;
				a ^= (c >> 3);
				b -= c;
				b -= a;
				b ^= (a << 10);
				c -= a;
				c -= b;
				c ^= (b >> 15);

				k += 12;
				len -= 12;
			}
			/*---------------------------------------- handle most of the key */
			c += count;
			switch (len) 
			{
				case 11 :
					c += (source[k + 10] << 24);
					goto case 9;
				case 10 :
					c += (source[k + 9] << 16);
					goto case 9;
				case 9 :
					c += (source[k + 8] << 8);
					goto case 8;
					/* the first byte of c is reserved for the length */
				case 8 :
					b += (source[k + 7] << 24);
					goto case 7;
				case 7 :
					b += (source[k + 6] << 16);
					goto case 6;
				case 6 :
					b += (source[k + 5] << 8);
					goto case 5;
				case 5 :
					b += source[k + 4];
					goto case 4;
				case 4 :
					a += (source[k + 3] << 24);
					goto case 3;
				case 3 :
					a += (source[k + 2] << 16);
					goto case 2;
				case 2 :
					a += (source[k + 1] << 8);
					goto case 1;
				case 1 :
					a += source[k + 0];
					break;
					/* case 0: nothing left to add */
			}
			//mix(a,b,c);
			a -= b;
			a -= c;
			a ^= (c >> 13);
			b -= c;
			b -= a;
			b ^= (a << 8);
			c -= a;
			c -= b;
			c ^= (b >> 13);
			a -= b;
			a -= c;
			a ^= (c >> 12);
			b -= c;
			b -= a;
			b ^= (a << 16);
			c -= a;
			c -= b;
			c ^= (b >> 5);
			a -= b;
			a -= c;
			a ^= (c >> 3);
			b -= c;
			b -= a;
			b ^= (a << 10);
			c -= a;
			c -= b;
			c ^= (b >> 15);

			//System.out.println("static doobs_hashFunctionAFun = " + c + ": " + name);
			return c;
		}
		
	}
}
