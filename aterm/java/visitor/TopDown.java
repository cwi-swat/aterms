package visitor;

public class TopDown
  implements Visitor
{
  Visitor visitor;

  public TopDown(Visitor visitor)
  {
    this.visitor = visitor;
  }

  public boolean visit(Visitable visitable)
  {
    if (!visitor.visit(visitable)) {
      return false;
    }

    return visitable.acceptChildren(this);
  }
}
