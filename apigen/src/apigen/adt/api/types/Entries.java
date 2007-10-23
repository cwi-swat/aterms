package apigen.adt.api.types;

public class Entries extends apigen.adt.api.AbstractList {
  private aterm.ATerm term = null;
  public Entries(apigen.adt.api.Factory factory, aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
     super(factory, annos, first, next);
  }

  public boolean equivalent(shared.SharedObject peer) {
    if (peer instanceof Entries) {
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
      Entries reversed = (Entries)this.reverse();
      aterm.ATermList tmp = atermFactory.makeList();
      for (; !reversed.isEmpty(); reversed = reversed.getTail()) {
        aterm.ATerm elem = reversed.getHead().toTerm();
        tmp = atermFactory.makeList(elem, tmp);
      }
      this.term = tmp;
    }
    return this.term;
  }

  public boolean isSortEntries()  {
    return true;
  }

  public apigen.adt.api.types.Entry getHead() {
    return (apigen.adt.api.types.Entry)getFirst();
  }

  public Entries getTail() {
    return (Entries) getNext();
  }

  public aterm.ATermList getEmpty() {
    return getApiFactory().makeEntries();
  }

  public Entries insert(apigen.adt.api.types.Entry head) {
    return getApiFactory().makeEntries(head, this);
  }

  public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail, aterm.ATermList annos) {
    return getApiFactory().makeEntries(head, tail, annos);
  }

  public aterm.ATermList make(aterm.ATerm head, aterm.ATermList tail) {
    return make(head, tail, getApiFactory().getPureFactory().getEmpty());
  }

  public aterm.ATermList insert(aterm.ATerm head) {
    return make(head, this);
  }

  public Entries reverseEntries() {
    return getApiFactory().reverse(this);
  }

  public aterm.ATermList reverse() {
    return reverseEntries();
  }

  public Entries concat(Entries peer) {
    return getApiFactory().concat(this, peer);
  }

  public aterm.ATermList concat(aterm.ATermList peer) {
    return concat((Entries) peer);
  }

  public Entries append(apigen.adt.api.types.Entry elem) {
    return getApiFactory().append(this, elem);
  }

  public aterm.ATermList append(aterm.ATerm elem) {
    return append((apigen.adt.api.types.Entry) elem);
  }

  public apigen.adt.api.types.Entry getEntryAt(int index) {
    return (apigen.adt.api.types.Entry) elementAt(index);
  }

}
