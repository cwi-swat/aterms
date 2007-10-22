package apigen.adt.api.types.type;

public class Type extends apigen.adt.api.types.Type {
  public Type(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super(factory, annos, fun, args);
  }

  private static int index_name = 0;
  
  public shared.SharedObject duplicate() {
	  return this;
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Type) {
      return super.equivalent(peer);
    }
    return false;
  }

  protected aterm.ATermAppl make(aterm.AFun fun, aterm.ATerm[] args, aterm.ATermList annos) {
    return getApiFactory().makeType_Type(fun, args, annos);
  }

  public aterm.ATerm toTerm() {
    if (term == null) {
      term = getApiFactory().toTerm(this);
    }
    return term;
  }

  public boolean isType()
  {
    return true;
  }

  public boolean hasName() {
    return true;
  }

  public String getName() {
   return ((aterm.ATermAppl) getArgument(index_name)).getAFun().getName();
  }


  public apigen.adt.api.types.Type setName(String _name) {
    return (apigen.adt.api.types.Type) super.setArgument(getFactory().makeAppl(getFactory().makeAFun(_name, 0, true)), index_name);
  }


  public aterm.ATermAppl setArgument(aterm.ATerm arg, int i) {
    switch(i) {
      case 0:
        if (! (arg instanceof aterm.ATermAppl)) { 
          throw new RuntimeException("Argument 0 of a Type should have type str");
        }
        break;
      default: throw new RuntimeException("Type does not have an argument at " + i );
    }
    return super.setArgument(arg, i);
  }
}
