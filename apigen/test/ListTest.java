package test;

import test.list.Factory;
import test.list.types.Layout;
import test.list.types.Module;
import test.list.types.ModuleList;
import test.list.types.Modules;
import test.list.types.NineSeps;
import test.list.types.Separated;
import aterm.ATerm;
import aterm.ATermList;
import aterm.pure.PureFactory;

public class ListTest {

	private Factory factory;

	public ListTest(Factory factory) {
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
			mods[0] = factory.ModulesFromTerm(factory.getPureFactory().parse("meuk([\"m1\",\"m2\",\"m3\",\"m4\"])"));
		}
		catch (RuntimeException e) {
			exceptionThrown = true;
		}

		testAssert(exceptionThrown, "negative fromTermTest");
		exceptionThrown = false;

		mods[0] = factory.ModulesFromTerm(factory.getPureFactory().parse("[\"m1\",\"m2\",\"m3\",\"m4\"]"));

		mod[0] = mods[0].getHead();
		testAssert("\"m1\"".equals(mod[0].toString()), "getFirstTest");

		testAssert(mods[0].hasTail(), "hasTail test");

		mods[4] = mods[0].getTail();
		testAssert("[\"m2\",\"m3\",\"m4\"]".equals(mods[4].toString()), "getNextTest");

		mods[1] = factory.makeModules(m, factory.makeModules());
		strs[1] = mods[1].toString();
		testAssert("[\"amodule\"]".equals(strs[1]), "toStringTest1");

		mods[2] = factory.makeModules(m, mods[1]);
		strs[2] = mods[2].toString();
		testAssert("[\"amodule\",\"amodule\"]".equals(strs[2]), "toStringTest2");

		mods[3] = factory.makeModules(m, mods[2]);
		strs[3] = mods[3].toString();
		testAssert("[\"amodule\",\"amodule\",\"amodule\"]".equals(strs[3]), "toStringTest3");

		l = factory.makeModuleList_Modules(mods[3]);
		strs[4] = l.toString();

		testAssert("list([\"amodule\",\"amodule\",\"amodule\"])".equals(strs[4]), "toStringTest4");

		/*
		 * If the hash code generator does not generate different hash codes
		 * for these two constructors (they have the same pattern, same
		 * alternative name, but a different type) then a ClassCastException
		 * will occurr:
		 */
		l = factory.makeModuleList_Modules(mods[3]);
		testAssert(l.isSortModuleList() == true, "is<type> test");
		testAssert(l.isModules() == true, "is<cons> test");

		String example = "[]";
		ATerm termExample;

		try {
			termExample = factory.getPureFactory().parse(example);
			testAssert(termExample != null, "need reference to prevent gc");
			mods[4] = (Modules) factory.makeModules();
		}
		catch (ClassCastException ex) {
			testAssert(false, "ClassCastException occurred due to wrong equivalence implementation");
		}

		example = "\"amodule\"";
		termExample = factory.getPureFactory().parse(example);
		Module amodule = (Module) factory.makeModule_Default(example);
		testAssert(amodule != null, "test cast");

		ATerm pattern =
			factory.getPureFactory().parse(
				"[\"m1\",l(\"l1\"),\"sep\",l(\"l2\"),\"m2\",l(\"l3\"),\"sep\",l(\"l4\"),\"m3\",l(\"l5\"),\"sep\",l(\"l6\"),\"m4\"]");

		Separated sep = factory.SeparatedFromTerm(pattern);
		testAssert(sep.toTerm().isEqual(pattern), " fromTerm == toTerm separated lists");

		ATerm patternReversed =
			factory.getPureFactory().parse(
				"[\"m4\",l(\"l5\"),\"sep\",l(\"l6\"),\"m3\",l(\"l3\"),\"sep\",l(\"l4\"),\"m2\",l(\"l1\"),\"sep\",l(\"l2\"),\"m1\"]");
		Separated sepReversed = factory.SeparatedFromTerm(patternReversed);
		testAssert(sep.reverse().isEqual(sepReversed), " separated reverse test");
		testAssert(sep.reverseSeparated().toTerm().isEqual(patternReversed), "separated reverse toTerm test");

		Module head = sep.getHead();

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
		sep = factory.makeSeparated(m, l1, l2, factory.makeSeparated(m2));

		ATerm p = factory.getPureFactory().parse("[\"m\",l(\"l1\"),\"sep\",l(\"l2\"),\"m2\"]");
		testAssert(sep.toTerm().isEqual(p), "many separated list");

		ATerm pc =
			((ATermList) p).concat((ATermList) factory.getPureFactory().parse("[l(\"l1\"),\"sep\",l(\"l2\")]")).concat(
				(ATermList) p);
		testAssert(factory.concat(sep, l1, l2, sep).toTerm().isEqual(pc), "concat test");

		testMakeLists();
		testReverse();
		testConcat();
		testAppend();
		testElementAt();

		testSeparatedElementAt();
		testSeparatedAppend();
		testMakeSeparatedLists();
		testNineSeps();
	}

	private void testElementAt() {
		Modules list = factory.makeModules();
		for (int i = 0; i < 10; i++) {
			list = list.insert(factory.makeModule_Default("m" + i));
		}

		for (int i = 0; i < list.getLength(); i++) {
			Module m = factory.makeModule_Default("m" + (list.getLength() - i - 1));
			testAssert(list.elementAt(i).equals(m), "list element at " + i);
			testAssert(list.getModuleAt(i).equals(m), "typed list element at " + i);
		}
	}

	private void testAppend() {
		Module m = factory.makeModule_Default("m");
		Modules list = factory.makeModules();

		list = list.append(m);
		testAssert(list.equals(factory.makeModules(m)), "append to empty list");

		Module q = factory.makeModule_Default("q");
		list = list.append(q);
		testAssert(list.equals(factory.makeModules(m, q)), "append to singleton list");
	}

	private void testConcat() {
		Module m[] = new Module[3];
		for (int i = 0; i < m.length; i++) {
			m[i] = factory.makeModule_Default("m" + i);
		}

		Modules list = factory.makeModules(m[0], m[1], m[2]);
		Modules concatenated = list.concat(list);
		testAssert(concatenated.getLength() == 2 * list.getLength(), "length concatenated lists");
		testAssert(concatenated.equals(factory.makeModules(m[0], m[1], m[2], m[0], m[1], m[2])), "concatenated lists");
	}

	private void testReverse() {
		Module m[] = new Module[3];
		for (int i = 0; i < m.length; i++) {
			m[i] = factory.makeModule_Default("m" + i);
		}

		Modules forward = factory.makeModules(m[0], m[1], m[2]);
		Modules reverse = factory.makeModules(m[2], m[1], m[0]);
		testAssert(forward.reverse().equals(reverse), "reverse list");

		Modules ml = reverse.reverseModules();
		testAssert(forward.equals(ml), "typed reverse list");
	}

	private void testMakeLists() {
		Module m[] = new Module[6];

		for (int i = 0; i < m.length; i++) {
			m[i] = factory.makeModule_Default("m" + i);
		}

		Modules list[] = new Modules[7];

		list[0] = factory.makeModules();
		list[1] = factory.makeModules(m[0]);
		list[2] = factory.makeModules(m[0], m[1]);
		list[3] = factory.makeModules(m[0], m[1], m[2]);
		list[4] = factory.makeModules(m[0], m[1], m[2], m[3]);
		list[5] = factory.makeModules(m[0], m[1], m[2], m[3], m[4]);
		list[6] = factory.makeModules(m[0], m[1], m[2], m[3], m[4], m[5]);

		for (int i = 0; i < 7; i++) {
			testAssert(list[i].getLength() == i, "" + i + " element list");
		}
	}

	private void testMakeSeparatedLists() {
		Module m[] = new Module[6];

		for (int i = 0; i < 6; i++) {
			m[i] = factory.makeModule_Default("m" + i);
		}
		Layout l = factory.makeLayout_Default("l");

		Separated list[] = new Separated[7];

		list[0] = factory.makeSeparated();
		list[1] = factory.makeSeparated(m[0]);
		list[2] = factory.makeSeparated(l, l, m[0], m[1]);
		list[3] = factory.makeSeparated(l, l, m[0], m[1], m[2]);
		list[4] = factory.makeSeparated(l, l, m[0], m[1], m[2], m[3]);
		list[5] = factory.makeSeparated(l, l, m[0], m[1], m[2], m[3], m[4]);
		list[6] = factory.makeSeparated(l, l, m[0], m[1], m[2], m[3], m[4], m[5]);

		for (int i = 0; i < 7; i++) {
			testAssert(list[i].getLength() == i, "" + i + " element list");
		}
	}

	private void testSeparatedElementAt() {
		Separated triple =
			factory.SeparatedFromString("[\"m0\",l(\" \"),\"sep\",l(\" \"),\"m1\",l(\" \"),\"sep\",l(\" \"),\"m2\"]");

		for (int i = 0; i < 3; i++) {
			Module m = (Module) triple.elementAt(i);
			Module ref = factory.makeModule_Default("m" + i);
			testAssert(m.isEqual(ref), "elementAt " + i);

			m = triple.getModuleAt(i);
			testAssert(m.isEqual(ref), "getModuleAt " + i);
		}
	}

	private void testSeparatedAppend() {
		Separated empty = factory.makeSeparated();
		Module m = factory.makeModule_Default("m");
		Layout l = factory.makeLayout_Default(" ");
		Separated single = empty.append(l, l, m);

		testAssert(single.isEqual(factory.SeparatedFromString("[\"m\"]")), "test append on empty list");

		Separated twin = single.append(l, l, m);
		testAssert(
			twin.isEqual(factory.SeparatedFromString("[\"m\",l(\" \"),\"sep\",l(\" \"),\"m\"]")),
			"append on singleton");
	}

	private void testNineSeps() {
		Module m = factory.makeModule_Default("m");
		Module m2 = factory.makeModule_Default("m2");
		NineSeps ns = factory.makeNineSeps(m, factory.makeNineSeps(m2));

		testAssert(
			ns.toTerm().isEqual(factory.getPureFactory().parse("[\"m\",1,2,3,4,5,6,7,8,9,\"m2\"]")),
			"many separated toTerm");
		testAssert(ns.reverse().isEqual(factory.makeNineSeps(m2, factory.makeNineSeps(m))), "many separated reverse");
		testAssert(ns.reverse().reverse().isEqual(ns), "reverse separated list test");

		try {
			ns.append((aterm.ATerm) m2);
			testAssert(false, "illegal call did not throw an exception");
		}
		catch (UnsupportedOperationException ex) {
			// this is what should happen
		}

		NineSeps ns2 = ns.append(m2);
		testAssert(
			ns2.isEqual(factory.NineSepsFromString("[\"m\",1,2,3,4,5,6,7,8,9,\"m2\",1,2,3,4,5,6,7,8,9,\"m2\"]")),
			"test append");
	}

	public final static void main(String[] args) {
		ListTest test = new ListTest(Factory.getInstance(new PureFactory()));

		test.run();
		return;
	}

	void testAssert(boolean b, String name) {
		if (!b) {
			throw new RuntimeException("Test " + name + " failed!");
		}
	}
}
