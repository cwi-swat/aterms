
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

#define MAX_SIZE 16
#define BLOCK_SIZE (1<<16)
#define GC_THRESHOLD BLOCK_SIZE/4

#define MAX_BLOCKS_PER_SIZE 1024

#define TERM_HASH_OPT      "-termtable"

#define CHECK_ARITY(ari1,ari2) DBG_ARITY(assert((ari1) == (ari2)))

/*}}}  */
/*{{{  globals */

static ATerm blocks[MAX_SIZE][MAX_BLOCKS_PER_SIZE];
static int nrblocks[MAX_SIZE];
static ATerm freelist[MAX_SIZE];

static int table_size;
static ATerm *hashtable;

/*}}}  */

/*{{{  AT_initMemory(int argc, char *argv[]) */

/**
  * Initialize memory allocation datastructures
  */

void AT_initMemory(int argc, char *argv[])
{
  int i;

  table_size = 16411;
  for (i = 1; i < argc; i++)
    if (streq(argv[i], TERM_HASH_OPT))
      table_size = atoi(argv[++i]);

  DBG_MEM(printf("initial term table size = %d\n", table_size));

  for(i=0; i<MAX_SIZE; i++) {
    nrblocks[i] = 0;
    freelist[i] = NULL;
  }

  hashtable = (ATerm *)calloc(table_size, sizeof(ATerm ));
  if(!hashtable) {
    ATerror("AT_initMemory: cannot allocate term table of size %d\n", 
	    table_size);
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
	int block_nr = nrblocks[size];
	blocks[size][block_nr] = (ATerm ) malloc(BLOCK_SIZE * sizeof(header_type));

	if (blocks[size][block_nr] == NULL)
		ATerror("allocate_block: out of memory!\n");

	last = BLOCK_SIZE - size * sizeof(header_type);
	freelist[size] = blocks[size][block_nr];
	for (idx=0; idx < last; idx += size)
	{
		((ATerm )(((header_type *)blocks[size][block_nr])+idx))->next =
			(ATerm )(((header_type *)blocks[size][block_nr])+idx+size);
	}
	((ATerm )(((header_type *)blocks[size][block_nr])+idx))->next = NULL;
}

/*}}}  */
/*{{{  void AT_collect() */

/**
  * Collect all garbage
  */

void AT_collect(int size)
{
  fprintf(stderr, "collection not implemented yet, "
	                "allocating new block of size %d\n", size);

	allocate_block(size);
}

/*}}}  */
/*{{{  ATerm AT_allocate(int size) */

/**
  * Allocate a node of a particular size
  */

ATerm AT_allocate(int size)
{
	ATerm at;

	if (!freelist[size])
		AT_collect(size);

	at = freelist[size];
	freelist[size] = freelist[size]->next;
	return at;
}

/*}}}  */

/*{{{  ATermAppl ATmakeAppl(Symbol sym, ...) */

/**
  * Create a new ATermAppl. The argument count can be found in the symbol.
  */

ATermAppl ATmakeAppl(Symbol sym, ...)
{
  int arity = ATgetArity(sym);
  va_list args;

  va_start(args, sym);

  #define NEXTARG va_arg(args, ATerm )
  switch(arity) {
    case 0:  return ATmakeAppl0(sym);
      break;
    case 1:  return ATmakeAppl1(sym, NEXTARG);
      break;
    case 2:  return ATmakeAppl2(sym, NEXTARG, NEXTARG);
      break;
    case 3:  return ATmakeAppl3(sym, NEXTARG, NEXTARG, NEXTARG);
      break;
    case 4:  return ATmakeAppl4(sym, NEXTARG, NEXTARG, NEXTARG, NEXTARG);
      break;
    case 5:  return ATmakeAppl5(sym, NEXTARG, NEXTARG, NEXTARG, NEXTARG, 
				NEXTARG);
      break;
    case 6:  return ATmakeAppl6(sym, NEXTARG, NEXTARG, NEXTARG, NEXTARG, 
				NEXTARG, NEXTARG);
      break;

    default:
      ATerror("makeAppl with > 6 args not implemented yet.");
      return NULL;
  }

  va_end(args);
}

/*}}}  */
/*{{{  ATermAppl ATmakeAppl0(Symbol sym) */

/**
  * Create an ATermAppl with zero arguments.
  */

ATermAppl ATmakeAppl0(Symbol sym)
{
  ATerm cur;
  header_type header;
  unsigned int hnr = ((unsigned int)sym) >> 2;
  hnr %= table_size;
  
  CHECK_ARITY(ATgetArity(sym), 0);

  header = APPL_HEADER(0, 0, sym);
  cur = hashtable[hnr];
  while(cur && cur->header != header)
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(2);
    cur->header = header;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

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
  header_type header;

  unsigned int hnr = (((unsigned int)sym) >> 2) ^ ((int)arg0);
  hnr %= table_size;
 
  CHECK_ARITY(ATgetArity(sym), 1);
 
  header = APPL_HEADER(0, 1, sym);
  cur = hashtable[hnr];
  while(cur && (cur->header != header || ATgetArgument(cur, 0) != arg0))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(3);
    cur->header = header;
    ATgetArgument(cur, 0) = arg0;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

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
  header_type header;

  unsigned int hnr = (((unsigned int)sym) >> 2) ^ (int)arg0 ^ ((int)arg1<<1);
  hnr %= table_size;
  
  CHECK_ARITY(ATgetArity(sym), 2);

  header = APPL_HEADER(0, 2, sym);

  cur = hashtable[hnr];
  while(cur && (cur->header != header || 
		ATgetArgument(cur, 0) != arg0 ||
		ATgetArgument(cur, 1) != arg1))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(4);
    cur->header = header;
    ATgetArgument(cur, 0) = arg0;
    ATgetArgument(cur, 1) = arg1;
    cur->next = hashtable[hnr];
    hashtable[hnr] = cur;
  }

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
  header_type header;

  unsigned int hnr = (((unsigned int)sym) >> 2) ^ (int)arg0 ^ ((int)arg1<<1) ^
    ((int)arg2<<2);
  hnr %= table_size;
  
  CHECK_ARITY(ATgetArity(sym), 3);

  header = APPL_HEADER(0, 3, sym);
  cur = hashtable[hnr];
  while(cur && (cur->header != header ||
		ATgetArgument(cur, 0) != arg0 ||
		ATgetArgument(cur, 1) != arg1 ||
		ATgetArgument(cur, 2) != arg2))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(5);
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
  header_type header;

  unsigned int hnr = (((unsigned int)sym) >> 2) ^ (int)arg0 ^ ((int)arg1<<1) ^ 
    ((int)arg2<<2) ^ ((int)arg3<<3);
  hnr %= table_size;
  
  CHECK_ARITY(ATgetArity(sym), 4);

  header = APPL_HEADER(0, 4, sym);
  cur = hashtable[hnr];
  while(cur && (cur->header != header ||
		ATgetArgument(cur, 0) != arg0 ||
		ATgetArgument(cur, 1) != arg1 ||
		ATgetArgument(cur, 2) != arg2 ||
		ATgetArgument(cur, 3) != arg3))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(6);
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
  header_type header;

  unsigned int hnr = (((unsigned int)sym) >> 2) ^ (int)arg0 ^ ((int)arg1<<1) ^ 
    ((int)arg2<<2) ^ ((int)arg3<<3) ^ ((int)arg4<<4);
  hnr %= table_size;
  
  CHECK_ARITY(ATgetArity(sym), 5);

  header = APPL_HEADER(0, 5, sym);
  cur = hashtable[hnr];
  while(cur && (cur->header != header ||
		ATgetArgument(cur, 0) != arg0 ||
		ATgetArgument(cur, 1) != arg1 ||
		ATgetArgument(cur, 2) != arg2 ||
		ATgetArgument(cur, 3) != arg3 ||
		ATgetArgument(cur, 4) != arg4))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(7);
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
  header_type header;

  unsigned int hnr = (((unsigned int)sym) >> 2) ^ (int)arg0 ^ ((int)arg1<<1) ^ 
    ((int)arg2<<2) ^ ((int)arg3<<3) ^ ((int)arg4<<4) ^ ((int)arg5<<5);
  hnr %= table_size;
  
  CHECK_ARITY(ATgetArity(sym), 6);

  header = APPL_HEADER(0, 6, sym);
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
    cur = AT_allocate(8);
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

/*{{{  ATermInt ATmakeInt(int val) */

/**
  * Create an ATermInt
  */

ATermInt ATmakeInt(int val)
{
  ATerm cur;
  header_type header;

  unsigned int hnr = val % table_size;
 
  header = INT_HEADER(0);
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
  header_type header;

  unsigned int hnr = ((int) val) % table_size;
 
  header = REAL_HEADER(0);
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
