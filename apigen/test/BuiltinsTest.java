package test;

import test.builtins.Factory;
import test.builtins.types.SepIntList;
import aterm.pure.PureFactory;

public class BuiltinsTest {

    private Factory factory;

    public BuiltinsTest(Factory factory) {
        this.factory = factory;
    }

    public void run() {
        test.builtins.types.D t =
            factory.makeD_Trm(factory.getPureFactory().parse("one"));
        testAssert(t.toString().equals("term(one)"), "make term test");

        test.builtins.types.D d = factory.makeD_Ddouble(1.0);
        testAssert(d.toString().equals("double(1.0)"), "make double test");
        testAssert(d.getNumber() == 1.0, "get double test");
        testAssert(d.setNumber(2.0).getNumber() == 2.0, "set double test");

        test.builtins.types.D i = factory.makeD_Iinteger(1);
        testAssert(i.toString().equals("int(1)"), "make int test");
        testAssert(i.getInteger() == 1, "get integer test");
        testAssert(i.setInteger(2).getInteger() == 2, "set integer test");

        test.builtins.types.D l =
            factory.makeD_Lst((aterm.ATermList) factory.getPureFactory().parse("[one]"));
        testAssert(l.toString().equals("list([one])"), "make list test");

        test.builtins.types.D s = factory.makeD_Sstring("one");
        testAssert(s.toString().equals("str(\"one\")"), "make str test");

        SepIntList sepi1 = factory.makeSepIntList(1);
        SepIntList sepi2 = factory.makeSepIntList(2, "sep", sepi1);
        testAssert(sepi2.toString().equals("[2,\"sep\",\",\",1]"), "make builtins list");

        SepIntList sepi3 = factory.makeSepIntList(2);
        SepIntList sepi4 = factory.makeSepIntList(1, "sep", sepi3);
        testAssert(
            sepi2.reverseSepIntList().isEqual(sepi4),
            "reverse separated builtins list");

  
        test.builtins.types.Lexical L = factory.makeLexical_Default("hello");
        testAssert(L.getString().equals("hello"), "string getter from a chars builtin");
        
        aterm.ATerm atermLex = factory.getPureFactory().parse("string([104,101,108,108,111])");
        testAssert(L.toTerm().isEqual(atermLex), "term representation of chars");
        testAssert(factory.LexicalFromTerm(atermLex).isEqual(L), "chars fromTerm");

	test.builtins.types.Character C = factory.makeCharacter_Default('A');
	testAssert(C.getCh() == 'A', "char getter from char builtin");

	aterm.ATerm atermChar = factory.getPureFactory().parse("character(65)");
	testAssert(C.toTerm().isEqual(atermChar), "term representation of char");
    }

    public final static void main(String[] args) {
        BuiltinsTest test = new BuiltinsTest(Factory.getInstance(new PureFactory()));
        test.run();
        return;
    }

    void testAssert(boolean b, String name) {
        if (!b) {
            throw new RuntimeException("Test " + name + " failed!");
        }
    }
}
