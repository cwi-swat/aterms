package apigen.adt.api.types;

public class Separators extends aterm.pure.ATermListImpl {
  private apigen.adt.api.Factory localFactory = null;
  public void init(int hashCode, aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
    super.init(hashCode, annos, first, next);
  }

  public void initHashCode(aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
    super.initHashCode(annos, first, next);
  }

  public Separators(apigen.adt.api.Factory localFactory) {
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
      Separators reversed = (Separators)this.reverse();
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

  public apigen.adt.api.types.Separator getHead() {
    return (apigen.adt.api.types.Separator)getFirst();
  }

  public Separators getTail() {
    return (Separators) getNext();
  }

  public boolean isSortSeparators()  {
    return true;
  }

  public boolean isEmpty() {
    return this == getApiFactory().makeSeparators();
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
    if (peer instanceof Separators) {
      return super.equivalent(peer);
    }
    else {
      return false;
    }
  }

  public shared.SharedObject duplicate() {
    Separators clone = new Separators(localFactory);
    clone.init(hashCode(), getAnnotations(), getFirst(), getNext());
    return clone;
  }

  public aterm.ATermList getEmpty() {
    return (aterm.ATermList)getApiFactory().makeSeparators();
  }

  public Separators insert(apigen.adt.api.types.Separator head) {
    return getApiFactory().makeSeparators(head, (Separators) this);
  }

  public aterm.ATermList insert(aterm.ATerm head) {
    return insert((apigen.adt.api.types.Separator) head);
  }

  public Separators reverseSeparators() {
    return getApiFactory().reverse(this);
  }

  public aterm.ATermList reverse() {
    return reverseSeparators();
  }

  public Separators concat(Separators peer) {
    return getApiFactory().concat(this, peer);
  }

  public aterm.ATermList concat(aterm.ATermList peer) {
    return concat((Separators) peer);
  }

  public Separators append(apigen.adt.api.types.Separator elem) {
    return getApiFactory().append(this, elem);
  }

  public aterm.ATermList append(aterm.ATerm elem) {
    return append((apigen.adt.api.types.Separator) elem);
  }

  public apigen.adt.api.types.Separator getSeparatorAt(int index) {
    return (apigen.adt.api.types.Separator) elementAt(index);
  }

}
