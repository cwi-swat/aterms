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
	/// Summary description for ATermListImpl.
	/// </summary>
	public class ATermListImpl : ATermImpl, ATermList
	{
		internal ATerm first;
		internal ATermList next;
		internal int length;

		public ATermListImpl(PureFactory factory) : base(factory)
		{
		}

		public override ATermType getType() 
		{
			return ATermType.LIST;
		}

		/**
		 * init is used internally by the PureFactory to initialize a prototype of
		 * an ATermList without using the new operator all the time
		 * 
		 */
		public virtual void init(int hashCode, ATermList annos, ATerm first, ATermList next) 
		{
			base.init(hashCode, annos);
			this.first = first;
			this.next = next;

			if (first == null && next == null) 
			{
				this.length = 0;
			}
			else 
			{
				this.length = 1 + next.getLength();
			}
		}

		public virtual void initHashCode(ATermList annos, ATerm first, ATermList next) 
		{
			this.first = first;
			this.next = next;
			this.internSetAnnotations(annos);
			this.setHashCode(this.hashFunction());
			//super.init(hashCode, annos);

			if (first == null && next == null) 
			{
				this.length = 0;
			}
			else 
			{
				this.length = 1 + next.getLength();
			}
		}

		public override SharedObject duplicate() 
		{
			ATermListImpl clone = new ATermListImpl(factory);
			clone.init(GetHashCode(), getAnnotations(), first, next);
			return clone;
		}

		public override bool equivalent(SharedObject obj) 
		{
			if (base.equivalent(obj)) 
			{
				ATermList peer = (ATermList) obj;
				if (peer.getLength() == length) 
				{
					return peer.getFirst().Equals(first) && peer.getNext().Equals(next);
				}
			}
			return false;
		}

		public virtual ATermList insert(ATerm el) 
		{
			return getPureFactory().makeList(el, this);
		}

		public virtual ATermList getEmpty() 
		{
			return getPureFactory().makeList();
		}

		public override ATerm setAnnotations(ATermList annos) 
		{
			return getPureFactory().makeList(first, next, annos);
		}

		internal override bool match(ATerm pattern, ArrayList list) 
		{
			if (pattern.getType() == ATermType.LIST) 
			{
				ATermList l = (ATermList) pattern;

				if (l.isEmpty()) 
				{
					return this.isEmpty();
				}

				if (l.getFirst().getType() == ATermType.PLACEHOLDER) 
				{
					ATerm ph_type = ((ATermPlaceholder) l.getFirst()).getPlaceholder();
					if (ph_type.getType() == ATermType.APPL) 
					{
						ATermAppl appl = (ATermAppl) ph_type;
						if (appl.getName().Equals("list") && appl.getArguments().isEmpty()) 
						{
							list.Add(this);
							return true;
						}
					}
				}

				if (!isEmpty()) 
				{
					ArrayList submatches = first.match(l.getFirst());
					if (submatches == null) 
					{
						return false;
					}

					list.AddRange(submatches);

					submatches = next.match(l.getNext());

					if (submatches == null) 
					{
						return false;
					}

					list.AddRange(submatches);
					return true;
				}
				else 
				{
					return l.isEmpty();
				}
			}

			return base.match(pattern, list);
		}

		public override ATerm make(ArrayList args) 
		{
			if (first == null) 
			{
				return this;
			}

			ATerm head = first.make(args);
			ATermList tail = (ATermList) next.make(args);
			if (isListPlaceHolder(first)) 
			{
				/*
				 * this is to solve the make([<list>],[]) problem
				 * the result should be [] and not [[]]
				 * to be compatible with the C version
				 */
				return head;
			}
			else 
			{
				return tail.insert(head);
			}

		}

		private bool isListPlaceHolder(ATerm pattern) 
		{
			if (pattern.getType() == ATermType.PLACEHOLDER) 
			{
				ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
				if (type.getType() == ATermType.APPL) 
				{
					ATermAppl appl = (ATermAppl) type;
					AFun afun = appl.getAFun();
					if (afun.getName().Equals("list") && afun.getArity() == 0 && !afun.isQuoted()) 
					{
						return true;
					}
				}
			}
			return false;
		}

		public virtual bool isEmpty() 
		{
			return this == ((PureFactory) getFactory()).getEmpty();
		}

		public virtual int getLength() 
		{
			return length;
		}

		public virtual ATerm getFirst() 
		{
			return first;
		}

		public virtual ATermList getNext() 
		{
			return next;
		}

		public virtual ATerm getLast() 
		{
			ATermList cur;

			cur = this;
			while (!cur.getNext().isEmpty()) 
			{
				cur = cur.getNext();
			}

			return cur.getFirst();
		}

		public virtual int indexOf(ATerm el, int start) 
		{
			int i;
			ATermList cur;

			if (start < 0) 
			{
				start += length + 1;
			}

			if (start > length) 
			{
				throw new ArgumentException("start (" + start + ") > length of list (" + length + ")");
			}

			cur = this;
			for (i = 0; i < start; i++) 
			{
				cur = cur.getNext();
			}

			while (!cur.isEmpty() && cur.getFirst() != el) 
			{
				cur = cur.getNext();
				++i;
			}

			return cur.isEmpty() ? -1 : i;
		}

		public virtual int lastIndexOf(ATerm el, int start) 
		{
			int result;

			if (start < 0) 
			{
				start += length + 1;
			}

			if (start > length) 
			{
				throw new ArgumentException("start (" + start + ") > length of list (" + length + ")");
			}

			if (start > 0) 
			{
				result = next.lastIndexOf(el, start - 1);
				if (result >= 0) 
				{
					return result + 1;
				}
			}

			if (first == el) 
			{
				return 0;
			}
			else 
			{
				return -1;
			}
		}

		public virtual ATermList concat(ATermList rhs) 
		{
			if (isEmpty()) 
			{
				return rhs;
			}

			if (next.isEmpty()) 
			{
				return rhs.insert(first);
			}

			return next.concat(rhs).insert(first);
		}

		public ATermList append(ATerm el) 
		{
			return this.concat(getEmpty().insert(el));
		}

		public virtual ATerm elementAt(int index) 
		{
			if (0 > index || index > length) 
			{
				throw new ArgumentException("illegal list index: " + index);
			}

			ATermList cur = this;
			for (int i = 0; i < index; i++) 
			{
				cur = cur.getNext();
			}

			return cur.getFirst();
		}

		public virtual ATermList remove(ATerm el) 
		{
			if (first == el) 
			{
				return next;
			}

			ATermList result = next.remove(el);

			if (result == next) 
			{
				return this;
			}

			return result.insert(first);
		}

		public virtual ATermList removeElementAt(int index) 
		{
			if (0 > index || index > length) 
			{
				throw new ArgumentException("illegal list index: " + index);
			}

			if (index == 0) 
			{
				return next;
			}

			return next.removeElementAt(index - 1).insert(first);
		}

		public virtual ATermList removeAll(ATerm el) 
		{
			if (first == el) 
			{
				return next.removeAll(el);
			}

			ATermList result = next.removeAll(el);

			if (result == next) 
			{
				return this;
			}

			return result.insert((ATerm) first);
		}

		public virtual ATermList insertAt(ATerm el, int i) 
		{
			if (0 > i || i > length) 
			{
				throw new ArgumentException("illegal list index: " + i);
			}

			if (i == 0) 
			{
				return insert(el);
			}

			return next.insertAt(el, i - 1).insert(first);
		}

		public virtual ATermList getPrefix() 
		{
			ATermList cur, next;
			ArrayList elems;

			if (isEmpty()) 
			{
				return this;
			}

			cur = this;
			elems = new ArrayList();

			while (true) 
			{
				next = cur.getNext();
				if (next.isEmpty()) 
				{
					cur = ((PureFactory) getFactory()).getEmpty();
					for (int i = elems.Count - 1; i >= 0; i--) 
					{
						cur = cur.insert((ATerm) elems[i]);
					}
					return cur;
				}
				else 
				{
					elems.Add(cur.getFirst());
					cur = cur.getNext();
				}
			}
		}

		public virtual ATermList getSlice(int start, int end) 
		{
			int i, size = end - start;
			ATermList result = ((PureFactory) getFactory()).getEmpty();
			ATermList list;

			ArrayList buffer = new ArrayList(size);

			list = this;
			for (i = 0; i < start; i++) 
			{
				list = list.getNext();
			}

			for (i = 0; i < size; i++) 
			{
				buffer.Add(list.getFirst());
				list = list.getNext();
			}

			for (--i; i >= 0; i--) 
			{
				result = result.insert((ATerm) buffer[i]);
			}

			return result;
		}

		public virtual ATermList replace(ATerm el, int i) 
		{
			int lcv;
			ArrayList buffer;
			ATermList cur;

			if (0 > i || i > length) 
			{
				throw new ArgumentException("illegal list index: " + i);
			}

			buffer = new ArrayList(i);

			cur = this;
			for (lcv = 0; lcv < i; lcv++) 
			{
				buffer.Add(cur.getFirst());
				cur = cur.getNext();
			}

			/* Skip the old element */
			cur = cur.getNext();

			/* Add the new element */
			cur = cur.insert(el);

			/* Add the prefix */
			for (--lcv; lcv >= 0; lcv--) 
			{
				cur = cur.insert((ATerm) buffer[lcv]);
			}

			return cur;
		}

		public virtual ATermList reverse() 
		{
			ATermList cur = this;
			ATermList reverse = this.getEmpty();
			while (!cur.isEmpty()) 
			{
				reverse = reverse.insert(cur.getFirst());
				cur = cur.getNext();
			}
			return reverse;
		}

		public virtual ATerm dictGet(ATerm key) 
		{
			if (isEmpty()) 
			{
				return null;
			}

			ATermList pair = (ATermList) first;

			if (key.equals(pair.getFirst())) 
			{
				return pair.getNext().getFirst();
			}

			return next.dictGet(key);
		}

		public virtual ATermList dictPut(ATerm key, ATerm value) 
		{
			ATermList pair;

			if (isEmpty()) 
			{
				pair = getEmpty().insert(value).insert(key);
				return getEmpty().insert(pair);
			}

			pair = (ATermList) first;

			if (key.equals(pair.getFirst())) 
			{
				pair = getEmpty().insert(value).insert(pair);
				return next.insert(pair);

			}

			return (ATermList) next.dictPut(key, value).insert(first).setAnnotations(getAnnotations());
			//return getPureFactory().makeList(first, next.dictPut(key, value), getAnnotations());
		}

		public virtual ATermList dictRemove(ATerm key) 
		{
			ATermList pair;

			if (isEmpty()) 
			{
				return this;
			}

			pair = (ATermList) first;

			if (key.equals(pair.getFirst())) 
			{
				return next;
			}

			return (ATermList) next.dictRemove(key).insert(first).setAnnotations(getAnnotations());
			//return getPureFactory().makeList(first, next.dictRemove(key), getAnnotations());
		}

		public override void accept(ATermVisitor v) // throws VisitFailure 
		{
			v.visitList(this);
		}

		public override int getNrSubTerms() 
		{
			return length;
		}

		public override ATerm getSubTerm(int index) 
		{
			return elementAt(index);
		}

		public override ATerm setSubTerm(int index, ATerm t) 
		{
			return replace(t, index);
		}

		private int hashFunction() 
		{
			/* Set up the internal state */
			int a = unchecked((int)0x9e3779b9); /* the golden ratio; an arbitrary value */
			int b = unchecked((int)0x9e3779b9); /* the golden ratio; an arbitrary value */
			int c = 3; /* the previous hash value */

			/*------------------------------------- handle the last 11 bytes */
			a += (getAnnotations().GetHashCode() << 16);
			a += (next.GetHashCode() << 8);
			a += (first.GetHashCode());

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

