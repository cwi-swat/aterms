
/**
  * memory.c: Memory allocation of ATerms.
  */

/*{{{  includes */

#include <ctype.h>
#include <stdlib.h>
#include <stdarg.h>
#include <assert.h>
#include "aterm2.h"
#include "memory.h"
#include "util.h"
#include "debug.h"

/*}}}  */
/*{{{  defines */

/* when less than GC_THRESHOLD percent of the terms has been freed by
   the previous garbage collect, a new block will be allocated.
   Otherwise a new garbage collect is started. */
#define GC_THRESHOLD 45 

#define MAX_DESTRUCTORS     16
#define MAX_BLOCKS_PER_SIZE 1024

#define TERM_HASH_OPT       "-termtable"
#define HASH_INFO_OPT       "-hashinfo"

#define CHECK_ARITY(ari1,ari2) DBG_ARITY(assert((ari1) == (ari2)))

#define EMPTY_HASH_NR 12347

#define INFO_HASHING    1
#define MAX_INFO_SIZES 256

#define UI(i)          ((unsigned int)(i))
#define IDX(w)         UI(((w>>2) ^ (w>>10)) & 0xFF)
#define SHIFT(w)       UI((w>>3) & 0xF)
#define NOISE(w)       (CRC_TABLE[IDX(w)])
#define CSTEP(crc,c)   ((((crc)>>8) & 0x00FFFFFFL) ^ \
												CRC_TABLE[((int)crc^(c))&0xFF])

/*#define CNOISE(w)      CSTEP(CSTEP(CSTEP(CSTEP(0xFFFFFFFFL, \
												 (w)), (w)>>8), (w)>>16), (w)>>24)*/
/*#define CNOISE(w)      CSTEP(CSTEP(0xFFFFFFFFL, \
												 (((w)>>8) ^ (w))), (w)>>16)
#define CNOISE(w)        CSTEP(0xFFFFFFFFL, ((w)>>24)^((w)>>16)^((w)>>8)^w) 
*/
#define CNOISE(w)        CSTEP(0xFFFFFFFFL, ((w)>>24)^((w)>>16)^((w)>>8)^w) 

/* Best sofar with noise:
#define S(w)           (UI(w))
#define C(hnr,w)       ((UI(hnr)>>1) ^ (UI(w)))
#define F(hnr)         ((UI(hnr) ^ (CNOISE(hnr))) & table_mask)
*/

/* Best sofar without noise */
/*
#define S(w)           (UI(w) ^ (UI(w)>>8))
#define C(hnr,w)       ((UI(hnr)>>1) ^ (UI(w)))
#define F(hnr)         (UI(hnr) & table_mask)
*/

#define S(w)           ((UI(w)>>8))
#define C(hnr,w)       ((UI(hnr)>>1) ^ (UI(w)))
#define F(hnr)         (UI(hnr) & table_mask)


/*
#define HNUM1(h)                   F(S(h))
#define HNUM2(h,w0)                F(C(S(h),w0))
#define HNUM3(h,w0,w1)             F(C(C(S(h),w0),w1))
#define HNUM4(h,w0,w1,w2)          F(C(C(C(S(h),w0),w1),w2))
#define HNUM5(h,w0,w1,w2,w3)       F(C(C(C(C(S(h),w0),w1),w2),w3))
#define HNUM6(h,w0,w1,w2,w3,w4)    F(C(C(C(C(C(S(h),w0),w1),w2),w3),w4))
#define HNUM7(h,w0,w1,w2,w3,w4,w5) F(C(C(C(C(C(C(S(h),w0),w1),w2),w3),w4),w5))
*/
#define HNUM1(h)                   F(S(h))
#define HNUM2(h,w0)                F(C(w0,S(h)))
#define HNUM3(h,w0,w1)             F(C(C(w1,w0),S(h)))
#define HNUM4(h,w0,w1,w2)          F(C(C(C(w2,w1),w0),S(h)))
#define HNUM5(h,w0,w1,w2,w3)       F(C(C(C(C(w3,w2),w1),w0),S(h)))
#define HNUM6(h,w0,w1,w2,w3,w4)    F(C(C(C(C(C(w4,w3),w2),w1),w0),S(h)))
#define HNUM7(h,w0,w1,w2,w3,w4,w5) F(C(C(C(C(C(C(w5,w4),w3),w2),w1),w0),S(h)))

/*}}}  */
/*{{{  types */


/*}}}  */
/*{{{  globals */

char memory_id[] = "$Id$";

Block *at_blocks[MAX_TERM_SIZE]  = { NULL };
int at_nrblocks[MAX_TERM_SIZE]   = { 0 };
ATerm at_freelist[MAX_TERM_SIZE] = { NULL };
BlockBucket block_table[BLOCK_TABLE_SIZE] = { { NULL, NULL } };

static int alloc_since_gc[MAX_TERM_SIZE] = { 0 };
static unsigned int table_class = 14;
static unsigned int table_size;
static unsigned int table_mask;
static ATerm *hashtable;

static int destructor_count = 0;
static ATbool (*destructors[MAX_DESTRUCTORS])(ATermBlob) = { NULL };

static ATerm arg_buffer[MAX_ARITY];

ATermList ATempty;

static int infoflags = 0;

static int gc_count = 0;
static int hash_info_before_gc[MAX_INFO_SIZES][3];
static int hash_info_after_gc[MAX_INFO_SIZES][3];

/*}}}  */

/*{{{  static unsigned hash_number(unsigned int header, int n, ATerm w[]) */

static unsigned int hash_number(unsigned int header, int n, ATerm w[])
{
  int i;
/*  unsigned int hnr = S(header);

  for(i=0; i<n; i++)
		hnr = C(hnr, w[i]);
*/
	unsigned int hnr;
	if(n>0) {
		hnr = UI(w[n-1]);
		for(i=n-2; i>=0; i--)
			hnr = C(hnr, w[i]);

		hnr = C(hnr, S(header));
	} else
		hnr = S(header);

  return F(hnr);
}

/*}}}  */
/*{{{  static unsigned hash_number_anno(unsigned int header, int n, w[], anno) */

static unsigned int hash_number_anno(unsigned int header, int n, ATerm w[], ATerm anno)
{
/*  int i;
  unsigned int hnr = S(header);

  for(i=0; i<n; i++)
    hnr = C(hnr, w[i]);
  hnr = C(hnr, anno);

  return F(hnr);
*/
	int i;

	unsigned int hnr = UI(anno);
	for(i=n-1; i>=0; i--)
		hnr = C(hnr, w[i]);

	hnr = C(hnr, S(header));

  return F(hnr);
}

/*}}}  */
/*{{{  static void hash_info(int stats[3][]) */

static void hash_info(int stats[MAX_INFO_SIZES][3]) 
{
	int i, len;
	static int count[MAX_INFO_SIZES];

	/* Initialize statistics */
	for(i=0; i<MAX_INFO_SIZES; i++)
		count[i] = 0;

	/* Gather statistics on the current fill of the hashtable */
	for(i=0; i<table_size; i++) {
		ATerm cur = hashtable[i];
		len = 0;
		while(cur) {
			len++;
			cur = cur->next; 
		}
		if(len >= MAX_INFO_SIZES)
			len = MAX_INFO_SIZES-1;
		count[len]++;
	}

	/* Update global statistic information */
	for(i=0; i<MAX_INFO_SIZES; i++) {
		STATS(stats[i], count[i]);
	}
}

/*}}}  */

/*{{{  void AT_initMemory(int argc, char *argv[]) */

/**
  * Initialize memory allocation datastructures
  */

void AT_initMemory(int argc, char *argv[])
{
  int i;

	table_class = 16;

	/*{{{  Analyze arguments */

  for (i = 1; i < argc; i++) {
    if (streq(argv[i], TERM_HASH_OPT))
      table_class = atoi(argv[++i]);
		if(streq(argv[i], HASH_INFO_OPT))
			infoflags |= INFO_HASHING;
	}

	/*}}}  */

	table_size  = 1<<table_class;
	table_mask  = table_size-1;

	/*{{{  Initialize blocks */

  for(i=0; i<MAX_TERM_SIZE; i++) {
    at_nrblocks[i] = 0;
    at_freelist[i] = NULL;
  }

	/*}}}  */
	/*{{{  Create term term table */

  hashtable = (ATerm *)calloc(table_size, sizeof(ATerm ));
  if(!hashtable) {
    ATerror("AT_initMemory: cannot allocate term table of size %d\n", 
	    table_size);
  }

  for(i=0; i<BLOCK_TABLE_SIZE; i++) {
		block_table[i].first_before = NULL;
		block_table[i].first_after  = NULL;
  }

	/*}}}  */
	/*{{{  Create the empty list */

  ATempty = (ATermList)AT_allocate(TERM_SIZE_LIST);
  ATempty->header = EMPTY_HEADER(0);
  ATempty->next = NULL;
  hashtable[EMPTY_HASH_NR % table_size] = (ATerm)ATempty;
	ATprotect((ATerm *)&ATempty);

	/*}}}  */
	/*{{{  Initialize info structures */

	for(i=0; i<MAX_INFO_SIZES; i++) {
		hash_info_before_gc[i][IDX_TOTAL] = 0;
		hash_info_before_gc[i][IDX_MIN] = MYMAXINT;
		hash_info_before_gc[i][IDX_MAX] = 0;
		hash_info_after_gc[i][IDX_TOTAL] = 0;
		hash_info_after_gc[i][IDX_MIN] = MYMAXINT;
		hash_info_after_gc[i][IDX_MAX] = 0;
	}

	/*}}}  */

	ATprotectArray(arg_buffer, MAX_ARITY);

  DBG_MEM(fprintf(stderr, "initial term table size = %d\n", table_size));
}

/*}}}  */
/*{{{  void AT_cleanupMemory() */

/**
	* Print hashtable info
	*/

void AT_cleanupMemory()
{
	int i, info[MAX_INFO_SIZES][3];

	if(infoflags & INFO_HASHING) {
		int max = MAX_INFO_SIZES-1;
		FILE *f = fopen("hashing.stats", "w");

		if(!f)
			ATerror("cannot open hashing statisics file: \"hashing.stats\"\n");

		while(hash_info_before_gc[max][IDX_MAX] == 0)
			max--;

		if(gc_count > 0) {
      fprintf(f, "hash statistics before and after garbage collection:\n");
			for(i=0; i<=max; i++) {
				fprintf(f, "%8d %8d %8d   %8d %8d %8d\n", 
								hash_info_before_gc[i][IDX_MIN],
								hash_info_before_gc[i][IDX_TOTAL]/gc_count,
								hash_info_before_gc[i][IDX_MAX],
								hash_info_after_gc[i][IDX_MIN],
								hash_info_after_gc[i][IDX_TOTAL]/gc_count,
								hash_info_after_gc[i][IDX_MAX]);
			}
		}

		for(i=0; i<MAX_INFO_SIZES; i++) {
			info[i][IDX_MIN] = MYMAXINT;
			info[i][IDX_TOTAL] = 0;
			info[i][IDX_MAX] = 0;
		}
		hash_info(info);
		fprintf(f, "hash statistics at end of program:\n");
		max = MAX_INFO_SIZES-1;
		while(info[max][IDX_MAX] == 0)
			max--;
	  for(i=0; i<=max; i++) {
			fprintf(f, "%8d\n", info[i][IDX_TOTAL]);
		}

		for(i=0; i<table_size; i++) {
			int size = 0;
			ATerm cur = hashtable[i];
			for(size=0; cur; size++)
				cur = cur->next;
			if(size > 20) {
				fprintf(f, "bucket %d has length %d\n", i, size);
				cur = hashtable[i];
				while(cur) {
					ATfprintf(f, "%t\n", cur);
					cur = cur->next;
				}
			}
		}
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
	Block *newblock = (Block *)calloc(1, sizeof(Block));
	ATerm data;

	if (newblock == NULL)
		ATerror("allocate_block: out of memory!\n");
	at_nrblocks[size]++;

	newblock->size = size;
	newblock->next_by_size = at_blocks[size];
	at_blocks[size] = newblock;
	data = (ATerm)newblock->data;

	/* subtract garbage, and 1xsize */
	last = BLOCK_SIZE - (BLOCK_SIZE % size) - size;
	at_freelist[size] = data;
	for (idx=0; idx < last; idx += size)
	{
		((ATerm)(((header_type *)data)+idx))->next =
			(ATerm)(((header_type *)data)+idx+size);
		((ATerm)(((header_type *)data)+idx))->header = AT_FREE;
	}
	((ATerm)(((header_type *)data)+idx))->next = NULL;
	/*fprintf(stderr, "last of block = %p\n", (ATerm)(((header_type *)data)+idx));*/

	/* Place the new block in the block_table */
	idx = (((unsigned int)newblock) >> (BLOCK_SHIFT+2)) % BLOCK_TABLE_SIZE;
	newblock->next_after = block_table[idx].first_after;
	block_table[idx].first_after = newblock;
	idx = (idx+1) % BLOCK_TABLE_SIZE;
	newblock->next_before = block_table[idx].first_before;
	block_table[idx].first_before = newblock;
}

/*}}}  */
/*{{{  ATerm AT_allocate(int size) */

/**
  * Allocate a node of a particular size
  */

ATerm AT_allocate(int size)
{
	int i;
	ATerm at;

	while (!at_freelist[size])
	{
		int total = at_nrblocks[size]*(BLOCK_SIZE/size);
		/*printf("alloc_since_gc[%d] = %d, total=%d\n", size,
					 alloc_since_gc[size], total);*/
		if((100*alloc_since_gc[size]) <= GC_THRESHOLD*total) {
				allocate_block(size);
		} else {
			gc_count++;
			if(infoflags & INFO_HASHING)
				hash_info(hash_info_before_gc);
			AT_collect(size);	
			if(infoflags & INFO_HASHING)
				hash_info(hash_info_after_gc);
			for(i=MIN_TERM_SIZE; i<MAX_TERM_SIZE; i++)
				alloc_since_gc[size] = 0;
		}
	}

	at = at_freelist[size];
	++alloc_since_gc[size];
	at_freelist[size] = at_freelist[size]->next;
	return at;
}

/*}}}  */
/*{{{  void AT_freeTerm(int size, ATerm t) */

/**
	* Free a term of a particular size.
	*/

void AT_freeTerm(int size, ATerm t)
{
	ATbool found = ATfalse;
	int idx, nrargs = size-ARG_OFFSET;

	/* Remove the node from the hashtable */
	unsigned int hnr = hash_number(t->header, nrargs, (ATerm *)(t+1));
	ATerm prev = NULL, cur = hashtable[hnr];

	while(1) {
		if(t->header == cur->header) {
			found = ATtrue;
			for(idx=0; idx<nrargs; idx++) {
				if(ATgetArgument((ATermAppl)t, idx) != 
					 ATgetArgument((ATermAppl)cur, idx)) {
					found = ATfalse;
					break;
				}
			}
		} else {
			found = ATfalse;
		}

		if(found)
			break;

		prev = cur;
		cur = cur->next;
	}

	if(prev)
		prev->next = cur->next;
	else
		hashtable[hnr] = cur->next;

	/* Put the node in the appropriate free list */
	t->header = FREE_HEADER;
	t->next  = at_freelist[size];
	at_freelist[size] = t;
}

/*}}}  */

/*{{{  ATermAppl ATmakeAppl(Symbol sym, ...) */

/**
  * Create a new ATermAppl. The argument count can be found in the symbol.
  */

ATermAppl ATmakeAppl(Symbol sym, ...)
{
  int i, arity = ATgetArity(sym);
  ATbool found;
  ATerm cur;
  ATermAppl appl;
  header_type header;

  unsigned int hnr;

  va_list args;

  va_start(args, sym);

  for(i=0; i<arity; i++)
    arg_buffer[i] = va_arg(args, ATerm);
  va_end(args);

  header = APPL_HEADER(0, arity > MAX_INLINE_ARITY ?
		MAX_INLINE_ARITY+1 : arity, sym);

  hnr = hash_number(header, arity, arg_buffer);
  cur = hashtable[hnr];

  while(cur) {
    if(cur->header == header) {
      appl = (ATermAppl)cur;
      found = ATtrue;
      for(i=0; i<arity; i++) {
				if(!ATisEqual(ATgetArgument(appl, i), arg_buffer[i])) {
					found = ATfalse;
					break;
				}
      }
      if(found)
				break;
    }
    cur = cur->next;
  }

  if(!cur) {
    cur = AT_allocate(arity + ARG_OFFSET);
    cur->header = header;
    for(i=0; i<arity; i++) {
      ATgetArgument(cur, i) = arg_buffer[i];
			arg_buffer[i] = NULL;
		}
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }
  
  return (ATermAppl)cur;
}

/*}}}  */
/*{{{  ATermAppl ATmakeAppl0(Symbol sym) */

/**
  * Create an ATermAppl with zero arguments.
  */

ATermAppl ATmakeAppl0(Symbol sym)
{
  ATerm cur;
  header_type header = APPL_HEADER(0, 0, sym);

  unsigned int hnr = HNUM1(header);
  
  CHECK_ARITY(ATgetArity(sym), 0);

  cur = hashtable[hnr];
  while(cur) {
		if(cur->header == header)
			return (ATermAppl)cur;
    cur = cur->next;
	}

	cur = AT_allocate(ARG_OFFSET);
	cur->header = header;
	cur->next = hashtable[hnr];
	hashtable[hnr] = cur;

  return (ATermAppl) cur;  
}

/*}}}  */
/*{{{  ATermAppl ATmakeAppl1(Symbol sym, ATerm arg0) */

/**
  * Create an ATermAppl with one argument.
  */

ATermAppl ATmakeAppl1(Symbol sym, ATerm arg0)
{
  ATerm cur;
  header_type header = APPL_HEADER(0, 1, sym);
  unsigned int hnr = HNUM2(header, arg0);
 
  CHECK_ARITY(ATgetArity(sym), 1);
 
  cur = hashtable[hnr];
  while(cur) {
    if(cur->header == header && ATgetArgument(cur, 0) == arg0)
			return (ATermAppl)cur;
    cur = cur->next;
	}

	cur = AT_allocate(ARG_OFFSET+1);
	cur->header = header;
	ATgetArgument(cur, 0) = arg0;
	cur->next = hashtable[hnr];
	hashtable[hnr] = cur;

  return (ATermAppl) cur;  
}

/*}}}  */
/*{{{  ATermAppl ATmakeAppl2(Symbol sym, arg0, arg1) */

/**
  * Create an ATermAppl with one argument.
  */

ATermAppl ATmakeAppl2(Symbol sym, ATerm arg0, ATerm arg1)
{
  ATerm cur;
  header_type header = APPL_HEADER(0, 2, sym);
  unsigned int hnr = HNUM3(header, arg0, arg1);
  
  CHECK_ARITY(ATgetArity(sym), 2);

  cur = hashtable[hnr];
  while(cur) {
		if(cur->header == header &&	ATgetArgument(cur, 0) == arg0 &&
			 ATgetArgument(cur, 1) == arg1)
			return (ATermAppl)cur;
    cur = cur->next;
	}

	cur = AT_allocate(ARG_OFFSET+2);
	cur->header = header;
	ATgetArgument(cur, 0) = arg0;
	ATgetArgument(cur, 1) = arg1;
	cur->next = hashtable[hnr];
	hashtable[hnr] = cur;

  return (ATermAppl)cur;  
}

/*}}}  */
/*{{{  ATermAppl ATmakeAppl3(Symbol sym, ATerm arg0, arg1, arg2) */

/**
  * Create an ATermAppl with one argument.
  */

ATermAppl ATmakeAppl3(Symbol sym, ATerm arg0, ATerm arg1, ATerm arg2)
{
  ATerm cur;
  header_type header = APPL_HEADER(0, 3, sym);
  unsigned int hnr = HNUM4(header, arg0, arg1, arg2);
  
  CHECK_ARITY(ATgetArity(sym), 3);

  cur = hashtable[hnr];
  while(cur && (cur->header != header ||
		ATgetArgument(cur, 0) != arg0 ||
		ATgetArgument(cur, 1) != arg1 ||
		ATgetArgument(cur, 2) != arg2))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(ARG_OFFSET+3);
    cur->header = header;
    ATgetArgument(cur, 0) = arg0;
    ATgetArgument(cur, 1) = arg1;
    ATgetArgument(cur, 2) = arg2;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

  return (ATermAppl)cur;  
}

/*}}}  */
/*{{{  ATermAppl ATmakeAppl4(Symbol sym, ATerm arg0, arg1, arg2, a3) */

/**
  * Create an ATermAppl with four arguments.
  */

ATermAppl ATmakeAppl4(Symbol sym, ATerm arg0, ATerm arg1, ATerm arg2, ATerm arg3)
{
  ATerm cur;
  header_type header = APPL_HEADER(0, 4, sym);
  unsigned int hnr = HNUM5(header, arg0, arg1, arg2, arg3);
  
  CHECK_ARITY(ATgetArity(sym), 4);

  cur = hashtable[hnr];
  while(cur && (cur->header != header ||
		ATgetArgument(cur, 0) != arg0 ||
		ATgetArgument(cur, 1) != arg1 ||
		ATgetArgument(cur, 2) != arg2 ||
		ATgetArgument(cur, 3) != arg3))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(ARG_OFFSET+4);
    cur->header = header;
    ATgetArgument(cur, 0) = arg0;
    ATgetArgument(cur, 1) = arg1;
    ATgetArgument(cur, 2) = arg2;
    ATgetArgument(cur, 3) = arg3;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

  return (ATermAppl)cur;  
}

/*}}}  */
/*{{{  ATermAppl ATmakeAppl5(Symbol sym, ATerm arg0, arg1, arg2, a3, a4) */

/**
  * Create an ATermAppl with five arguments.
  */

ATermAppl ATmakeAppl5(Symbol sym, ATerm arg0, ATerm arg1, ATerm arg2, 
		    ATerm arg3, ATerm arg4)
{
  ATerm cur;
  header_type header = APPL_HEADER(0, 5, sym);
  unsigned int hnr = HNUM6(header, arg0, arg1, arg2, arg3, arg4);
  
  CHECK_ARITY(ATgetArity(sym), 5);

  cur = hashtable[hnr];
  while(cur && (cur->header != header ||
		ATgetArgument(cur, 0) != arg0 ||
		ATgetArgument(cur, 1) != arg1 ||
		ATgetArgument(cur, 2) != arg2 ||
		ATgetArgument(cur, 3) != arg3 ||
		ATgetArgument(cur, 4) != arg4))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(ARG_OFFSET+5);
    cur->header = header;
    ATgetArgument(cur, 0) = arg0;
    ATgetArgument(cur, 1) = arg1;
    ATgetArgument(cur, 2) = arg2;
    ATgetArgument(cur, 3) = arg3;
    ATgetArgument(cur, 4) = arg4;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

  return (ATermAppl)cur;  
}

/*}}}  */
/*{{{  ATermAppl ATmakeAppl6(Symbol sym, ATerm arg0, arg1, arg2, a3, a4, a5) */

/**
  * Create an ATermAppl with six arguments.
  */

ATermAppl ATmakeAppl6(Symbol sym, ATerm arg0, ATerm arg1, ATerm arg2, 
		    ATerm arg3, ATerm arg4, ATerm arg5)
{
  ATerm cur;
  header_type header = APPL_HEADER(0, 6, sym);
  unsigned int hnr = HNUM7(header, arg0, arg1, arg2, arg3, arg4, arg5);
  
  CHECK_ARITY(ATgetArity(sym), 6);

  cur = hashtable[hnr];
  while(cur && (cur->header != header ||
		ATgetArgument(cur, 0) != arg0 ||
		ATgetArgument(cur, 1) != arg1 ||
		ATgetArgument(cur, 2) != arg2 ||
		ATgetArgument(cur, 3) != arg3 ||
		ATgetArgument(cur, 4) != arg4 ||
		ATgetArgument(cur, 5) != arg5))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(ARG_OFFSET+6);
    cur->header = header;
    ATgetArgument(cur, 0) = arg0;
    ATgetArgument(cur, 1) = arg1;
    ATgetArgument(cur, 2) = arg2;
    ATgetArgument(cur, 3) = arg3;
    ATgetArgument(cur, 4) = arg4;
    ATgetArgument(cur, 5) = arg5;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

  return (ATermAppl)cur;  
}

/*}}}  */
/*{{{  ATermAppl ATmakeApplList(Symbol sym, ATermList args) */

/**
  * Build a function application from a symbol and a list of arguments.
  */

ATermAppl ATmakeApplList(Symbol sym, ATermList args)
{
  int i, arity = ATgetArity(sym);
  ATbool found;
  ATerm cur;
  ATermAppl appl;
  header_type header = APPL_HEADER(0, arity > MAX_INLINE_ARITY ?
																	 MAX_INLINE_ARITY+1 : arity, sym);
  unsigned int hnr;
	
  assert(arity == ATgetLength(args));

  for(i=0; i<arity; i++) {
    arg_buffer[i] = ATgetFirst(args);
    args = ATgetNext(args);
  }

	hnr = hash_number(header, arity, arg_buffer);
  cur = hashtable[hnr];

  while(cur)
	{
		if(cur->header == header)
		{
      appl = (ATermAppl)cur;
      found = ATtrue;
      for(i=0; i<arity; i++)
			{
				if(!ATisEqual(ATgetArgument(appl, i), arg_buffer[i]))
				{
					found = ATfalse;
					break;
				}
      }
      if(found)
				break;
    }
    cur = cur->next;
  }

  if(!cur)
	{
    cur = AT_allocate(ARG_OFFSET + arity);
    cur->header = header;
    for(i=0; i<arity; i++) {
      ATgetArgument(cur, i) = arg_buffer[i];
			arg_buffer[i] = NULL;
		}
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }
  
  return (ATermAppl)cur;
}

/*}}}  */
/*{{{  ATermAppl ATmakeApplArray(Symbol sym, ATerm args[]) */

/**
  * Build a function application from a symbol and an array of arguments.
  */

ATermAppl ATmakeApplArray(Symbol sym, ATerm args[])
{
  int i, arity = ATgetArity(sym);
  ATbool found;
  ATerm cur;
  ATermAppl appl;
  unsigned int hnr;
  header_type header = APPL_HEADER(0, arity > MAX_INLINE_ARITY ?
		MAX_INLINE_ARITY+1 : arity, sym);

  for(i=0; i<arity; i++) {
    arg_buffer[i] = args[i];
    hnr = (int)arg_buffer[i] << i;
  }

	hnr = hash_number(header, arity, arg_buffer);
  cur = hashtable[hnr];

  while(cur) {
    if(cur->header == header) {
      appl = (ATermAppl)cur;
      found = ATtrue;
      for(i=0; i<arity; i++) {
				if(!ATisEqual(ATgetArgument(appl, i), arg_buffer[i])) {
					found = ATfalse;
					break;
				}
      }
      if(found)
				break;
    }
    cur = cur->next;
  }

  if(!cur) {
    cur = AT_allocate(ARG_OFFSET + arity);
    cur->header = header;
    for(i=0; i<arity; i++) {
      ATgetArgument(cur, i) = arg_buffer[i];
			arg_buffer[i] = NULL;
		}
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }
  
  return (ATermAppl)cur;
}

/*}}}  */
/*{{{  ATermAppl ATsetArgument(ATermAppl appl, ATerm arg, int n) */

/**
  * Change one argument of an application.
  */

ATermAppl ATsetArgument(ATermAppl appl, ATerm arg, int n)
{
  int i, arity;
  Symbol sym = ATgetSymbol(appl);

  arity = ATgetArity(sym);
	assert(n >= 0 && n < arity);

  for(i=0; i<arity; i++)
		arg_buffer[i] = ATgetArgument(appl, i);
	arg_buffer[n] = arg;

  return ATmakeApplArray(sym, arg_buffer);
}

/*}}}  */

/*{{{  ATermInt ATmakeInt(int val) */

/**
  * Create an ATermInt
  */

ATermInt ATmakeInt(int val)
{
  ATerm cur;
  header_type header = INT_HEADER(0);
  unsigned int hnr = HNUM2(header, val);
 
  cur = hashtable[hnr];
  while(cur && (cur->header != header || ((ATermInt)cur)->value != val))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(3);
    cur->header = header;
		((ATermInt)cur)->value = val;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

  return (ATermInt)cur;  
}

/*}}}  */
/*{{{  ATermReal ATmakeReal(double val) */

/**
  * Create an ATermReal
  */

ATermReal ATmakeReal(double val)
{
  ATerm cur;
  header_type header = REAL_HEADER(0);
  unsigned int hnr = HNUM3(header, *((ATerm *)&val), *(((ATerm *)&val)+1));

  cur = hashtable[hnr];
  while(cur && (cur->header != header || ((ATermReal)cur)->value != val))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(4);
    cur->header = header;
		((ATermReal)cur)->value = val;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

  return (ATermReal)cur;  
}

/*}}}  */

/*{{{  ATermList ATmakeList(int n, ...) */

/**
  * Create a list with n arguments.
  */

ATermList ATmakeList(int n, ...)
{
  int i;
  va_list args;
  ATermList l;
  static ATerm *elems = 0;
  static int maxelems = 0;

  /* See if we have enough space to store the elements */
  if(n > maxelems) {
	if(!elems)
	  elems = (ATerm *)malloc(n*sizeof(ATerm));
	else
      elems = (ATerm *)realloc(elems, n*sizeof(ATerm));
    if(!elems)
      ATerror("ATmakeListn: cannot allocate space for %d terms.\n", n);
    maxelems = n;
  }

  va_start(args, n);

  for(i=0; i<n; i++)
    elems[i] = va_arg(args, ATerm);

  l = ATempty;
  for(i=n-1; i>=0; i--)
    l = ATinsert(l, elems[i]);

  va_end(args);
  return l;
}

/*}}}  */
/*{{{  ATermList ATmakeList1(ATerm el) */

/**
  * Build a list with one element.
  */

ATermList ATmakeList1(ATerm el)
{
  ATerm cur;
  header_type header = LIST_HEADER(0, 1);
  unsigned int hnr = HNUM3(header, el, ATempty);

  cur = hashtable[hnr];
  while(cur && (cur->header != header || 
		ATgetFirst((ATermList)cur) != el ||
		ATgetNext((ATermList)cur) != ATempty))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(TERM_SIZE_LIST);
    cur->header = header;
    ATgetFirst((ATermList)cur) = el;
    ATgetNext((ATermList)cur) = ATempty;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

  return (ATermList) cur;
}

/*}}}  */
/*{{{  ATermList ATinsert(ATermList tail, ATerm el) */

/**
  * Insert an element at the front of a list.
  */

ATermList ATinsert(ATermList tail, ATerm el)
{
  ATerm cur;
  header_type header = LIST_HEADER(0, (GET_LENGTH(tail->header)+1));

  unsigned int hnr = HNUM3(header, el, tail);

  cur = hashtable[hnr];
  while(cur && (cur->header != header || 
		ATgetFirst((ATermList)cur) != el || 
		ATgetNext((ATermList)cur) != tail))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(TERM_SIZE_LIST);
    cur->header = header;
    ATgetFirst((ATermList)cur) = el;
    ATgetNext((ATermList)cur) = tail;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

  return (ATermList) cur;
}

/*}}}  */

/*{{{  ATermPlaceholder ATmakePlaceholder(ATerm type) */

/**
  * Create a new placeholder.
  */

ATermPlaceholder ATmakePlaceholder(ATerm type)
{
  ATerm cur;
  header_type header = PLACEHOLDER_HEADER(0);

  unsigned int hnr = HNUM2(header, type);

  cur = hashtable[hnr];
  while(cur && (cur->header != header || 
		ATgetPlaceholder((ATermPlaceholder)cur) != type))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(3);
    cur->header = header;
    ((ATermPlaceholder)cur)->ph_type = type;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

  return (ATermPlaceholder) cur;

}

/*}}}  */

/*{{{  ATermBlob ATmakeBlob(void *data, int size) */

/**
  * Create a new BLOB (Binary Large OBject)
  */

ATermBlob ATmakeBlob(int size, void *data)
{
  ATerm cur;
  header_type header = BLOB_HEADER(0, size);

  unsigned int hnr = HNUM2(header, data);

  cur = hashtable[hnr];
  while(cur && (cur->header != header || ((ATermBlob)cur)->data != data))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(3);
    cur->header = header;
    ((ATermBlob)cur)->data = data;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

  return (ATermBlob)cur;
}

/*}}}  */

/*{{{  void ATregisterBlobDestructor(ATbool (*destructor)(ATermBlob)) */

/**
  * Add a blob destructor.
  */

void ATregisterBlobDestructor(ATbool (*destructor)(ATermBlob))
{
  int i;

  for(i=0; i<MAX_DESTRUCTORS; i++) {
    if(destructors[i] == NULL) {
      destructors[i] = destructor;
      if(i>=destructor_count)
	destructor_count = i+1;
      return;
    }
  }
}

/*}}}  */
/*{{{  void ATunregisterBlobDestructor(ATbool (*destructor)(ATermBlob)) */

/**
  * Add a blob destructor.
  */

void ATunregisterBlobDestructor(ATbool (*destructor)(ATermBlob))
{
  int i;

  for(i=0; i<MAX_DESTRUCTORS; i++) {
    if(destructors[i] == destructor) {
      destructors[i] = NULL;
      break;
    }
  }

  for(i=MAX_DESTRUCTORS-1; i>=0; i--) {
    if(destructors[i] != NULL) {
      destructor_count = i+1;
      return;
    }
  }
}

/*}}}  */

/*{{{  static int term_size(ATerm t) */

/**
  * Calculate the size (in words) of a term.
  */

static int term_size(ATerm t)
{
  int size = (HAS_ANNO(t->header) ? 3 : 2);
  int arity;
  Symbol sym;

  switch(ATgetType(t)) {
    case AT_INT:
    case AT_PLACEHOLDER:
      size++;
      break;
    case AT_REAL:
    case AT_LIST:
    case AT_BLOB:
      size += 2;
      break;
    case AT_APPL:
      sym = ATgetSymbol(t);
      arity = ATgetArity(sym);
      size += (arity > MAX_INLINE_ARITY ? arity+1 : arity);
      break;
  }
  return size;
}

/*}}}  */
/*{{{  ATermList AT_getAnnotations(ATerm t) */

/**
  * Retrieve the annotations of a term.
  */

ATerm AT_getAnnotations(ATerm t)
{
  if(HAS_ANNO(t->header)) {
    int size = term_size(t);
    return ((ATerm *)t)[size-1];
  }
  return NULL;
}

/*}}}  */
/*{{{  ATerm AT_setAnnotations(ATerm t, ATermList annos) */

/**
  * Change the annotations of a term.
  */

ATerm AT_setAnnotations(ATerm t, ATerm annos)
{
  unsigned int hnr;
  int i, size = term_size(t);
  header_type header;
  ATbool found;
  ATerm cur;
  
	header = t->header;
  if(HAS_ANNO(header))
    size--;
  else
    SET_ANNO(header);

  hnr = hash_number_anno(header, size-2, ((ATerm *)t)+2, annos);
  cur = hashtable[hnr];
  found = ATfalse;
  
  /* Look through the hashtable for an identical term */
  while(cur && !found) {
    if(cur->header != header || !ATisEqual(((ATerm *)cur)[size],annos)) {
      /* header or annos are different, must be another term */
      cur = cur->next;
    } else {
      ATbool rest_equal = ATtrue;

      /* check if other components are equal */
      for(i=2; i<size; i++) {
				if(((ATerm *)cur)[i] != ((ATerm *)t)[i]) {
					rest_equal = ATfalse;
					break;
				}
      }

      if(rest_equal)
				found = ATtrue;
      else
				cur = cur->next;
    }
  }

  if(!found) {
    /* We need to create a new term */
    cur = AT_allocate(size+1);
    cur->header = header;
    cur->next   = hashtable[hnr];
    hashtable[hnr] = cur;

    for(i=2; i<size; i++)
      ((ATerm *)cur)[i] = ((ATerm *)t)[i];
    ((ATerm *)cur)[i] = annos;
  }
  return cur;
}

/*}}}  */
/*{{{  ATerm AT_removeAnnotations(ATerm t) */

/**
  * Remove all annotations of a term.
  */

ATerm AT_removeAnnotations(ATerm t)
{
  unsigned int hnr;
  int i, size;
  header_type header;
  ATbool found;
  ATerm cur;
  
  if(!HAS_ANNO(t->header))
    return t;

	header = t->header;
  CLR_ANNO(header);
  size = term_size(t)-1;

  hnr = hash_number(header, size-2, ((ATerm *)t)+2);
  cur = hashtable[hnr];
  found = ATfalse;
  
  /* Look through the hashtable for an identical term */
  while(cur && !found) {
    if(cur->header != header) {
      /* header is different, must be another term */
      cur = cur->next;
    } else {
      ATbool rest_equal = ATtrue;

      /* check if other components are equal */
      for(i=2; i<size; i++) {
	if(((ATerm *)cur)[i] != ((ATerm *)t)[i]) {
	  rest_equal = ATfalse;
	  break;
	}
      }

      if(rest_equal)
	found = ATtrue;
      else
	cur = cur->next;
    }
  }

  if(!found) {
    /* We need to create a new term */
    cur = AT_allocate(size);
    cur->header = header;
    cur->next   = hashtable[hnr];
    hashtable[hnr] = cur;

    for(i=2; i<size; i++)
      ((ATerm *)cur)[i] = ((ATerm *)t)[i];
  }
  return cur;
}

/*}}}  */

/*{{{  ATerm ATsetAnnotation(ATerm t, ATerm label, ATerm anno) */

ATerm ATsetAnnotation(ATerm t, ATerm label, ATerm anno)
{
  ATerm newannos, oldannos = AT_getAnnotations(t);

  if(!oldannos)
    oldannos = ATdictCreate();

  newannos = ATdictPut(oldannos, label, anno);

  if(ATisEqual(oldannos, newannos))
    return t;
  return AT_setAnnotations(t, newannos);
}

/*}}}  */
/*{{{  ATerm ATgetAnnotation(ATerm t, ATerm label) */

/**
  * Retrieve an annotation with a specific label.
  */

ATerm ATgetAnnotation(ATerm t, ATerm label)
{
  ATerm annos = AT_getAnnotations(t);
  if(!annos)
    return NULL;

  return ATdictGet(annos, label);
}

/*}}}  */
/*{{{  ATerm ATremoveAnnotation(ATerm t, ATerm label) */

/**
  * Remove an annotation
  */

ATerm ATremoveAnnotation(ATerm t, ATerm label)
{
  ATerm newannos, oldannos = AT_getAnnotations(t);

  if(!oldannos)
    return t;

  newannos = ATdictRemove(oldannos, label);

  if(ATisEqual(newannos, oldannos))
    return t;

  if(ATisEmpty((ATermList)newannos))
    return AT_removeAnnotations(t);

  return AT_setAnnotations(t, newannos);
}

/*}}}  */
/*{{{  ATbool ATisValidTerm(ATerm term) */

/**
 * Determine if a given term is valid.
 */

ATbool AT_isValidTerm(ATerm term)
{
  Block *cur;
  int idx = (((unsigned int)term)>>(BLOCK_SHIFT+2)) % BLOCK_TABLE_SIZE;
  header_type header;
  int         type;
	ATbool inblock = ATfalse;
	int offset = 0;

	for(cur=block_table[idx].first_after; cur; cur=cur->next_after) 
	{
		offset  = ((char *)term) - ((char *)&cur->data);
		if (offset >= 0	&& offset < (BLOCK_SIZE * sizeof(header_type))) {
			inblock = ATtrue;
			/*fprintf(stderr, "term %p in block %p, size=%d, offset=%d\n", 
						 term, &cur->data[0], cur->size, offset);*/
			break;
		}
	}

	if(!inblock)
  {
		for(cur=block_table[idx].first_before; cur; cur=cur->next_before) 
		{
			offset  = ((char *)term) - ((char *)&cur->data);
			if (offset >= 0 && offset < (BLOCK_SIZE * sizeof(header_type))) {
				inblock = ATtrue;
				/*fprintf(stderr, "term %p in block %p, size=%d, offset=%d\n", 
							 term, &cur->data[0], cur->size, offset);*/
				break;
			}
		}
	}

	if(!inblock) {
		/*fprintf(stderr, "not in block: %p\n", term);*/
		return ATfalse;
	}

	/* Check if we point to the start of a term. Pointers inside terms
     are not allowed.
	 */
	if(offset % (cur->size*sizeof(header)))
		return ATfalse;

  /* <PO> was: 
		 if((((header_type)term - (header_type)&cur->data) % cur->size) != 0)
		   return ATfalse;
	*/

  header = term->header;
  type = GET_TYPE(header);

	/* The only possibility left for an invalid term is AT_FREE */
	return (((type == AT_FREE) || (type == AT_SYMBOL)) ? ATfalse : ATtrue);
}

/*}}}  */

/*{{{  void AT_validateFreeList(int size) */

void AT_validateFreeList(int size)
{
	ATerm cur1, cur2;

	for(cur1=at_freelist[size]; cur1; cur1=cur1->next) {
		for(cur2=cur1->next; cur2; cur2=cur2->next)
			assert(cur1 != cur2);
		assert(ATgetType(cur1) == AT_FREE);
	}
	
}

/*}}}  */
/*{{{  int AT_inAnyFreeList(ATerm t) */

/**
	* Check if a term is in any free list.
	*/

int AT_inAnyFreeList(ATerm t)
{
	int i;

	for(i=0; i<MAX_TERM_SIZE; i++) {
		ATerm cur = at_freelist[i];

		while(cur) {
			if(cur == t)
				return i;
			cur = cur->next;
		}
	}
	return 0;
}

/*}}}  */
