
#include <stdio.h>

#include "aterm2.h"

int calc_size(FILE *file)
{
	ATerm t = ATreadFromTextFile(file);
	return ATinternalSize(t);
}

int main(int argc, char *argv[])
{
  ATerm bottomOfStack;
	int size;

	ATinit(argc, argv, &bottomOfStack);
	size = calc_size(stdin);
	printf("size: %d bytes\n", size);

	return 0;
}
