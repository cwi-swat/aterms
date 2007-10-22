package apigen.adt.api.types.entry;

public class Constructor extends apigen.adt.api.types.Entry {
	
  public Constructor(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super(factory, annos, fun, args);
  }

  private static int index_sort = 0;
  private static int index_alternative = 1;
  private static int index_termPattern = 2;
  
  public shared.SharedObject duplicate() {
    return this;
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Constructor) {
      return super.equivalent(peer);
    }
    return false;
  }

  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    return getApiFactory().makeEntry_Constructor(fun, args, annos);
  }

  public aterm.ATerm toTerm() {
    if (term == null) {
      term = getApiFactory().toTerm(this);
    }
    return term;
  }

  public boolean isConstructor()
  {
    return true;
  }

  public boolean hasSort() {
    return true;
  }

  public boolean hasAlternative() {
    return true;
  }

  public boolean hasTermPattern() {
    return true;
  }

  public aterm.ATerm getSort() {
   return getArgument(index_sort);
  }


  public apigen.adt.api.types.Entry setSort(aterm.ATerm _sort) {
    return (apigen.adt.api.types.Entry) super.setArgument(_sort, index_sort);
  }


  public aterm.ATerm getAlternative() {
   return getArgument(index_alternative);
  }


  public apigen.adt.api.types.Entry setAlternative(aterm.ATerm _alternative) {
    return (apigen.adt.api.types.Entry) super.setArgument(_alternative, index_alternative);
  }


  public aterm.ATerm getTermPattern() {
   return getArgument(index_termPattern);
  }


  public apigen.adt.api.types.Entry setTermPattern(aterm.ATerm _termPattern) {
    return (apigen.adt.api.types.Entry) super.setArgument(_termPattern, index_termPattern);
  }


  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
      case 1:
      case 2:
        break;
      default: throw new RuntimeException("Constructor does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }
}
