
#ifndef GC_H
#define GC_H

#include "abool.h"
#include "aterm2.h"

void AT_initGC(int argc, char *argv[], ATerm *bottomOfStack);
void AT_cleanupGC();
void AT_collect(int size);

#endif
