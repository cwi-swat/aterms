
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

#define MAX_DESTRUCTORS     16
#define MAX_BLOCKS_PER_SIZE 1024

#define TERM_HASH_OPT       "-termtable"

#define CHECK_ARITY(ari1,ari2) DBG_ARITY(assert((ari1) == (ari2)))

#define EMPTY_HASH_NR 12347


/*}}}  */
/*{{{  globals */

static ATerm blocks[MAX_SIZE][MAX_BLOCKS_PER_SIZE];
static int nrblocks[MAX_SIZE];
static ATerm freelist[MAX_SIZE];

static int table_size;
static ATerm *hashtable;

static int destructor_count = 0;
static ATbool (*destructors[MAX_DESTRUCTORS])(ATermBlob) = { NULL };

static ATerm arg_buffer_prefix[MAX_ARITY+1];
static ATerm *arg_buffer = arg_buffer_prefix+1;

ATermList ATempty;

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

  ATempty = (ATermList)AT_allocate(4);
  ATempty->header = LIST_HEADER(0,0);
  ATempty->next = NULL;
  hashtable[EMPTY_HASH_NR % table_size] = (ATerm)ATempty;
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
/*{{{  static unsigned int hash_number1(header_type header) */

static unsigned int hash_number1(header_type header)
{
  return header % table_size;
}

/*}}}  */
/*{{{  static unsigned int hash_number2(header_type header,w0) */

static unsigned int hash_number2(header_type header, ATerm w0)
{
  unsigned int hnr = header + (((unsigned int)w0)>>2);
  return hnr % table_size;
}

/*}}}  */
/*{{{  static unsigned int hash_number3(header_type header,w0,w1) */

static unsigned int hash_number3(header_type header, ATerm w0,
				 ATerm w1)
{
  unsigned int hnr = header + (((unsigned int)w0)>>2) + (((unsigned int)w1)>>1);
  return hnr % table_size;
}

/*}}}  */
/*{{{  static unsigned int hash_number4(header_type header,w0,w1,w2) */

static unsigned int hash_number4(header_type header, ATerm w0,
				 ATerm w1, ATerm w2)
{
  unsigned int hnr = header + (((unsigned int)w0)>>2) + 
    (((unsigned int)w1)>>1) + (unsigned int)w2;
  return hnr % table_size;
}

/*}}}  */
/*{{{  static unsigned int hash_number5(header_type header,w0,w1,w2,w3) */

static unsigned int hash_number5(header_type header, ATerm w0,
				 ATerm w1, ATerm w2, 
				 ATerm w3)
{
  unsigned int hnr = header + (((unsigned int)w0)>>2) + 
    (((unsigned int)w1)>>1) + (unsigned int)w2 + (((unsigned int)w3)<<1);
  return hnr % table_size;
}

/*}}}  */
/*{{{  static unsigned int hash_number6(header_type header,w0,w1,w2,w3,w4) */

static unsigned int hash_number6(header_type header, ATerm w0,
				 ATerm w1, ATerm w2,
				 ATerm w3, ATerm w4)
{
  unsigned int hnr = header + (((unsigned int)w0)>>2) +
    (((unsigned int)w1)>>1) + (unsigned int)w2 + 
    (((unsigned int)w3)<<1) + (((unsigned int)w4)<<2);
  return hnr % table_size;
}

/*}}}  */
/*{{{  static unsigned int hash_number7(header_type header,w0,w1,w2,w3,w4,w5) */

static unsigned int hash_number7(header_type header, ATerm w0,
				 ATerm w1, ATerm w2,
				 ATerm w3, ATerm w4,
				 ATerm w5)
{
  unsigned int hnr = header + (((unsigned int)w0)>>2) + 
    (((unsigned int)w1)>>1) + (unsigned int)w2 + 
    (((unsigned int)w3)<<1) + (((unsigned int)w4)<<2) + 
    (((unsigned int)w5)<<3);
  return hnr % table_size;
}

/*}}}  */
/*{{{  static unsigged hash_number(unsigned int header, int n, ATerm w[]) */

static unsigned int hash_number(unsigned int header, int n, ATerm w[])
{
  int i;
  unsigned int hnr = header;

  for(i=0; i<n; i++)
    hnr += ((unsigned int)w[i]) << (i-2);

  hnr %= table_size;

  return hnr;
}

/*}}}  */
/*{{{  static unsigged hash_number_anno(unsigned int header, int n, w[], anno) */

static unsigned int hash_number_anno(unsigned int header, int n, ATerm w[], ATerm anno)
{
  int i;
  unsigned int hnr = header;

  for(i=0; i<n; i++)
    hnr += ((unsigned int)w[i]) << (i-2);
  hnr += ((unsigned int)anno) << (i-2);

  hnr %= table_size;

  return hnr;
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

  unsigned int hnr = (((unsigned int)sym) >> 2);

  va_list args;

  va_start(args, sym);

  for(i=0; i<arity; i++) {
    arg_buffer[i] = va_arg(args, ATerm);
    hnr = (int)arg_buffer[i] << i;
  }
  hnr %= table_size;
  va_end(args);

  header = APPL_HEADER(0, arity > 6 ? 7 : arity, sym);

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
    cur = AT_allocate(arity + (arity > 6 ? 3:2));
    cur->header = header;
    for(i=0; i<arity; i++)
      ATgetArgument(cur, i) = arg_buffer[i];
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

  unsigned int hnr = hash_number1(header);
  
  CHECK_ARITY(ATgetArity(sym), 0);

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
  header_type header = APPL_HEADER(0, 1, sym);
  unsigned int hnr = hash_number2(header, arg0);
 
  CHECK_ARITY(ATgetArity(sym), 1);
 
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
  header_type header = APPL_HEADER(0, 2, sym);
  unsigned int hnr = hash_number3(header, arg0, arg1);
  
  CHECK_ARITY(ATgetArity(sym), 2);

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
  header_type header = APPL_HEADER(0, 3, sym);
  unsigned int hnr = hash_number4(header, arg0, arg1, arg2);
  
  CHECK_ARITY(ATgetArity(sym), 3);

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
  header_type header = APPL_HEADER(0, 4, sym);
  unsigned int hnr = hash_number5(header, arg0, arg1, arg2, arg3);
  
  CHECK_ARITY(ATgetArity(sym), 4);

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
  header_type header = APPL_HEADER(0, 5, sym);
  unsigned int hnr = hash_number6(header, arg0, arg1, arg2, arg3, arg4);
  
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
  header_type header = APPL_HEADER(0, 6, sym);
  unsigned int hnr = hash_number7(header, arg0, arg1, arg2, arg3, arg4, arg5);
  
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
  header_type header = APPL_HEADER(0, arity > 6 ? 7 : arity, sym);
  unsigned int hnr;

  assert(arity == ATgetLength(args));

  for(i=0; i<arity; i++) {
    arg_buffer[i] = ATgetFirst(args);
    args = ATgetNext(args);
  }
  if(arity > 6) {
    arg_buffer_prefix[0] = (ATerm)arity;
    hnr = hash_number(header, arity+1, arg_buffer_prefix);
  } else {
    hnr = hash_number(header, arity, arg_buffer);
  }
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
    cur = AT_allocate(arity + (arity > 6 ? 3:2));
    cur->header = header;
    for(i=0; i<arity; i++)
      ATgetArgument(cur, i) = arg_buffer[i];
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
  header_type header = APPL_HEADER(0, arity > 6 ? 7 : arity, sym);

  for(i=0; i<arity; i++) {
    arg_buffer[i] = args[i];
    hnr = (int)arg_buffer[i] << i;
  }
  if(arity > 6) {
    arg_buffer_prefix[0] = (ATerm)arity;
    hnr = hash_number(header, arity+1, arg_buffer_prefix);
  } else {
    hnr = hash_number(header, arity, arg_buffer);
  }

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
    cur = AT_allocate(arity + (arity > 6 ? 3:2));
    cur->header = header;
    for(i=0; i<arity; i++)
      ATgetArgument(cur, i) = arg_buffer[i];
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
  header_type header = INT_HEADER(0);
  unsigned int hnr = hash_number2(header, (ATerm)val);
 
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
  unsigned int hnr = hash_number2(header, (ATerm)((int)val));

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
  static ATerm *elems;
  static int maxelems;

  /* See if we have enough space to store the elements */
  if(n > maxelems) {
    free(elems);
    elems = (ATerm *)malloc(n*sizeof(ATerm));
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
  unsigned int hnr = hash_number3(header, el, (ATerm)ATempty);

  cur = hashtable[hnr];
  while(cur && (cur->header != header || 
		ATgetFirst((ATermList)cur) != el ||
		ATgetNext((ATermList)cur) != ATempty))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(4);
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

  unsigned int hnr = hash_number3(header, el, (ATerm)tail);

  cur = hashtable[hnr];
  while(cur && (cur->header != header || 
		ATgetFirst((ATermList)cur) != el || 
		ATgetNext((ATermList)cur) != tail))
    cur = cur->next;

  if(!cur) {
    cur = AT_allocate(4);
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

  unsigned int hnr = hash_number2(header, type);

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

ATermBlob ATmakeBlob(void *data, int size)
{
  ATerm cur;
  header_type header = BLOB_HEADER(0, size);

  unsigned int hnr = hash_number2(header, (ATerm)data);

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
      size += (arity > 6 ? arity+1 : arity);
      break;
  }
  return size;
}

/*}}}  */
/*{{{  ATermList AT_getAnnotations(ATerm t) */

/**
  * Retrieve the annotations of a term.
  */

ATermList AT_getAnnotations(ATerm t)
{
  if(HAS_ANNO(t->header)) {
    int size = term_size(t);
    return ((ATermList *)t)[size-1];
  }
  return NULL;
}

/*}}}  */
/*{{{  ATerm AT_setAnnotations(ATerm t, ATermList annos) */

/**
  * Change the annotations of a term.
  */

ATerm AT_setAnnotations(ATerm t, ATermList annos)
{
  unsigned int hnr;
  int i, size = term_size(t);
  header_type header;
  ATbool found;
  ATerm cur;
  
  if(HAS_ANNO(t->header)) {
    header = t->header;
    size--;
  } else {
    header = SET_ANNO(t->header);
  }

  hnr = hash_number_anno(t->header, size-2, ((ATerm *)t)+2, (ATerm)annos);
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
    ((ATerm *)cur)[i] = (ATerm)annos;
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

  header = CLR_ANNO(t->header);
  size = term_size(t)-1;

  hnr = hash_number(t->header, size-2, ((ATerm *)t)+2);
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
  ATermList newannos, oldannos = AT_getAnnotations(t);

  if(!oldannos)
    oldannos = ATempty;

  newannos = ATdictSet(oldannos, label, anno);

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
  ATermList annos = AT_getAnnotations(t);
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
  ATermList newannos, oldannos = AT_getAnnotations(t);

  if(!oldannos)
    return t;

  newannos = ATdictRemove(oldannos, label);

  if(ATisEqual(newannos, oldannos))
    return t;

  return AT_setAnnotations(t, newannos);
}

/*}}}  */
