
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
#include "symbol.h"
#include "abool.h"

/**
  * We define some new datatypes.
  */

typedef struct
{
	header_type header;
	ATerm       next;
	int         value;
} *ATermInt;

typedef struct
{
	header_type header;
	ATerm       next;
	double      value;
} *ATermReal;

typedef struct
{
	header_type header;
	ATerm       next;
} *ATermAppl;

typedef struct ATermList
{
	header_type header;
	ATerm       next;
	ATerm       head;
	struct ATermList *tail;
} *ATermList;

typedef struct
{
	header_type header;
	ATerm       next;
	ATerm       ph_type;
} *ATermPlaceholder;

typedef struct
{
	header_type header;
	ATerm       next;
	void       *data;
} *ATermBlob;

/** The following functions implement the operations of
  * the 'standard' ATerm interface, and should appear
  * in some form in every implementation of the ATerm
  * datatype.
  */

/* The ATermInt type */
ATermInt ATmakeInt(int value);
int      ATgetInt(ATermInt term);

/* The ATermReal type */
ATermReal ATmakeReal(double value);
double    ATgetReal(ATermReal term);

/* The ATermAppl type */
ATermAppl ATmakeAppl(Symbol sym, ...);
ATermAppl ATmakeAppl0(Symbol sym);
ATermAppl ATmakeAppl1(Symbol sym, ATerm arg0);
ATermAppl ATmakeAppl2(Symbol sym, ATerm arg0, ATerm arg1);
ATermAppl ATmakeAppl3(Symbol sym, ATerm arg0, ATerm arg1, ATerm arg2);
ATermAppl ATmakeAppl4(Symbol sym, ATerm arg0, ATerm arg1, ATerm arg2,
		       ATerm arg3);
ATermAppl ATmakeAppl5(Symbol sym, ATerm arg0, ATerm arg1, ATerm arg2,
		       ATerm arg4, ATerm arg5);
ATermAppl ATmakeAppl6(Symbol sym, ATerm arg0, ATerm arg1, ATerm arg2,
		       ATerm arg4, ATerm arg5, ATerm arg6);

/*Symbol    ATgetSymbol(ATermAppl appl);*/
#define ATgetSymbol(appl) GET_SYMBOL((appl)->header)

/* ATerm     ATgetArgument(ATermAppl appl, int arg); */
#define ATgetArgument(appl,arg) (*((ATerm *)(appl)+2+(arg)))

/* Portability */
ATermList ATgetArguments(ATermAppl appl);

/* The ATermList type */
extern ATermList ATempty;

ATermList ATmakeList(int n, ...);

/* ATermList ATmakeList0(); */
#define ATmakeList0() (ATempty)

ATermList ATmakeList1(ATerm el0);

/* ATermList ATmakeList2(ATerm el0, ATerm el1); */
#define ATmakeList2(el0, el1)           ATinsert(ATmakeList1(el1), el0)
#define ATmakeList3(el0, el1, el2)      ATinsert(ATmakeList2(el1,el2), el0)
#define ATmakeList4(el0, el1, el2, el3) ATinsert(ATmakeList3(el1,el2,el3), el0)
#define ATmakeList5(el0, el1, el2, el3, el4) \
                ATinsert(ATmakeList4(el1,el2,el3,el4), el0)
#define ATmakeList6(el0, el1, el2, el3, el4, el5) \
                ATinsert(ATmakeList5(el1,el2,el3,el4,el5), el0)

/*
int       ATgetLength(ATermList list);*/
#define   ATgetLength(l) GET_LENGTH((l)->header)

/*
ATerm     ATgetFirst(ATermList list);*/
#define   ATgetFirst(l) ((l)->head)

/*
ATermList ATgetNext(ATermList list);*/
#define   ATgetNext(l)  ((l)->tail)

ATermList ATgetPrefix(ATermList list);
ATerm     ATgetLast(ATermList list);
ATermList ATgetSlice(ATermList list, int start, int end);

/*ATbool    ATisEmpty(ATermList list);*/
#define ATisEmpty(list) ((list) == ATempty)

ATermList ATinsert(ATermList list, ATerm el);
ATermList ATinsertAt(ATermList list, ATerm el, int index);
ATermList ATappend(ATermList list, ATerm el);
ATermList ATconcat(ATermList list1, ATermList list2);
int       ATindexOf(ATermList list, ATerm el, int start);
int       ATlastIndexOf(ATermList list, ATerm el, int start);
ATerm     ATelementAt(ATermList list, int index);

/* The ATermPlaceholder type */
ATermPlaceholder ATmakePlaceholder(ATerm type);
/*ATerm            ATgetPlaceholder(ATermPlaceholder ph);*/
#define ATgetPlaceholder(ph) ((ph)->ph_type)

/* The ATermBlob type */
ATermBlob ATmakeBlob(void *data, int size);
/*void   *ATgetBlobData(ATermBlob blob);*/
#define ATgetBlobData(blob) ((blob)->data)
/*int     ATgetBlobSize(ATermBlob blob);*/
#define ATgetBlobSize(blob) GET_LENGTH((blob)->header)

void    ATregisterBlobDestructor(ATbool (*destructor)(ATermBlob));
void    ATunregisterBlobDestructor(ATbool (*destructor)(ATermBlob));

/* The Symbol type */
Symbol  ATmakeSymbol(char *name, int arity, ATbool quoted);
char   *ATgetName(Symbol sym);
int     ATgetArity(Symbol sym);
ATbool  ATisQuoted(Symbol sym);

#endif
