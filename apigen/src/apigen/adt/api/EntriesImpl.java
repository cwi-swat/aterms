package apigen.adt.api;

import aterm.*;
import java.io.InputStream;
import java.io.IOException;

abstract public class EntriesImpl extends ADTConstructor
{
  EntriesImpl(ADTFactory factory) {
     super(factory);
  }
  protected void init(int hashCode, aterm.ATermList annos, aterm.AFun fun,	aterm.ATerm[] args) {
    super.init(hashCode, annos, fun, args);
  }
  protected void initHashCode(aterm.ATermList annos, aterm.AFun fun, aterm.ATerm[] i_args) {
  	super.initHashCode(annos, fun, i_args);
  }
  public boolean isEqual(Entries peer)
  {
    return term.isEqual(peer.toTerm());
  }
  public boolean isSortEntries()  {
    return true;
  }

  public boolean isEmpty()
  {
    return false;
  }

  public boolean isList()
  {
    return false;
  }

  public boolean hasHead()
  {
    return false;
  }

  public boolean hasTail()
  {
    return false;
  }

  public Entry getHead()
  {
     throw new RuntimeException("This Entries has no Head");
  }

  public Entries setHead(Entry _head)
  {
     throw new RuntimeException("This Entries has no Head");
  }

  public Entries getTail()
  {
     throw new RuntimeException("This Entries has no Tail");
  }

  public Entries setTail(Entries _tail)
  {
     throw new RuntimeException("This Entries has no Tail");
  }

}

