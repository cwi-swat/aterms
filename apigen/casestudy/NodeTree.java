public class NodeTree
  extends Tree
{
  Node node;

  public NodeTree(Node node)
  {
    this.node = node;
  }

  public int nrOfChildren()
  {
    return 1;
  }

  public Visitable getChild(int index)
  {
    if (index == 0) {
      return node;
    }

    throw new RuntimeException("illegal child: " + index);
  }

  public void acceptTreeVisitor(TreeVisitor visitor)
  {
    visitor.visitNodeTree(this);
  }
}
