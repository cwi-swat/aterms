package test;

import java.util.List;

import aterm.AFun;
import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermFactory;
import aterm.pure.PureFactory;

public class TestFibInterpreted {

	private ATermFactory factory;

	private AFun zero, suc, plus, fib;
	private ATermAppl tzero;
	private ATerm fail;

	private ATerm lhs[];
	private ATerm rhs[];

	public final static void main(String[] args) {
		TestFibInterpreted t = new TestFibInterpreted(new PureFactory());

		t.initRules();
		t.test1(5);
	}

	public TestFibInterpreted(ATermFactory factory) {
		this.factory = factory;

		zero = factory.makeAFun("zero", 0, false);
		suc = factory.makeAFun("suc", 1, false);
		plus = factory.makeAFun("plus", 2, false);
		fib = factory.makeAFun("fib", 1, false);
		tzero = factory.makeAppl(zero);
		fail = factory.parse("fail");
	}

	public void initRules() {
		lhs = new ATerm[10];
		rhs = new ATerm[10];
		int ruleNumber = 0;

		// fib(zero) -> suc(zero)
		lhs[ruleNumber] = factory.parse("fib(zero)");
		rhs[ruleNumber] = factory.parse("suc(zero)");
		ruleNumber++;

		// fib(suc(zero)) -> suc(zero)
		lhs[ruleNumber] = factory.parse("fib(suc(zero))");
		rhs[ruleNumber] = factory.parse("suc(zero)");
		ruleNumber++;

		// fib(suc(suc(X))) -> plus(fib(X),fib(suc(X)))
		lhs[ruleNumber] = factory.parse("fib(suc(suc(<term>)))");
		rhs[ruleNumber] = factory.parse("plus(fib(<term>),fib(suc(<term>)))");
		ruleNumber++;

		// plus(zero,X) -> X
		lhs[ruleNumber] = factory.parse("plus(zero,<term>)");
		rhs[ruleNumber] = factory.parse("<term>");
		ruleNumber++;

		// plus(suc(X),Y) -> plus(X,suc(Y))
		lhs[ruleNumber] = factory.parse("plus(suc(<term>),<term>)");
		rhs[ruleNumber] = factory.parse("plus(<term>,suc(<term>))");
		ruleNumber++;

		// congruence (suc)
		lhs[ruleNumber] = factory.parse("suc(<term>)");
		rhs[ruleNumber] = factory.parse("suc(<term>)");
		ruleNumber++;

		// congruence (plus)
		lhs[ruleNumber] = factory.parse("plus(<term>,<term>)");
		rhs[ruleNumber] = factory.parse("plus(<term>,<term>)");
		ruleNumber++;

	}

	public ATerm oneStep(ATerm subject) {
		int ruleNumber = 0;
		List list;

		// fib(zero) -> suc(zero)
		list = subject.match(lhs[ruleNumber]);
		if (list != null) {
			return rhs[ruleNumber];
		}
		ruleNumber++;

		// fib(suc(zero)) -> suc(zero)
		list = subject.match(lhs[ruleNumber]);
		if (list != null) {
			return rhs[ruleNumber];
		}
		ruleNumber++;

		// fib(suc(suc(X))) -> plus(fib(X),fib(suc(X)))
		list = subject.match(lhs[ruleNumber]);
		if (list != null) {
			ATerm X = (ATerm) list.get(0);
			list.add(X);
			return factory.make(rhs[ruleNumber], list);
		}
		ruleNumber++;

		// plus(zero,X) -> X
		list = subject.match(lhs[ruleNumber]);
		if (list != null) {
			return factory.make(rhs[ruleNumber], list);
		}
		ruleNumber++;

		// plus(suc(X),Y) -> plus(X,suc(Y))
		list = subject.match(lhs[ruleNumber]);
		if (list != null) {
			return factory.make(rhs[ruleNumber], list);
		}
		ruleNumber++;

		// congruence (suc)
		list = subject.match(lhs[ruleNumber]);
		if (list != null) {
			//System.out.println("congsuc"); // applied 1184122 times fir fib(14)
			ATerm X = (ATerm) list.get(0);
			ATerm Xp = oneStep(X);
			if (Xp.equals(fail)) {
				return fail;
			} else {
				list.clear();
				list.add(Xp);
				return factory.make(rhs[ruleNumber], list);
			}
		}
		ruleNumber++;

		// congruence (plus)
		list = subject.match(lhs[ruleNumber]);
		if (list != null) {
			//System.out.println("congplus"); // applied 9159 times fir fib(14)
			ATerm X = (ATerm) list.get(0);
			ATerm Xp = oneStep(X);
			if (Xp.equals(fail)) {
				ATerm Y = (ATerm) list.get(1);
				ATerm Yp = oneStep(Y);
				if (Yp.equals(fail)) {
					return fail;
				} else {
					list.clear();
					list.add(X);
					list.add(Yp);
					return factory.make(rhs[ruleNumber], list);
				}
			} else {
				ATerm Y = (ATerm) list.get(1);
				list.clear();
				list.add(Xp);
				list.add(Y);
				return factory.make(rhs[ruleNumber], list);
			}
		}
		ruleNumber++;

		return fail;
	}

	public ATerm oneStepInnermost(ATerm subject) {
		List list;

		// fib(zero) -> suc(zero)
		list = subject.match(lhs[0]);
		if (list != null) {
			return rhs[0];
		}

		// fib(suc(zero)) -> suc(zero)
		list = subject.match(lhs[1]);
		if (list != null) {
			return rhs[1];
		}

		// fib(suc(suc(X))) -> plus(fib(X),fib(suc(X)))
		list = subject.match(lhs[2]);
		if (list != null) {
			ATerm X = (ATerm) list.get(0);
			ATerm X1 = normalize(factory.makeAppl(fib, X));
			ATerm X2 = normalize(factory.makeAppl(fib, factory.makeAppl(suc, X)));
			return factory.makeAppl(plus, X1, X2);
		}

		// plus(zero,X) -> X
		list = subject.match(lhs[3]);
		if (list != null) {
			return (ATerm) list.get(0);
		}

		// plus(suc(X),Y) -> plus(X,suc(Y)))
		list = subject.match(lhs[4]);
		if (list != null) {
			return factory.make(rhs[4], list);
		}

		return fail;
	}

	public ATerm normalize(ATerm t) {
		ATerm s = t;
		do {
			t = s;
			s = oneStep(t);
		} while (!s.equals(fail));
		return t;
	}

	public void test1(int n) {
		ATermAppl N = tzero;
		for (int i = 0; i < n; i++) {
			N = factory.makeAppl(suc, N);
		}
		ATerm tfib = factory.makeAppl(fib, N);
		normalize(tfib);
	}
}
