package apigen.adt.api.types;

abstract public class ModuleName extends apigen.adt.api.AbstractType {
  public ModuleName(apigen.adt.api.Factory factory) {
     super(factory);
  }

  public void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super.init(hashCode, annos, fun, args);
  }

  public void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {
  	super.initHashCode(annos, fun, args);
  }

  public boolean isEqual(ModuleName peer) {
    return super.isEqual(peer);
  }

  public boolean isSortModuleName()  {
    return true;
  }

  public boolean isName() {
    return false;
  }

  public boolean hasName() {
    return false;
  }

  public String getName() {
     throw new UnsupportedOperationException("This ModuleName has no Name");
  }

  public ModuleName setName(String _name) {
     throw new IllegalArgumentException("Illegal argument: " + _name);
  }

}

