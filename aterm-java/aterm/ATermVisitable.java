package aterm;

public interface ATermVisitable
  extends aterm.visitor.Visitable
{
  public void accept(ATermVisitor visitor) throws ATermVisitFailure;
}

