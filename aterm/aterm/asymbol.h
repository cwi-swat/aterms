
/**
  * asymbol.h: Definition of ATerm datatype Symbol
  */

#ifndef ASYMBOL_H
#define ASYMBOL_H

#include "tbool.h"

typedef struct Symbol {
  char *name;
  int   arity;
  Tbool quoted;
} Symbol;

/* The Symbol type */
Symbol *TmakeSymbol(char *name, int arity, Tbool quoted);
char   *TgetName(Symbol *sym);
int     TgetArity(Symbol *sym);
Tbool   TisQuoted(Symbol *sym);

#endif
