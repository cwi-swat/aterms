package apigen.adt.api.types.separator;

public class Default extends apigen.adt.api.types.Separator {
  public Default(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super(factory, annos, fun, args);
  }

  private static int index_termPattern = 0;
  
  public shared.SharedObject duplicate() {
    return this;
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Default) {
      return super.equivalent(peer);
    }
    return false;
  }

  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    return getApiFactory().makeSeparator_Default(fun, args, annos);
  }

  public aterm.ATerm toTerm() {
    if (term == null) {
      term = getApiFactory().toTerm(this);
    }
    return term;
  }

  public boolean isDefault()
  {
    return true;
  }

  public boolean hasTermPattern() {
    return true;
  }

  public aterm.ATerm getTermPattern() {
   return getArgument(index_termPattern);
  }


  public apigen.adt.api.types.Separator setTermPattern(aterm.ATerm _termPattern) {
    return (apigen.adt.api.types.Separator) super.setArgument(_termPattern, index_termPattern);
  }


  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
        break;
      default: throw new RuntimeException("Default does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }
}
