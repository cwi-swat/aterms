package apigen.adt.api.types;

public class Imports extends aterm.pure.ATermListImpl {
  private apigen.adt.api.Factory localFactory = null;
  public void init(int hashCode, aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
    super.init(hashCode, annos, first, next);
  }

  public void initHashCode(aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
    super.initHashCode(annos, first, next);
  }

  public Imports(apigen.adt.api.Factory localFactory) {
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
      Imports reversed = (Imports)this.reverse();
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

  public apigen.adt.api.types.ModuleName getHead() {
    return (apigen.adt.api.types.ModuleName)getFirst();
  }

  public Imports getTail() {
    return (Imports) getNext();
  }

  public boolean isSortImports()  {
    return true;
  }

  public boolean isEmpty() {
    return this == getApiFactory().makeImports();
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
    if (peer instanceof Imports) {
      return super.equivalent(peer);
    }
    else {
      return false;
    }
  }

  public shared.SharedObject duplicate() {
    Imports clone = new Imports(localFactory);
    clone.init(hashCode(), getAnnotations(), getFirst(), getNext());
    return clone;
  }

  public aterm.ATermList getEmpty() {
    return (aterm.ATermList)getApiFactory().makeImports();
  }

  public Imports insert(apigen.adt.api.types.ModuleName head) {
    return getApiFactory().makeImports(head, (Imports) this);
  }

  public aterm.ATermList insert(aterm.ATerm head) {
    return insert((apigen.adt.api.types.ModuleName) head);
  }

  public Imports reverseImports() {
    return getApiFactory().reverse(this);
  }

  public aterm.ATermList reverse() {
    return reverseImports();
  }

  public Imports concat(Imports peer) {
    return getApiFactory().concat(this, peer);
  }

  public aterm.ATermList concat(aterm.ATermList peer) {
    return concat((Imports) peer);
  }

  public Imports append(apigen.adt.api.types.ModuleName elem) {
    return getApiFactory().append(this, elem);
  }

  public aterm.ATermList append(aterm.ATerm elem) {
    return append((apigen.adt.api.types.ModuleName) elem);
  }

  public apigen.adt.api.types.ModuleName getModuleNameAt(int index) {
    return (apigen.adt.api.types.ModuleName) elementAt(index);
  }

}
