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
	/// Summary description for ATermApplImpl.
	/// </summary>
	public class ATermApplImpl : ATermImpl, ATermAppl
	{

		internal AFun fun;
		internal ATerm[] args;

		public ATermApplImpl(PureFactory factory): base(factory)
		{
		}

		public override ATermType getType() 
		{
			return ATermType.APPL;
		}

		internal virtual void init(int hashCode, ATermList annos, AFun fun, ATerm[] i_args) 
		{
			base.init(hashCode, annos);
			this.fun = fun;
			this.args = new ATerm[fun.getArity()];
    
			for(int i=0; i<fun.getArity(); i++) 
			{
				this.args[i] = i_args[i];
			}
		}

		public virtual void initHashCode(ATermList annos, AFun fun, ATerm[] i_args) 
		{
			this.fun  = fun;
			this.args = i_args;
			this.internSetAnnotations(annos);
			this.setHashCode(this.hashFunction());
		}

		public override SharedObject duplicate() 
		{
			ATermApplImpl clone = new ATermApplImpl(factory);
			clone.init(GetHashCode(), getAnnotations(), fun, args);
			return clone;
		}

		internal virtual ATermAppl make(AFun fun, ATerm[] i_args, ATermList annos) 
		{
			return getPureFactory().makeAppl(fun, i_args, annos);
		}

		internal virtual ATermAppl make(AFun fun, ATerm[] i_args) 
		{
			return make(fun, i_args, getPureFactory().makeList());
		}
  
		public override bool equivalent(SharedObject obj) 
		{
			if(base.equivalent(obj)) 
			{
				ATermAppl peer = (ATermAppl) obj;
				if (peer.getAFun().equals(fun)) 
				{
					for (int i=0; i<args.Length; i++) 
					{
						if (!peer.getArgument(i).equals(args[i])) 
						{
							return false;
						}
					}
					return true;
				}
			}
			return false;
		}

		internal override bool match(ATerm pattern, ArrayList list) 
		{
			if (pattern.getType() == ATermType.APPL) 
			{
				ATermAppl appl = (ATermAppl) pattern;
				if (fun.equals(appl.getAFun())) 
				{
					return matchArguments(appl.getArgumentArray(), list);
				} 
				else 
				{
					return false;
				}
			}

			if (pattern.getType() == ATermType.PLACEHOLDER) 
			{
				ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
				if (type.getType() == ATermType.APPL) 
				{
					ATermAppl appl = (ATermAppl) type;
					AFun afun = appl.getAFun();
					if (afun.getName().Equals("appl") && !afun.isQuoted()) 
					{
						list.Add(fun.getName());
						return matchArguments(appl.getArgumentArray(), list);
					} 
					else if (afun.getName().Equals("str") && !afun.isQuoted()) 
					{
						if (fun.isQuoted()) 
						{
							list.Add(fun.getName());
							return matchArguments(appl.getArgumentArray(), list);
						}
					} 
					else if (afun.getName().Equals("fun") && !afun.isQuoted()) 
					{
						if (!fun.isQuoted()) 
						{
							list.Add(fun.getName());
							return matchArguments(appl.getArgumentArray(), list);
						}
					} 
					else if (afun.getName().Equals("id") && !afun.isQuoted()) 
					{
						if (!fun.isQuoted()) 
						{
							list.Add(fun.getName());
							return matchArguments(appl.getArgumentArray(), list);
						}
					}
				}
			}
			return base.match(pattern, list);
		}

		internal virtual bool matchArguments(ATerm[] pattern_args, ArrayList list) 
		{
			for (int i = 0; i < args.Length; i++) 
			{
				if (i >= pattern_args.Length) 
				{
					return false;
				}

				ATerm arg = args[i];
				ATerm pattern_arg = pattern_args[i];

				if (pattern_arg.getType() == ATermType.PLACEHOLDER) 
				{
					ATerm ph_type = ((ATermPlaceholder) pattern_arg).getPlaceholder();
					if (ph_type.getType() == ATermType.APPL) 
					{
						ATermAppl appl = (ATermAppl) ph_type;
						if (appl.getName().Equals("list") && appl.getArguments().isEmpty()) 
						{
							ATermList result = ((PureFactory) getFactory()).getEmpty();
							for (int j = args.Length - 1; j >= i; j--) 
							{
								result = result.insert(args[j]);
							}
							list.Add(result);
							return true;
						}
					}
				}

				ArrayList submatches = arg.match(pattern_arg);
				if (submatches == null) 
				{
					return false;
				}
				list.AddRange(submatches);
			}

			return args.Length == pattern_args.Length;
		}

		public virtual ATerm[] getArgumentArray() 
		{
			return args;
		}

		public virtual AFun getAFun() 
		{
			return fun;
		}

		public virtual ATermList getArguments() 
		{
			ATermList result = ((PureFactory) getFactory()).getEmpty();

			for (int i = args.Length - 1; i >= 0; i--) 
			{
				result = result.insert(args[i]);
			}
			return result;
		}

		public virtual ATerm getArgument(int index) 
		{
			return args[index];
		}

		public virtual ATermAppl setArgument(ATerm newarg, int index) 
		{
			ATerm[] newargs = (ATerm[]) args.Clone();
			newargs[index] = newarg;

			return make(fun, newargs, getAnnotations());
		}

		public virtual bool isQuoted() 
		{
			return fun.isQuoted();
		}

		public virtual string getName() 
		{
			return fun.getName();
		}

		public virtual int getArity() 
		{
			return args.Length;
		}

		public override ATerm make(ArrayList args) 
		{
			ATerm[] newargs = new ATerm[this.args.Length];
			for (int i = 0; i < this.args.Length; i++) 
			{
				newargs[i] = this.args[i].make(args);
			}
			//return make(fun, newargs);
			return getPureFactory().makeAppl(fun, newargs, getPureFactory().makeList());
		}

		public override ATerm setAnnotations(ATermList annos) 
		{
			return make(fun, args, annos);
		}

		public override void accept(ATermVisitor v) // throws VisitFailure 
		{
			v.visitAppl(this);
		}

		public override int getNrSubTerms() 
		{
			return args.Length;
		}

		public override ATerm getSubTerm(int index) 
		{
			return args[index];
		}

		public override ATerm setSubTerm(int index, ATerm t) 
		{
			return setArgument(t, index);
		}

		internal virtual Object[] serialize() 
		{
			int arity = getArity();
			Object[] o = new Object[arity+2];
			for(int i=0; i<arity ; i++) 
			{
				o[i] = getArgument(i);
			}
			o[arity]   = getAnnotations();
			o[arity+1] = getAFun();
			return o;
		}

		internal virtual int hashFunction() 
		{
			int initval = 0; /* the previous hash value */
			int a, b, c, len;

			/* Set up the internal state */
			len = getArity();
			a = b = unchecked((int)0x9e3779b9); /* the golden ratio; an arbitrary value */
			c = initval; /* the previous hash value */
			/*---------------------------------------- handle most of the key */
			if (len >= 12) 
			{
				return staticDoobs_hashFuntion(serialize());
				//return PureFactory.doobs_hashFunction(serialize());
			}

			/*------------------------------------- handle the last 11 bytes */
			c += len;
			c += (getAnnotations().GetHashCode()<<8);
			b += (getAFun().GetHashCode()<<8);
    
			switch (len) 
			{
				case 11 :c += (getArgument(10).GetHashCode() << 24);goto case 10;
				case 10 :c += (getArgument(9).GetHashCode() << 16);goto case 9;
				case 9 : c += (getArgument(8).GetHashCode() << 8);goto case 8;
				case 8 : b += (getArgument(7).GetHashCode() << 24);goto case 7;
				case 7 : b += (getArgument(6).GetHashCode() << 16);goto case 6;
				case 6 : b += (getArgument(5).GetHashCode() << 8);goto case 5;
				case 5 : b += (getArgument(4).GetHashCode());goto case 4;
				case 4 : a += (getArgument(3).GetHashCode() << 24);goto case 3;
				case 3 : a += (getArgument(2).GetHashCode() << 16);goto case 2;
				case 2 : a += (getArgument(1).GetHashCode() << 8);goto case 1;
				case 1 : a += (getArgument(0).GetHashCode());break;
					/* case 0: nothing left to add */
			}
			a -= b; a -= c; a ^= (c >> 13);
			b -= c; b -= a; b ^= (a << 8);
			c -= a; c -= b; c ^= (b >> 13);
			a -= b; a -= c; a ^= (c >> 12);
			b -= c; b -= a; b ^= (a << 16);
			c -= a; c -= b; c ^= (b >> 5);
			a -= b; a -= c; a ^= (c >> 3);
			b -= c; b -= a; b ^= (a << 10);
			c -= a; c -= b; c ^= (b >> 15);

			/*-------------------------------------------- report the result */
			return c;
		}

		static private int staticDoobs_hashFuntion(Object[] o) 
		{
			//System.out.println("static doobs_hashFuntion");
    
			int initval = 0; /* the previous hash value */
			int a,b,c,len;

			/* Set up the internal state */
			len = o.Length;
			a = b = unchecked((int)0x9e3779b9);  /* the golden ratio; an arbitrary value */
			c = initval;         /* the previous hash value */

			/*---------------------------------------- handle most of the key */
			int k=0;
			while(len >= 12) 
			{
				a += (o[k+0].GetHashCode() +(o[k+1].GetHashCode()<<8) +
					(o[k+2].GetHashCode()<<16) +(o[k+3].GetHashCode()<<24));
				b += (o[k+4].GetHashCode() +(o[k+5].GetHashCode()<<8) +
					(o[k+6].GetHashCode()<<16) +(o[k+7].GetHashCode()<<24));
				c += (o[k+8].GetHashCode() +(o[k+9].GetHashCode()<<8) +
					(o[k+10].GetHashCode()<<16)+(o[k+11].GetHashCode()<<24));
				//mix(a,b,c);
				a -= b; a -= c; a ^= (c>>13); 
				b -= c; b -= a; b ^= (a<<8); 
				c -= a; c -= b; c ^= (b>>13); 
				a -= b; a -= c; a ^= (c>>12);  
				b -= c; b -= a; b ^= (a<<16); 
				c -= a; c -= b; c ^= (b>>5); 
				a -= b; a -= c; a ^= (c>>3);  
				b -= c; b -= a; b ^= (a<<10); 
				c -= a; c -= b; c ^= (b>>15);
      
				k += 12;
				len -= 12;
			}

			/*------------------------------------- handle the last 11 bytes */
			c += o.Length;
			switch(len)              /* all the case statements fall through */
			{
				case 11: c+=(o[k+10].GetHashCode()<<24);goto case 10;
				case 10: c+=(o[k+9].GetHashCode()<<16);goto case 9;
				case 9 : c+=(o[k+8].GetHashCode()<<8);goto case 8;
					/* the first byte of c is reserved for the length */
				case 8 : b+=(o[k+7].GetHashCode()<<24);goto case 7;
				case 7 : b+=(o[k+6].GetHashCode()<<16);goto case 6;
				case 6 : b+=(o[k+5].GetHashCode()<<8);goto case 5;
				case 5 : b+=o[k+4].GetHashCode();goto case 4;
				case 4 : a+=(o[k+3].GetHashCode()<<24);goto case 3;
				case 3 : a+=(o[k+2].GetHashCode()<<16);goto case 2;
				case 2 : a+=(o[k+1].GetHashCode()<<8);goto case 1;
				case 1 : a+=o[k+0].GetHashCode();break;
					/* case 0: nothing left to add */
			}
			//mix(a,b,c);
			a -= b; a -= c; a ^= (c>>13); 
			b -= c; b -= a; b ^= (a<<8); 
			c -= a; c -= b; c ^= (b>>13); 
			a -= b; a -= c; a ^= (c>>12);  
			b -= c; b -= a; b ^= (a<<16); 
			c -= a; c -= b; c ^= (b>>5); 
			a -= b; a -= c; a ^= (c>>3);  
			b -= c; b -= a; b ^= (a<<10); 
			c -= a; c -= b; c ^= (b>>15); 
   
			/*-------------------------------------------- report the result */
			return c;
		}

	}
}