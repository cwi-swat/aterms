package apigen.adt.api;

abstract public class ADTConstructor
extends aterm.pure.ATermApplImpl
implements aterm.ATerm
{
  protected aterm.ATerm term = null;

  abstract protected aterm.ATerm getPattern();

  public aterm.ATerm toTerm() {
    if(term == null) {
      java.util.List args = new java.util.LinkedList();
      for(int i = 0; i<getArity() ; i++) {
        args.add(((ADTConstructor) getArgument(i)).toTerm());
      }
      setTerm(getFactory().make(getPattern(), args));
    }
    return term;
  }

  public String toString() {
    return toTerm().toString();
  }

  protected void setTerm(aterm.ATerm term) {
   this.term = term;
  }

  public ADTFactory getADTFactory() {
    return (ADTFactory) getFactory();
  }

  static protected ADTFactory getStaticADTFactory() {
    return (ADTFactory) getStaticFactory();
  }

  public boolean isEntries() {
    return false;
  }

  public boolean isEntry() {
    return false;
  }

}
