package aterm.visitor;

public class TopDown
  implements Visitor
{
  Visitor visitor;

  public TopDown(Visitor visitor)
  {
    this.visitor = visitor;
  }

  public void visit(Visitable visitable)
    throws VisitFailure
  {
    visitor.visit(visitable);

    int nr_children = visitable.getNrChildren();
    for (int i=0; i<nr_children; i++) {
      visit(visitable.getChild(i));
    }
  }
}
