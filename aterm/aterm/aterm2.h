
/**
  * aterm2.h: Definition of the level 2 interface
  * of the ATerm library.
  */

#ifndef ATERM2_H
#define ATERM2_H

/**
  * The level 2 interface is a strict superset 
  * of the level 1 interface.
  */

#include "aterm1.h"
#include "asymbol.h"
#include "abool.h"

/**
  * We define some new datatypes.
  */

typedef ATerm ATermInt;
typedef ATerm ATermReal;
typedef ATerm ATermAppl;
typedef ATerm ATermList;
typedef ATerm ATerms;
typedef ATerm ATermPlaceholder;
typedef ATerm ATermBlob;

/** The following functions implement the operations of
  * the 'standard' ATerm interface, and should appear
  * in some form in every implementation of the ATerm
  * datatype.
  */

/* The ATermInt type */
ATermInt *ATmakeInt(int value);
int       ATgetInt(ATermInt *term);

/* The ATermReal type */
ATermInt *ATmakeReal(double value);
double    ATgetReal(ATermReal *term);

/* The ATermAppl type */
ATermAppl *ATmakeAppl(Symbol *sym, ...);
ATermAppl *ATmakeAppl0(Symbol *sym);
ATermAppl *ATmakeAppl1(Symbol *sym, ATerm *arg0);
ATermAppl *ATmakeAppl2(Symbol *sym, ATerm *arg0, ATerm *arg1);
ATermAppl *ATmakeAppl3(Symbol *sym, ATerm *arg0, ATerm *arg1, ATerm *arg2);
Symbol    *ATgetSymbol(ATermAppl *appl);
ATerm     *ATgetArgument(ATermAppl *appl, int arg);

/* Portability */
ATerms *ATgetArguments(ATermAppl *appl);

/* The ATermList type */
ATermList *ATmakeList(ATerms *terms);
ATerms    *ATgetTerms(ATermList *list);

/* The ATerms type */
ATerms *ATmakeTerms0();
ATerms *ATmakeTerms1(ATerm *el0);
ATerms *ATmakeTerms2(ATerm *el0, ATerm *el1);
ATerms *ATmakeTerms3(ATerm *el0, ATerm *el1, ATerm *el2);
ATerms *ATmakeTerms4(ATerm *el0, ATerm *el1, ATerm *el2, ATerm *el3);
ATerms *ATmakeTerms5(ATerm *el0, ATerm *el1, ATerm *el2, ATerm *el3, 
		    ATerm *el4);
ATerms *ATmakeTerms6(ATerm *el0, ATerm *el1, ATerm *el2, ATerm *el3, 
		    ATerm *el4, ATerm *el5);
ATerms *ATmakeTerms7(ATerm *el0, ATerm *el1, ATerm *el2, ATerm *el3, 
		    ATerm *el4, ATerm *el5, ATerm *el6);
int     ATgetLength(ATerms *terms);
ATerm  *ATgetFirst(ATerms *terms);
ATerms *ATgetNext(ATerms *terms);
ATerms *ATgetPrefix(ATerms *terms);
ATerm  *ATgetLast(ATerms *terms);
ATerms *ATgetSlice(ATerms *terms, int start, int end);
ATbool *ATisEmpty(ATerms *terms);
ATerms *ATinsert(ATerms *terms, ATerm *el);
ATerms *ATinsertAt(ATerms *terms, ATerm *el, int index);
ATerms *ATappend(ATerms *terms, ATerm *el);
ATerms *ATconcat(ATerms *terms1, ATerms *terms2);
int     ATindexOf(ATerms *terms, ATerm *el, int start);
int     ATlastIndexOf(ATerms *terms, ATerm *el, int start);
ATerm   ATelementAt(ATerms *terms, int index);

/* The ATermPlaceholder type */
ATermPlaceholder *ATmakePlaceholder(ATerm *type);
ATerm            *ATgetPlaceholder(ATermPlaceholder *ph);

/* The ATermBlob type */
ATermBlob *ATmakeBlob(void *data, int size, int flags);
void   *ATgetBlobData(ATermBlob *blob);
int     ATgetBlobSize(ATermBlob *blob);
int     ATgetBlobFlags(ATermBlob *blob);
void    ATsetBlobDestructor(void (*destructor)(ATermBlob *));

#endif
