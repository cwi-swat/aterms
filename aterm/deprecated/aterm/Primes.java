/*

    ATerm -- The ATerm (Annotated Term) library
    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
                             The  Netherlands.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/
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
