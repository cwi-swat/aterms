
/**
  * memory.h: Memory allocation.
  */

#ifndef MEMORY_H
#define MEMORY_H

#include "aterm2.h"

void T_initMemory(int argc, char *argv[]);
ATerm *T_allocate(int size);
ATerm *T_collect();

#endif
