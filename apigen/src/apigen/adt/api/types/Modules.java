package apigen.adt.api.types;

public class Modules extends aterm.pure.ATermListImpl {
  private apigen.adt.api.Factory localFactory = null;
  public void init(int hashCode, aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
    super.init(hashCode, annos, first, next);
  }

  public void initHashCode(aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
    super.initHashCode(annos, first, next);
  }

  public Modules(apigen.adt.api.Factory localFactory) {
     super(localFactory.getPureFactory());
     this.localFactory = localFactory;
  }

  public apigen.adt.api.Factory getApiFactory() {
    return localFactory;
}

  protected aterm.ATerm term = null;
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

  public String toString() {
    return toTerm().toString();
  }

  public apigen.adt.api.types.Module getHead() {
    return (apigen.adt.api.types.Module)getFirst();
  }

  public Modules getTail() {
    return (Modules) getNext();
  }

  public boolean isSortModules()  {
    return true;
  }

  public boolean isEmpty() {
    return this == getApiFactory().makeModules();
  }

  public boolean isMany() {
    return !isEmpty();
  }

  public boolean isSingle() {
    return !isEmpty() && getNext().isEmpty();
  }

  public boolean hasHead() {
    return !isEmpty();
  }

  public boolean hasTail() {
    return !isEmpty();
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Modules) {
      return super.equivalent(peer);
    }
    else {
      return false;
    }
  }

  public shared.SharedObject duplicate() {
    Modules clone = new Modules(localFactory);
    clone.init(hashCode(), getAnnotations(), getFirst(), getNext());
    return clone;
  }

  public aterm.ATermList getEmpty() {
    return (aterm.ATermList)getApiFactory().makeModules();
  }

  public Modules insert(apigen.adt.api.types.Module head) {
    return getApiFactory().makeModules(head, (Modules) this);
  }

  public aterm.ATermList insert(aterm.ATerm head) {
    return insert((apigen.adt.api.types.Module) head);
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
