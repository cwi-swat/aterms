abstract public class Node
  implements Visitable
{
  private Class visitorInterface;

  public Class getVisitorInterface()
  {
    if (visitorInterface == null) {
      try {
	visitorInterface = Class.forName("NodeVisitor");
      } catch (ClassNotFoundException e) {
	throw new RuntimeException("cannot find class: 'NodeVisitor'");
      }
    }

    return visitorInterface;
  }

  abstract void acceptNodeVisitor(NodeVisitor visitor);

  public void accept(Visitor visitor)
  {
    acceptNodeVisitor((NodeVisitor)visitor);
  }
}
