package aterm.visitor;

public interface Visitable
{
  public int getNrChildren();
  public Visitable getChild(int index);
}

