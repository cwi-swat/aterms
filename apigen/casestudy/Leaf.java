package casestudy;

public class Leaf
  extends Node
{
  int value;

  public Leaf(int value)
  {
    this.value = value;
  }

  public int getValue()
  {
    return value;
  }

  public void acceptNodeVisitor(NodeVisitor visitor)
  {
    visitor.visitLeaf(this);
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
