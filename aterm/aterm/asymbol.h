
/**
  * asymbol.h
  */

#ifndef ASYMBOL_H
#define ASYMBOL_H

#include "abool.h"
#include "encoding.h"

typedef int Symbol;

/* The Symbol type */
typedef struct SymEntry
{
  header_type header;
  struct SymEntry *next;
  Symbol  id;
  char   *name;
} *SymEntry;

struct ATerm;
extern struct ATerm **lookup_table_alias;
extern SymEntry *lookup_table;

void AT_initSymbol(int argc, char *argv[]);
void AT_printSymbol(Symbol sym, FILE *f);
ATbool AT_isValidSymbol(Symbol sym);
/*void AT_markSymbol(Symbol sym);*/
#define AT_markSymbol(s)   (lookup_table[(s)]->header |= MASK_MARK)
/*void AT_unmarkSymbol(Symbol sym);*/
#define AT_unmarkSymbol(s) (lookup_table[(s)]->header &= ~MASK_MARK)
ATbool AT_isMarkedSymbol(Symbol sym);
void  AT_freeSymbol(SymEntry sym);

#endif
