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
using SharedObjects;

namespace aterm
{
	/// <summary>
	/// Summary description for ATermIntImpl.
	/// </summary>
	public class ATermIntImpl : ATermImpl, ATermInt
	{
		internal int value;

		public ATermIntImpl(PureFactory factory) : base(factory)
		{
		}

		public override ATermType getType() 
		{
			return ATermType.INT;
		}

		internal virtual void init(int hashCode, ATermList annos, int value) 
		{
			base.init(hashCode, annos);
			this.value = value;
		}

		public virtual void initHashCode(ATermList annos, int value) 
		{
			this.value = value;
			this.internSetAnnotations(annos);
			this.setHashCode(this.hashFunction());
			//super.init(hashCode, annos);
		}

		public override SharedObject duplicate() 
		{
			ATermIntImpl clone = new ATermIntImpl(factory);
			clone.init(GetHashCode(), getAnnotations(), value);
			return clone;
		}

		public override bool equivalent(SharedObject obj) 
		{
			if (base.equivalent(obj)) 
			{
				ATermInt peer = (ATermInt) obj;
				return peer.getInt() == value;
			}
			return false;
		}

		internal override bool match(ATerm pattern, ArrayList list) 
		{
			if (this.equals(pattern)) 
			{
				return true;
			}

			if (pattern.getType() == ATermType.PLACEHOLDER) 
			{
				ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
				if (type.getType() == ATermType.APPL) 
				{
					ATermAppl appl = (ATermAppl) type;
					AFun afun = appl.getAFun();
					if (afun.getName().Equals("int") && afun.getArity() == 0 && !afun.isQuoted()) 
					{
						list.Add(value);
						return true;
					}
				}
			}
			return base.match(pattern, list);
		}

		public virtual int getInt() 
		{
			return value;
		}

		public override ATerm setAnnotations(ATermList annos) 
		{
			return getPureFactory().makeInt(value, annos);
		}

		public override void accept(ATermVisitor v) // throws VisitFailure 
		{
			v.visitInt(this);
		}

		private int hashFunction() 
		{
			/* Set up the internal state */
			int a = unchecked((int)0x9e3779b9); /* the golden ratio; an arbitrary value */
			int b = unchecked((int)0x9e3779b9); /* the golden ratio; an arbitrary value */
			int c = 2; /* the previous hash value */

			/*------------------------------------- handle the last 11 bytes */
			a += (getAnnotations().GetHashCode() << 8);
			a += (value);

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

			/*-------------------------------------------- report the result */
			return c;
		}

		
	}
}
