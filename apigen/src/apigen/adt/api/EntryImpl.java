package apigen.adt.api;

import aterm.*;
import java.io.InputStream;
import java.io.IOException;

abstract public class EntryImpl extends ADTConstructor
{
  static Entry fromString(String str)
  {
    aterm.ATerm trm = getStaticADTFactory().parse(str);
    return fromTerm(trm);
  }
  static Entry fromTextFile(InputStream stream) throws aterm.ParseError, IOException
  {
    aterm.ATerm trm = getStaticADTFactory().readFromTextFile(stream);
    return fromTerm(trm);
  }
  public boolean isEqual(Entry peer)
  {
    return term.isEqual(peer.toTerm());
  }
  public static Entry fromTerm(aterm.ATerm trm)
  {
    Entry tmp;
    if ((tmp = Entry_Constructor.fromTerm(trm)) != null) {
      return tmp;
    }


    throw new RuntimeException("This is not a Entry: " + trm);
  }

  public boolean isConstructor()
  {
    return false;
  }

  public boolean hasSort()
  {
    return false;
  }

  public boolean hasAlt()
  {
    return false;
  }

  public boolean hasPat()
  {
    return false;
  }

  public String getSort()
  {
     throw new RuntimeException("This Entry has no Sort");
  }

  public Entry setSort(String _sort)
  {
     throw new RuntimeException("This Entry has no Sort");
  }

  public String getAlt()
  {
     throw new RuntimeException("This Entry has no Alt");
  }

  public Entry setAlt(String _alt)
  {
     throw new RuntimeException("This Entry has no Alt");
  }

  public ATerm getPat()
  {
     throw new RuntimeException("This Entry has no Pat");
  }

  public Entry setPat(ATerm _pat)
  {
     throw new RuntimeException("This Entry has no Pat");
  }


}

