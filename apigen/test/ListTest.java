package test;

import test.list.*;

public class ListTest {

  private ListFactory factory;

  public ListTest(ListFactory factory) {
    this.factory = factory;
  }

  public void run() {
    String[] strs = new String[8];
    Modules[] mods = new Modules[8];
    Module[] mod = new Module[8];
    Module m = factory.makeModule_Default("amodule");
    ModuleList l;
   
    mods[0] = Modules.fromTerm(
      factory.parse("modules([\"m1\",\"m2\",\"m3\",\"m4\"])"));

    mod[0] = mods[0].getFirst();
    testAssert("\"m1\"".equals(mod[0].toString()), "getFirstTest");

    testAssert(mods[0].hasNext(), "hasNextTest");

    mods[4] = mods[0].getNext();
    testAssert("[\"m2\",\"m3\",\"m4\"]".equals(mods[4].toString()), 
	       "getNextTest");

    mods[1] = factory.makeModules_Body(m, factory.makeModules_Empty());
    strs[1] = mods[1].toString();
    testAssert("[\"amodule\"]".equals(strs[1]), "toStringTest1");
   
    
    mods[2] = factory.makeModules_Body(m,  mods[1]);
    strs[2] = mods[2].toString();
    testAssert("[\"amodule\",\"amodule\"]".equals(strs[2]), "toStringTest2");
    
    
    mods[3] = factory.makeModules_List(m, mods[2]);
    strs[3] = mods[3].toString();
    testAssert("modules([\"amodule\",\"amodule\",\"amodule\"])".equals(strs[3]),
	       "toStringTest3");

    l = factory.makeModuleList_Modules(mods[3]);
    strs[4] = l.toString();
    testAssert("list(modules([\"amodule\",\"amodule\",\"amodule\"]))".equals(
	     strs[4]), "toStringTest4");
         
    /* If the hash code generator does not generate different hash codes for these
     * two constructors (they have the same pattern, same alternative name, but a different type)
     * then a ClassCastException will occurr:
     */
    Modules mempty = factory.makeModules_Empty();
    Modules2 mempty2 = factory.makeModules2_Empty();

    ListFactory f = mempty.getListFactory();
 
    l = factory.makeModuleList_Modules(mods[3]);
    testAssert(l.isSortModuleList() == true, "is<type> test");
    testAssert(l.isModules() == true, "is<cons> test");
        
  }

  public final static void main(String[] args) {
    ListTest test = new ListTest(new ListFactory());
   
    test.run();
    return;
  }

  void testAssert(boolean b, String name) {
    if (!b) {
      throw new RuntimeException("Test " + name + " failed!");
    }
  }
}

