package apigen.adt.api;

import aterm.*;
import java.io.InputStream;
import java.io.IOException;

abstract public class EntryImpl extends ADTConstructor
{
  public static Entry fromString(String str)
  {
    aterm.ATerm trm = getStaticADTFactory().parse(str);
    return fromTerm(trm);
  }
  public static Entry fromTextFile(InputStream stream) throws aterm.ParseError, IOException
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

  public boolean isEntry()  {
    return true;
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

  public aterm.ATerm getSort()
  {
     throw new RuntimeException("This Entry has no Sort");
  }

  public Entry setSort(aterm.ATerm _sort)
  {
     throw new RuntimeException("This Entry has no Sort");
  }

  public aterm.ATerm getAlternative()
  {
     throw new RuntimeException("This Entry has no Alternative");
  }

  public Entry setAlternative(aterm.ATerm _alternative)
  {
     throw new RuntimeException("This Entry has no Alternative");
  }

  public aterm.ATerm getTermPattern()
  {
     throw new RuntimeException("This Entry has no TermPattern");
  }

  public Entry setTermPattern(aterm.ATerm _termPattern)
  {
     throw new RuntimeException("This Entry has no TermPattern");
  }


}

