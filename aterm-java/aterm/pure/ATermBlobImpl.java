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

import aterm.*;

class ATermBlobImpl extends ATermImpl implements ATermBlob {
	byte[] data;

	protected ATermBlobImpl(PureFactory factory) {
		super(factory);
	}

	public int getType() {
		return ATerm.BLOB;
	}

	protected void init(int hashCode, ATermList annos, byte[] data) {
		super.init(hashCode, annos);
		this.data = data;
	}

	public SharedObject duplicate() {
		ATermBlobImpl clone = new ATermBlobImpl(factory);
		clone.init(hashCode(), getAnnotations(), data);
		return clone;
	}

	public boolean equivalent(SharedObject obj) {
		if (super.equivalent(obj)) {
			ATermBlob peer = (ATermBlob) obj;
			return peer.getBlobData() == data;
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
				if (afun.getName().equals("blob")
					&& afun.getArity() == 0
					&& !afun.isQuoted()) {
					list.add(data);
					return true;
				}
			}
		}

		return super.match(pattern, list);
	}

	public byte[] getBlobData() {
		return data;
	}

	public int getBlobSize() {
		return data.length;
	}

	public ATerm setAnnotations(ATermList annos) {
		return getPureFactory().makeBlob(data, annos);
	}

	public void accept(Visitor v) throws VisitFailure {
		v.visitBlob(this);
	}

}
