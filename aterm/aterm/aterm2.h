
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

typedef struct ATermTable
{
  int size;
  int free_slots;
  int max_load;
  ATermList *entries;
} *ATermTable;

/** The following functions implement the operations of
  * the 'standard' ATerm interface, and should appear
  * in some form in every implementation of the ATerm
  * datatype.
  */

/* The ATermInt type */
ATermInt ATmakeInt(int value);
/*int      ATgetInt(ATermInt term);*/
#define ATgetInt(t) ((t)->value)

/* The ATermReal type */
ATermReal ATmakeReal(double value);
/*double    ATgetReal(ATermReal term);*/
#define ATgetReal(t) ((t)->value)

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
#define ATgetArgument(appl,arg) (*((ATerm *)(appl) + ARG_OFFSET + (arg)))
ATermAppl ATsetArgument(ATermAppl appl, ATerm arg, int n);

/* Portability */
ATermList ATgetArguments(ATermAppl appl);
ATermAppl ATmakeApplList(Symbol sym, ATermList args);
ATermAppl ATmakeApplArray(Symbol sym, ATerm args[]);

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

/*ATbool    ATisEmpty(ATermList list);*/
#define ATisEmpty(list) ((list) == ATempty)

ATermList ATgetPrefix(ATermList list);
ATerm     ATgetLast(ATermList list);
ATermList ATgetSlice(ATermList list, int start, int end);
ATermList ATinsert(ATermList list, ATerm el);
ATermList ATinsertAt(ATermList list, ATerm el, int index);
ATermList ATappend(ATermList list, ATerm el);
ATermList ATconcat(ATermList list1, ATermList list2);
int       ATindexOf(ATermList list, ATerm el, int start);
int       ATlastIndexOf(ATermList list, ATerm el, int start);
ATerm     ATelementAt(ATermList list, int index);
ATermList ATremoveElement(ATermList list, ATerm el);
ATermList ATremoveElementAt(ATermList list, int idx);
ATermList ATremoveAll(ATermList list, ATerm el);
ATermList ATreplace(ATermList list, ATerm el, int idx);
ATermList ATreverse(ATermList list);
ATerm     ATdictCreate();
ATerm     ATdictGet(ATerm dict, ATerm key);
ATerm     ATdictPut(ATerm dict, ATerm key, ATerm value);
ATerm     ATdictRemove(ATerm dict, ATerm key);

ATermTable ATtableCreate(int initial_size, int max_load_pct);
void       ATtableDestroy(ATermTable table);
void       ATtablePut(ATermTable table, ATerm key, ATerm value);
ATerm	   ATtableGet(ATermTable table, ATerm key);
void       ATtableRemove(ATermTable table, ATerm key);
ATermList  ATtableKeys(ATermTable table);

/* Higher order functions */
ATermList ATfilter(ATermList list, ATbool (*predicate)(ATerm));

/* The ATermPlaceholder type */
ATermPlaceholder ATmakePlaceholder(ATerm type);
/*ATerm            ATgetPlaceholder(ATermPlaceholder ph);*/
#define ATgetPlaceholder(ph) ((ph)->ph_type)

/* The ATermBlob type */
ATermBlob ATmakeBlob(int size, void *data);
/*void   *ATgetBlobData(ATermBlob blob);*/
#define ATgetBlobData(blob) ((blob)->data)
/*int     ATgetBlobSize(ATermBlob blob);*/
#define ATgetBlobSize(blob) GET_LENGTH((blob)->header)

void    ATregisterBlobDestructor(ATbool (*destructor)(ATermBlob));
void    ATunregisterBlobDestructor(ATbool (*destructor)(ATermBlob));

extern ATerm *lookup_table_alias;
extern SymEntry *lookup_table;

Symbol  ATmakeSymbol(char *name, int arity, ATbool quoted);
/*char   *ATgetName(Symbol sym);*/
#define ATgetName(sym) (lookup_table[(sym)]->name)
/*int     ATgetArity(Symbol sym);*/
#define ATgetArity(sym) GET_LENGTH(lookup_table_alias[(sym)]->header)
/*ATbool  ATisQuoted(Symbol sym);*/
#define ATisQuoted(sym) IS_QUOTED(lookup_table_alias[(sym)]->header)

void    ATprotectSymbol(Symbol sym);
void    ATunprotectSymbol(Symbol sym);

/* C specific version of ATmake */
ATerm ATvmake(const char *pat, va_list args);
ATerm ATvmakeTerm(ATerm pat, va_list args);

#endif
