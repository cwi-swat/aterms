package apigen.adt.api;

import aterm.*;
import java.io.InputStream;
import java.io.IOException;

abstract public class EntriesImpl extends ADTConstructor
{
  public static Entries fromString(String str)
  {
    aterm.ATerm trm = getStaticADTFactory().parse(str);
    return fromTerm(trm);
  }
  public static Entries fromTextFile(InputStream stream) throws aterm.ParseError, IOException
  {
    aterm.ATerm trm = getStaticADTFactory().readFromTextFile(stream);
    return fromTerm(trm);
  }
  public boolean isEqual(Entries peer)
  {
    return term.isEqual(peer.toTerm());
  }
  public static Entries fromTerm(aterm.ATerm trm)
  {
    Entries tmp;
    if ((tmp = Entries_Empty.fromTerm(trm)) != null) {
      return tmp;
    }

    if ((tmp = Entries_List.fromTerm(trm)) != null) {
      return tmp;
    }


    throw new RuntimeException("This is not a Entries: " + trm);
  }

  public boolean isEntries()  {
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

