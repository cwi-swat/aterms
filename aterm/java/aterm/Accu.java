package aterm;

import java.io.*;

import visitor.*;

public class Accu
  extends ATermVisitorBridge
  implements ATermVisitor
{
  int sum;

  public static final void main(String[] args)
    throws IOException
  {
    ATermFactory factory = new aterm.pure.PureFactory();
    ATerm t = factory.readFromFile(System.in);
    Accu accu = new Accu();
    Visitor td = new TopDown(accu);

    td.visit(t);

    System.out.println("accumulated: " + accu.sum);
  }

  public Accu()
  {
    super.visitor = this;
  }

  public boolean visitInt(ATermInt i)
  {
    sum += i.getInt();
    return true;
  }

  public boolean visitAppl(ATermAppl appl) { return true; }
  public boolean visitList(ATermList appl) { return true; }
  public boolean visitReal(ATermReal appl) { return true; }
  public boolean visitBlob(ATermBlob appl) { return true; }
  public boolean visitPlaceholder(ATermPlaceholder appl) { return true; }
}
