package apigen.adt.api;

abstract public class Entry_ListImpl
extends Entry
{
  static private aterm.ATerm pattern = null;

  protected aterm.ATerm getPattern() {
    return pattern;
  }
  private static int index_sort = 0;
  private static int index_elementSort = 1;
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
    pattern = getStaticFactory().parse("list(<str>,<str>)");
  }

  static public Entry fromTerm(aterm.ATerm trm)
  {
    java.util.List children = trm.match(pattern);

    if (children != null) {
      Entry tmp = getStaticADTFactory().makeEntry_List((String) children.get(0), (String) children.get(1));
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
      args.add(((aterm.ATermAppl) getArgument(0)).getAFun().getName());
      args.add(((aterm.ATermAppl) getArgument(1)).getAFun().getName());
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

  public boolean hasElementSort()
  {
    return true;
  }

  public String getSort()
  {
   return ((aterm.ATermAppl) this.getArgument(index_sort)).getAFun().getName();
  }

  public Entry setSort(String _sort)
  {
    return (Entry) super.setArgument(getFactory().makeAppl(getFactory().makeAFun(_sort, 0, true)), index_sort);
  }

  public String getElementSort()
  {
   return ((aterm.ATermAppl) this.getArgument(index_elementSort)).getAFun().getName();
  }

  public Entry setElementSort(String _elementSort)
  {
    return (Entry) super.setArgument(getFactory().makeAppl(getFactory().makeAFun(_elementSort, 0, true)), index_elementSort);
  }

  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
        if (! (arg instanceof aterm.ATermAppl)) { 
          throw new RuntimeException("Argument 0 of a Entry_List should have type str");
        }
        break;
      case 1:
        if (! (arg instanceof aterm.ATermAppl)) { 
          throw new RuntimeException("Argument 1 of a Entry_List should have type str");
        }
        break;
      default: throw new RuntimeException("Entry_List does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }
}
