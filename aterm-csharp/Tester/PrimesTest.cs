using System;
using aterm;
using dotUnit.Framework;

namespace ATermTests
{
	/// <summary>
	/// Summary description for TestPrimes.
	/// </summary>
	public class PrimesTest : TestCase
	{
		private ATermFactory factory;
		public PrimesTest(string name) : base(name) {}
		protected override void SetUp()
		{
			base.SetUp ();
			factory = Tester.theFactory;

		}

		public virtual void TestPrimes() 
		{
			AssertTrue(getPrimes(30) == factory.parse("[1,2,3,5,7,11,13,17,19,23,29]"));
			AssertTrue(getPrimes(500).getLength() == 96);
		}


		/**
		 * Compute the list of primes.
		 */
		public virtual ATermList getPrimes(int max) 
		{
			ATermList primes;
			primes = generateNumbers(max);
			primes = filterNonPrimes(primes);
			return primes;
		}

		/**
		 * Generate a list of consecutive numbers.
		 */
		private ATermList generateNumbers(int max) 
		{
			ATermList numbers = factory.makeList();
			for(int i=max; i>0; i--)
				numbers = factory.makeList(factory.makeInt(i), numbers);
			return numbers;
		}

		/**
		 * Filter non-primes
		 */
		private ATermList filterNonPrimes(ATermList numbers) 
		{
			ATermList primes = factory.makeList();
			numbers = numbers.getNext();
			while(!numbers.isEmpty()) 
			{
				ATermInt prime = (ATermInt)numbers.getFirst();
				numbers = filterMultiples(prime.getInt(), numbers);
				primes = primes.append(prime);
			}
			return factory.makeList(factory.makeInt(1), primes);
		}

		/**
		 * Filter multiples of n.
		 */
		private ATermList filterMultiples(int n, ATermList numbers) 
		{
			int nr, len = numbers.getLength();

			for(int i=0; i<len; i++) 
			{
				ATermInt el = (ATermInt)numbers.elementAt(i);
				nr = el.getInt();
				if(nr % n == 0) 
				{
					len--;
					numbers = numbers.removeElementAt(i);
				}
			}
			return numbers;
		}
	}
}

