package aterm.pure;

import aterm.*;
import java.util.List;

class ATermApplImpl
  extends ATermImpl
  implements ATermAppl
{
  AFun fun;
  ATerm[] args;

  //{{{ static int hashFunction(AFun fun, ATerm[] args, ATermList annos)

  static int hashFunction(AFun fun, ATerm[] args, ATermList annos)
  {
    int hnr;

    hnr = fun.hashCode();
    for (int i=0; i<args.length; i++) {
      hnr = (hnr << 1) ^ (hnr >> 1) ^ args[i].hashCode();
    }

    return Math.abs(hnr + annos.hashCode());
  }

  //}}}

  //{{{ public int getType()

  public int getType()
  {
    return ATerm.APPL;
  }

  //}}}

  //{{{ protected ATermApplImpl(PureFactory factory, AFun fun, ATerm[] args, annos)

  protected ATermApplImpl(PureFactory factory, AFun fun, ATerm[] args,
			  ATermList annos)
  {
    super(factory, annos);
    setHashCode(hashFunction(fun, args, annotations));
    this.fun  = fun;
    this.args = args;
  }

  //}}}
  //{{{ protected boolean match(ATerm pattern, List list)

  protected boolean match(ATerm pattern, List list)
  {
    if (pattern.getType() == APPL) {
      ATermAppl appl = (ATermAppl)pattern;
      if (fun.equals(appl.getAFun())) {
	return matchArguments(appl.getArgumentArray(), list);
      } else {
	return false;
      }
    }

    if (pattern.getType() == PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder)pattern).getPlaceholder();
      if (type.getType() == APPL) {
	ATermAppl appl = (ATermAppl)type;
	AFun  afun = appl.getAFun();
	if(afun.getName().equals("appl") && !afun.isQuoted()) {
	  list.add(fun.getName());
	  return matchArguments(appl.getArgumentArray(), list);
	} else if (afun.getName().equals("str") && !afun.isQuoted()) {
	  if (fun.isQuoted()) {
	    list.add(fun.getName());
	    return matchArguments(appl.getArgumentArray(), list);
	  }
	} else if (afun.getName().equals("fun") && !afun.isQuoted()) {
	  if (!fun.isQuoted()) {
	    list.add(fun.getName());
	    return matchArguments(appl.getArgumentArray(), list);
	  }
	} else if (afun.getName().equals("id") && !afun.isQuoted()) {
	  if (!fun.isQuoted()) {
	    list.add(fun.getName());
	    return matchArguments(appl.getArgumentArray(), list);
	  }
	}
      }
    }

    return super.match(pattern, list);
  }

  //}}}
  //{{{ boolean matchArguments(ATerm[] args, List list)

  boolean matchArguments(ATerm[] pattern_args, List list)
  {
    for (int i=0; i<args.length; i++) {
      if(i >= pattern_args.length) {
	return false;
      }

      ATerm arg = args[i];
      ATerm pattern_arg = pattern_args[i];

      if(pattern_arg.getType() == PLACEHOLDER) {
	ATerm ph_type = ((ATermPlaceholder)pattern_arg).getPlaceholder();
	if (ph_type.getType() == APPL) {
	  ATermAppl appl = (ATermAppl)ph_type;
	  if (appl.getName().equals("list") && appl.getArguments().isEmpty()) {
	    ATermList result = PureFactory.empty;
	    for (int j=args.length-1; j>=i; j--) {
	      result = factory.makeList(args[j], result);
	    }
	    list.add(result);
	    return true;
	  }
	}
      }

      List submatches = arg.match(pattern_arg);
      if(submatches == null) {
	return false;
      }
      list.addAll(submatches);
    }

    return args.length == pattern_args.length;
  }

  //}}}
  //{{{ public ATerm[] getArgumentArray()

  public ATerm[] getArgumentArray()
  {
    return args;
  }

  //}}}

  //{{{ public AFun getAFun()

  public AFun getAFun()
  {
    return fun;
  }

  //}}}
  //{{{ public ATermList getArguments()

  public ATermList getArguments()
  {
    ATermList result = PureFactory.empty;

    for (int i=args.length-1; i>=0; i--) {
      result = factory.makeList(args[i], result);
    }

    return result;
  }

  //}}}

  //{{{ public ATerm getArgument(int index)

  public ATerm getArgument(int index)
  {
    return args[index];
  }

  //}}}
  //{{{ public ATermAppl setArgument(ATerm newarg, int index)

  public ATermAppl setArgument(ATerm newarg, int index)
  {
    ATerm[] newargs = (ATerm [])args.clone();
    newargs[index] = newarg;

    return factory.makeAppl(fun, newargs);
  }

  //}}}

  //{{{ public boolean isQuoted()

  public boolean isQuoted()
  {
    return fun.isQuoted();
  }

  //}}}
  //{{{ public String getName()

  public String getName()
  {
    return fun.getName();
  }

  //}}}
  //{{{ public int getArity()

  public int getArity()
  {
    return args.length;
  }

  //}}}

  //{{{ public ATerm make(List args)

  public ATerm make(List args)
  {
    ATerm[] newargs = new ATerm[this.args.length];
    for (int i=0; i<this.args.length; i++) {
      newargs[i] = this.args[i].make(args);
    }
    return factory.makeAppl(fun, newargs);
  }

  //}}}
  //{{{ public ATerm setAnnotations(ATermList annos)

  public ATerm setAnnotations(ATermList annos)
  {
    return factory.makeAppl(fun, args, annos);
  }

  //}}}

  //{{{ public void accept(ATermVisitor v)

  public void accept(ATermVisitor v)
    throws ATermVisitFailure
  {
    v.visitAppl(this);
  }

  //}}}
  //{{{ public int getNrSubTerms()

  public int getNrSubTerms()
  {
    return args.length;
  }

  //}}}
  //{{{ public ATerm getSubTerm(int index)

  public ATerm getSubTerm(int index)
  {
    return args[index];
  }

  //}}}
  //{{{ public ATerm setSubTerm(int index, ATerm t)

  public ATerm setSubTerm(int index, ATerm t)
  {
    return setArgument(t,index);
  }

  //}}}
}
