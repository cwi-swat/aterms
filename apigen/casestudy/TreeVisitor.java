package casestudy;

public interface TreeVisitor
  extends Visitor
{
  void visitBranch(Branch branch);
  void visitApple(Apple apple);
  void visitNodeTree(NodeTree tree);
}
