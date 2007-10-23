package apigen.adt.api.types;

public class Modules extends apigen.adt.api.AbstractList {
  private aterm.ATerm term = null;
  public Modules(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
     super(factory, annos, first, next);
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Modules) {
      return super.equivalent(peer);
    }
    return false;
  }

  public shared.SharedObject duplicate() {
    return this;
  }

  public aterm.ATerm toTerm() {
    aterm.ATermFactory atermFactory = getApiFactory().getPureFactory();
    if (this.term == null) {
      Modules reversed = (Modules)this.reverse();
      aterm.ATermList tmp = atermFactory.makeList();
      for (; !reversed.isEmpty(); reversed = reversed.getTail()) {
        aterm.ATerm elem = reversed.getHead().toTerm();
        tmp = atermFactory.makeList(elem, tmp);
      }
      this.term = tmp;
    }
    return this.term;
  }

  public boolean isSortModules()  {
    return true;
  }

  public apigen.adt.api.types.Module getHead() {
    return (apigen.adt.api.types.Module)getFirst();
  }

  public Modules getTail() {
    return (Modules) getNext();
  }

  public aterm.ATermList getEmpty() {
    return getApiFactory().makeModules();
  }

  public Modules insert(apigen.adt.api.types.Module head) {
    return getApiFactory().makeModules(head, this);
  }

  public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {
    return getApiFactory().makeModules(head, tail, annos);
  }

  public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail) {
    return make(head, tail, getApiFactory().getPureFactory().getEmpty());
  }

  public aterm.ATermList insert(aterm.ATerm head) {
    return make(head, this);
  }

  public Modules reverseModules() {
    return getApiFactory().reverse(this);
  }

  public aterm.ATermList reverse() {
    return reverseModules();
  }

  public Modules concat(Modules peer) {
    return getApiFactory().concat(this, peer);
  }

  public aterm.ATermList concat(aterm.ATermList peer) {
    return concat((Modules) peer);
  }

  public Modules append(apigen.adt.api.types.Module elem) {
    return getApiFactory().append(this, elem);
  }

  public aterm.ATermList append(aterm.ATerm elem) {
    return append((apigen.adt.api.types.Module) elem);
  }

  public apigen.adt.api.types.Module getModuleAt(int index) {
    return (apigen.adt.api.types.Module) elementAt(index);
  }

}
