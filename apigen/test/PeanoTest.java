package test;

import test.Replace;
import test.peano.Factory;
import test.peano.nat.Nat;
import aterm.ATerm;
import aterm.pure.PureFactory;

public class PeanoTest {

	private Factory factory;

	public PeanoTest(Factory factory) {
		this.factory = factory;
	}

	public Factory getPeanoFactory() {
		return factory;
	}

	public Nat make(int n) {
		Nat N = factory.makeNat_Zero();
		for (int i = 0; i < n; i++) {
			N = factory.makeNat_Suc(N);
		}

		return N;
	}

	public int unmake(Nat N) {
		int n;

		for (n = 0; !N.isZero(); N = N.getPred(), n++);

		return n;
	}

	public void run1() {
		int n = 17;
		int r = fib(n);
		Nat N = make(n);
		Nat R = fib(N);

		if (unmake(R) != r) {
			System.err.println("unexpected result of computation!");
			System.exit(1);
		}
	}

	public void run2() {
		//    GenericTraversal traversal = new GenericTraversal();
		Replace replace = new Replace() {
			public ATerm apply(ATerm atermSubject) {
				Nat subject = (Nat) atermSubject;
				if (subject.isPlus()) {
					Nat arg1 = subject.getArg1();
					Nat arg2 = subject.getArg2();
					if (arg2.isZero()) {
						return arg1;
					}
					else if (arg2.isSuc()) {
						Nat y = arg2.getPred();
						return getPeanoFactory().makeNat_Suc(getPeanoFactory().makeNat_Plus(arg1, y));
					}
				}
				else if (subject.isFib()) {
					Nat t = subject.getArg1();
					if (t.isZero()) {
						return getPeanoFactory().makeNat_Suc(t);
					}
					else if (t.isSuc() && t.getPred().isZero()) {
						return t.getPred();
					}
					else {
						Nat pred1 = t.getPred();
						Nat pred2 = pred1.getPred();
						return getPeanoFactory().makeNat_Plus(
							getPeanoFactory().makeNat_Fib(pred2),
							getPeanoFactory().makeNat_Fib(pred1));
					}
				}
				return null;
			}
		};

		int n = 17;
		int r = fib(n);
		Nat N = make(n);
		Nat R = factory.makeNat_Fib(N);

		Nat res = null;
		while (res != R) {
			res = (Nat) replace.apply(R);
			R = res;
		}

		if (unmake(R) != r) {
			System.err.println("unexpected result of computation!");
			System.exit(1);
		}

	}

	public final static void main(String[] args) {
		PeanoTest test = new PeanoTest(new Factory(new PureFactory()));
		test.run1();
	}

	public Nat plus(Nat t1, Nat t2) {
		if (t2.isZero()) {
			return t1;
		}
		else if (t2.isSuc()) {
			Nat y = t2.getPred();
			return factory.makeNat_Suc(plus(t1, y));
		}
		else {
			return null;
		}
	}

	public Nat fib(Nat t) {
		if (t.isZero()) {
			return factory.makeNat_Suc(factory.makeNat_Zero());
		}
		else if (t.isSuc() && t.getPred().isZero()) {
			return factory.makeNat_Zero();
		}
		else {
			Nat pred1 = t.getPred();
			Nat pred2 = pred1.getPred();

			return plus(fib(pred2), fib(pred1));
		}
	}

	public int fib(int n) {
		if (n == 0) {
			return 1;
		}
		else if (n == 1) {
			return 0;
		}
		else {
			return fib(n - 2) + fib(n - 1);
		}
	}
}
