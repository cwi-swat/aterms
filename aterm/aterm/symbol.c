
/**
 * symbol.c
 */

/*{{{  includes */

#include <ctype.h>
#include <stdlib.h>
#include <assert.h>
#include "aterm2.h"
#include "symbol.h"
#include "memory.h"
#include "util.h"
#include "debug.h"

/*}}}  */
/*{{{  defines */

#define SYMBOL_HASH_SIZE	16411	/* nextprime(16384) */
#define SYMBOL_HASH_OPT		"-symboltable"

#define SYM_ARITY	16

#define SHIFT_INDEX 1
#define SYM_GET_NEXT_FREE(sym)    ((sym) >> SHIFT_INDEX)
#define SYM_SET_NEXT_FREE(next)   (1 | ((next) << SHIFT_INDEX))
#define SYM_IS_FREE(sym)          (((sym) & 1) == 1)

/*}}}  */
/*{{{  types */

typedef struct SymEntry
{
  unsigned char  header;
  unsigned char  dummy;		/* fill to short-alignment */
  unsigned short arity;
  struct SymEntry *next;
  Symbol  id;
  char   *name;
} *SymEntry;

/*}}}  */
/*{{{  globals */

static unsigned int table_size = 0;
static SymEntry *hash_table = NULL;

static Symbol first_free = -1;
static SymEntry *lookup_table = NULL;

/*}}}  */
/*{{{  function declarations */

extern char *strdup(const char *s);

/*}}}  */

/*{{{  void AT_initSymbol(int argc, char *argv[]) */
void AT_initSymbol(int argc, char *argv[])
{
	int i;

	table_size = SYMBOL_HASH_SIZE;

	for (i = 1; i < argc; i++)
		if (streq(argv[i], SYMBOL_HASH_OPT))
			table_size = atoi(argv[++i]);

	DBG_MEM(fprintf(stderr, "initial symbol table size = %d\n", table_size));

	hash_table = (SymEntry *) calloc(table_size, sizeof(SymEntry));
	if (hash_table == NULL)
		ATerror("AT_initSymbol: cannot allocate %d hash-entries.\n",
		        table_size);

	lookup_table = (SymEntry *) calloc(table_size, sizeof(SymEntry));
	if (lookup_table == NULL)
		ATerror("AT_initSymbol: cannot allocate %d lookup-entries.\n",
		        table_size);

	for (i = first_free = 0; i < table_size;)
		lookup_table[i] = (SymEntry) SYM_SET_NEXT_FREE(++i);
	lookup_table[i-1] = (SymEntry) SYM_SET_NEXT_FREE(-1);		/* Sentinel */
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
	   id++;
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

/*{{{  Symbol ATmakeSymbol(char *name, int arity, ATbool quoted) */

Symbol ATmakeSymbol(char *name, int arity, ATbool quoted)
{
  header_type   mask = (quoted ? MASK_QUOTED : 0);
  unsigned int  hash_val;
  char         *walk = name;
  SymEntry      cur;
  
  for(hash_val = arity*3; *walk; walk++)
    hash_val = 251 * hash_val + *walk;
  hash_val %= table_size;
  
  /* Find symbol in table */
  cur = hash_table[hash_val];
  while (cur && (cur->arity != arity || !streq(cur->name, name) ||
		 IS_QUOTED(cur->header) != mask))
    cur = cur->next;
  
  if (cur == NULL) {
    Symbol free_entry;

    cur = (SymEntry) AT_allocate(sizeof(struct SymEntry)/sizeof(header_type));
    cur->header = mask;
    cur->arity = arity;
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
/*{{{  char *ATgetName(Symbol sym) */

/**
  * Retrieve the name of a symbol
  */

char *ATgetName(Symbol sym)
{
  return lookup_table[sym]->name;
}

/*}}}  */
/*{{{  int ATgetArity(Symbol sym) */

/**
  * Retrieve the arity of a symbol
  */

int ATgetArity(Symbol sym)
{
  return lookup_table[sym]->arity;
}

/*}}}  */
/*{{{  ATbool ATisQuoted(Symbol sym) */

/**
  * Check if a symbol needs quotes.
  */

ATbool ATisQuoted(Symbol sym)
{
  return IS_QUOTED(lookup_table[sym]->header);
}

/*}}}  */
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

void AT_markSymbol(Symbol s)
{
  lookup_table[s]->header |= MASK_MARK;
}

/*}}}  */

