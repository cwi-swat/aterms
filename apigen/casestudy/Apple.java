package casestudy;

public class Apple
  extends Tree
{
  String color;

  public Apple(String color)
  {
    this.color = color;
  }

  public void acceptTreeVisitor(TreeVisitor visitor)
  {
    visitor.visitApple(this);
  }

  public int nrOfChildren()
  {
    return 0;
  }

  public Visitable getChild(int index)
  {
    throw new RuntimeException("no children");
  }
}
