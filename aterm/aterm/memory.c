
/**
  * memory.c: Memory allocation of ATerms.
  */

/*{{{  includes */

#include "aterm2.h"
#include "debug.h"
#include <malloc.h>

/*}}}  */
/*{{{  defines */

#define MAX_SIZE 16
#define BLOCK_SIZE (1<<16)
#define GC_THRESHOLD BLOCK_SIZE/4

#define MAX_BLOCKS_PER_SIZE 1024

/*}}}  */
/*{{{  globals */

static ATerm *blocks[MAX_SIZE][MAX_BLOCKS_PER_SIZE];
static int nrblocks[MAX_SIZE];
static ATerm *freelist[MAX_SIZE];

/*}}}  */

/*{{{  AT_initMemory(int argc, char *argv[]) */

/**
  * Initialize memory allocation datastructures
  */

void AT_initMemory(int argc, char *argv[])
{
  int i;

  for(i=0; i<MAX_SIZE; i++) {
    nrblocks[i] = 0;
		freelist[i] = NULL;
  }
}

/*}}}  */
/*{{{  static void allocate_block(int size_class) */

/**
  * Allocate a new block of a particular size class
  */

static void allocate_block(int size)
{
	int idx, last;
	int block_nr = nrblocks[size];
	blocks[size][block_nr] = (ATerm *) malloc(BLOCK_SIZE * sizeof(header_type));

	if (blocks[size][block_nr] == NULL)
		ATerror("allocate_block: out of memory!\n");

	last = BLOCK_SIZE - size * sizeof(header_type);
	freelist[size] = blocks[size][block_nr];
	for (idx=0; idx < last; idx += size)
	{
		((ATerm *)(((header_type *)blocks[size][block_nr])+idx))->next =
			(ATerm *)(((header_type *)blocks[size][block_nr])+idx+size);
	}
	((ATerm *)(((header_type *)blocks[size][block_nr])+idx))->next = NULL;
}

/*}}}  */
/*{{{  void AT_collect() */

/**
  * Collect all garbage
  */

void AT_collect(int size)
{
  fprintf(stderr, "collection not implemented yet, "
	                "allocating new block of size %d\n", size);

	allocate_block(size);
}

/*}}}  */
/*{{{  ATerm *AT_allocate(int size) */

/**
  * Allocate a node of a particular size
  */

ATerm *AT_allocate(int size)
{
	ATerm *at;

	if (!freelist[size])
		AT_collect(size);

	at = freelist[size];
	freelist[size] = freelist[size]->next;
	return at;
}

/*}}}  */

/*{{{  ATerm *todo(int size) */
/*}}}  */
