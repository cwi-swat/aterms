package apigen.adt.api.types.entry;

public class List extends apigen.adt.api.types.Entry {
  public List(apigen.adt.api.Factory factory) {
    super(factory);
  }

  public void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super.init(hashCode, annos, fun, args);
  }

  public void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {
  	super.initHashCode(annos, fun, args);
  }

  private static int index_sort = 0;
  private static int index_elemSort = 1;
  public shared.SharedObject duplicate() {
    List clone = new List(getApiFactory());
    clone.init(hashCode(), getAnnotations(), getAFun(), getArgumentArray());
    return clone;
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
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 0 of a List should have type term");
        }
        break;
      case 1:
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 1 of a List should have type term");
        }
        break;
      default: throw new RuntimeException("List does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }
}
