
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
#include "tbool.h"

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
ATermInt *TmakeInt(int value);
int       TgetInt(ATermInt *term);

/* The ATermReal type */
ATermInt *TmakeReal(double value);
double    TgetReal(ATermReal *term);

/* The ATermAppl type */
ATermAppl *TmakeAppl(Symbol *sym, ...);
ATermAppl *TmakeAppl0(Symbol *sym);
ATermAppl *TmakeAppl1(Symbol *sym, ATerm *arg0);
ATermAppl *TmakeAppl2(Symbol *sym, ATerm *arg0, ATerm *arg1);
ATermAppl *TmakeAppl3(Symbol *sym, ATerm *arg0, ATerm *arg1, ATerm *arg2);
Symbol    *TgetSymbol(ATermAppl *appl);
ATerm     *TgetArgument(ATermAppl *appl, int arg);

/* The ATermList type */
ATermList *TmakeList(ATerms *terms);
ATerms    *TgetTerms(ATermList *list);

/* The ATerms type */
ATerms *TmakeTerms(ATerm *head, ATerms *tail);
ATerms *TmakeTerms0();
ATerms *TmakeTerms1(ATerm *el0);
ATerms *TmakeTerms2(ATerm *el0, ATerm *el1);
ATerms *TmakeTerms3(ATerm *el0, ATerm *el1, ATerm *el2);
ATerms *TmakeTerms4(ATerm *el0, ATerm *el1, ATerm *el2, ATerm *el3);
ATerms *TmakeTerms5(ATerm *el0, ATerm *el1, ATerm *el2, ATerm *el3, 
		    ATerm *el4);
ATerms *TmakeTerms6(ATerm *el0, ATerm *el1, ATerm *el2, ATerm *el3, 
		    ATerm *el4, ATerm *el5);
ATerms *TmakeTerms7(ATerm *el0, ATerm *el1, ATerm *el2, ATerm *el3, 
		    ATerm *el4, ATerm *el5, ATerm *el6);
int     Tlength(ATerms *l);
ATerm  *Tfirst(ATerms *l);
ATerms *Tnext(ATerms *l);
ATerms *Tprefix(ATerms *l);
ATerm  *Tlast(ATerms *l);
ATerms *Tslice(ATerms *l, int start, int end);
Tbool  *TisEmpty(ATerms *l);
ATerms *Tappend(ATerms *l, ATerm *el);
ATerms *Tconcat(ATerms *l1, ATerms *l2);
int     Tsearch(ATerms *l, ATerm *el, int start);
int     Trsearch(ATerms *l, ATerm *el, int start);
ATerm   Tindex(ATerms *l, int index);

/* The ATermPlaceholder type */
ATermPlaceholder *TmakePlaceholder(ATerm *type);
ATerm            *TplaceholderType(ATermPlaceholder *ph);

/* The ATermBlob type */
ATermBlob *makeBlob(void *data, int size, int flags);
int     TgetBlobSize(ATermBlob *blob);
void    TgetBlobData(ATermBlob *blob);
void    TsetBlobDestructor(void (*destructor)(ATermBlob *));

#endif
