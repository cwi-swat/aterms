package apigen.adt.api;


abstract public class SeparatorImpl extends ADTConstructor
{
  protected SeparatorImpl(ADTFactory factory) {
     super(factory);
  }
  protected void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super.init(hashCode, annos, fun, args);
  }
  protected void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] i_args) {
  	super.initHashCode(annos, fun, i_args);
  }
  public boolean isEqual(Separator peer)
  {
    return super.isEqual(peer);
  }
  public boolean isSortSeparator()  {
    return true;
  }

  public boolean isDefault()
  {
    return false;
  }

  public boolean hasTermPattern()
  {
    return false;
  }

  public aterm.ATerm getTermPattern()
  {
     throw new UnsupportedOperationException("This Separator has no TermPattern");
  }

  public Separator setTermPattern(aterm.ATerm _termPattern)
  {
     throw new IllegalArgumentException("Illegal argument: " + _termPattern);
  }

}

