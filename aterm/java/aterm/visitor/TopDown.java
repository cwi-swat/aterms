package aterm.visitor;

public class TopDown
  implements Visitor
{
  Visitor visitor;

  //{{{ public TopDown(Visitor visitor)

  public TopDown(Visitor visitor)
  {
    this.visitor = visitor;
  }

  //}}}

  //{{{ public void visit(Visitable visitable) throws VisitFailure

  public void visit(Visitable visitable) throws VisitFailure
  {
    visitor.visit(visitable);

    int nr_children = visitable.getChildCount();
    for (int i=0; i<nr_children; i++) {
      visit(visitable.getChildAt(i));
    }
  }

  //}}}
}
