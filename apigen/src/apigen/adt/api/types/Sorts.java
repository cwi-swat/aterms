package apigen.adt.api.types;

public class Sorts extends aterm.pure.ATermListImpl {
  private apigen.adt.api.Factory localFactory = null;
  public void init(int hashCode, aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
    super.init(hashCode, annos, first, next);
  }

  public void initHashCode(aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
    super.initHashCode(annos, first, next);
  }

  public Sorts(apigen.adt.api.Factory localFactory) {
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

  public String toString() {
    return toTerm().toString();
  }

  public apigen.adt.api.types.Type getHead() {
    return (apigen.adt.api.types.Type)getFirst();
  }

  public Sorts getTail() {
    return (Sorts) getNext();
  }

  public boolean isSortSorts()  {
    return true;
  }

  public boolean isEmpty() {
    return this == getApiFactory().makeSorts();
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
    if (peer instanceof Sorts) {
      return super.equivalent(peer);
    }
    else {
      return false;
    }
  }

  public shared.SharedObject duplicate() {
    Sorts clone = new Sorts(localFactory);
    clone.init(hashCode(), getAnnotations(), getFirst(), getNext());
    return clone;
  }

  public aterm.ATermList getEmpty() {
    return (aterm.ATermList)getApiFactory().makeSorts();
  }

  public Sorts insert(apigen.adt.api.types.Type head) {
    return getApiFactory().makeSorts(head, (Sorts) this);
  }

  public aterm.ATermList insert(aterm.ATerm head) {
    return insert((apigen.adt.api.types.Type) head);
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
