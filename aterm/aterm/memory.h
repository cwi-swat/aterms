
/**
  * memory.h: Memory allocation.
  */

#ifndef MEMORY_H
#define MEMORY_H

#include "aterm2.h"

#define MAX_ARITY           256

void AT_initMemory(int argc, char *argv[]);
ATerm AT_allocate(int size);
void  AT_collect(int size);
ATermList AT_getAnnotations(ATerm t);
ATerm AT_setAnnotations(ATerm t, ATermList annos);
ATerm AT_removeAnnotations(ATerm t);

#endif
