package apigen.adt.api.types;

abstract public class Type extends apigen.adt.api.AbstractType {
  public Type(apigen.adt.api.Factory factory) {
     super(factory);
  }

  public void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super.init(hashCode, annos, fun, args);
  }

  public void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {
  	super.initHashCode(annos, fun, args);
  }

  public boolean isEqual(Type peer) {
    return super.isEqual(peer);
  }

  public boolean isSortType()  {
    return true;
  }

  public boolean isType() {
    return false;
  }

  public boolean hasName() {
    return false;
  }

  public String getName() {
     throw new UnsupportedOperationException("This Type has no Name");
  }

  public Type setName(String _name) {
     throw new IllegalArgumentException("Illegal argument: " + _name);
  }

}

