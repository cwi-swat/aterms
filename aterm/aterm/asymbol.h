
/**
  * asymbol.h: Definition of ATerm datatype Symbol
  */

#ifndef ASYMBOL_H
#define ASYMBOL_H

#include "abool.h"

typedef struct Symbol {
  char  *name;
  int    arity;
  ATbool quoted;
} Symbol;

/* The Symbol type */
Symbol *ATmakeSymbol(char *name, int arity, ATbool quoted);
char   *ATgetName(Symbol *sym);
int     ATgetArity(Symbol *sym);
ATbool  ATisQuoted(Symbol *sym);

#endif
