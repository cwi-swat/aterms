package aterm.pure;

import java.util.List;

import shared.SharedObject;

import aterm.*;

class ATermApplImpl extends ATermImpl implements ATermAppl {
  AFun fun;
  ATerm[] args;

  public int getType() {
    return ATerm.APPL;
  }

  protected ATermApplImpl(PureFactory factory) {
    super(factory);
  }

  protected void init(int hashCode, ATermList annos, AFun fun, ATerm[] i_args) {
    super.init(hashCode, annos);
    this.fun = fun;
    this.args = i_args;
  }

  protected void initCopy(int hashCode, ATermList annos, AFun fun, ATerm[] i_args) {
    super.init(hashCode, annos);
    this.fun = fun;
    this.args = new ATerm[fun.getArity()];
    for(int i=0; i<this.args.length; i++) {
      this.args[i] = i_args[i];
    }
  }

  public Object clone() {
    ATermApplImpl clone = new ATermApplImpl(getPureFactory());
    clone.init(hashCode(), getAnnotations(), fun, args);
    return clone;
  }
  
  public boolean equivalent(SharedObject obj) {
    if(super.equivalent(obj)) {
      ATermAppl peer = (ATermAppl) obj;
      if (peer.getAFun().equals(fun)) {
        for (int i=0; i<args.length; i++) {
          if (!peer.getArgument(i).equals(args[i])) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  protected boolean match(ATerm pattern, List list) {
    if (pattern.getType() == APPL) {
      ATermAppl appl = (ATermAppl) pattern;
      if (fun.equals(appl.getAFun())) {
        return matchArguments(appl.getArgumentArray(), list);
      } else {
        return false;
      }
    }

    if (pattern.getType() == PLACEHOLDER) {
      ATerm type = ((ATermPlaceholder) pattern).getPlaceholder();
      if (type.getType() == APPL) {
        ATermAppl appl = (ATermAppl) type;
        AFun afun = appl.getAFun();
        if (afun.getName().equals("appl") && !afun.isQuoted()) {
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

  boolean matchArguments(ATerm[] pattern_args, List list) {
    for (int i = 0; i < args.length; i++) {
      if (i >= pattern_args.length) {
        return false;
      }

      ATerm arg = args[i];
      ATerm pattern_arg = pattern_args[i];

      if (pattern_arg.getType() == PLACEHOLDER) {
        ATerm ph_type = ((ATermPlaceholder) pattern_arg).getPlaceholder();
        if (ph_type.getType() == APPL) {
          ATermAppl appl = (ATermAppl) ph_type;
          if (appl.getName().equals("list") && appl.getArguments().isEmpty()) {
            ATermList result = PureFactory.empty;
            for (int j = args.length - 1; j >= i; j--) {
              result = getPureFactory().makeList(args[j], result);
            }
            list.add(result);
            return true;
          }
        }
      }

      List submatches = arg.match(pattern_arg);
      if (submatches == null) {
        return false;
      }
      list.addAll(submatches);
    }

    return args.length == pattern_args.length;
  }

  public ATerm[] getArgumentArray() {
    return args;
  }

  public AFun getAFun() {
    return fun;
  }

  public ATermList getArguments() {
    ATermList result = PureFactory.empty;

    for (int i = args.length - 1; i >= 0; i--) {
      result = getPureFactory().makeList(args[i], result);
    }

    return result;
  }

  public ATerm getArgument(int index) {
    return args[index];
  }

  public ATermAppl setArgument(ATerm newarg, int index) {
    ATerm[] newargs = (ATerm[]) args.clone();
    newargs[index] = newarg;

    return getPureFactory().makeAppl(fun, newargs);
  }

  public boolean isQuoted() {
    return fun.isQuoted();
  }

  public String getName() {
    return fun.getName();
  }

  public int getArity() {
    return args.length;
  }

  public ATerm make(List args) {
    ATerm[] newargs = new ATerm[this.args.length];
    for (int i = 0; i < this.args.length; i++) {
      newargs[i] = this.args[i].make(args);
    }
    return getPureFactory().makeAppl(fun, newargs);
  }

  public ATerm setAnnotations(ATermList annos) {
    return getPureFactory().makeAppl(fun, args, annos);
  }

  public void accept(ATermVisitor v) throws ATermVisitFailure {
    v.visitAppl(this);
  }

  public int getNrSubTerms() {
    return args.length;
  }

  public ATerm getSubTerm(int index) {
    return args[index];
  }

  public ATerm setSubTerm(int index, ATerm t) {
    return setArgument(t, index);
  }

}
