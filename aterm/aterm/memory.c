
/**
  * memory.c: Memory allocation of ATerms.
  */

/*{{{  includes */

#include "aterm2.h"
#include "debug.h"
#include <malloc.h>

/*}}}  */
/*{{{  defines */

#define NR_SIZES 16
#define BLOCK_SIZE (1<<16)
#define GC_THRESHOLD BLOCK_SIZE/4

#define MAX_BLOCKS_PER_SIZE 1024
#define CLASS_SIZE(s) ((s) + 2)
#define SIZE_CLASS(s) ((s) - 2)

/*}}}  */
/*{{{  globals */

static unsigned char *freemap[NR_SIZES];
static unsigned char *sweeper[NR_SIZES];
static unsigned char *endsweep[NR_SIZES];
static int allocated_since_gc[NR_SIZES];

static ATerm *blocks[NR_SIZES][MAX_BLOCKS_PER_SIZE];
static int nrblocks[NR_SIZES];

/*}}}  */

/*{{{  AT_initMemory(int argc, char *argv[]) */

/**
  * Initialize memory allocation datastructures
  */

void AT_initMemory(int argc, char *argv[])
{
  int i;

  for(i=0; i<NR_SIZES; i++) {
    nrblocks[i] = 0;
    allocated_since_gc[i] = 0;
    freemap[i] = malloc(1);
    freemap[i][0] = 0x00;
    sweeper[i] = freemap[i];
    endsweep[i] = freemap[i];
  }
}

/*}}}  */
/*{{{  static void allocate_block(int size_class) */

/**
  * Allocate a new block of a particular size class
  */

static void allocate_block(int size_class)
{
  int entries, nr = nrblocks[size_class]++;
  unsigned char *ptr;

  /* Calculate the number of entries in the new block */
  entries = BLOCK_SIZE/CLASS_SIZE(size_class);

  blocks[size_class][nr] = (ATerm *)malloc(BLOCK_SIZE*4);
  DBG_MEM(printf("Allocated new block at %p. "
		 "This is block nr %d in size class %d.\n", 
		 blocks[size_class][nr], nr, size_class));

  /* Don't forget to allocate sentinel at the end of freemap */
  freemap[size_class] = (unsigned char *)malloc(((nr+1)*BLOCK_SIZE/8)+1);
 
  /* Start sweeping at the start of 'freemap' */
  sweeper[size_class] = &freemap[size_class][nr*entries/8];
  endsweep[size_class] = &freemap[size_class][(nr+1)*entries/8];

  /* Mark new entries as 'free' 
     <PO>: This loop should be replaced by memset or equivalent */
  for(ptr = sweeper[size_class]; ptr <= endsweep[size_class]; ptr++)
    *ptr++ = 0x00;
}

/*}}}  */
/*{{{  void AT_collect() */

/**
  * Collect all garbage
  */

void AT_collect()
{
  fprintf(stderr, "collection not implemented yet!");
}

/*}}}  */
/*{{{  ATerm *AT_allocate(int size) */

/**
  * Allocate a node of a particular size
  */

ATerm *AT_allocate(int size)
{
  int idx, size_class = SIZE_CLASS(size);
  ATerm *result;

  while(*sweeper[size_class] == 0xFF)
    ++sweeper[size_class];
  
  if(sweeper[size_class] >= endsweep[size_class]) {
    DBG_MEM(printf("No free nodes left.\n"));
    /* Allocate new block, or garbage collect */
    if(allocated_since_gc[size_class] < GC_THRESHOLD)
      allocate_block(size_class);
    else
      AT_collect();
  }

  for(idx=0; idx<7; idx++)
    if(!(*sweeper[size_class] & (1<<idx)))
      break;

  allocated_since_gc[size_class]++;
  *sweeper[size_class] |= (1<<idx);

  /* Found the index of the next free entry */
  idx += (sweeper[size_class] - freemap[size_class])*8;
  idx *= size;

  /* Calculate the ATerm address */
  result = &blocks[size_class][idx/BLOCK_SIZE][idx%BLOCK_SIZE];

  return result;
}

/*}}}  */
