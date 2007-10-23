package apigen.adt.api.types;

abstract public class Separator extends apigen.adt.api.AbstractType {
  public Separator(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {
     super(factory, annos, fun, args);
  }

  public boolean isEqual(Separator peer) {
    return super.isEqual(peer);
  }

  public boolean isSortSeparator()  {
    return true;
  }

  public boolean isDefault() {
    return false;
  }

  public boolean hasTermPattern() {
    return false;
  }

  public aterm.ATerm getTermPattern() {
     throw new UnsupportedOperationException("This Separator has no TermPattern");
  }

  public Separator setTermPattern(aterm.ATerm _termPattern) {
     throw new IllegalArgumentException("Illegal argument: " + _termPattern);
  }

}

