package casestudy;
import java.util.*;

abstract public class VisitorSupport
  implements Visitor
{
  private Map interfaces;

  public VisitorSupport()
  {
    interfaces = new HashMap();
  }

  public void visit(Visitable visitable)
  {
    Class inter_face = visitable.getVisitorInterface();

    Boolean ok = (Boolean)interfaces.get(inter_face);
    if (ok == null) {
      ok = new Boolean(inter_face.isInstance(this));
      interfaces.put(inter_face, ok);
    }

    if (ok.booleanValue()) {
      visitable.accept(this);
    } else {
      //throw new VisitFailure();
    }
  }
}
