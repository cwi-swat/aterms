
#include <stdio.h>

#include "_aterm.h"

void termsize(FILE *file)
{
	int core_size, text_size, term_depth;

	ATerm t = ATreadFromFile(file);
	core_size = AT_calcCoreSize(t);
	text_size = AT_calcTextSize(t);
	term_depth = AT_calcTermDepth(t);
	printf("internal size: %d bytes, text size: %d bytes, depth: %d\n", 
				 core_size, text_size, term_depth);
}

int main(int argc, char *argv[])
{
  ATerm bottomOfStack;
	ATinit(argc, argv, &bottomOfStack);
	termsize(stdin);

	return 0;
}
