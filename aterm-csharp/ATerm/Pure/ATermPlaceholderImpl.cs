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
 */using System;
   using System.Collections;
   using SharedObjects;

   namespace aterm
   {
	   /// <summary>
	   /// Summary description for ATermPlaceholderImpl.
	   /// </summary>
	   public class ATermPlaceholderImpl : ATermImpl, ATermPlaceholder
	   {
		   internal ATerm type;

		   public ATermPlaceholderImpl(PureFactory factory) : base(factory)
		   {
		   }

		   public override ATermType getType() 
		   {
			   return ATermType.PLACEHOLDER;
		   }

		   public virtual void init(int hashCode, ATermList annos, ATerm type) 
		   {
			   base.init(hashCode, annos);
			   this.type = type;
		   }

		   public override SharedObject duplicate() 
		   {
			   ATermPlaceholderImpl clone = new ATermPlaceholderImpl(factory);
			   clone.init(GetHashCode(), getAnnotations(), type);
			   return clone;
		   }

		   public override bool equivalent(SharedObject obj) 
		   {
			   if (base.equivalent(obj)) 
			   {
				   ATermPlaceholder peer = (ATermPlaceholder) obj;
				   return peer.getPlaceholder() == type;
			   }
			   return false;
		   }

		   internal override bool match(ATerm pattern, ArrayList list) 
		   {
			   if (pattern.getType() == ATermType.PLACEHOLDER) 
			   {
				   ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
				   if (type.getType() == ATermType.APPL) 
				   {
					   ATermAppl appl = (ATermAppl) type;
					   AFun afun = appl.getAFun();
					   if (afun.getName().Equals("placeholder") && afun.getArity() == 0 && !afun.isQuoted()) 
					   {
						   list.Add(type);
						   return true;
					   }
				   }
			   }

			   return base.match(pattern, list);
		   }

		   public override ATerm make(ArrayList args) 
		   {
			   PureFactory factory = getPureFactory();
			   ATermAppl appl;
			   AFun fun;
			   string name;

			   appl = (ATermAppl) type;
			   fun = appl.getAFun();
			   name = fun.getName();
			   if (!fun.isQuoted()) 
			   {
				   if (fun.getArity() == 0) 
				   {
					   if (name.Equals("term")) 
					   {
						   ATerm t = (ATerm) args[0];
						   args.RemoveAt(0);

						   return t;
					   } 
					   else if (name.Equals("list")) 
					   {
						   ATermList l = (ATermList) args[0];
						   args.RemoveAt(0);

						   return l;
					   } 
					   else if (name.Equals("bool")) 
					   {
						   bool b = (bool) args[0];
						   args.RemoveAt(0);

						   return factory.makeAppl(factory.makeAFun(b.ToString(), 0, false));
					   } 
					   else if (name.Equals("int")) 
					   {
						   int i = (int) args[0];
						   args.RemoveAt(0);

						   return factory.makeInt(i);
					   } 
					   else if (name.Equals("real")) 
					   {
						   double d = (double) args[0];
						   args.RemoveAt(0);

						   return factory.makeReal(d);
					   } 
					   else if (name.Equals("placeholder")) 
					   {
						   ATerm atype = (ATerm) args[0];
						   args.RemoveAt(0);
						   return factory.makePlaceholder(atype);
					   } 
					   else if (name.Equals("str")) 
					   {
						   string str = (string) args[0];
						   args.RemoveAt(0);
						   return factory.makeAppl(factory.makeAFun(str, 0, true));
					   } 
					   else if (name.Equals("id")) 
					   {
						   string str = (string) args[0];
						   args.RemoveAt(0);
						   return factory.makeAppl(factory.makeAFun(str, 0, false));
					   } 
					   else if (name.Equals("fun")) 
					   {
						   string str = (string) args[0];
						   args.RemoveAt(0);
						   return factory.makeAppl(factory.makeAFun(str, 0, false));
					   }
				   }
				   if (name.Equals("appl")) 
				   {
					   ATermList oldargs = appl.getArguments();
					   string newname = (string) args[0];
					   args.RemoveAt(0);
					   ATermList newargs = (ATermList) oldargs.make(args);
					   AFun newfun = factory.makeAFun(newname, newargs.getLength(), false);
					   return factory.makeApplList(newfun, newargs);
				   }
			   }
			   throw new Exception("illegal pattern: " + this);
		   }

		   public virtual ATerm getPlaceholder() 
		   {
			   return type;
		   }

		   public virtual ATerm setPlaceholder(ATerm newtype) 
		   {
			   return getPureFactory().makePlaceholder(newtype, getAnnotations());
		   }

		   public override ATerm setAnnotations(ATermList annos) 
		   {
			   return getPureFactory().makePlaceholder(type, annos);
		   }

		   public override void accept(ATermVisitor v) // throws VisitFailure 
		   {
			   v.visitPlaceholder(this);
		   }

		   public override int getNrSubTerms() 
		   {
			   return 1;
		   }

		   public override ATerm getSubTerm(int index) 
		   {
			   return type;
		   }

		   public override ATerm setSubTerm(int index, ATerm t) 
		   {
			   if (index == 1) 
			   {
				   return setPlaceholder(t);
			   } 
			   else 
			   {
				   throw new Exception("no " + index + "-th child!");
			   }
		   }
	   }
   }


