package apigen.adt.api;

abstract public class Entry_SeparatedListImpl
extends Entry
{
  protected void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super.init(hashCode, annos, fun, args);
  }
  protected void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] i_args) {
  	super.initHashCode(annos, fun, i_args);
  }
  protected Entry_SeparatedListImpl(ADTFactory factory) {
    super(factory);
  }
  private static int index_sort = 0;
  private static int index_elemSort = 1;
  private static int index_separators = 2;
  public shared.SharedObject duplicate() {
    Entry_SeparatedList clone = new Entry_SeparatedList(factory);
     clone.init(hashCode(), getAnnotations(), getAFun(), getArgumentArray());
    return clone;
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Entry_SeparatedList) {
      return super.equivalent(peer);
    }
    return false;
  }
  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] i_args, aterm.ATermList annos) {
    return getADTFactory().makeEntry_SeparatedList(fun, i_args, annos);
  }
  public aterm.ATerm toTerm() {
    if (term == null) {
      term = getADTFactory().toTerm(this);
    }
    return term;
  }

  public boolean isSeparatedList()
  {
    return true;
  }

  public boolean hasSort()
  {
    return true;
  }

  public boolean hasElemSort()
  {
    return true;
  }

  public boolean hasSeparators()
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

  public aterm.ATerm getElemSort()
  {
   return this.getArgument(index_elemSort);
  }

  public Entry setElemSort(aterm.ATerm _elemSort)
  {
    return (Entry) super.setArgument(_elemSort, index_elemSort);
  }

  public Separators getSeparators()
  {
    return (Separators) this.getArgument(index_separators) ;
  }

  public Entry setSeparators(Separators _separators)
  {
    return (Entry) super.setArgument(_separators, index_separators);
  }

  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 0 of a Entry_SeparatedList should have type term");
        }
        break;
      case 1:
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 1 of a Entry_SeparatedList should have type term");
        }
        break;
      case 2:
        if (! (arg instanceof Separators)) { 
          throw new RuntimeException("Argument 2 of a Entry_SeparatedList should have type Separators");
        }
        break;
      default: throw new RuntimeException("Entry_SeparatedList does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }
}
