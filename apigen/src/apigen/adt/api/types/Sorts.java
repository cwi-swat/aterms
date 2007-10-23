package apigen.adt.api.types;

public class Sorts extends apigen.adt.api.AbstractList {
  private aterm.ATerm term = null;
  public Sorts(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
     super(factory, annos, first, next);
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Sorts) {
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
      Sorts reversed = (Sorts)this.reverse();
      aterm.ATermList tmp = atermFactory.makeList();
      for (; !reversed.isEmpty(); reversed = reversed.getTail()) {
        aterm.ATerm elem = reversed.getHead().toTerm();
        tmp = atermFactory.makeList(elem, tmp);
      }
      this.term = tmp;
    }
    return this.term;
  }

  public boolean isSortSorts()  {
    return true;
  }

  public apigen.adt.api.types.Type getHead() {
    return (apigen.adt.api.types.Type)getFirst();
  }

  public Sorts getTail() {
    return (Sorts) getNext();
  }

  public aterm.ATermList getEmpty() {
    return getApiFactory().makeSorts();
  }

  public Sorts insert(apigen.adt.api.types.Type head) {
    return getApiFactory().makeSorts(head, this);
  }

  public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {
    return getApiFactory().makeSorts(head, tail, annos);
  }

  public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail) {
    return make(head, tail, getApiFactory().getPureFactory().getEmpty());
  }

  public aterm.ATermList insert(aterm.ATerm head) {
    return make(head, this);
  }

  public Sorts reverseSorts() {
    return getApiFactory().reverse(this);
  }

  public aterm.ATermList reverse() {
    return reverseSorts();
  }

  public Sorts concat(Sorts peer) {
    return getApiFactory().concat(this, peer);
  }

  public aterm.ATermList concat(aterm.ATermList peer) {
    return concat((Sorts) peer);
  }

  public Sorts append(apigen.adt.api.types.Type elem) {
    return getApiFactory().append(this, elem);
  }

  public aterm.ATermList append(aterm.ATerm elem) {
    return append((apigen.adt.api.types.Type) elem);
  }

  public apigen.adt.api.types.Type getTypeAt(int index) {
    return (apigen.adt.api.types.Type) elementAt(index);
  }

}
