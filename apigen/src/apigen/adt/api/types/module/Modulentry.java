package apigen.adt.api.types.module;

public class Modulentry extends apigen.adt.api.types.Module {
  public Modulentry(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {
    super(factory, annos, fun, args);
  }

  private static int index_modulename = 0;
  private static int index_imports = 1;
  private static int index_sorts = 2;
  private static int index_entries = 3;
  public shared.SharedObject duplicate() {
    return this;
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Modulentry) {
      return super.equivalent(peer);
    }
    return false;
  }

  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    return getApiFactory().makeModule_Modulentry(fun, args, annos);
  }

  public aterm.ATerm toTerm() {
    if (term == null) {
      term = getApiFactory().toTerm(this);
    }
    return term;
  }

  public boolean isModulentry()
  {
    return true;
  }

  public boolean hasModulename() {
    return true;
  }

  public boolean hasImports() {
    return true;
  }

  public boolean hasSorts() {
    return true;
  }

  public boolean hasEntries() {
    return true;
  }

  public apigen.adt.api.types.ModuleName getModulename() {
    return (apigen.adt.api.types.ModuleName) getArgument(index_modulename);
  }

  public apigen.adt.api.types.Module setModulename(apigen.adt.api.types.ModuleName _modulename) {
    return (apigen.adt.api.types.Module) super.setArgument(_modulename, index_modulename);
  }

  public apigen.adt.api.types.Imports getImports() {
    return (apigen.adt.api.types.Imports) getArgument(index_imports);
  }

  public apigen.adt.api.types.Module setImports(apigen.adt.api.types.Imports _imports) {
    return (apigen.adt.api.types.Module) super.setArgument(_imports, index_imports);
  }

  public apigen.adt.api.types.Sorts getSorts() {
    return (apigen.adt.api.types.Sorts) getArgument(index_sorts);
  }

  public apigen.adt.api.types.Module setSorts(apigen.adt.api.types.Sorts _sorts) {
    return (apigen.adt.api.types.Module) super.setArgument(_sorts, index_sorts);
  }

  public apigen.adt.api.types.Entries getEntries() {
    return (apigen.adt.api.types.Entries) getArgument(index_entries);
  }

  public apigen.adt.api.types.Module setEntries(apigen.adt.api.types.Entries _entries) {
    return (apigen.adt.api.types.Module) super.setArgument(_entries, index_entries);
  }

  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
        if (!(arg instanceof apigen.adt.api.types.ModuleName)) { 
          throw new RuntimeException("Argument 0 of a Modulentry should have type ModuleName");
        }
        break;
      case 1:
        if (!(arg instanceof apigen.adt.api.types.Imports)) { 
          throw new RuntimeException("Argument 1 of a Modulentry should have type Imports");
        }
        break;
      case 2:
        if (!(arg instanceof apigen.adt.api.types.Sorts)) { 
          throw new RuntimeException("Argument 2 of a Modulentry should have type Sorts");
        }
        break;
      case 3:
        if (!(arg instanceof apigen.adt.api.types.Entries)) { 
          throw new RuntimeException("Argument 3 of a Modulentry should have type Entries");
        }
        break;
      default: throw new RuntimeException("Modulentry does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }

  protected int hashFunction() {
    int c = 0 + (getAnnotations().hashCode()<<8);
    int a = 0x9e3779b9;
    int b = (getAFun().hashCode()<<8);
    a += (getArgument(3).hashCode() << 24);
    a += (getArgument(2).hashCode() << 16);
    a += (getArgument(1).hashCode() << 8);
    a += (getArgument(0).hashCode() << 0);

    a -= b; a -= c; a ^= (c >> 13);
    b -= c; b -= a; b ^= (a << 8);
    c -= a; c -= b; c ^= (b >> 13);
    a -= b; a -= c; a ^= (c >> 12);
    b -= c; b -= a; b ^= (a << 16);
    c -= a; c -= b; c ^= (b >> 5);
    a -= b; a -= c; a ^= (c >> 3);
    b -= c; b -= a; b ^= (a << 10);
    c -= a; c -= b; c ^= (b >> 15);

    return c;
  }

}
