public interface NodeVisitor
  extends Visitor
{
  public void visitLeaf(Leaf leaf);
  public void visitFork(Fork fork);
}
