package aterm;

public class Fwd extends Visitor implements jjtraveler.Visitor
{
  private Visitor any;

  //{{{ public ATermForward(Visitor visitor)

  public Fwd(Visitor visitor)
  {
    this.any = visitor;
  }

  //}}}
  //{{{ public void visit(Visitable visitable)

  public Visitable visit(Visitable visitable) throws jjtraveler.VisitFailure
  {
    try {
      return (Visitable) any.visit(visitable);
    } catch (jjtraveler.VisitFailure ex) {
      jjtraveler.VisitFailure vf = new jjtraveler.VisitFailure(ex.getMessage());
      vf.fillInStackTrace();
      throw vf;
    }
  }

  //}}}
}
