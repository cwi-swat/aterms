package test;

import test.list.ListFactory;
import test.list.Module;
import test.list.ModuleList;
import test.list.Modules;
import test.list.Modules2;
import test.list.Separated;

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
    boolean exceptionThrown = false;
    
    try {
      mods[0] = factory.ModulesFromTerm(
        factory.parse("meuk([\"m1\",\"m2\",\"m3\",\"m4\"])"));
    }
    catch (RuntimeException e) {
      exceptionThrown = true;	
    }
    
    testAssert(exceptionThrown, "negative fromTermTest");
    exceptionThrown = false;
    
    mods[0]= factory.ModulesFromTerm(factory.parse("[\"m1\",\"m2\",\"m3\",\"m4\"]"));
    
    mod[0] = mods[0].getHead();
    testAssert("\"m1\"".equals(mod[0].toString()), "getFirstTest");

    testAssert(mods[0].hasTail(), "hasTail test");

    mods[4] = mods[0].getTail();
    testAssert("[\"m2\",\"m3\",\"m4\"]".equals(mods[4].toString()), 
	       "getNextTest");

    mods[1] = factory.makeModules(m, factory.makeModules());
    strs[1] = mods[1].toString();
    testAssert("[\"amodule\"]".equals(strs[1]), "toStringTest1");
   
    
    mods[2] = factory.makeModules(m,  mods[1]);
    strs[2] = mods[2].toString();
    testAssert("[\"amodule\",\"amodule\"]".equals(strs[2]), "toStringTest2");
    
    
    mods[3] = factory.makeModules(m, mods[2]);
    strs[3] = mods[3].toString();
    testAssert("[\"amodule\",\"amodule\",\"amodule\"]".equals(strs[3]),
	       "toStringTest3");

    l = factory.makeModuleList_Modules(mods[3]);
    strs[4] = l.toString();
    
    testAssert("list([\"amodule\",\"amodule\",\"amodule\"])".equals(
	     strs[4]), "toStringTest4");
         
    /* If the hash code generator does not generate different hash codes for these
     * two constructors (they have the same pattern, same alternative name, but a different type)
     * then a ClassCastException will occurr:
     */
    Modules mempty = factory.makeModules();
    Modules2 mempty2 = factory.makeModules2();

    ListFactory f = mempty.getListFactory();
 
    l = factory.makeModuleList_Modules(mods[3]);
    testAssert(l.isSortModuleList() == true, "is<type> test");
    testAssert(l.isModules() == true, "is<cons> test");

    /* Test whether we get a term of the correct type! A ClassCastException
     * will occur otherwise
     */
    String example = "[]";
    aterm.ATerm termExample = factory.parse(example);
    mods[4] = (Modules) factory.makeModules();

    example = "\"amodule\"";
    termExample = factory.parse(example);
    Module amodule = (Module) factory.makeModule_Default(example);

    Separated sep = factory.SeparatedFromTerm(factory.parse("[\"m2\",l(\"l2\"),\"sep\",l(\"l1\"),\"m1\",l(\"l2\"),\"sep\",l(\"l1\"),\"m2\",l(\"l2\"),\"sep\",l(\"l1\"),\"m1\"]"));

    testAssert(sep.getLength() == 4, "separated list length test");
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

