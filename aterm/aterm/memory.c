/*

    ATerm -- The ATerm (Annotated Term) library
    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
                             The  Netherlands.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/

/**
  * memory.c: Memory allocation of ATerms.
  */

/*{{{  includes */

#include <ctype.h>
#include <stdlib.h>
#include <stdarg.h>
#include <assert.h>
#include "_aterm.h"
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

#define TERM_HASH_OPT       "-at-termtable"
#define HASH_INFO_OPT       "-at-hashinfo"

#define CHECK_ARITY(ari1,ari2) DBG_ARITY(assert((ari1) == (ari2)))

#define INFO_HASHING    1
#define MAX_INFO_SIZES 256

#define HN(i)          ((HashNumber)(i))
#define IDX(w)         HN(((w>>2) ^ (w>>10)) & 0xFF)
#define SHIFT(w)       HN((w>>3) & 0xF)

#define START(w)       FOLD(w)
#define COMBINE(hnr,w) ((hnr)<<1 ^ (hnr)>>1 ^ (HashNumber)(FOLD(w)))
#define FINISH(hnr)	   (hnr)

#ifdef AT_64BIT 
#define FOLD(w)        ((HN(w)) ^ (HN(w) >> 32))
#define PTR_ALIGN_SHIFT	4
#else
#define FOLD(w)        (HN(w))
#define PTR_ALIGN_SHIFT	2
#endif

#define ADDR_TO_BLOCK_IDX(a) \
	((((HashNumber)(a))>>(BLOCK_SHIFT+PTR_ALIGN_SHIFT)) % BLOCK_TABLE_SIZE)

#ifdef TERM_CHECK
#define CHECK_TERM(t) assert(AT_isValidTerm(t) && "term is invalid")
#else
#define CHECK_TERM(t)
#endif

/*}}}  */
/*{{{  types */


/*}}}  */
/*{{{  globals */

char memory_id[] = "$Id$";

Block *at_blocks[MAX_TERM_SIZE]  = { NULL };
int at_nrblocks[MAX_TERM_SIZE]   = { 0 };
ATerm at_freelist[MAX_TERM_SIZE] = { NULL };
BlockBucket block_table[BLOCK_TABLE_SIZE] = { { NULL, NULL } };
MachineWord total_nodes = 0;

static MachineWord alloc_since_gc[MAX_TERM_SIZE] = { 0 };
static unsigned int table_class = 14;
static HashNumber table_size;
static HashNumber table_mask;
#ifndef NO_SHARING
static int maxload = 80;
#endif
static ATerm *hashtable;

static int destructor_count = 0;
static ATbool (*destructors[MAX_DESTRUCTORS])(ATermBlob) = { NULL };

static MachineWord *protoTerm = NULL;
static ATerm *arg_buffer = NULL;
static ATerm protected_buffer[MAX_ARITY] = { NULL };

ATermList ATempty;

static int infoflags = 0;

static int gc_count = 0;
static int hash_info_before_gc[MAX_INFO_SIZES][3];
static int hash_info_after_gc[MAX_INFO_SIZES][3];

/*}}}  */

/*{{{  static int term_size(ATerm t) */

/**
  * Calculate the size (in words) of a term.
  */

static int term_size(ATerm t)
{
	int size = 0;

	if(HAS_ANNO(t->header)) {
    size++;
	}

  switch(ATgetType(t)) {
    case AT_INT:
				size += TERM_SIZE_INT;
				break;
    case AT_PLACEHOLDER:
				size += TERM_SIZE_PLACEHOLDER;
				break;
    case AT_REAL:
				size += TERM_SIZE_REAL;
				break;
    case AT_LIST:
				size += TERM_SIZE_LIST;
				break;
    case AT_BLOB:
				size += TERM_SIZE_BLOB;
        break;
    case AT_APPL:
        size += 2+ATgetArity(ATgetSymbol(t));
        break;
  }
  return size;
}

/*}}}  */
/*{{{  static HashNumber hash_number(ATerm t, int size) */

static HashNumber hash_number(ATerm t, int size)
{
	MachineWord *words = (MachineWord *) t;
  int i;
	HashNumber hnr;

  hnr = START(words[0]);
  for (i=2; i<size; i++) {
		hnr = COMBINE(hnr, words[i]);
	}

  return FINISH(hnr);
}

/*}}}  */
/*{{{  static HashNumber hash_number_anno(ATerm t, int size, anno) */

#ifndef NO_SHARING
static HashNumber hash_number_anno(ATerm t, int size, ATerm anno)
{
	MachineWord *words = (MachineWord *) t;
  int i;
	HashNumber hnr;

  hnr = START(words[0]);
  for (i=2; i<size; i++) {
		hnr = COMBINE(hnr, words[i]);
	}
  hnr = COMBINE(hnr, (MachineWord)anno);

  return FINISH(hnr);
}
#endif

/*}}}  */
/*{{{  HashNumber AT_hashnumber(ATerm t) */

/**
	* Calculate the hashnumber of a term.
	*/

HashNumber AT_hashnumber(ATerm t)
{
	return hash_number(t, term_size(t));
}

/*}}}  */
/*{{{  static void hash_info(int stats[3][]) */

static void hash_info(int stats[MAX_INFO_SIZES][3]) 
{
#ifndef NO_SHARING
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
#endif
}

/*}}}  */
/*{{{  static void resize_hashtable() */

/**
	* Resize the hashtable
	*/

void resize_hashtable()
{
	ATerm *oldtable;
	ATerm *newhalf, *p;
	HashNumber oldsize;

	oldtable = hashtable;
	oldsize = table_size;

	table_class++;
	table_size = 1<<table_class;
	table_mask = table_size-1;
	if (!silent)
		fprintf(stderr, "resizing hashtable, class = %d\n", table_class);
	
	/*{{{  Create new term table */
	hashtable = (ATerm *) realloc(hashtable, table_size * sizeof(ATerm));
	if (!hashtable) {
		fprintf(stderr, "warning: could not resize hashtable to class %d.\n",
						table_class);
		table_class--;
		hashtable = oldtable;
		table_size = oldsize;
		table_mask = oldsize-1;
		return;
	}
	/*}}}  */
	
	/*{{{  Clear 2nd half of new table, uses increment == 2*oldsize */
	memset(hashtable+oldsize, 0, oldsize*sizeof(ATerm));
	/*}}}  */
	
	/*{{{  Rehash all old elements */

	newhalf = hashtable + oldsize;
	for(p=hashtable; p < newhalf; p++) {
		ATerm marked = *p;
		/*{{{  Loop over marked part */
		while(marked && IS_MARKED(marked->header)) {
			CLR_MARK(marked->header);
			marked = marked->next;
		}
		/*}}}  */
		
		/*{{{  Loop over unmarked part */

		if (marked) {
			ATerm unmarked;
			ATerm *hashspot;

			if (marked == *p) {
				/* No marked terms */
				unmarked = marked;
				*p = NULL;
			} else {
				/* disconnect unmarked terms from rest */
				unmarked = marked->next;
				marked->next = NULL;
			}

			while(unmarked) {
				ATerm next = unmarked->next;
				HashNumber hnr;
				
				hnr = hash_number(unmarked, term_size(unmarked));
				hnr &= table_mask;
				hashspot = hashtable+hnr;
				unmarked->next = *hashspot;
				*hashspot = unmarked;

				if (hashspot > p && hashspot < newhalf)
					SET_MARK(unmarked->header);

				unmarked = next;
			}
		}

		/*}}}  */
	}

	/*}}}  */

}


/*}}}  */

/*{{{  void AT_initMemory(int argc, char *argv[]) */

/**
  * Initialize memory allocation datastructures
  */

void AT_initMemory(int argc, char *argv[])
{
  int i;

#ifndef NO_SHARING
	HashNumber hnr;
#endif

	table_class = 17;

  protoTerm  = (MachineWord *) calloc(MAX_TERM_SIZE, sizeof(MachineWord));
  arg_buffer = (ATerm *) (protoTerm + 2);

	/*{{{  Analyze arguments */

  for (i = 1; i < argc; i++) {
    if (streq(argv[i], TERM_HASH_OPT))
      table_class = atoi(argv[++i]);
		else if(streq(argv[i], HASH_INFO_OPT))
			infoflags |= INFO_HASHING;
		else if(strcmp(argv[i], "-at-help") == 0) {
			fprintf(stderr, "    %-20s: initial termtable size " 
						"(2^size, default=%d)\n",	TERM_HASH_OPT " <size>", table_class);
			fprintf(stderr, "    %-20s: write information to 'hashing.stats'\n",
							HASH_INFO_OPT);
		}
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

#ifdef NO_SHARING
	hashtable = NULL;
#else
  hashtable = (ATerm *)calloc(table_size, sizeof(ATerm ));
  if(!hashtable) {
    ATerror("AT_initMemory: cannot allocate term table of size %d\n", 
	    table_size);
  }
#endif

  for(i=0; i<BLOCK_TABLE_SIZE; i++) {
		block_table[i].first_before = NULL;
		block_table[i].first_after  = NULL;
  }

	/*}}}  */
	/*{{{  Create the empty list */

  ATempty = (ATermList)AT_allocate(TERM_SIZE_LIST);
  ATempty->header = EMPTY_HEADER(0);
  ATempty->next = NULL;
  ATempty->head = NULL;
  ATempty->tail = NULL;

#ifndef NO_SHARING
	hnr = hash_number((ATerm)ATempty, TERM_SIZE_LIST);
  hashtable[hnr & table_mask] = (ATerm)ATempty;
#endif
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
	ATprotectArray(protected_buffer, MAX_ARITY);
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

#ifdef NO_SHARING
		fprintf(f, "sharing has been disabled.\n");
#else
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
#endif
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

	assert(size >= MIN_TERM_SIZE && size < MAX_TERM_SIZE);


	if (newblock == NULL) {
		ATerror("allocate_block: out of memory!\n");
	}
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

	/* Place the new block in the block_table */
	/*idx = (((MachineWord)newblock) >> (BLOCK_SHIFT+2)) % BLOCK_TABLE_SIZE;*/
	idx = ADDR_TO_BLOCK_IDX(newblock);
	newblock->next_after = block_table[idx].first_after;
	block_table[idx].first_after = newblock;
	idx = (idx+1) % BLOCK_TABLE_SIZE;
	newblock->next_before = block_table[idx].first_before;
	block_table[idx].first_before = newblock;

	total_nodes += BLOCK_SIZE/size;
}

/*}}}  */
/*{{{  ATerm AT_allocate(int size) */

/**
  * Allocate a node of a particular size
  */

ATerm AT_allocate(int size)
{
  ATerm at;
  
  while (!at_freelist[size])
    {
      long total = ((long)at_nrblocks[size])*(BLOCK_SIZE/size);
      /*printf("alloc_since_gc[%d] = %d, total=%d\n", size,
	alloc_since_gc[size], total);*/
      if(100*(alloc_since_gc[size]/GC_THRESHOLD) <= total) {
	/*if(1) {*/
	allocate_block(size);
#ifndef NO_SHARING
	/* Hashtable might need resizing. */
	if((total_nodes/maxload)*100 > table_size) {
	  resize_hashtable();
	}
#endif
      } else {
	gc_count++;
	if(infoflags & INFO_HASHING) {
	  hash_info(hash_info_before_gc);
	}
	AT_collect(size);	
	if(infoflags & INFO_HASHING) {
	  hash_info(hash_info_after_gc);
	}
	alloc_since_gc[size] = 0;
      }
    }
  
  at = at_freelist[size];
  ++alloc_since_gc[size];
  at_freelist[size] = at_freelist[size]->next;
  return at;
}

/*}}}  */

#ifdef NO_SHARING

/*{{{  void AT_freeTerm(int size, ATerm t) */

/**
	* Free a term of a particular size.
	*/

void AT_freeTerm(int size, ATerm t)
{
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
  ATerm cur;
  header_type header;

  va_list args;

  va_start(args, sym);

  header = APPL_HEADER(0, arity > MAX_INLINE_ARITY ?
		MAX_INLINE_ARITY+1 : arity, sym);

	cur = AT_allocate(arity + ARG_OFFSET);
	cur->header = header;
	for(i=0; i<arity; i++)
		ATgetArgument(cur, i) = va_arg(args, ATerm);

  va_end(args);
  
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
  
  CHECK_ARITY(ATgetArity(sym), 0);

	cur = AT_allocate(ARG_OFFSET);
	cur->header = header;

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
 
  CHECK_TERM(arg0);
  CHECK_ARITY(ATgetArity(sym), 1);
 
  cur = AT_allocate(ARG_OFFSET+1);
  cur->header = header;
  ATgetArgument(cur, 0) = arg0;

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
  
  CHECK_TERM(arg0);
  CHECK_TERM(arg1);
  CHECK_ARITY(ATgetArity(sym), 2);

	cur = AT_allocate(ARG_OFFSET+2);
	cur->header = header;
	ATgetArgument(cur, 0) = arg0;
	ATgetArgument(cur, 1) = arg1;

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
  
  CHECK_TERM(arg0);
  CHECK_TERM(arg1);
  CHECK_TERM(arg2);
  CHECK_ARITY(ATgetArity(sym), 3);

	cur = AT_allocate(ARG_OFFSET+3);
	cur->header = header;
	ATgetArgument(cur, 0) = arg0;
	ATgetArgument(cur, 1) = arg1;
	ATgetArgument(cur, 2) = arg2;

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
  
  CHECK_TERM(arg0);
  CHECK_TERM(arg1);
  CHECK_TERM(arg2);
  CHECK_TERM(arg3);
  CHECK_ARITY(ATgetArity(sym), 4);

	cur = AT_allocate(ARG_OFFSET+4);
	cur->header = header;
	ATgetArgument(cur, 0) = arg0;
	ATgetArgument(cur, 1) = arg1;
	ATgetArgument(cur, 2) = arg2;
	ATgetArgument(cur, 3) = arg3;

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

  CHECK_TERM(arg0);
  CHECK_TERM(arg1);
  CHECK_TERM(arg2);
  CHECK_TERM(arg3);
  CHECK_TERM(arg4);
  CHECK_ARITY(ATgetArity(sym), 5);

	cur = AT_allocate(ARG_OFFSET+5);
	cur->header = header;
	ATgetArgument(cur, 0) = arg0;
	ATgetArgument(cur, 1) = arg1;
	ATgetArgument(cur, 2) = arg2;
	ATgetArgument(cur, 3) = arg3;
	ATgetArgument(cur, 4) = arg4;

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
  
  CHECK_TERM(arg0);
  CHECK_TERM(arg1);
  CHECK_TERM(arg2);
  CHECK_TERM(arg3);
  CHECK_TERM(arg4);
  CHECK_TERM(arg5);
  CHECK_ARITY(ATgetArity(sym), 6);

	cur = AT_allocate(ARG_OFFSET+6);
	cur->header = header;
	ATgetArgument(cur, 0) = arg0;
	ATgetArgument(cur, 1) = arg1;
	ATgetArgument(cur, 2) = arg2;
	ATgetArgument(cur, 3) = arg3;
	ATgetArgument(cur, 4) = arg4;
	ATgetArgument(cur, 5) = arg5;

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
  ATerm cur;
  header_type header = APPL_HEADER(0, arity > MAX_INLINE_ARITY ?
				   MAX_INLINE_ARITY+1 : arity, sym);
  
  CHECK_TERM(args);
  assert(arity == ATgetLength(args));
  
  cur = AT_allocate(ARG_OFFSET + arity);
  cur->header = header;
  for(i=0; i<arity; i++) {
    ATgetArgument(cur, i) = ATgetFirst(args);
    args = ATgetNext(args);
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
  ATerm cur;
  header_type header = APPL_HEADER(0, arity > MAX_INLINE_ARITY ?
				   MAX_INLINE_ARITY+1 : arity, sym);
  
  cur = AT_allocate(ARG_OFFSET + arity);
  cur->header = header;
  for(i=0; i<arity; i++) {
    CHECK_TERM(args[i]);
    ATgetArgument(cur, i) = args[i];
  }
  
  return (ATermAppl)cur;
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
 
	cur = AT_allocate(TERM_SIZE_INT);
	cur->header = header;
	((ATermInt)cur)->value = val;

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

	cur = AT_allocate(TERM_SIZE_REAL);
	cur->header = header;
	((ATermReal)cur)->value = val;

  return (ATermReal)cur;  
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

  CHECK_TERM(el);
  cur = AT_allocate(TERM_SIZE_LIST);
  cur->header = header;
  ATgetFirst((ATermList)cur) = el;
  ATgetNext((ATermList)cur) = ATempty;

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

  CHECK_TERM(tail);
  CHECK_TERM(el);

  cur = AT_allocate(TERM_SIZE_LIST);
  cur->header = header;
  ATgetFirst((ATermList)cur) = el;
  ATgetNext((ATermList)cur) = tail;
	
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

  CHECK_TERM(type);

  cur = AT_allocate(TERM_SIZE_PLACEHOLDER);
  cur->header = header;
  ((ATermPlaceholder)cur)->ph_type = type;
  
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

  cur = AT_allocate(TERM_SIZE_BLOB);
  cur->header = header;
  ((ATermBlob)cur)->data = data;
  
  return (ATermBlob)cur;
}

/*}}}  */

/*{{{  ATerm AT_setAnnotations(ATerm t, ATermList annos) */

/**
  * Change the annotations of a term.
  */

ATerm AT_setAnnotations(ATerm t, ATerm annos)
{
  int i, size = term_size(t);
  header_type header;
  ATerm cur;

  CHECK_TERM(t);
  CHECK_TERM(annos);

  assert(annos != NULL);

	header = t->header;
  if(HAS_ANNO(header))
    size--;
  else
    SET_ANNO(header);

	/* We need to create a new term */
	cur = AT_allocate(size+1);
	cur->header = header;

	for(i=2; i<size; i++)
		((ATerm *)cur)[i] = ((ATerm *)t)[i];
	((ATerm *)cur)[i] = annos;

  return cur;
}

/*}}}  */
/*{{{  ATerm AT_removeAnnotations(ATerm t) */

/**
  * Remove all annotations of a term.
  */

ATerm AT_removeAnnotations(ATerm t)
{
  int i, size;
  header_type header;
  ATerm cur;
  
  CHECK_TERM(t); 
  if(!HAS_ANNO(t->header))
    return t;

  header = t->header;
  CLR_ANNO(header);
  size = term_size(t)-1;

  /* We need to create a new term */
  cur = AT_allocate(size);
  cur->header = header;
  for(i=2; i<size; i++)
    ((ATerm *)cur)[i] = ((ATerm *)t)[i];
  
  return cur;
}

/*}}}  */

#else

/*{{{  void AT_freeTerm(int size, ATerm t) */

/**
	* Free a term of a particular size.
	*/

void AT_freeTerm(int size, ATerm t)
{
	ATbool found;
	int i, nrargs = size-ARG_OFFSET;

	/* Remove the node from the hashtable */
	HashNumber hnr = hash_number(t, size);
	ATerm prev = NULL, cur;

  hnr &= table_mask; 
  cur = hashtable[hnr];

	do {
		ATerm *arg_cur, *arg_t;

		if(!cur) {
			ATabort("### cannot find term %n at %p in hashtable at pos %d"
							", header = %d\n", t, t, hnr, t->header);
    }
		assert(cur);

		if(t->header != cur->header)
			continue;

		arg_cur = ((ATerm *)cur) + ARG_OFFSET + nrargs;
		arg_t = ((ATerm *)t) + ARG_OFFSET + nrargs;

		switch(nrargs) {
			/* fall-throughs are intended */
			default:
				found = ATtrue;
				for(i=nrargs-6; found && i>0; i--)
					if(*(--arg_cur) != *(--arg_t))
						found = ATfalse;
				if(!found)
					continue;
			case 6: if(*(--arg_cur) != *(--arg_t)) continue;
			case 5: if(*(--arg_cur) != *(--arg_t)) continue;
			case 4: if(*(--arg_cur) != *(--arg_t)) continue;
			case 3: if(*(--arg_cur) != *(--arg_t)) continue;
			case 2: if(*(--arg_cur) != *(--arg_t)) continue;
			case 1: if(*(--arg_cur) != *(--arg_t)) continue;
			case 0:
				/* Actually free the node */
				if(prev)
					prev->next = cur->next;
				else
					hashtable[hnr] = cur->next;

				/* Put the node in the appropriate free list */
				t->header = FREE_HEADER;
				t->next  = at_freelist[size];
				at_freelist[size] = t;
				return;
		}
	} while(((prev=cur), (cur=cur->next)));
}

/*}}}  */

/*{{{  ATermAppl ATmakeAppl(Symbol sym, ...) */

/**
  * Create a new ATermAppl. The argument count can be found in the symbol.
  */

ATermAppl ATmakeAppl(Symbol sym, ...)
{
	ATermAppl protoAppl;
  int i, arity = ATgetArity(sym);
  ATbool found;
  ATerm cur;
  ATermAppl appl;
  header_type header;
  HashNumber hnr;
  va_list args;

	protoAppl = (ATermAppl) protoTerm;

  va_start(args, sym);
  for (i=0; i<arity; i++) {
    arg_buffer[i] = va_arg(args, ATerm);
	}
  va_end(args);

  header = APPL_HEADER(0, arity > MAX_INLINE_ARITY ?
		MAX_INLINE_ARITY+1 : arity, sym);

	protoAppl->header = header;

  hnr = hash_number((ATerm)protoAppl, arity + 2);
  cur = hashtable[hnr & table_mask];

  while (cur) {
    if (cur->header == header) {
      appl = (ATermAppl)cur;
      found = ATtrue;
      for (i=0; i<arity; i++) {
				if (!ATisEqual(ATgetArgument(appl, i), arg_buffer[i])) {
					found = ATfalse;
					break;
				}
      }
      if (found)
				break;
    }
    cur = cur->next;
  }

  if (!cur) {
    cur = AT_allocate(arity + ARG_OFFSET);
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
    cur->header = header;
    for (i=0; i<arity; i++) {
      ATgetArgument(cur, i) = arg_buffer[i];
		}
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

	for (i=0; i<arity; i++) {
		arg_buffer[i] = NULL;
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
	ATermAppl protoAppl;
  ATerm cur, prev;
	ATerm *hashspot;
  header_type header = APPL_HEADER(0, 0, sym);
  HashNumber hnr;

  CHECK_ARITY(ATgetArity(sym), 0);

	protoAppl = (ATermAppl) protoTerm;
	protoAppl->header = header;
	hnr = hash_number((ATerm) protoAppl, 2);

	prev = NULL;
	hashspot = &(hashtable[hnr & table_mask]);

  for(cur = *hashspot; cur; cur = cur->next) {
		if(cur->header == header) {
			/* Promote current entry to front of hashtable */
			if (prev != NULL) {
				prev->next = cur->next;
				cur->next = *hashspot;
				*hashspot = cur;
			}
			return (ATermAppl)cur;
		}
		prev = cur;
	}

	cur = AT_allocate(ARG_OFFSET);
	/* Delay masking until after AT_allocate */
	hnr &= table_mask;
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
	ATermAppl protoAppl;
  ATerm cur, prev;
	ATerm *hashspot;
  header_type header = APPL_HEADER(0, 1, sym);
  HashNumber hnr;
 
  CHECK_ARITY(ATgetArity(sym), 1);
 
	protoAppl = (ATermAppl) protoTerm;
	protoAppl->header = header;
	arg_buffer[0] = arg0;
	hnr = hash_number((ATerm) protoAppl, 3);

	prev = NULL;
	hashspot = &(hashtable[hnr & table_mask]);

  for(cur = *hashspot; cur; cur = cur->next) {
    if(cur->header == header && ATgetArgument(cur, 0) == arg0) {
			/* Promote current entry to front of hashtable */
			if (prev != NULL) {
				prev->next = cur->next;
				cur->next = *hashspot;
				*hashspot = cur;
			}

			return (ATermAppl)cur;
		}
		prev = cur;
	}

	cur = AT_allocate(ARG_OFFSET+1);
	/* Delay masking until after AT_allocate */
	hnr &= table_mask;
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
	ATermAppl protoAppl;
  ATerm cur, prev;
  ATerm *hashspot;
  header_type header = APPL_HEADER(0, 2, sym);
  HashNumber hnr;
  
  CHECK_ARITY(ATgetArity(sym), 2);

	protoAppl = (ATermAppl) protoTerm;
	protoAppl->header = header;
	arg_buffer[0] = arg0;
	arg_buffer[1] = arg1;
	hnr = hash_number((ATerm) protoAppl, 4);

	prev = NULL;
	hashspot = &(hashtable[hnr & table_mask]);

  for(cur = *hashspot; cur; cur = cur->next) {
		if(cur->header == header && ATgetArgument(cur, 0) == arg0 &&
			 ATgetArgument(cur, 1) == arg1) {
			/* Promote current entry to front of hashtable */
			if (prev != NULL) {
				prev->next = cur->next;
				cur->next = *hashspot;
				*hashspot = cur;
			}
			return (ATermAppl)cur;
		}
		prev = cur;
	}

	cur = AT_allocate(ARG_OFFSET+2);
	/* Delay masking until after AT_allocate */
	hnr &= table_mask;
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
	ATermAppl protoAppl;
  ATerm cur;
  header_type header = APPL_HEADER(0, 3, sym);
  HashNumber hnr;
  
  CHECK_ARITY(ATgetArity(sym), 3);

	protoAppl = (ATermAppl) protoTerm;
	protoAppl->header = header;
	arg_buffer[0] = arg0;
	arg_buffer[1] = arg1;
	arg_buffer[2] = arg2;
	hnr = hash_number((ATerm) protoAppl, 5);

  cur = hashtable[hnr & table_mask];
  while(cur && (cur->header != header ||
		ATgetArgument(cur, 0) != arg0 ||
		ATgetArgument(cur, 1) != arg1 ||
		ATgetArgument(cur, 2) != arg2))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(ARG_OFFSET+3);
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
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
	ATermAppl protoAppl;
  ATerm cur;
  header_type header = APPL_HEADER(0, 4, sym);
  HashNumber hnr;
  
  CHECK_ARITY(ATgetArity(sym), 4);

	protoAppl = (ATermAppl) protoTerm;
	protoAppl->header = header;
	arg_buffer[0] = arg0;
	arg_buffer[1] = arg1;
	arg_buffer[2] = arg2;
	arg_buffer[3] = arg3;
	hnr = hash_number((ATerm) protoAppl, 6);

  cur = hashtable[hnr & table_mask];
  while(cur && (cur->header != header ||
		ATgetArgument(cur, 0) != arg0 ||
		ATgetArgument(cur, 1) != arg1 ||
		ATgetArgument(cur, 2) != arg2 ||
		ATgetArgument(cur, 3) != arg3))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(ARG_OFFSET+4);
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
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
	ATermAppl protoAppl;
  ATerm cur;
  header_type header = APPL_HEADER(0, 5, sym);
  HashNumber hnr;
  
  CHECK_ARITY(ATgetArity(sym), 5);

	protoAppl = (ATermAppl) protoTerm;
	protoAppl->header = header;
	arg_buffer[0] = arg0;
	arg_buffer[1] = arg1;
	arg_buffer[2] = arg2;
	arg_buffer[3] = arg3;
	arg_buffer[4] = arg4;
	hnr = hash_number((ATerm) protoAppl, 7);

  cur = hashtable[hnr & table_mask];
  while(cur && (cur->header != header ||
		ATgetArgument(cur, 0) != arg0 ||
		ATgetArgument(cur, 1) != arg1 ||
		ATgetArgument(cur, 2) != arg2 ||
		ATgetArgument(cur, 3) != arg3 ||
		ATgetArgument(cur, 4) != arg4))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(ARG_OFFSET+5);
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
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
	ATermAppl protoAppl;
  ATerm cur;
  header_type header = APPL_HEADER(0, 6, sym);
  HashNumber hnr;
  
  CHECK_ARITY(ATgetArity(sym), 6);

	protoAppl = (ATermAppl) protoTerm;
	protoAppl->header = header;
	arg_buffer[0] = arg0;
	arg_buffer[1] = arg1;
	arg_buffer[2] = arg2;
	arg_buffer[3] = arg3;
	arg_buffer[4] = arg4;
	arg_buffer[5] = arg5;
	hnr = hash_number((ATerm) protoAppl, 8);

  cur = hashtable[hnr & table_mask];
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
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
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
	ATermAppl protoAppl;
  int i, arity = ATgetArity(sym);
  ATbool found;
  ATerm cur;
  ATermAppl appl;
  header_type header = APPL_HEADER(0, arity > MAX_INLINE_ARITY ?
																	 MAX_INLINE_ARITY+1 : arity, sym);
  HashNumber hnr;
	
  assert(arity == ATgetLength(args));

	protoAppl = (ATermAppl) protoTerm;
	protoAppl->header = header;

  for (i=0; i<arity; i++) {
    arg_buffer[i] = ATgetFirst(args);
    args = ATgetNext(args);
  }

	hnr = hash_number((ATerm) protoAppl, arity + 2);

  cur = hashtable[hnr & table_mask];
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
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
    cur->header = header;
    for (i=0; i<arity; i++) {
      ATgetArgument(cur, i) = arg_buffer[i];
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
	ATermAppl protoAppl;
  int i, arity = ATgetArity(sym);
  ATbool found;
  ATerm cur;
  ATermAppl appl;
  HashNumber hnr;
  header_type header = APPL_HEADER(0, arity > MAX_INLINE_ARITY ?
		MAX_INLINE_ARITY+1 : arity, sym);

	protoAppl = (ATermAppl) protoTerm;
	protoAppl->header = header;

	if (args != arg_buffer) {
		for (i=0; i<arity; i++) {
			arg_buffer[i] = args[i];
			protected_buffer[i] = args[i];
		}
	}

	hnr = hash_number((ATerm) protoAppl, arity + 2);

  cur = hashtable[hnr & table_mask];
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
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
    cur->header = header;
    for (i=0; i<arity; i++) {
      ATgetArgument(cur, i) = arg_buffer[i];
		}
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

	if (args != arg_buffer) {
		for (i=0; i<arity; i++) {
			protected_buffer[i] = NULL;
		}
	}

  return (ATermAppl)cur;
}

/*}}}  */

/*{{{  ATermInt ATmakeInt(int val) */

/**
  * Create an ATermInt
  */

ATermInt ATmakeInt(int val)
{
  ATermInt protoInt;
  ATerm cur;
  header_type header = INT_HEADER(0);
  HashNumber hnr;
#ifdef AT_64BIT
  MachineWord *words;
#endif

	protoTerm[2] = 0;			/* clear 1st (and only) data-word for int */

	protoInt = (ATermInt) protoTerm;
  protoInt->header = header;
  protoInt->value  = val;

	hnr = hash_number((ATerm)protoInt, TERM_SIZE_INT);

  cur = hashtable[hnr & table_mask];
  while (cur && (cur->header != header || ((ATermInt)cur)->value != val)) {
    cur = cur->next;
	}

  if (!cur) {
    cur = AT_allocate(TERM_SIZE_INT);
#ifdef AT_64BIT
    /* Clear unused half of int word */
    words = (MachineWord *)cur;   
    words[2] = 0;
#endif
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
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
	ATermReal protoReal;
  ATerm cur;
  header_type header = REAL_HEADER(0);
  HashNumber hnr;

	protoReal = (ATermReal) protoTerm;
	protoReal->header = header;
	protoReal->value = val;

	hnr = hash_number((ATerm)protoReal, TERM_SIZE_REAL);

  cur = hashtable[hnr & table_mask];
  while (cur && (cur->header != header || ((ATermReal)cur)->value != val)) {
    cur = cur->next;
	}

  if (!cur) {
    cur = AT_allocate(TERM_SIZE_REAL);
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
    cur->header = header;
		((ATermReal)cur)->value = val;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

  return (ATermReal)cur;  
}

/*}}}  */

/*{{{  ATermList ATmakeList1(ATerm el) */

/**
  * Build a list with one element.
  */

ATermList ATmakeList1(ATerm el)
{
	ATermList protoList;
  ATerm cur;
  header_type header = LIST_HEADER(0, 1);
  HashNumber hnr;

	protoList = (ATermList) protoTerm;
	protoList->header = header;
	protoList->head = el;
	protoList->tail = ATempty;

	hnr = hash_number((ATerm)protoList, TERM_SIZE_LIST);

  cur = hashtable[hnr & table_mask];
  while (cur && (cur->header != header
								 || ATgetFirst((ATermList)cur) != el
								 || ATgetNext((ATermList)cur) != ATempty)) {
		cur = cur->next;
	}

  if (!cur) {
    cur = AT_allocate(TERM_SIZE_LIST);
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
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
	ATermList protoList;
  ATerm cur;
  header_type header = LIST_HEADER(0, (GET_LENGTH(tail->header)+1));
  HashNumber hnr;

	protoList = (ATermList) protoTerm;
	protoList->header = header;
	protoList->head = el;
	protoList->tail = tail;

	hnr = hash_number((ATerm)protoList, TERM_SIZE_LIST);

  cur = hashtable[hnr & table_mask];
  while (cur && (cur->header != header
								 || ATgetFirst((ATermList)cur) != el
								 || ATgetNext((ATermList)cur) != tail)) {
    cur = cur->next;
	}

  if (!cur) {
    cur = AT_allocate(TERM_SIZE_LIST);
		/* Hashtable might be resized, so delay masking until after AT_allocate */
		hnr &= table_mask; 
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
	ATermPlaceholder protoPlaceholder;
  ATerm cur;
  header_type header = PLACEHOLDER_HEADER(0);
  HashNumber hnr;

	protoPlaceholder = (ATermPlaceholder) protoTerm;
	protoPlaceholder->header = header;
	protoPlaceholder->ph_type = type;

	hnr = hash_number((ATerm) protoPlaceholder, TERM_SIZE_PLACEHOLDER);

  cur = hashtable[hnr & table_mask];
  while (cur && (cur->header != header
								 || ATgetPlaceholder((ATermPlaceholder)cur) != type)) {
    cur = cur->next;
	}

  if (!cur) {
    cur = AT_allocate(TERM_SIZE_PLACEHOLDER);
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
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
	ATermBlob protoBlob;
  ATerm cur;
  header_type header = BLOB_HEADER(0, size);
  HashNumber hnr;

	protoBlob = (ATermBlob) protoTerm;
	protoBlob->header = header;
	protoBlob->data = data;

	hnr = hash_number((ATerm) protoBlob, TERM_SIZE_BLOB);
	
  cur = hashtable[hnr & table_mask];
  while (cur && (cur->header != header
								 || ((ATermBlob)cur)->data != data)) {
    cur = cur->next;
	}

  if (!cur) {
    cur = AT_allocate(TERM_SIZE_BLOB);
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
    cur->header = header;
    ((ATermBlob)cur)->data = data;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

ATwarning("ATmakeBlob(%d): hnr = %d, t = %t\n", size, (int)hnr, cur);

  return (ATermBlob)cur;
}

/*}}}  */

/*{{{  ATerm AT_setAnnotations(ATerm t, ATermList annos) */

/**
  * Change the annotations of a term.
  */

ATerm AT_setAnnotations(ATerm t, ATerm annos)
{
  HashNumber hnr;
  int i, size = term_size(t);
  header_type header;
  ATbool found;
  ATerm cur;

  assert(annos != NULL);

  if (HAS_ANNO(t->header)) {
	  header = t->header;
		size--;
    hnr = hash_number_anno(t, size, annos);
	} else {
    SET_ANNO(t->header);
	  header = t->header;
    hnr = hash_number_anno(t, size, annos);
    CLR_ANNO(t->header);
	}

  cur = hashtable[hnr & table_mask];
  found = ATfalse;
  
  /* Look through the hashtable for an identical term */
  while (cur && !found) {
    if (cur->header != header || !ATisEqual(((ATerm *)cur)[size],annos)) {
      /* header or annos are different, must be another term */
      cur = cur->next;
    } else {
      ATbool rest_equal = ATtrue;

      /* check if other components are equal */
      for (i=2; i<size; i++) {
				if (!ATisEqual(((ATerm *)cur)[i], ((ATerm *)t)[i])) {
					rest_equal = ATfalse;
					break;
				}
      }

      if (rest_equal) {
				found = ATtrue;
			} else {
				cur = cur->next;
			}
    }
  }

  if (!found) {
    /* We need to create a new term */
    cur = AT_allocate(size+1);
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
    cur->header = header;
    cur->next   = hashtable[hnr];
    hashtable[hnr] = cur;

    for (i=2; i<size; i++) {
      ((ATerm *)cur)[i] = ((ATerm *)t)[i];
		}
    ((ATerm *)cur)[i] = annos;
  } else {
    
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
  HashNumber hnr;
  int i, size;
  header_type header;
  ATbool found;
  ATerm cur;
  
  if (!HAS_ANNO(t->header)) {
    return t;
	}

  CLR_ANNO(t->header);
	header = t->header;
  size = term_size(t);
  hnr = hash_number(t, size);
	SET_ANNO(t->header);

  cur = hashtable[hnr & table_mask];
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

      if (rest_equal) {
				found = ATtrue;
			} else {
				cur = cur->next;
			}
    }
  }

  if (!found) {
    /* We need to create a new term */
    cur = AT_allocate(size);
		/* Delay masking until after AT_allocate */
		hnr &= table_mask;
    cur->header = header;
    cur->next   = hashtable[hnr];
    hashtable[hnr] = cur;

    for (i=2; i<size; i++) {
      ((ATerm *)cur)[i] = ((ATerm *)t)[i];
		}
  }

  return cur;
}

/*}}}  */
#endif

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

  for (i=0; i<arity; i++) {
		arg_buffer[i] = ATgetArgument(appl, i);
	}
	arg_buffer[n] = arg;

  return ATmakeApplArray(sym, arg_buffer);
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
  if (n > maxelems) {
		if (!elems) {
			elems = (ATerm *)malloc(n*sizeof(ATerm));
		} else {
			elems = (ATerm *)realloc(elems, n*sizeof(ATerm));
		}
    if (!elems) {
      ATerror("ATmakeListn: cannot allocate space for %d terms.\n", n);
		}
    maxelems = n;
  }

  va_start(args, n);

  for (i=0; i<n; i++) {
    elems[i] = va_arg(args, ATerm);
	}

  l = ATempty;
  for (i=n-1; i>=0; i--) {
    l = ATinsert(l, elems[i]);
	}

  va_end(args);
  return l;
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
  header_type header;
	ATbool inblock = ATfalse;
	int idx = ADDR_TO_BLOCK_IDX(term);
  int type;
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
	/*return ((type == AT_FREE) ? ATfalse : ATtrue);*/
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

	for(i=MIN_TERM_SIZE; i<MAX_TERM_SIZE; i++) {
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

/*{{{  void AT_printAllTerms(FILE *file) */

void AT_printAllTerms(FILE *file)
{
	int i;

	for(i=0; i<table_size; i++) {
		ATerm cur = hashtable[i];
		while(cur) {
			ATfprintf(file, "%t\n", cur);
			cur = cur->next;
		}
	}
}

/*}}}  */
/*{{{  void AT_printAllAFunCounts(FILE *file) */

static int compare_afuns(const void *l, const void *r)
{
	AFun left, right;
	int left_count, right_count;

	left = *((AFun *)l);
	right = *((AFun *)r);

	if(left == -1)
		return 1;
	if(right == -1)
		return -1;

	left_count = at_lookup_table[left]->count;
	right_count = at_lookup_table[right]->count;

	if(left_count < right_count)
		return 1;

	if(left_count > right_count)
		return -1;

	return 0;
}

void AT_printAllAFunCounts(FILE *file)
{
	int i, nr_syms;
	AFun *afuns;

	nr_syms = AT_symbolTableSize();

	for(i=0; i<nr_syms; i++) {
		if(!SYM_IS_FREE(at_lookup_table[i]))
			at_lookup_table[i]->count = 0;
	}

	for(i=0; i<table_size; i++) {
		ATerm cur = hashtable[i];
		while(cur) {
			if(ATgetType(cur) == AT_APPL) {
				ATermAppl appl = (ATermAppl)cur;
				AFun fun = ATgetAFun(appl);
				at_lookup_table[fun]->count++;
			}
			cur = cur->next;
		}
	}

	afuns = (AFun *)calloc(nr_syms, sizeof(AFun));
	assert(afuns);

	for(i=0; i<nr_syms; i++) {
		if(SYM_IS_FREE(at_lookup_table[i]))
			afuns[i] = -1;
		else
			afuns[i] = i;
	}

	qsort(afuns, nr_syms, sizeof(AFun), compare_afuns);

	for(i=0; i<nr_syms; i++) {
		if(afuns[i] != -1)
			ATfprintf(file, "%y: %d\n", afuns[i], at_lookup_table[afuns[i]]->count);
	}
}

/*}}}  */
/*{{{  int AT_calcAllocatedBytes() */

/**
	* Calculate all allocated bytes containing ATerms.
	*/

int AT_calcAllocatedSize()
{
	int i;
	int total = 0;

	for(i=0; i<MAX_TERM_SIZE; i++)
		total += at_nrblocks[i]*sizeof(Block);

	total += table_size*sizeof(ATerm);

	return total;
}

/*}}}  */
