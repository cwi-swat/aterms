
/**
 * symbol.c
 */

#include <ctype.h>
#include <stdlib.h>
#include "aterm2.h"
#include "symbol.h"
#include "memory.h"
#include "util.h"
#include "debug.h"

#define SYMBOL_HASH_SIZE	16411	/* nextprime(16384) */
#define SYMBOL_HASH_OPT		"-symboltable"

#define SYM_ARITY	16

struct Symbol
{
  unsigned char  header;
  unsigned char  dummy;		/* fill to short-alignment */
  unsigned short arity;
  Symbol *next;
  char   *name;
};

static unsigned int table_size = 0;
static Symbol **hash_table = NULL;

static int first_free = -1;
static Symbol **lookup_table = NULL;

extern char *strdup(const char *s);

/*{{{  void AT_initSymbol(int argc, char *argv[]) */
void AT_initSymbol(int argc, char *argv[])
{
	int i;

	table_size = SYMBOL_HASH_SIZE;

	for (i = 1; i < argc; i++)
		if (streq(argv[i], SYMBOL_HASH_OPT))
			table_size = atoi(argv[++i]);

	hash_table = (Symbol **) calloc(table_size, sizeof(Symbol *));
	if (hash_table == NULL)
		ATerror("AT_initSymbol: cannot allocate %d hash-entries.\n",
		        table_size);

	lookup_table = (Symbol **) calloc(table_size, sizeof(Symbol *));
	if (lookup_table == NULL)
		ATerror("AT_initSymbol: cannot allocate %d lookup-entries.\n",
		        table_size);

	for (i = first_free = 0; i < table_size;)
		lookup_table[i] = (Symbol *) ++i;
	lookup_table[i-1] = (Symbol *) -1;			/* Sentinel */
}
/*}}}  */

/*{{{  void AT_printSymbol(Symbol *sym, FILE *f); */
void AT_printSymbol(Symbol *sym, FILE *f)
{
	if (IS_QUOTED(sym->header))
		fprintf(f, "\"%s\":%d\n", ATgetName(sym), ATgetArity(sym));
	else
		fprintf(f, "%s:%d\n", ATgetName(sym), ATgetArity(sym));
}
/*}}}  */

/*{{{  Symbol *ATmakeSymbol(char *name, int arity, ATbool quoted) */
Symbol *ATmakeSymbol(char *name, int arity, ATbool quoted)
{
	header_type   mask = (quoted ? MASK_QUOTED : 0);
	unsigned int  hash_val;
	char         *walk = name;
	Symbol       *cur;

	for(hash_val = arity*3; *walk; walk++)
		hash_val = 251 * hash_val + *walk;
	hash_val %= table_size;


	/* Find symbol in table */
	cur = hash_table[hash_val];
	while (cur && (cur->arity != arity || !streq(cur->name, name) ||
		           IS_QUOTED(cur->header) != mask))
		cur = cur->next;

	if (cur == NULL)
	{
		cur = (Symbol *) AT_allocate(sizeof(Symbol)/sizeof(header_type));
		cur->header = (quoted ? MASK_QUOTED : 0);
		cur->arity = arity;
		cur->next = hash_table[hash_val];
		cur->name = strdup(name);
		if (cur->name == NULL)
			ATerror("ATmakeSymbol: no room for name of length %d\n",
				    strlen(name));
		hash_table[hash_val] = cur;
	}

	return cur;
}

/*}}}  */
