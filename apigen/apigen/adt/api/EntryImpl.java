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

  public boolean hasAlternative()
  {
    return false;
  }

  public boolean hasTermPattern()
  {
    return false;
  }

  public ATerm getSort()
  {
     throw new RuntimeException("This Entry has no Sort");
  }

  public Entry setSort(ATerm _sort)
  {
     throw new RuntimeException("This Entry has no Sort");
  }

  public ATerm getAlternative()
  {
     throw new RuntimeException("This Entry has no Alternative");
  }

  public Entry setAlternative(ATerm _alternative)
  {
     throw new RuntimeException("This Entry has no Alternative");
  }

  public ATerm getTermPattern()
  {
     throw new RuntimeException("This Entry has no TermPattern");
  }

  public Entry setTermPattern(ATerm _termPattern)
  {
     throw new RuntimeException("This Entry has no TermPattern");
  }


}

