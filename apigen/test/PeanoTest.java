package test;


public class PeanoTest {

  private PeanoFactory factory;

  public PeanoTest(PeanoFactory factory) {
    this.factory = factory;
  }

  public Nat make(int n) {
    Nat N = factory.makeNat_Zero();
    for(int i=0 ; i<n ; i++) {
      N = factory.makeNat_Suc(N);
    }

    return N;
  }

  public int unmake(Nat N) {
    int n;

    for (n=0;!N.isZero();N = N.getPred(), n++);

    return n;
  }

  public void run() {
    int n = 17;
    int r = fib(n);
    Nat N = make(n);
    Nat R = fib(N);

    if (unmake(R) != r) {
      System.err.println("unexpected result of computation!");
      System.exit (1);
    }
  }

  public final static void main(String[] args) {
    PeanoTest test = new PeanoTest(new PeanoFactory());
    test.run();
  }

  public Nat plus(Nat t1, Nat t2) {
    if (t2.isZero()) {
      return t1;
    }
    else if (t2.isSuc()) {
      Nat y = t2.getPred();
      return factory.makeNat_Suc(plus(t1,y));
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

      return plus(fib(pred2),fib(pred1));
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

