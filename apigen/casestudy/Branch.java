package casestudy;

public class Branch
  extends Tree
{
  Tree left;
  Tree middle;
  Tree right;

  public Branch(Tree left, Tree middle, Tree right)
  {
    this.left = left;
    this.middle = middle;
    this.right = right;
  }

  public void acceptTreeVisitor(TreeVisitor visitor)
  {
    visitor.visitBranch(this);
  }

  public int nrOfChildren()
  {
    return 3;
  }

  public Visitable getChild(int index)
  {
    switch (index) {
      case 0: return left;
      case 1: return middle;
      case 2: return right;
      default: throw new RuntimeException("illegal child: " + index);
    }
  }
}
