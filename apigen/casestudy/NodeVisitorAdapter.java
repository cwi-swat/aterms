public class NodeVisitorAdapter
  extends VisitorSupport
  implements NodeVisitor
{
  //{{{ public void visitNode(Node node)

  public void visitNode(Node node)
  {
  }

  //}}}

  //{{{ public void visitLeaf(Leaf leaf)

  public void visitLeaf(Leaf leaf)
  {
    visitNode(leaf);
  }

  //}}}
  //{{{ public void visitFork(Fork fork)

  public void visitFork(Fork fork)
  {
    visitNode(fork);
  }

  //}}}
}
  
