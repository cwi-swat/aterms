package apigen.adt.api;

abstract public class Separator_DefaultImpl
extends Separator
{
  protected void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super.init(hashCode, annos, fun, args);
  }
  protected void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] i_args) {
  	super.initHashCode(annos, fun, i_args);
  }
  protected Separator_DefaultImpl(ADTFactory factory) {
    super(factory);
  }
  private static int index_termPattern = 0;
  public shared.SharedObject duplicate() {
    Separator_Default clone = new Separator_Default(factory);
     clone.init(hashCode(), getAnnotations(), getAFun(), getArgumentArray());
    return clone;
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Separator_Default) {
      return super.equivalent(peer);
    }
    return false;
  }
  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] i_args, aterm.ATermList annos) {
    return getADTFactory().makeSeparator_Default(fun, i_args, annos);
  }
  public aterm.ATerm toTerm() {
    if (term == null) {
      term = getADTFactory().toTerm(this);
    }
    return term;
  }

  public boolean isDefault()
  {
    return true;
  }

  public boolean hasTermPattern()
  {
    return true;
  }

  public aterm.ATerm getTermPattern()
  {
   return this.getArgument(index_termPattern);
  }

  public Separator setTermPattern(aterm.ATerm _termPattern)
  {
    return (Separator) super.setArgument(_termPattern, index_termPattern);
  }

  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 0 of a Separator_Default should have type term");
        }
        break;
      default: throw new RuntimeException("Separator_Default does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }
}
