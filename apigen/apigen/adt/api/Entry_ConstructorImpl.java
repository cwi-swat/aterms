package apigen.adt.api;

abstract public class Entry_ConstructorImpl
extends Entry
{
  Entry_ConstructorImpl(ADTFactory factory) {
    super(factory);
  }
  private static int index_sort = 0;
  private static int index_alternative = 1;
  private static int index_termPattern = 2;
  public shared.SharedObject duplicate() {
    Entry_Constructor clone = new Entry_Constructor(factory);
     clone.init(hashCode(), getAnnotations(), getAFun(), getArgumentArray());
    return clone;
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Entry_Constructor) {
      return super.equivalent(peer);
    }
    return false;
  }
  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] i_args, aterm.ATermList annos) {
    return getADTFactory().makeEntry_Constructor(fun, i_args, annos);
  }
  public aterm.ATerm toTerm() {
    if (term == null) {
      term = getADTFactory().toTerm(this);
    }
    return term;
  }

  public boolean isConstructor()
  {
    return true;
  }

  public boolean hasSort()
  {
    return true;
  }

  public boolean hasAlternative()
  {
    return true;
  }

  public boolean hasTermPattern()
  {
    return true;
  }

  public aterm.ATerm getSort()
  {
   return this.getArgument(index_sort);
  }

  public Entry setSort(aterm.ATerm _sort)
  {
    return (Entry) super.setArgument(_sort, index_sort);
  }

  public aterm.ATerm getAlternative()
  {
   return this.getArgument(index_alternative);
  }

  public Entry setAlternative(aterm.ATerm _alternative)
  {
    return (Entry) super.setArgument(_alternative, index_alternative);
  }

  public aterm.ATerm getTermPattern()
  {
   return this.getArgument(index_termPattern);
  }

  public Entry setTermPattern(aterm.ATerm _termPattern)
  {
    return (Entry) super.setArgument(_termPattern, index_termPattern);
  }

  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 0 of a Entry_Constructor should have type term");
        }
        break;
      case 1:
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 1 of a Entry_Constructor should have type term");
        }
        break;
      case 2:
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 2 of a Entry_Constructor should have type term");
        }
        break;
      default: throw new RuntimeException("Entry_Constructor does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }
}
