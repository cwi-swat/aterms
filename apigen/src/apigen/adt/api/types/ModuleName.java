package apigen.adt.api.types;

abstract public class ModuleName extends apigen.adt.api.AbstractType {
  public ModuleName(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {
     super(factory, annos, fun, args);
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

