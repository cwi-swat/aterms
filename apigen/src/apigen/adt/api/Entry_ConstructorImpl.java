package apigen.adt.api;

import aterm.ATerm;

abstract public class Entry_ConstructorImpl
extends Entry
{
  static private aterm.ATerm pattern = null;

  protected aterm.ATerm getPattern() {
    return pattern;
  }
  private static int index_sort = 0;
  private static int index_alternative = 1;
  private static int index_termPattern = 2;
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
    pattern = getStaticFactory().parse("[<term>,<term>,<term>]");
  }

  static public Entry fromTerm(aterm.ATerm trm)
  {
    java.util.List children = trm.match(pattern);

    if (children != null) {
      Entry tmp = getStaticADTFactory().makeEntry_Constructor((aterm.ATerm) children.get(0), (aterm.ATerm) children.get(1), (aterm.ATerm) children.get(2));
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

  public boolean hasAlternative()
  {
    return true;
  }

  public boolean hasTermPattern()
  {
    return true;
  }

  public ATerm getSort()
  {
   return this.getArgument(index_sort);
  }

  public Entry setSort(ATerm _sort)
  {
    return (Entry) super.setArgument(_sort, index_sort);
  }

  public ATerm getAlternative()
  {
   return this.getArgument(index_alternative);
  }

  public Entry setAlternative(ATerm _alternative)
  {
    return (Entry) super.setArgument(_alternative, index_alternative);
  }

  public ATerm getTermPattern()
  {
   return this.getArgument(index_termPattern);
  }

  public Entry setTermPattern(ATerm _termPattern)
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
