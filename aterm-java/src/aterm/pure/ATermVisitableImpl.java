package aterm.pure;

import aterm.*;

public abstract class ATermVisitableImpl
  implements Visitable
{
  abstract public int getNrSubTerms();
  abstract public ATerm getSubTerm(int index);
  abstract public ATerm setSubTerm(int index, ATerm t);

  public int getChildCount()
  {
    return getNrSubTerms();
  }

  public jjtraveler.Visitable getChildAt(int index)
  {
    return getSubTerm(index);
  }

  public jjtraveler.Visitable setChildAt(int index, jjtraveler.Visitable v)
  {
    return setSubTerm(index, (ATerm) v);
  }
}
