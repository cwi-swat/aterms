
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
#include "abool.h"

/**
  * We define some new datatypes.
  */
typedef struct Symbol Symbol;

typedef struct
{
	header_type   header;
	struct ATerm *next;
	int           value;
} ATermInt;

typedef struct
{
	header_type   header;
	struct ATerm *next;
	double        value;
} ATermReal;

typedef struct
{
	header_type   header;
	struct ATerm *next;
} ATermAppl;

typedef struct ATermList
{
	header_type       header;
	struct ATerm     *next;
	struct ATerm     *head;
	struct ATermList *tail;
} ATermList;

typedef struct
{
	header_type   header;
	struct ATerm *next;
	struct ATerm *ph_type;
} ATermPlaceholder;

typedef struct
{
	header_type   header;
	struct ATerm *next;
	void         *data;
} ATermBlob;

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
ATermList *ATgetArguments(ATermAppl *appl);

/* The ATermList type */
ATermList *ATmakeList0();
ATermList *ATmakeList1(ATerm *el0);
ATermList *ATmakeList2(ATerm *el0, ATerm *el1);
ATermList *ATmakeList3(ATerm *el0, ATerm *el1, ATerm *el2);
ATermList *ATmakeList4(ATerm *el0, ATerm *el1, ATerm *el2, ATerm *el3);
ATermList *ATmakeList5(ATerm *el0, ATerm *el1, ATerm *el2, ATerm *el3, 
		       ATerm *el4);
ATermList *ATmakeList6(ATerm *el0, ATerm *el1, ATerm *el2, ATerm *el3, 
		       ATerm *el4, ATerm *el5);
ATermList *ATmakeList7(ATerm *el0, ATerm *el1, ATerm *el2, ATerm *el3, 
		       ATerm *el4, ATerm *el5, ATerm *el6);
int        ATgetLength(ATermList *list);
ATerm     *ATgetFirst(ATermList *list);
ATermList *ATgetNext(ATermList *list);
ATermList *ATgetPrefix(ATermList *list);
ATerm     *ATgetLast(ATermList *list);
ATermList *ATgetSlice(ATermList *list, int start, int end);
ATbool    *ATisEmpty(ATermList *list);
ATermList *ATinsert(ATermList *list, ATerm *el);
ATermList *ATinsertAt(ATermList *list, ATerm *el, int index);
ATermList *ATappend(ATermList *list, ATerm *el);
ATermList *ATconcat(ATermList *list1, ATermList *list2);
int        ATindexOf(ATermList *list, ATerm *el, int start);
int        ATlastIndexOf(ATermList *list, ATerm *el, int start);
ATerm      ATelementAt(ATermList *list, int index);

/* The ATermPlaceholder type */
ATermPlaceholder *ATmakePlaceholder(ATerm *type);
ATerm            *ATgetPlaceholder(ATermPlaceholder *ph);

/* The ATermBlob type */
ATermBlob *ATmakeBlob(void *data, int size, int flags);
void   *ATgetBlobData(ATermBlob *blob);
int     ATgetBlobSize(ATermBlob *blob);
int     ATgetBlobFlags(ATermBlob *blob);
void    ATsetBlobDestructor(void (*destructor)(ATermBlob *));

/* The Symbol type */
Symbol *ATmakeSymbol(char *name, int arity, ATbool quoted);

/* char   *ATgetName(Symbol *sym); */
#define	ATgetName(sym)	((sym)->name)

/* int     ATgetArity(Symbol *sym); */
#define ATgetArity(sym)	((sym)->arity)

/* ATbool  ATisQuoted(Symbol *sym); */
#define	ATisQuoted(sym)	(IS_QUOTED((sym)->header))

#endif
