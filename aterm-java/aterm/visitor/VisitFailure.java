package aterm.visitor;

public class VisitFailure
  extends Exception
{
  public VisitFailure()
  {
    super();
  }


  public VisitFailure(String msg)
  {
    super(msg);
  }
}
