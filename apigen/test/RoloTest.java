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
      RoloList list = 
      RoloList.fromTerm(factory.readFromFile(directory + "/rolodex.trm"));

      Collector c = new Collector();
      jjtraveler.Visitor tester = new jjtraveler.BottomUp(c);
      tester.visit(list);

      testAssert(c.concatenation.equals("123"),"bottom-up");
  }

  public final static void main(String[] args) throws jjtraveler.VisitFailure,
  java.io.IOException
  {
    RoloTest test = new RoloTest (new RolodexFactory()); 
    directory = args[0];

    test.run();
    System.out.println("klaar!");
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
    concatenation = concatenation + v.getVoice().toString();
  }
}
