abstract public class Tree
  implements Visitable
{
  private Class visitorInterface;

  public Class getVisitorInterface()
  {
    if (visitorInterface == null) {
      try {
	visitorInterface = Class.forName("TreeVisitor");
      } catch (ClassNotFoundException e) {
	throw new RuntimeException("cannot find class: 'TreeVisitor'");
      }
    }

    return visitorInterface;
  }

  abstract void acceptTreeVisitor(TreeVisitor visitor);

  public void accept(Visitor visitor)
  {
    acceptTreeVisitor((TreeVisitor)visitor);
  }
}
