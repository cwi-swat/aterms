package apigen.adt.api;


abstract public class SeparatorsImpl extends aterm.pure.ATermListImpl
{
  protected void init (int hashCode, aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
    super.init(hashCode, annos, first, next);
  }
  protected void initHashCode(aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
    super.initHashCode(annos, first, next);
  }
  protected ADTFactory factory = null;
  SeparatorsImpl(ADTFactory factory) {
     super(factory);
     this.factory = factory;
  }
  public ADTFactory getADTFactory(){
    return factory;
}
  protected aterm.ATerm term = null;
  public aterm.ATerm toTerm()
  {
    if (this.term == null) {
      Separators reversed = (Separators)this.reverse();
      aterm.ATermList tmp = getADTFactory().makeList();
      for (; !reversed.isEmpty(); reversed = reversed.getTail()) {
         aterm.ATerm elem = reversed.getHead().toTerm();
         tmp = getADTFactory().makeList(elem, tmp);
      }
      this.term = tmp;
    }
    return this.term;
  }
  public String toString() {
    return toTerm().toString();
  }
  public Separator getHead() {
    return (Separator) getFirst();
  }
  public Separators getTail() {
    return (Separators) getNext();
  }
  public boolean isSortSeparators()  {
    return true;
  }

  public boolean isEmpty() {
    return this == ((ADTFactory) getFactory()).makeSeparators();
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
	 Separators clone = new Separators(factory);
	 clone.init(hashCode(), getAnnotations(), getFirst(), getNext());
	 return clone;
  }
  public aterm.ATermList getEmpty() {
    return (aterm.ATermList)getADTFactory().makeSeparators();
  }

  public Separators insert(Separator head) {
    return getADTFactory().makeSeparators(head, (Separators) this);
  }
  public aterm.ATermList insert(aterm.ATerm head) {
    return insert((Separator) head);
  }
}
