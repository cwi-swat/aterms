
/**
 * symbol.c
 */

/*{{{  includes */

#include <ctype.h>
#include <stdlib.h>
#include <assert.h>
#include "aterm2.h"
#include "afun.h"
#include "memory.h"
#include "util.h"
#include "debug.h"

/*}}}  */
/*{{{  defines */

#define SYMBOL_HASH_SIZE	65353	/* nextprime(65335) */
#define SYMBOL_HASH_OPT		"-symboltable"

#define SYM_ARITY	16

#define SHIFT_INDEX 1
#define SYM_GET_NEXT_FREE(sym)    ((sym) >> SHIFT_INDEX)
#define SYM_SET_NEXT_FREE(next)   (1 | ((next) << SHIFT_INDEX))
#define SYM_IS_FREE(sym)          (((sym) & 1) == 1)

#define INITIAL_PROTECTED_SYMBOLS   1024
#define SYM_PROTECT_EXPAND_SIZE     1024

/*}}}  */
/*{{{  globals */

char afun_id[] = "$Id$";

static unsigned int table_size = 0;
static SymEntry *hash_table = NULL;

static Symbol first_free = -1;

static Symbol *protected_symbols = NULL;
static int nr_protected_symbols  = 0;
static int max_protected_symbols  = 0;

/* Efficiency hack: was static */
SymEntry *lookup_table = NULL;
ATerm    *lookup_table_alias = NULL;

/*}}}  */
/*{{{  function declarations */

extern char *strdup(const char *s);

/*}}}  */

/*{{{  void AT_initSymbol(int argc, char *argv[]) */
void AT_initSymbol(int argc, char *argv[])
{
	int i;

	table_size = SYMBOL_HASH_SIZE;

	for (i = 1; i < argc; i++) {
		if (streq(argv[i], SYMBOL_HASH_OPT))
			table_size = atoi(argv[++i]);
		else if(strcmp(argv[i], "-help") == 0) {
			fprintf(stderr, "    %-20s: initial symboltable size " 
					"(default=%d)\n",	SYMBOL_HASH_OPT " <size>", table_size);
		}
	}

	hash_table = (SymEntry *) calloc(table_size, sizeof(SymEntry));
	if (hash_table == NULL)
		ATerror("AT_initSymbol: cannot allocate %d hash-entries.\n",
		        table_size);

	lookup_table = (SymEntry *) calloc(table_size, sizeof(SymEntry));
	lookup_table_alias = (ATerm *)lookup_table;
	if (lookup_table == NULL)
		ATerror("AT_initSymbol: cannot allocate %d lookup-entries.\n",
		        table_size);

	for (i = first_free = 0; i < table_size; i++)
		lookup_table[i] = (SymEntry) SYM_SET_NEXT_FREE(i+1);

	lookup_table[i-1] = (SymEntry) SYM_SET_NEXT_FREE(-1);		/* Sentinel */

	protected_symbols = (Symbol *)calloc(INITIAL_PROTECTED_SYMBOLS, 
																			 sizeof(Symbol));
	if(!protected_symbols)
		ATerror("AT_initSymbol: cannot allocate initial protection buffer.\n");
}
/*}}}  */

/*{{{  void AT_printSymbol(Symbol sym, FILE *f) */

/**
  * Print a symbol.
  */

void AT_printSymbol(Symbol sym, FILE *f)
{
  SymEntry entry = lookup_table[sym];
  char *id = entry->name;

  if (IS_QUOTED(entry->header)) {
    /* This symbol needs quotes */
    fputc('"', f);
    while(*id) {
      /* We need to escape special characters */
      switch(*id) {
				case '\\':
				case '"':
					fputc('\\', f);
					fputc(*id, f);
					break;
				case '\n':
					fputc('\\', f);
					fputc('n', f);
					break;
				case '\t':
					fputc('\\', f);
					fputc('t', f);
					break;
				case '\r':
					fputc('\\', f);
					fputc('r', f);
					break;
				default:
					fputc(*id, f);
      }
      id++;
    }
    fputc('"', f);
  } else
    fprintf(f, "%s", id);
}
/*}}}  */

/*{{{  unsigned int AT_hashSymbol(char *name, int arity) */

/**
	* Calculate the hash value of a symbol.
	*/

unsigned int AT_hashSymbol(char *name, int arity)
{
	unsigned int hash_val;
  char *walk = name;

  for(hash_val = arity*3; *walk; walk++)
    hash_val = 251 * hash_val + *walk;
  
	return hash_val;
}


/*}}}  */

/*{{{  Symbol ATmakeSymbol(char *name, int arity, ATbool quoted) */

Symbol ATmakeSymbol(char *name, int arity, ATbool quoted)
{
  header_type   header = SYMBOL_HEADER(arity, quoted);
  unsigned int  hash_val = AT_hashSymbol(name, arity) % table_size;
  SymEntry      cur;
  
	assert(arity < MAX_ARITY);

  /* Find symbol in table */
  cur = hash_table[hash_val];
  while (cur && (cur->header != header || !streq(cur->name, name)))
    cur = cur->next;
  
  if (cur == NULL) {
    Symbol free_entry;

    cur = (SymEntry) AT_allocate(TERM_SIZE_SYMBOL);
    cur->header = header;
    cur->next = hash_table[hash_val];
    cur->name = strdup(name);
    if (cur->name == NULL)
      ATerror("ATmakeSymbol: no room for name of length %d\n",
	      strlen(name));
    hash_table[hash_val] = cur;

    free_entry = first_free;
    if(free_entry == -1)
      ATerror("AT_initSymbol: out of symbol slots!\n");

    first_free = SYM_GET_NEXT_FREE((int)lookup_table[first_free]);
    lookup_table[free_entry] = cur;
    cur->id = free_entry;
  }

  return cur->id;
}

/*}}}  */
/*{{{  void AT_freeSymbol(SymEntry sym) */

/**
	* Free a symbol
	*/

void AT_freeSymbol(SymEntry sym)
{
	unsigned int hash_val;
	ATerm t = (ATerm)sym;
	char *walk = sym->name;

	/* Assert valid name-pointer */
	assert(sym->name);

	/* Calculate hashnumber */
	for(hash_val = GET_LENGTH(sym->header)*3; *walk; walk++)
		hash_val = 251 * hash_val + *walk;
	hash_val %= table_size;
  
	/* Update hashtable */
	if (hash_table[hash_val] == sym)
		hash_table[hash_val] = sym->next;
	else
	{
		SymEntry cur, prev;
		prev = hash_table[hash_val]; 
		for (cur = prev->next; cur != sym; prev = cur, cur = cur->next)
			assert(cur != NULL);
		prev->next = cur->next;
	}
	
	/* Free symbol name */
	free(sym->name);
	sym->name = NULL;

	lookup_table[sym->id] = (SymEntry)SYM_SET_NEXT_FREE(first_free);
	first_free = sym->id;

	/* Put the node in the appropriate free list */
	t->header = FREE_HEADER;
	t->next  = at_freelist[TERM_SIZE_SYMBOL];
	at_freelist[TERM_SIZE_SYMBOL] = t;
}

/*}}}  */

#if 0
Replaced by ATgetArity macro
/*{{{  int ATgetArity(Symbol sym) */

/**
  * Retrieve the arity of a symbol
  */

int ATgetArity(Symbol sym)
{
  return GET_LENGTH(lookup_table[sym]->header);
}

/*}}}  */
#endif

/*{{{  ATbool AT_isValidSymbol(Symbol sym) */

/**
  * Check if a symbol is valid.
  */

ATbool AT_isValidSymbol(Symbol sym)
{
	return (sym >= 0 && sym < table_size
		&& !SYM_IS_FREE((int)lookup_table[sym])) ?  ATtrue : ATfalse;
}

/*}}}  */
/*{{{  void AT_markSymbol(Symbol s) */

/**
  * Mark a symbol by setting its mark bit.
  */

/* <PO> This is now a macro
void AT_markSymbol(Symbol s)
{
  lookup_table[s]->header |= MASK_MARK;
}
*/

/*}}}  */
/*{{{  void AT_unmarkSymbol(Symbol s) */

/**
  * Unmark a symbol by clearing its mark bit.
  */

/* <PO> This is now a macro
void AT_unmarkSymbol(Symbol s)
{
  lookup_table[s]->header &= ~MASK_MARK;
}
*/

/*}}}  */
/*{{{  ATbool AT_isMarkedSymbol(Symbol s) */

/**
  * Check the mark bit of a symbol.
  */

ATbool AT_isMarkedSymbol(Symbol s)
{
  return IS_MARKED(lookup_table[s]->header);
}

/*}}}  */

/*{{{  void ATprotectSymbol(Symbol sym) */

/**
	* Protect a symbol.
	*/

void ATprotectSymbol(Symbol sym)
{

	if(nr_protected_symbols >= max_protected_symbols) {
		max_protected_symbols += SYM_PROTECT_EXPAND_SIZE;
		protected_symbols = (Symbol *)realloc(protected_symbols,
													max_protected_symbols * sizeof(Symbol));
		if(!protected_symbols)
			ATerror("ATprotectSymbol: no space to hold %d protected symbols.\n",
							max_protected_symbols);
	}

	protected_symbols[nr_protected_symbols++] = sym;
}

/*}}}  */
/*{{{  void ATunprotectSymbol(Symbol sym) */

/**
	* Unprotect a symbol.
	*/

void ATunprotectSymbol(Symbol sym)
{
	int lcv;

	for(lcv = 0; lcv < nr_protected_symbols; ++lcv) {
		if(protected_symbols[lcv] == sym) {
			protected_symbols[lcv] = protected_symbols[--nr_protected_symbols];
			protected_symbols[nr_protected_symbols] = -1;
			break;
		}
	}
}

/*}}}  */
/*{{{  void AT_markProtectedSymbols() */

/**
	* Mark all symbols that were protected previously using ATprotectSymbol.
	*/

void AT_markProtectedSymbols()
{
	int lcv;

	for(lcv = 0; lcv < nr_protected_symbols; lcv++)
		SET_MARK(((ATerm)lookup_table[protected_symbols[lcv]])->header);
}

/*}}}  */

