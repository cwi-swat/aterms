package aterm;

public class Primes
{
  private int max;
  private ATermList primes;
  private World world;

	//{ public static void main(String[] args)

	/**
		* Read the maximum prime to be generated, and start the generation.
		*/

  public static void main(String[] args)
  {
    int max = 100;
    for(int i=0; i<args.length; i++) {
      if(args[i].startsWith("-max"))
				max = Integer.parseInt(args[++i]);
    }
    Primes primes = new Primes(max);
    primes.calcPrimes();
    System.out.println("Primes: " + primes.getPrimes());
  }

	//}
	//{ public Primes(int max)

	/**
		* Construct a new Primes object.
		*/

  public Primes(int max)
  {
    this.max = max;
    world = new World();
  }

	//}
	//{ public void calcPrimes()

	/**
		* Calculate the list of primes.
		*/

  public void calcPrimes()
  {
    primes = generateNumbers(max);
    primes = filterNonPrimes(primes);
  }

	//}
	//{ public ATermList getPrimes()

	/**
		* Retrieve the list of primes.
		*/

  public ATermList getPrimes()
  {
    return primes;
  }

	//}
	//{ private ATermList generateNumbers(int max)

	/**
		* Generate a list of consecutive numbers.
		*/

  private ATermList generateNumbers(int max)
  {
    ATermList numbers = world.empty;
    for(int i=max; i>0; i--)
      numbers = world.makeList(world.makeInt(i), numbers);
    return numbers;
  }

	//}
	//{ private ATermList filterNonPrimes(ATermList numbers)

	/**
		* Filter non-primes
		*/
 
  private ATermList filterNonPrimes(ATermList numbers)
  {
    ATermList primes = world.empty;
    numbers = numbers.getNext();
    while(!numbers.isEmpty()) {
      ATermInt prime = (ATermInt)numbers.getFirst();
      numbers = filterMultiples(prime.getInt(), numbers);
      primes = primes.append(prime);
    }
    return world.makeList(world.makeInt(1), primes);
  }

	//}
	//{ private ATermList filterMultiples(int n, ATermList numbers)

	/**
		* Filter multiples of n.
		*/

  private ATermList filterMultiples(int n, ATermList numbers)
  {
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

	//}
}
