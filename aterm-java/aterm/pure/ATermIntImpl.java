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

package aterm.pure;

import java.util.List;

import jjtraveler.VisitFailure;

import shared.SharedObject;
import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermInt;
import aterm.ATermList;
import aterm.ATermPlaceholder;
import aterm.Visitor;

class ATermIntImpl extends ATermImpl implements ATermInt {
	int value;

	protected ATermIntImpl(PureFactory factory) {
		super(factory);
	}

	public int getType() {
		return ATerm.INT;
	}

	protected void init(int hashCode, ATermList annos, int value) {
		super.init(hashCode, annos);
		this.value = value;
	}

	protected void initHashCode(ATermList annos, int value) {
		this.value = value;
		this.internSetAnnotations(annos);
		this.setHashCode(this.hashFunction());
		//super.init(hashCode, annos);
	}

	public SharedObject duplicate() {
		ATermIntImpl clone = new ATermIntImpl(factory);
		clone.init(hashCode(), getAnnotations(), value);
		return clone;
	}

	public boolean equivalent(SharedObject obj) {
		if (super.equivalent(obj)) {
			ATermInt peer = (ATermInt) obj;
			return peer.getInt() == value;
		}

		return false;
	}

	protected boolean match(ATerm pattern, List list) {
		if (this.equals(pattern)) {
			return true;
		}

		if (pattern.getType() == ATerm.PLACEHOLDER) {
			ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
			if (type.getType() == ATerm.APPL) {
				ATermAppl appl = (ATermAppl) type;
				AFun afun = appl.getAFun();
				if (afun.getName().equals("int") && afun.getArity() == 0 && !afun.isQuoted()) {
					list.add(new Integer(value));
					return true;
				}
			}
		}

		return super.match(pattern, list);
	}

	public int getInt() {
		return value;
	}

	public ATerm setAnnotations(ATermList annos) {
		return getPureFactory().makeInt(value, annos);
	}

	public void accept(Visitor v) throws VisitFailure {
		v.visitInt(this);
	}

	private int hashFunction() {
		/* Set up the internal state */
		int a = 0x9e3779b9; /* the golden ratio; an arbitrary value */
		int b = 0x9e3779b9; /* the golden ratio; an arbitrary value */
		int c = 2; /* the previous hash value */

		/*------------------------------------- handle the last 11 bytes */
		a += (getAnnotations().hashCode() << 8);
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
