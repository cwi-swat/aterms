package aterm.visitor;

public interface Visitable
{
  public Visitable getChildAt(int index);
  public Visitable setChildAt(int index, Visitable v);
  public int getChildCount();
}

