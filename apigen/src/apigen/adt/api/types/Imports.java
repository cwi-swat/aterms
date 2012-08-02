package apigen.adt.api.types;

public class Imports extends apigen.adt.api.AbstractList {
  private aterm.ATerm term = null;
  public Imports(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
     super(factory, annos, first, next);
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Imports) {
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

  public boolean isSortImports()  {
    return true;
  }

  public apigen.adt.api.types.ModuleName getHead() {
    return (apigen.adt.api.types.ModuleName)getFirst();
  }

  public Imports getTail() {
    return (Imports) getNext();
  }

  public aterm.ATermList getEmpty() {
    return getApiFactory().makeImports();
  }

  public Imports insert(apigen.adt.api.types.ModuleName head) {
    return getApiFactory().makeImports(head, this);
  }

  public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {
    return getApiFactory().makeImports(head, tail, annos);
  }

  public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail) {
    return make(head, tail, getApiFactory().getPureFactory().getEmpty());
  }

  public aterm.ATermList insert(aterm.ATerm head) {
    return make(head, this);
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
