
/**
  * symbol.h
  */

#ifndef SYMBOL_H
#define SYMBOL_H

#include "abool.h"

typedef int Symbol;

void AT_initSymbol(int argc, char *argv[]);
void AT_printSymbol(Symbol sym, FILE *f);
ATbool AT_isValidSymbol(Symbol sym);
void AT_markSymbol(Symbol sym);
void AT_unmarkSymbol(Symbol sym);
ATbool AT_isMarkedSymbol(Symbol sym);

#endif
