package apigen.adt.api;

abstract public class Entry_ListImpl
extends Entry
{
  static private aterm.ATerm pattern = null;

  protected aterm.ATerm getPattern() {
    return pattern;
  }
  private static int index_sort = 0;
  private static int index_elemSort = 1;
  public shared.SharedObject duplicate() {
    Entry_List clone = new Entry_List();
     clone.init(hashCode(), getAnnotations(), getAFun(), getArgumentArray());
    return clone;
  }

  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] i_args, aterm.ATermList annos) {
    return getADTFactory().makeEntry_List(fun, i_args, annos);
  }
  static public void initializePattern()
  {
    pattern = getStaticFactory().parse("list(<term>,<term>)");
  }

  static public Entry fromTerm(aterm.ATerm trm)
  {
    java.util.List children = trm.match(pattern);

    if (children != null) {
      Entry tmp = getStaticADTFactory().makeEntry_List((aterm.ATerm) children.get(0), (aterm.ATerm) children.get(1));
      tmp.setTerm(trm);
      return tmp;
    }
    else {
      return null;
    }
  }
  public aterm.ATerm toTerm() {
    if(term == null) {
      java.util.List args = new java.util.LinkedList();
      args.add((aterm.ATerm) getArgument(0));
      args.add((aterm.ATerm) getArgument(1));
      setTerm(getFactory().make(getPattern(), args));
    }
    return term;
  }

  public boolean isList()
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

  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 0 of a Entry_List should have type term");
        }
        break;
      case 1:
        if (! (arg instanceof aterm.ATerm)) { 
          throw new RuntimeException("Argument 1 of a Entry_List should have type term");
        }
        break;
      default: throw new RuntimeException("Entry_List does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }
}
