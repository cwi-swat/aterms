package apigen.adt.api;

import aterm.*;
import java.io.InputStream;
import java.io.IOException;

abstract public class EntriesImpl extends aterm.pure.ATermListImpl
{
  protected void init (int hashCode, aterm.ATermList annos, aterm.ATerm first,	aterm.ATermList next) {
    super.init(hashCode, annos, first, next);
  }
  protected void initHashCode(aterm.ATermList annos, aterm.ATerm first, aterm.ATermList next) {
    super.initHashCode(annos, first, next);
  }
  protected ADTFactory factory = null;
  EntriesImpl(ADTFactory factory) {
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
      Entries reversed = (Entries)this.reverse();
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
  public Entry getHead() {
    return (Entry) getFirst();
  }
  public Entries getTail() {
    return (Entries) getNext();
  }
  public boolean isSortEntries()  {
    return true;
  }

  public boolean isEmpty() {
    return this == ADTFactory.emptyEntries;
  }
  public boolean isMany() {
    return !isEmpty();
  }
  public boolean hasHead() {
    return !isEmpty();
  }
  public boolean hasTail() {
    return !isEmpty();
  }
  public boolean equivalent(shared.SharedObject peer) {
	 if (peer instanceof Entries) {
	 	return super.equivalent(peer);
	 }
	 else {
      return false;
	 }
  }
  public shared.SharedObject duplicate() {
	 Entries clone = new Entries(factory);
	 clone.init(hashCode(), getAnnotations(), getFirst(), getNext());
	 return clone;
  }
  public aterm.ATermList getEmpty() {
    return (aterm.ATermList)getADTFactory().makeEntries();
  }

  public Entries insert(Entry head) {
    return getADTFactory().makeEntries(head, (Entries) this);
  }
  public aterm.ATermList insert(aterm.ATerm head) {
    return insert((Entry) head);
  }
}
