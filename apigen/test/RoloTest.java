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

      System.err.println("data: " + list);
      Collector c = new Collector();
      jjtraveler.Visitor tester = new jjtraveler.BottomUp(c);
      tester.visit(list);

      System.err.println("result: " + c.concatenation);

      testAssert(c.concatenation.equals("voice(1)voice(2)voice(3)"),"bottom-up");
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

  public void visit_PhoneNumber_Voice(PhoneNumber_VoiceImpl v) {
    System.err.println("hai: " + v.toString());
    concatenation = concatenation + v.toString();
  }
}
