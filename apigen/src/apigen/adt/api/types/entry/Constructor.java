package apigen.adt.api.types.entry;

public class Constructor extends apigen.adt.api.types.Entry {
  public Constructor(apigen.adt.api.Factory factory) {
    super(factory);
  }

  public void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super.init(hashCode, annos, fun, args);
  }

  public void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {
  	super.initHashCode(annos, fun, args);
  }

  private static int index_sort = 0;
  private static int index_alternative = 1;
  private static int index_termPattern = 2;
  public shared.SharedObject duplicate() {
    Constructor clone = new Constructor(getApiFactory());
    clone.init(hashCode(), getAnnotations(), getAFun(), getArgumentArray());
    return clone;
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
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 0 of a Constructor should have type term");
        }
        break;
      case 1:
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 1 of a Constructor should have type term");
        }
        break;
      case 2:
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 2 of a Constructor should have type term");
        }
        break;
      default: throw new RuntimeException("Constructor does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }
}
