package test;

import test.rolodex.*;

public class RoloTest
{

  private RolodexFactory factory;
  static private String directory;

  public RoloTest
  (RolodexFactory factory) {
    this.factory = factory;
  }

  public void run() throws jjtraveler.VisitFailure, java.io.IOException {
      RoloList list = RoloList.fromTerm(factory.readFromFile(directory + "/rolodex.trm"));
      Collector d = new Collector();
      jjtraveler.Visitor debugger = new jjtraveler.BottomUp(d);
      debugger.visit(list);
      System.err.println(d.concatenation);

      testAssert(d.concatenation.equals("123456"),"bottom-up");
  }

  public final static void main(String[] args) throws jjtraveler.VisitFailure,
  java.io.IOException
  {
    RoloTest test = new RoloTest (new RolodexFactory()); 
    directory = args[0];

    test.run();
    return;
  }

  void testAssert(boolean b, String name) {
    if (!b) {
      throw new RuntimeException("Test " + name + " failed!");
    }
  }
}

class Collector extends Fwd {
  public String concatenation = "";

  public Collector () {
    super (new jjtraveler.Identity());
  }

  public void voidVisit(jjtraveler.Visitable v) {
    // do nothing
  }

  public void visit_PhoneNumber_Voice(jjtraveler.Visitable v) {
    System.err.println(v.toString());
    concatenation = concatenation + v.toString();
  }
}
