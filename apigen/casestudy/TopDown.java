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

  //{{{ public void visit(Visitable visitable)

  public void visit(Visitable visitable)
  {
    visitor.visit(visitable);

    int nr_children = visitable.nrOfChildren();
    for (int i=0; i<nr_children; i++) {
      visit(visitable.getChild(i));
    }
  }

  //}}}
}
