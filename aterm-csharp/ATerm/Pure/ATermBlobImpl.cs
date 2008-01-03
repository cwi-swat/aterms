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
	/// Summary description for ATermBlobImpl.
	/// </summary>
	public class ATermBlobImpl : ATermImpl, ATermBlob
	{
		internal byte[] data;
		public ATermBlobImpl(PureFactory factory) : base(factory)
		{
		}

		public override ATermType getType() 
		{
			return ATermType.BLOB;
		}

		public virtual void init(int hashCode, ATermList annos, byte[] data) 
		{
			base.init(hashCode, annos);
			this.data = data;
		}

		public override SharedObject duplicate() 
		{
			ATermBlobImpl clone = new ATermBlobImpl(factory);
			clone.init(GetHashCode(), getAnnotations(), data);
			return clone;
		}

		public override bool equivalent(SharedObject obj) 
		{
			if (base.equivalent(obj)) 
			{
				ATermBlob peer = (ATermBlob) obj;
				return peer.getBlobData() == data;
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
					if (afun.getName().Equals("blob")
						&& afun.getArity() == 0
						&& !afun.isQuoted()) 
					{
						list.Add(data);
						return true;
					}
				}
			}
			return base.match(pattern, list);
		}

		public virtual byte[] getBlobData() 
		{
			return data;
		}

		public virtual int getBlobSize() 
		{
			return data.Length;
		}

		public override ATerm setAnnotations(ATermList annos) 
		{
			return getPureFactory().makeBlob(data, annos);
		}

		public override void accept(ATermVisitor v) // throws VisitFailure 
		{
			v.visitBlob(this);
		}
	}
}

