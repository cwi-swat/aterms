
/**
  * memory.h: Memory allocation.
  */

#ifndef MEMORY_H
#define MEMORY_H

#include "aterm2.h"

#define MAX_ARITY            256
#define MAX_TERM_SIZE         32
#define MAX_BLOCKS_PER_SIZE 1024

void AT_initMemory(int argc, char *argv[]);
ATerm AT_allocate(int size);
void  AT_collect(int size);
ATerm AT_getAnnotations(ATerm t);
ATerm AT_setAnnotations(ATerm t, ATerm annos);
ATerm AT_removeAnnotations(ATerm t);

extern ATerm at_blocks[MAX_TERM_SIZE][MAX_BLOCKS_PER_SIZE];
extern int at_nrblocks[MAX_TERM_SIZE];
extern ATerm at_freelist[MAX_TERM_SIZE];

#endif
