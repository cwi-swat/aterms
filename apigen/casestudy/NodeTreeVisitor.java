package casestudy;

public class NodeTreeVisitor
  extends VisitorSupport
  implements NodeVisitor, TreeVisitor
{
  public void visitNode(Node node)
  {
  }

  public void visitLeaf(Leaf leaf)
  {
    visitNode(leaf);
  }

  public void visitFork(Fork fork)
  {
    visitNode(fork);
  }

  public void visitTree(Tree tree)
  {
  }

  public void visitBranch(Branch branch)
  {
    visitTree(branch);
  }

  public void visitApple(Apple apple)
  {
    visitTree(apple);
  }

  public void visitNodeTree(NodeTree tree)
  {
    visitTree(tree);
  }
}
