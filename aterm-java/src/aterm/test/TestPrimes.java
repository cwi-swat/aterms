/*
 * Copyright (c) 2002-2007, CWI and INRIA
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
