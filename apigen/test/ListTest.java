package test;

import test.list.layout.Layout;
import test.list.ListFactory;
import test.list.module.Module;
import test.list.moduleList.ModuleList;
import test.list.modules.Modules;
import test.list.modules2.Modules2;
import test.list.nineSeps.NineSeps;
import test.list.separated.Separated;
import aterm.ATerm;
import aterm.ATermList;
import aterm.pure.PureFactory;

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
        factory.getPureFactory().parse("meuk([\"m1\",\"m2\",\"m3\",\"m4\"])"));
    }
    catch (RuntimeException e) {
      exceptionThrown = true;	
    }
    
    testAssert(exceptionThrown, "negative fromTermTest");
    exceptionThrown = false;
    
    mods[0]= factory.ModulesFromTerm(factory.getPureFactory().parse("[\"m1\",\"m2\",\"m3\",\"m4\"]"));
    
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
    ATerm termExample = factory.getPureFactory().parse(example);
    mods[4] = (Modules) factory.makeModules();

    example = "\"amodule\"";
    termExample = factory.getPureFactory().parse(example);
    Module amodule = (Module) factory.makeModule_Default(example);

    ATerm pattern = factory.getPureFactory().parse("[\"m1\",l(\"l1\"),\"sep\",l(\"l2\"),\"m2\",l(\"l3\"),\"sep\",l(\"l4\"),\"m3\",l(\"l5\"),\"sep\",l(\"l6\"),\"m4\"]");

    Separated sep = factory.SeparatedFromTerm(pattern);
    testAssert(sep.toTerm().isEqual(pattern), " fromTerm == toTerm separated lists");


    ATerm patternReversed = factory.getPureFactory().parse("[\"m4\",l(\"l5\"),\"sep\",l(\"l6\"),\"m3\",l(\"l3\"),\"sep\",l(\"l4\"),\"m2\",l(\"l1\"),\"sep\",l(\"l2\"),\"m1\"]");
    Separated sepReversed = factory.SeparatedFromTerm(patternReversed);
    testAssert(sep.reverse().isEqual(sepReversed)," separated reverse test");
    testAssert(sep.reverseSeparated().toTerm().isEqual(patternReversed),"separated reverse toTerm test");
        
    Module head = sep.getHead();;
    testAssert(head.isEqual(factory.makeModule_Default("m1")), "separator from term test, head");
    Layout l1 = sep.getWsl();
    testAssert(l1.isEqual(factory.makeLayout_Default("l1")), "getSeparator test l1");
    Layout l2 = sep.getWsr();
    testAssert(l2.isEqual(factory.makeLayout_Default("l2")), "getSeparator test l2");
    Module second = sep.getTail().getHead();
    testAssert(second.isEqual(factory.makeModule_Default("m2")), "separator from term test, second element");
    Layout l3 = sep.getTail().getWsl();

    testAssert(l3.isEqual(factory.makeLayout_Default("l3")), "getSeparator test l3");
    Layout l4 = sep.getTail().getWsr();
    testAssert(l4.isEqual(factory.makeLayout_Default("l4")), "getSeparator test l4");
    
    testAssert(sep.getLength() == 4, "separated list length test");
    
    sep = factory.makeSeparated();
    testAssert(sep.toTerm().isEqual(factory.getPureFactory().makeList()), "empty separated list");
    
    m = factory.makeModule_Default("m");
    sep = factory.makeSeparated(m);
   
    testAssert(sep.toTerm().isEqual(factory.getPureFactory().makeList(m.toTerm())), "singleton separated list");
    
    Module m2 = factory.makeModule_Default("m2");
    l1 = factory.makeLayout_Default("l1");
    l2 = factory.makeLayout_Default("l2");
    sep = factory.makeSeparated(m,l1,l2,factory.makeSeparated(m2));

    ATerm p = factory.getPureFactory().parse("[\"m\",l(\"l1\"),\"sep\",l(\"l2\"),\"m2\"]");
    testAssert(sep.toTerm().isEqual(p), "many separated list");
    
    ATerm pc = ((ATermList) p).concat((ATermList) factory.getPureFactory().parse("[l(\"l1\"),\"sep\",l(\"l2\")]")).concat((ATermList) p);
    testAssert(factory.concat(sep,l1,l2,sep).toTerm().isEqual(pc), "concat test");
    NineSeps ns = factory.makeNineSeps(m,factory.makeNineSeps(m2));
    testAssert(ns.toTerm().isEqual(factory.getPureFactory().parse("[\"m\",1,2,3,4,5,6,7,8,9,\"m2\"]")), "many separated toTerm");
    testAssert(ns.reverse().isEqual(factory.makeNineSeps(m2, factory.makeNineSeps(m))), "many separated reverse");
    testAssert(ns.reverse().reverse().isEqual(ns), "reverse separated list test");
    
    
  }

  public final static void main(String[] args) {
    ListTest test = new ListTest(new ListFactory(new PureFactory()));
   
    test.run();
    return;
  }

  void testAssert(boolean b, String name) {
    if (!b) {
      throw new RuntimeException("Test " + name + " failed!");
    }
  }
}

