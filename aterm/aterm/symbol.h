
/**
  * symbol.h
  */

#ifndef SYMBOL_H
#define SYMBOL_H

typedef int Symbol;

void AT_initSymbol(int argc, char *argv[]);
void AT_printSymbol(Symbol sym, FILE *f);

#endif
