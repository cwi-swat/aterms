package aterm;

import visitor.*;

public class ATermVisitorBridge
  implements Visitor
{
  protected ATermVisitor visitor;

  public ATermVisitorBridge()
  {
  }

  public ATermVisitorBridge(ATermVisitor visitor)
  {
    this.visitor = visitor;
  }

  public boolean visit(Visitable visitable)
  {
    ATerm term = (ATerm)visitable;
    return term.accept(visitor);
  }
}
