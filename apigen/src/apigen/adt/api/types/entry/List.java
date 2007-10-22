package apigen.adt.api.types.entry;

public class List extends apigen.adt.api.types.Entry {
  public List(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super(factory, annos, fun, args);
  }

  private static int index_sort = 0;
  private static int index_elemSort = 1;
  
  public shared.SharedObject duplicate() {
    return this;
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof List) {
      return super.equivalent(peer);
    }
    return false;
  }

  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    return getApiFactory().makeEntry_List(fun, args, annos);
  }

  public aterm.ATerm toTerm() {
    if (term == null) {
      term = getApiFactory().toTerm(this);
    }
    return term;
  }

  public boolean isList()
  {
    return true;
  }

  public boolean hasSort() {
    return true;
  }

  public boolean hasElemSort() {
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


  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
      case 1:
        break;
      default: throw new RuntimeException("List does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }
}
