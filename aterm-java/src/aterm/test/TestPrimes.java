package aterm.test;

import aterm.*;
import aterm.pure.*;

public class TestPrimes {

  private ATermFactory factory;

  void assertTrue(boolean condition) {
    if(!condition) {
      throw new RuntimeException("assertion failed.");
    }
  }

  public final static void main(String[] args) {
    TestPrimes t = new TestPrimes(new PureFactory());

    t.test1();
  }

  public void test1() 
  {
    assertTrue(getPrimes(30) == factory.parse("[1,2,3,5,7,11,13,17,19,23,29]"));
    assertTrue(getPrimes(500).getLength() == 96);
  }

  public TestPrimes(ATermFactory factory) {
    this.factory = factory;
  }
  
    /**
     * Compute the list of primes.
     * 
     * @param max
     */
  public ATermList getPrimes(int max) {
    ATermList primes;
    primes = generateNumbers(max);
    primes = filterNonPrimes(primes);
    return primes;
  }

    /**
     * Generate a list of consecutive numbers.
     * 
     * @param max
     */
  private ATermList generateNumbers(int max) {
    ATermList numbers = factory.makeList();
    for(int i=max; i>0; i--)
      numbers = factory.makeList(factory.makeInt(i), numbers);
    return numbers;
  }

    /**
     * Filter non-primes
     * 
     * @param numbers
     */
   private ATermList filterNonPrimes(ATermList numbers) {
    ATermList primes = factory.makeList();
    numbers = numbers.getNext();
    while(!numbers.isEmpty()) {
      ATermInt prime = (ATermInt)numbers.getFirst();
      numbers = filterMultiples(prime.getInt(), numbers);
      primes = primes.append(prime);
    }
    return factory.makeList(factory.makeInt(1), primes);
  }

    /**
     * Filter multiples of n.
     * 
     * @param n
     * @param numbers
     */
  private ATermList filterMultiples(int n, ATermList numbers) {
    int nr, len = numbers.getLength();

    for(int i=0; i<len; i++) {
      ATermInt el = (ATermInt)numbers.elementAt(i);
      nr = el.getInt();
      if(nr % n == 0) {
        len--;
        numbers = numbers.removeElementAt(i);
      }
    }
    return numbers;
  }

}
