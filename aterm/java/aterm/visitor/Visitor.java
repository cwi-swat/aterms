package aterm.visitor;

public interface Visitor
{
  public void visit(Visitable v) throws VisitFailure;
}
