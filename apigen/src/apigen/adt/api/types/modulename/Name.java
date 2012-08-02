package apigen.adt.api.types.modulename;

public class Name extends apigen.adt.api.types.ModuleName {
  public Name(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] args) {
    super(factory, annos, fun, args);
  }

  private static int index_name = 0;
  public shared.SharedObject duplicate() {
    return this;
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Name) {
      return super.equivalent(peer);
    }
    return false;
  }

  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    return getApiFactory().makeModuleName_Name(fun, args, annos);
  }

  public aterm.ATerm toTerm() {
    if (term == null) {
      term = getApiFactory().toTerm(this);
    }
    return term;
  }

  public boolean isName()
  {
    return true;
  }

  public boolean hasName() {
    return true;
  }

  public String getName() {
   return ((aterm.ATermAppl) getArgument(index_name)).getAFun().getName();
  }

  public apigen.adt.api.types.ModuleName setName(String _name) {
    return (apigen.adt.api.types.ModuleName) super.setArgument(getFactory().makeAppl(getFactory().makeAFun(_name, 0, true)), index_name);
  }

  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
        if (!(arg instanceof aterm.ATermAppl)) { 
          throw new RuntimeException("Argument 0 of a Name should have type str");
        }
        break;
      default: throw new RuntimeException("Name does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }

}
