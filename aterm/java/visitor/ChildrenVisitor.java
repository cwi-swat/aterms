package visitor;

public class ChildrenVisitor
  implements Visitor
{
  Visitor visitor;

  public ChildrenVisitor(Visitor visitor)
  {
    this.visitor = visitor;
  }

  public boolean visit(Visitable visitable)
  {
    return visitable.acceptChildren(visitor);
  }
}
