/*

    ATerm -- The ATerm (Annotated Term) library
    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
                             The  Netherlands.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/

/**
  * afun.h
  */

#ifndef AFUN_H
#define AFUN_H

#include "atypes.h"
#include "encoding.h"

#define Symbol AFun

#define AS_INT					0
#define AS_REAL					1
#define AS_BLOB					2
#define AS_PLACEHOLDER	3
#define AS_LIST					4
#define AS_EMPTY_LIST		5
#define AS_ANNOTATION		6

typedef MachineWord AFun;

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
#define SYM_IS_FREE(sym)          (((MachineWord)(sym) & 1) == 1)

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
