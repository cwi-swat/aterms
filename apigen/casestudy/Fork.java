public class Fork
  extends Node
{
  private Node left;
  private Node right;

  public Fork(Node left, Node right)
  {
    this.left = left;
    this.right = right;
  }

  public void acceptNodeVisitor(NodeVisitor visitor)
  {
    visitor.visitFork(this);
  }

  public int nrOfChildren()
  {
    return 2;
  }

  public Visitable getChild(int index)
  {
    switch (index) {
      case 0: return left;
      case 1: return right;
      default: throw new RuntimeException("illegal child: " + index);
    }
  }
}
