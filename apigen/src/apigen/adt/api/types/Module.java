package apigen.adt.api.types;

abstract public class Module extends apigen.adt.api.AbstractType {
  public Module(apigen.adt.api.Factory factory) {
     super(factory);
  }

  public void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super.init(hashCode, annos, fun, args);
  }

  public void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {
  	super.initHashCode(annos, fun, args);
  }

  public boolean isEqual(Module peer) {
    return super.isEqual(peer);
  }

  public boolean isSortModule()  {
    return true;
  }

  public boolean isModulentry() {
    return false;
  }

  public boolean hasModulename() {
    return false;
  }

  public boolean hasImports() {
    return false;
  }

  public boolean hasSorts() {
    return false;
  }

  public boolean hasEntries() {
    return false;
  }

  public apigen.adt.api.types.ModuleName getModulename() {
     throw new UnsupportedOperationException("This Module has no Modulename");
  }

  public Module setModulename(apigen.adt.api.types.ModuleName _modulename) {
     throw new IllegalArgumentException("Illegal argument: " + _modulename);
  }

  public apigen.adt.api.types.Imports getImports() {
     throw new UnsupportedOperationException("This Module has no Imports");
  }

  public Module setImports(apigen.adt.api.types.Imports _imports) {
     throw new IllegalArgumentException("Illegal argument: " + _imports);
  }

  public apigen.adt.api.types.Sorts getSorts() {
     throw new UnsupportedOperationException("This Module has no Sorts");
  }

  public Module setSorts(apigen.adt.api.types.Sorts _sorts) {
     throw new IllegalArgumentException("Illegal argument: " + _sorts);
  }

  public apigen.adt.api.types.Entries getEntries() {
     throw new UnsupportedOperationException("This Module has no Entries");
  }

  public Module setEntries(apigen.adt.api.types.Entries _entries) {
     throw new IllegalArgumentException("Illegal argument: " + _entries);
  }

}

