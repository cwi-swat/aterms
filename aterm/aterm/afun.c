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
 * symbol.c
 */

/*{{{  includes */

#include <ctype.h>
#include <stdlib.h>
#include <assert.h>
#include "aterm2.h"
#include "_afun.h"
#include "memory.h"
#include "util.h"
#include "debug.h"

/*}}}  */
/*{{{  defines */

#define SYMBOL_HASH_SIZE	65353	/* nextprime(65335) */
#define SYMBOL_HASH_OPT		"-at-symboltable"

#define SYM_ARITY	16

#define SHIFT_INDEX 1
#define SYM_GET_NEXT_FREE(sym)    ((MachineWord)(sym) >> SHIFT_INDEX)
#define SYM_SET_NEXT_FREE(next)   (1 | ((next) << SHIFT_INDEX))

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
SymEntry *at_lookup_table = NULL;
ATerm    *at_lookup_table_alias = NULL;

/*}}}  */
/*{{{  function declarations */

/* extern char *strdup(const char *s); */

/*}}}  */

unsigned int AT_symbolTableSize()
{
  return table_size;
}

/*{{{  void AT_initSymbol(int argc, char *argv[]) */
void AT_initSymbol(int argc, char *argv[])
{
  int i;
  AFun sym;

  table_size = SYMBOL_HASH_SIZE;

  for (i = 1; i < argc; i++) {
    if (streq(argv[i], SYMBOL_HASH_OPT))
      table_size = atoi(argv[++i]);
    else if(strcmp(argv[i], "-at-help") == 0) {
      fprintf(stderr, "    %-20s: initial symboltable size " 
	      "(default=%d)\n",	SYMBOL_HASH_OPT " <size>", table_size);
    }
  }

  hash_table = (SymEntry *) calloc(table_size, sizeof(SymEntry));
  if (hash_table == NULL)
    ATerror("AT_initSymbol: cannot allocate %d hash-entries.\n",
	    table_size);

  at_lookup_table = (SymEntry *) calloc(table_size, sizeof(SymEntry));
  at_lookup_table_alias = (ATerm *)at_lookup_table;
  if (at_lookup_table == NULL)
    ATerror("AT_initSymbol: cannot allocate %d lookup-entries.\n",
	    table_size);

  for (i = first_free = 0; i < table_size; i++)
    at_lookup_table[i] = (SymEntry) SYM_SET_NEXT_FREE(i+1);

  at_lookup_table[i-1] = (SymEntry) SYM_SET_NEXT_FREE(-1);		/* Sentinel */

  protected_symbols = (Symbol *)calloc(INITIAL_PROTECTED_SYMBOLS, 
				       sizeof(Symbol));
  if(!protected_symbols)
    ATerror("AT_initSymbol: cannot allocate initial protection buffer.\n");
	
  sym = ATmakeAFun("<int>", 0, ATfalse);
  assert(sym == AS_INT);
  ATprotectAFun(sym);

  sym = ATmakeAFun("<real>", 0, ATfalse);
  assert(sym == AS_REAL);
  ATprotectAFun(sym);

  sym = ATmakeAFun("<blob>", 0, ATfalse);
  assert(sym == AS_BLOB);
  ATprotectAFun(sym);

  sym = ATmakeAFun("<_>", 1, ATfalse);
  assert(sym == AS_PLACEHOLDER);
  ATprotectAFun(sym);

  sym = ATmakeAFun("[_,_]", 2, ATfalse);
  assert(sym == AS_LIST);
  ATprotectAFun(sym);

  sym = ATmakeAFun("[]", 0, ATfalse);
  assert(sym == AS_EMPTY_LIST);
  ATprotectAFun(sym);

  sym = ATmakeAFun("{_}", 2, ATfalse);
  assert(sym == AS_ANNOTATION);
  ATprotectAFun(sym);
}
/*}}}  */

/*{{{  int AT_printSymbol(Symbol sym, FILE *f) */

/**
  * Print an afun.
  */

int AT_printSymbol(AFun fun, FILE *f)
{
  SymEntry entry = at_lookup_table[fun];
  char *id = entry->name;
  int size = 0;

  if (IS_QUOTED(entry->header)) {
    /* This function symbol needs quotes */
    fputc('"', f);
    size++;
    while(*id) {
      /* We need to escape special characters */
      switch(*id) {
      case '\\':
      case '"':
	fputc('\\', f);
	fputc(*id, f);
	size += 2;
	break;
      case '\n':
	fputc('\\', f);
	fputc('n', f);
	size += 2;
	break;
      case '\t':
	fputc('\\', f);
	fputc('t', f);
	size += 2;
	break;
      case '\r':
	fputc('\\', f);
	fputc('r', f);
	size += 2;
	break;
      default:
	fputc(*id, f);
	size++;
      }
      id++;
    }
    fputc('"', f);
    size++;
  } else {
    fputs(id, f);
    size += strlen(id);
  }
  return size;
}

/*}}}  */
/*{{{  int AT_writeAFun(AFun fun, byte_writer *writer) */

/**
  * Print an afun.
  */

int AT_writeAFun(AFun fun, byte_writer *writer)
{
  SymEntry entry = at_lookup_table[fun];
  char *id = entry->name;
  int size = 0;

  if (IS_QUOTED(entry->header)) {
    /* This function symbol needs quotes */
    write_byte('"', writer);
    size++;
    while(*id) {
      /* We need to escape special characters */
      switch(*id) {
      case '\\':
      case '"':
	write_byte('\\', writer);
	write_byte(*id, writer);
	size += 2;
	break;
      case '\n':
	write_byte('\\', writer);
	write_byte('n', writer);
	size += 2;
	break;
      case '\t':
	write_byte('\\', writer);
	write_byte('t', writer);
	size += 2;
	break;
      case '\r':
	write_byte('\\', writer);
	write_byte('r', writer);
	size += 2;
	break;
      default:
	write_byte(*id, writer);
	size++;
      }
      id++;
    }
    write_byte('"', writer);
    size++;
  } else {
    size += write_bytes(id, strlen(id), writer);
  }
  return size;
}

/*}}}  */

/*{{{  ShortHashNumber AT_hashSymbol(char *name, int arity) */

/**
	* Calculate the hash value of a symbol.
	*/

ShortHashNumber AT_hashSymbol(char *name, int arity)
{
  ShortHashNumber hnr;
  char *walk = name;

  for(hnr = arity*3; *walk; walk++)
    hnr = 251 * hnr + *walk;
  
  return hnr;
}


/*}}}  */

/*{{{  Symbol ATmakeSymbol(char *name, int arity, ATbool quoted) */

Symbol ATmakeSymbol(char *name, int arity, ATbool quoted)
{
  header_type header = SYMBOL_HEADER(arity, quoted);
  ShortHashNumber hnr = AT_hashSymbol(name, arity) % table_size;
  SymEntry cur;
  
  if(arity >= MAX_ARITY)
    ATabort("cannot handle symbols with arity %d (max=%d)\n",
	    arity, MAX_ARITY);

  /* Find symbol in table */
  cur = hash_table[hnr];
  while (cur && (cur->header != header || !streq(cur->name, name)))
    cur = cur->next;
  
  if (cur == NULL) {
    Symbol free_entry;

    cur = (SymEntry) AT_allocate(TERM_SIZE_SYMBOL);
    cur->header = header;
    cur->next = hash_table[hnr];
    cur->name = strdup(name);
    if (cur->name == NULL)
      ATerror("ATmakeSymbol: no room for name of length %d\n",
	      strlen(name));

    cur->count = 0;
    cur->index = -1;

    hash_table[hnr] = cur;

    free_entry = first_free;
    if(free_entry == -1)
      ATerror("AT_initSymbol: out of symbol slots!\n");

    first_free = SYM_GET_NEXT_FREE(at_lookup_table[first_free]);
    at_lookup_table[free_entry] = cur;
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
  ShortHashNumber hnr;
  ATerm t = (ATerm)sym;
  char *walk = sym->name;
  
  /* Assert valid name-pointer */
  assert(sym->name);
  
  /* Calculate hashnumber */
  for(hnr = GET_LENGTH(sym->header)*3; *walk; walk++)
    hnr = 251 * hnr + *walk;
  hnr %= table_size;
  
  /* Update hashtable */
  if (hash_table[hnr] == sym)
    hash_table[hnr] = sym->next;
  else
    {
      SymEntry cur, prev;
      prev = hash_table[hnr]; 
      for (cur = prev->next; cur != sym; prev = cur, cur = cur->next)
	assert(cur != NULL);
      prev->next = cur->next;
    }
  
  /* Free symbol name */
  free(sym->name);
  sym->name = NULL;
  
  at_lookup_table[sym->id] = (SymEntry)SYM_SET_NEXT_FREE(first_free);
  first_free = sym->id;
  
  /* Put the node in the appropriate free list */
  t->header = FREE_HEADER;
  t->next  = at_freelist[TERM_SIZE_SYMBOL];
  at_freelist[TERM_SIZE_SYMBOL] = t;
}

/*}}}  */
/*{{{  ATbool AT_findSymbol(char *name, int arity, ATbool quoted) */

/**
	* Check for the existence of a symbol
	*/

ATbool AT_findSymbol(char *name, int arity, ATbool quoted)
{
  header_type header = SYMBOL_HEADER(arity, quoted);
  ShortHashNumber hnr = AT_hashSymbol(name, arity) % table_size;
  SymEntry cur;
  
  if(arity >= MAX_ARITY)
    ATabort("cannot handle symbols with arity %d (max=%d)\n",
	    arity, MAX_ARITY);

  /* Find symbol in table */
  cur = hash_table[hnr];
  while (cur && (cur->header != header || !streq(cur->name, name)))
    cur = cur->next;
  
  return (cur == NULL) ? ATfalse : ATtrue;
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
  return GET_LENGTH(at_lookup_table[sym]->header);
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
	  && !SYM_IS_FREE(at_lookup_table[sym])) ?  ATtrue : ATfalse;
}

/*}}}  */
/*{{{  void AT_markSymbol(Symbol s) */

/**
  * Mark a symbol by setting its mark bit.
  */

/* <PO> This is now a macro
void AT_markSymbol(Symbol s)
{
  at_lookup_table[s]->header |= MASK_MARK;
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
  at_lookup_table[s]->header &= ~MASK_MARK;
}
*/

/*}}}  */
/*{{{  ATbool AT_isMarkedSymbol(Symbol s) */

/**
  * Check the mark bit of a symbol.
  */

ATbool AT_isMarkedSymbol(Symbol s)
{
  return IS_MARKED(at_lookup_table[s]->header);
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

  for(lcv = 0; lcv < nr_protected_symbols; lcv++) {
    SET_MARK(((ATerm)at_lookup_table[protected_symbols[lcv]])->header);
  }
}

/*}}}  */

