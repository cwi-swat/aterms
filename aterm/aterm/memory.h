
/**
  * memory.h: Memory allocation.
  */

#ifndef MEMORY_H
#define MEMORY_H

#include "aterm2.h"

void AT_initMemory(int argc, char *argv[]);
ATerm *AT_allocate(int size);
ATerm *AT_collect();

#endif
