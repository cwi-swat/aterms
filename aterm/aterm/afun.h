
/**
  * afun.h
  */

#ifndef AFUN_H
#define AFUN_H

#include "abool.h"
#include "encoding.h"

#define Symbol AFun

#define AS_INT					0
#define AS_REAL					1
#define AS_BLOB					2
#define AS_PLACEHOLDER	3
#define AS_LIST					4
#define AS_EMPTY_LIST		5
#define AS_ANNOTATION		6

typedef int AFun;

/* The Symbol type */
typedef struct SymEntry
{
  header_type header;
  struct SymEntry *next;
  Symbol  id;
  char   *name;
  int     count;  /* used in bafio.c */
  int     index;  /* used in bafio.c */
} *SymEntry;

/* defined on SymEntry */
#define SYM_IS_FREE(sym)          (((int)(sym) & 1) == 1)

struct ATerm;
extern struct ATerm **at_lookup_table_alias;
extern SymEntry *at_lookup_table;

unsigned int AT_symbolTableSize();
void AT_initSymbol(int argc, char *argv[]);
void AT_printSymbol(Symbol sym, FILE *f);
ATbool AT_isValidSymbol(Symbol sym);
/*void AT_markSymbol(Symbol sym);*/
#define AT_markSymbol(s)   (at_lookup_table[(s)]->header |= MASK_MARK)
/*void AT_unmarkSymbol(Symbol sym);*/
#define AT_unmarkSymbol(s) (at_lookup_table[(s)]->header &= ~MASK_MARK)
ATbool AT_isMarkedSymbol(Symbol sym);
void  AT_freeSymbol(SymEntry sym);
void AT_markProtectedSymbols();
unsigned int AT_hashSymbol(char *name, int arity);
ATbool AT_findSymbol(char *name, int arity, ATbool quoted);

#endif
