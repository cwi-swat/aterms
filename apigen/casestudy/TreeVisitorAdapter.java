package casestudy;
public class TreeVisitorAdapter
  extends VisitorSupport
  implements TreeVisitor
{
  //{{{ public void visitTree(Tree tree)

  public void visitTree(Tree tree)
  {
  }

  //}}}

  //{{{ public void visitApple(Apple apple)

  public void visitApple(Apple apple)
  {
    visitTree(apple);
  }

  //}}}
  //{{{ public void visitBranch(Branch branch)

  public void visitBranch(Branch branch)
  {
    visitTree(branch);
  }

  //}}}
  //{{{ public void visitNodeTree(NodeTree nodetree)

  public void visitNodeTree(NodeTree node_tree)
  {
    visitTree(node_tree);
  }

  //}}}
}
  
