
/*{{{  includes */

#include <unistd.h>
#include <stdlib.h>
#include <values.h>
#include <sys/times.h>
#include <limits.h>
#include <assert.h>
#include <setjmp.h>

#include "_aterm.h"
#include "asymbol.h"
#include "memory.h"
#include "util.h"
#include "gc.h"
#include "debug.h"

/*}}}  */
/*{{{  globals */

char gc_id[] = "$Id$";

static ATerm *stackBot = NULL;

#define PRINT_GC_TIME           1
#define PRINT_GC_STATS          2

static int     flags               = 0;
static int     gc_count            = 0;
static clock_t sweep_time[3]       = { 0, MYMAXINT, 0 };
static clock_t mark_time[3]        = { 0, MYMAXINT, 0 };
static int     stack_depth[3]      = { 0, MYMAXINT, 0 };
static int     stack_terms[3]      = { 0, MYMAXINT, 0 };
static int     stack_symbols[3]    = { 0, MYMAXINT, 0 };
static int     register_terms[3]   = { 0, MYMAXINT, 0 };
static int     register_symbols[3] = { 0, MYMAXINT, 0 };
static int     reclaim_perc[3]     = { 0, MYMAXINT, 0 };
extern int     mark_stats[3];
extern int     nr_marks;

/*}}}  */

/*{{{  void AT_initGC(int argc, char *argv[], ATerm *bottomOfStack) */

/**
  * Initialize the garbage collector.
  */

void AT_initGC(int argc, char *argv[], ATerm *bottomOfStack)
{
	int i;

  stackBot = bottomOfStack;

	for(i=1; i<argc; i++) {
		if(streq(argv[i], "-print-gc-time"))
			flags |= PRINT_GC_TIME;
		else if(streq(argv[i], "-print-gc-info"))
			flags |= (PRINT_GC_TIME | PRINT_GC_STATS);
		else if(strcmp(argv[i], "-help") == 0) {
			fprintf(stderr, "    %-20s: print non-intrusive gc information "
							"after execution\n", "-print-gc-time");
			fprintf(stderr, "    %-20s: print elaborate gc information "
							"after execution\n", "-print-gc-info");
		}
	}
}

/*}}}  */

/*{{{  ATerm *stack_top() */

/**
	* Find the top of the stack.
	*/

ATerm *stack_top()
{
    ATerm topOfStack;
		ATerm *top = &topOfStack;

    return top;
}

/*}}}  */
 
/*{{{  void mark_phase() */

/**
  * Mark all terms reachable from the root set.
  */

void mark_phase()
{
  int i;
	int stack_size;
	int nr_stack_terms, nr_stack_syms;
	int nr_reg_terms, nr_reg_syms;

  ATerm *stackTop;
  ATerm *start, *stop, *cur;
  sigjmp_buf env;

	/* Traverse possible register variables */
	sigsetjmp(env,0);

	start = (ATerm *)env;
	stop  = ((ATerm *)(((char *)env) + sizeof(sigjmp_buf)));

	nr_stack_terms = 0;
	nr_stack_syms  = 0;
	nr_reg_terms   = 0;
	nr_reg_syms    = 0;

	for(cur=start; cur<stop; cur++) {
		if (AT_isValidTerm(*cur)) {
			AT_markTerm(*cur);
			nr_reg_terms++;
		}
		if (AT_isValidSymbol((Symbol)*cur)) {
			AT_markSymbol((Symbol)*cur);
			nr_reg_syms++;
		}
	}

	STATS(register_terms, nr_reg_terms);
	STATS(register_symbols, nr_reg_syms);

	stackTop = stack_top();

  /* Determine stack orientation */
  start = MIN(stackTop, stackBot);
  stop  = MAX(stackTop, stackBot);

	stack_size = stop-start;
	STATS(stack_depth, stack_size);

  /* Traverse the stack */
  for(cur=start; cur<stop; cur++) {
		if (AT_isValidTerm(*cur)) {
			AT_markTerm(*cur);
			nr_stack_terms++;
		}

		if (AT_isValidSymbol((Symbol)*cur)) {
			AT_markSymbol((Symbol)*cur);
			nr_stack_syms++;
		}
  }

	STATS(stack_terms, nr_stack_terms);
	STATS(stack_symbols, nr_stack_syms);

  /* Traverse protected terms */
  for(i=0; i<at_nrprotected; i++)
		if(*at_protected[i])
			AT_markTerm(*at_protected[i]);

	/* Traverse protected arrays */
	for(i=0; i<at_nrprotected_arrays; i++) {
		ATerm *cur = at_protected_arrays[i].start;
		ATerm *end = cur + at_protected_arrays[i].size;
		while(cur < end) {
			if(*cur)
				AT_markTerm(*cur);
			cur++;
		}
	}

	/* Mark protected symbols */
	AT_markProtectedSymbols();
}

/*}}}  */
/*{{{  void sweep_phase() */

/**
  * Sweep all unmarked terms into the appropriate free lists.
  */

void sweep_phase()
{
  int size, idx;
	int total = 0;
	int reclaiming = 0, perc;

  for(size=MIN_TERM_SIZE; size<MAX_TERM_SIZE; size++) {
		Block *block = at_blocks[size];
		int end = BLOCK_SIZE - (BLOCK_SIZE % size);

		while(block) {
			assert(block->size == size);
			for(idx=0; idx<end; idx+=size) {
				ATerm t = (ATerm)&(block->data[idx]);
				if(IS_MARKED(t->header))
					CLR_MARK(t->header);
				else {
					switch(ATgetType(t)) {
						case AT_FREE:
							break;
						case AT_INT:
						case AT_REAL:
						case AT_APPL:
						case AT_LIST:
						case AT_PLACEHOLDER:
						case AT_BLOB:
							AT_freeTerm(size, t);
							reclaiming++;
							break;
						case AT_SYMBOL:
							AT_freeSymbol((SymEntry)t);
							break;
							
						default:
							abort();
					}
				}
			}
			block = block->next_by_size;
		}
		total += at_nrblocks[size]*(BLOCK_SIZE/size);
  }
  perc = (100*reclaiming)/total;
	STATS(reclaim_perc, perc);
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

	gc_count++;
	if (!silent)
	{
		fprintf(stderr, "collecting garbage..");
		fflush(stderr);
	}
  times(&start);
  mark_phase();
  times(&mark);
  user = mark.tms_utime - start.tms_utime;
	STATS(mark_time, user);
  sweep_phase();
  times(&sweep);
  user = sweep.tms_utime - mark.tms_utime;
	STATS(sweep_time, user);

	if (!silent)
		fprintf(stderr, "..\n");
}

/*}}}  */

/*{{{  void AT_cleanupGC() */

/**
	* Print garbage collection information
	*/

void AT_cleanupGC()
{
	if(flags & PRINT_GC_TIME) {
		fprintf(stderr, "%d garbage collects,\n", gc_count);
		fprintf(stderr, "(all statistics are printed min/avg/max)\n");
		if(gc_count > 0) {
			if(nr_marks > 0) {
				fprintf(stderr, "  mark stack needed: %d/%d/%d (%d marks)\n", 
								mark_stats[IDX_MIN], mark_stats[IDX_TOTAL]/nr_marks, 
								mark_stats[IDX_MAX], nr_marks);
			}
			fprintf(stderr, "  marking  took %.2f/%.2f/%.2f seconds, total: %.2f\n", 
							((double)mark_time[IDX_MIN])/(double)CLK_TCK,
							(((double)mark_time[IDX_TOTAL])/(double)gc_count)/(double)CLK_TCK,
							((double)mark_time[IDX_MAX])/(double)CLK_TCK,
							((double)mark_time[IDX_TOTAL])/(double)CLK_TCK);
			fprintf(stderr, "  sweeping took %.2f/%.2f/%.2f seconds, total: %.2f\n", 
							((double)sweep_time[IDX_MIN])/(double)CLK_TCK,
							(((double)sweep_time[IDX_TOTAL])/(double)gc_count)/(double)CLK_TCK,
							((double)sweep_time[IDX_MAX])/(double)CLK_TCK,
							((double)sweep_time[IDX_TOTAL])/(double)CLK_TCK);
		}
	}

	if(flags & PRINT_GC_STATS) {
		if(gc_count > 0) {
			fprintf(stderr, "\n  stack depth: %d/%d/%d words\n", 
							stack_depth[IDX_MIN],  
							stack_depth[IDX_TOTAL]/gc_count,
							stack_depth[IDX_MAX]);
			fprintf(stderr, "  term roots on stack: %d/%d/%d\n", 
							stack_terms[IDX_MIN], 
							stack_terms[IDX_TOTAL]/gc_count,
							stack_terms[IDX_MAX]);
			fprintf(stderr, "  symbol roots on stack: %d/%d/%d\n", 
							stack_symbols[IDX_MIN], 
							stack_symbols[IDX_TOTAL]/gc_count,
							stack_symbols[IDX_MAX]);
			fprintf(stderr, "  term roots in registers: %d/%d/%d\n", 
							register_terms[IDX_MIN], 
							register_terms[IDX_TOTAL]/gc_count,
							register_terms[IDX_MAX]);
			fprintf(stderr, "  symbol roots in registers: %d/%d/%d\n", 
							register_symbols[IDX_MIN], 
							register_symbols[IDX_TOTAL]/gc_count,
							register_symbols[IDX_MAX]);
			fprintf(stderr, "\n  reclamation percentage: %d/%d/%d\n",
							reclaim_perc[IDX_MIN],
							reclaim_perc[IDX_TOTAL]/gc_count,
							reclaim_perc[IDX_MAX]);
		}
	}
}

/*}}}  */


