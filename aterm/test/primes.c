
#include <stdio.h>
#include <assert.h>
#include <stdlib.h>

#include "aterm2.h"

char primes_id[] = "$Id";

ATermList generate_numbers(int max)
{
	int i;
	ATermList numbers = ATmakeList0();

	for(i=max; i>0; i--)
		numbers = ATinsert(numbers, (ATerm)ATmakeInt(i));

	return numbers;
}

/**
	* Iterate through 'numbers', and remove multiples of n.
	*/

ATermList filter_multiples(int n, ATermList numbers)
{
	int i, nr, len = ATgetLength(numbers);
	ATerm el;

	for(i=0; i<len; i++) {
		el = ATelementAt(numbers, i);
		nr = ATgetInt((ATermInt)el);
		if(nr % n == 0) {
			len--;
			numbers = ATremoveElementAt(numbers, i);
		}
	}

	return numbers;
}

ATermList filter_non_primes(ATermList numbers)
{
	ATermList primes = ATmakeList0();

	/* Skip 1, we dont want to filter that! */
	numbers = ATgetNext(numbers);

	while(!ATisEmpty(numbers)) {
    /* The first number must be prime. remove it from numbers. */
		ATerm prime = ATgetFirst(numbers);
    /* Remove all multiples of n, because they cannot be prime! */
		numbers = filter_multiples(ATgetInt((ATermInt)prime), numbers);
		/* Now add n to the list of primes */
		primes = ATappend(primes, prime);
	}

	return (ATermList)ATmake("[1,<list>]", primes);
}

int main(int argc, char *argv[])
{
	ATermList numbers, primes;
	ATerm bottomOfStack;
	int i, max = 100;

	for(i=1; i<argc; i++)
		if(strcmp(argv[i], "-max") == 0)
			max = atoi(argv[++i]);

	ATinit(argc, argv, NULL, &bottomOfStack);
	numbers = generate_numbers(max);
	primes  = filter_non_primes(numbers);

	ATprintf("primes up to %d: %, l\n", max, primes);

	return 0;
}
