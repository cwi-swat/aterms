package aterm.pure;

import aterm.*;

import aterm.visitor.*;

public abstract class ATermVisitableImpl
  implements ATermVisitable
{
  abstract public int getNrSubTerms();
  abstract public ATerm getSubTerm(int index);
  abstract public ATerm setSubTerm(int index, ATerm t);


  public int getChildCount()
  {
    return getNrSubTerms();
  }


  public Visitable getChildAt(int index)
  {
    return getSubTerm(index);
  }


  public Visitable setChildAt(int index, Visitable v)
  {
    return setSubTerm(index, (ATerm) v);
  }

}
