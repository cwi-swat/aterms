
/*{{{  includes */

#include <sys/times.h>
#include <limits.h>

#include "_aterm.h"
#include "symbol.h"
#include "memory.h"
#include "util.h"
#include "gc.h"

/*}}}  */
/*{{{  externals */

extern ATerm **at_protected;
extern int at_nrprotected;

/*}}}  */
/*{{{  globals */

char gc_id[] = "$Id$";

static ATerm *stackBot = NULL;

/*}}}  */

/*{{{  void AT_initGC(int argc, char *argv[], ATerm *bottomOfStack) */

/**
  * Initialize the garbage collector.
  */

void AT_initGC(int argc, char *argv[], ATerm *bottomOfStack)
{
  stackBot = bottomOfStack;
}

/*}}}  */

/*{{{  void mark_phase() */

/**
  * Mark all terms reachable from the root set.
  */

void mark_phase()
{
  int i;
  ATerm topOfStack;
  ATerm *stackTop = &topOfStack;
  ATerm *start, *stop, *cur;

  /* Determine stack orientation */
  start = MIN(stackTop, stackBot);
  stop  = MAX(stackTop, stackBot);

  /* Traverse protected terms */
  for(i=0; i<at_nrprotected; i++)
		if(*at_protected[i])
			AT_markTerm(*at_protected[i]);

  /* Traverse the stack */
  for(cur=start; cur<stop; cur++) {
	if (AT_isValidTerm(*cur))
	  AT_markTerm(*cur);
	if (AT_isValidSymbol((Symbol)*cur))
	  AT_markSymbol((Symbol)*cur);
  }
}

/*}}}  */
/*{{{  void sweep_phase() */

/**
  * Sweep all unmarked terms into the appropriate free lists.
  */

void sweep_phase()
{
  int i;

  for(i=MIN_TERM_SIZE; i<MAX_TERM_SIZE; i++) {
	
  }
}

/*}}}  */

/*{{{  void AT_collect() */

/**
  * Collect all garbage
  */

void AT_collect(int size)
{
  struct tms start, mark, sweep;
  clock_t user;

  fprintf(stderr, "collecting garbage...");
  times(&start);
  mark_phase();
  times(&mark);
  user = mark.tms_utime - start.tms_utime;
  fprintf(stderr, "marking took %f seconds\n", 
		  ((double)user)/(double)CLK_TCK);
  sweep_phase();
  times(&sweep);
  user = sweep.tms_utime - mark.tms_utime;
  fprintf(stderr, "sweeping took %f seconds\n",
		  ((double)user)/(double)CLK_TCK);
}

/*}}}  */
