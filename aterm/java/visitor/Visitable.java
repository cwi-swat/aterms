package visitor;

public interface Visitable
{
  public boolean acceptChildren(Visitor v);
}

