package casestudy;

import java.util.*;

public class Sequence
  implements Visitor
{
  List visitors;

  //{{{ public Sequence()

  public Sequence()
  {
    visitors = new LinkedList();
  }

  //}}}
  //{{{ public Sequence(Visitor v1, Visitor v2)

  public Sequence(Visitor v1, Visitor v2)
  {
    this();
    visitors.add(v1);
    visitors.add(v2);
  }

  //}}}

  //{{{ public void addVisitor(Visitor visitor)

  public void addVisitor(Visitor visitor)
  {
    visitors.add(visitor);
  }

  //}}}

  //{{{ public void visit(Visitable visitable)

  public void visit(Visitable visitable)
  {
    Iterator iter = visitors.iterator();

    while (iter.hasNext()) {
      Visitor visitor = (Visitor)iter.next();
      visitor.visit(visitable);
    }
  }

  //}}}
}
