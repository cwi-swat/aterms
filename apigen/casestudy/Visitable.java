package casestudy;
public interface Visitable
{
  public Class getVisitorInterface();
  public void accept(Visitor visitor);
  public int nrOfChildren();
  public Visitable getChild(int index);
}
