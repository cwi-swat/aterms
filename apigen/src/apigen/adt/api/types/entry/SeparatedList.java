package apigen.adt.api.types.entry;

public class SeparatedList extends apigen.adt.api.types.Entry {
  public SeparatedList(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {
    super(factory, annos, fun, args);
  }

  private static int index_sort = 0;
  private static int index_elemSort = 1;
  private static int index_separators = 2;
  public shared.SharedObject duplicate() {
    return this;
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof SeparatedList) {
      return super.equivalent(peer);
    }
    return false;
  }

  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    return getApiFactory().makeEntry_SeparatedList(fun, args, annos);
  }

  public aterm.ATerm toTerm() {
    if (term == null) {
      term = getApiFactory().toTerm(this);
    }
    return term;
  }

  public boolean isSeparatedList()
  {
    return true;
  }

  public boolean hasSort() {
    return true;
  }

  public boolean hasElemSort() {
    return true;
  }

  public boolean hasSeparators() {
    return true;
  }

  public aterm.ATerm getSort() {
   return getArgument(index_sort);
  }

  public apigen.adt.api.types.Entry setSort(aterm.ATerm _sort) {
    return (apigen.adt.api.types.Entry) super.setArgument(_sort, index_sort);
  }

  public aterm.ATerm getElemSort() {
   return getArgument(index_elemSort);
  }

  public apigen.adt.api.types.Entry setElemSort(aterm.ATerm _elemSort) {
    return (apigen.adt.api.types.Entry) super.setArgument(_elemSort, index_elemSort);
  }

  public apigen.adt.api.types.Separators getSeparators() {
    return (apigen.adt.api.types.Separators) getArgument(index_separators);
  }

  public apigen.adt.api.types.Entry setSeparators(apigen.adt.api.types.Separators _separators) {
    return (apigen.adt.api.types.Entry) super.setArgument(_separators, index_separators);
  }

  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
        // arg 0 is always of type aterm.ATerm
        break;
      case 1:
        // arg 1 is always of type aterm.ATerm
        break;
      case 2:
        if (!(arg instanceof apigen.adt.api.types.Separators)) { 
          throw new RuntimeException("Argument 2 of a SeparatedList should have type Separators");
        }
        break;
      default: throw new RuntimeException("SeparatedList does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }

}
