
#include <stdio.h>

#include "_aterm.h"

void termsize(FILE *file)
{
	int core_size, text_size;

	ATerm t = ATreadFromFile(file);
	core_size = AT_calcCoreSize(t);
	text_size = AT_calcTextSize(t);
	printf("internal size: %d bytes, text size: %d bytes\n", 
				 core_size, text_size);
}

int main(int argc, char *argv[])
{
  ATerm bottomOfStack;
	int size;

	ATinit(argc, argv, &bottomOfStack);
	termsize(stdin);

	return 0;
}
