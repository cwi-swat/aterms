package apigen.adt.api;

abstract public class Entry_ConstructorImpl
extends Entry
{
  static private aterm.ATerm pattern = null;

  protected aterm.ATerm getPattern() {
    return pattern;
  }
  private static int index_sort = 0;
  private static int index_alt = 1;
  private static int index_pat = 2;
  public shared.SharedObject duplicate() {
    Entry_Constructor clone = new Entry_Constructor();
     clone.init(hashCode(), getAnnotations(), getAFun(), getArgumentArray());
    return clone;
  }

  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] i_args, aterm.ATermList annos) {
    return getADTFactory().makeEntry_Constructor(fun, i_args, annos);
  }
  static public void initializePattern()
  {
    pattern = getStaticFactory().parse("[<str>,<str>,<term>]");
  }

  static public Entry fromTerm(aterm.ATerm trm)
  {
    java.util.List children = trm.match(pattern);

    if (children != null) {
      Entry tmp = getStaticADTFactory().makeEntry_Constructor((String) children.get(0), (String) children.get(1), (aterm.ATerm) children.get(2));
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
      args.add((aterm.ATerm) getArgument(2));
      setTerm(getFactory().make(getPattern(), args));
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

  public boolean hasAlt()
  {
    return true;
  }

  public boolean hasPat()
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

  public String getAlt()
  {
   return ((aterm.ATermAppl) this.getArgument(index_alt)).getAFun().getName();
  }

  public Entry setAlt(String _alt)
  {
    return (Entry) super.setArgument(getFactory().makeAppl(getFactory().makeAFun(_alt, 0, true)), index_alt);
  }

  public ATerm getPat()
  {
   return this.getArgument(index_pat);
  }

  public Entry setPat(ATerm _pat)
  {
    return (Entry) super.setArgument(_pat, index_pat);
  }

  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
        if (! (arg instanceof aterm.ATermAppl)) { 
          throw new RuntimeException("Argument 0 of a Entry_Constructor should have type str");
        }
        break;
      case 1:
        if (! (arg instanceof aterm.ATermAppl)) { 
          throw new RuntimeException("Argument 1 of a Entry_Constructor should have type str");
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
