package apigen.adt.api;


abstract public class EntryImpl extends ADTConstructor
{
  protected EntryImpl(ADTFactory factory) {
     super(factory);
  }
  protected void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super.init(hashCode, annos, fun, args);
  }
  protected void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] i_args) {
  	super.initHashCode(annos, fun, i_args);
  }
  public boolean isEqual(Entry peer)
  {
    return super.isEqual(peer);
  }
  public boolean isSortEntry()  {
    return true;
  }

  public boolean isConstructor()
  {
    return false;
  }

  public boolean isList()
  {
    return false;
  }

  public boolean isSeparatedList()
  {
    return false;
  }

  public boolean hasSort()
  {
    return false;
  }

  public boolean hasAlternative()
  {
    return false;
  }

  public boolean hasTermPattern()
  {
    return false;
  }

  public boolean hasElemSort()
  {
    return false;
  }

  public boolean hasSeparators()
  {
    return false;
  }

  public aterm.ATerm getSort()
  {
     throw new UnsupportedOperationException("This Entry has no Sort");
  }

  public Entry setSort(aterm.ATerm _sort)
  {
     throw new IllegalArgumentException("Illegal argument: " + _sort);
  }

  public aterm.ATerm getAlternative()
  {
     throw new UnsupportedOperationException("This Entry has no Alternative");
  }

  public Entry setAlternative(aterm.ATerm _alternative)
  {
     throw new IllegalArgumentException("Illegal argument: " + _alternative);
  }

  public aterm.ATerm getTermPattern()
  {
     throw new UnsupportedOperationException("This Entry has no TermPattern");
  }

  public Entry setTermPattern(aterm.ATerm _termPattern)
  {
     throw new IllegalArgumentException("Illegal argument: " + _termPattern);
  }

  public aterm.ATerm getElemSort()
  {
     throw new UnsupportedOperationException("This Entry has no ElemSort");
  }

  public Entry setElemSort(aterm.ATerm _elemSort)
  {
     throw new IllegalArgumentException("Illegal argument: " + _elemSort);
  }

  public Separators getSeparators()
  {
     throw new UnsupportedOperationException("This Entry has no Separators");
  }

  public Entry setSeparators(Separators _separators)
  {
     throw new IllegalArgumentException("Illegal argument: " + _separators);
  }

}

