/**
 *
 * make.c - creation and matching of ATerms.
 * 
 */


/*{{{  includes */

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include "aterm2.h"
#include "make.h"
#include "deprecated.h"
#include "util.h"

#include <assert.h>

/*}}}  */
/*{{{  defines */

#define TABLE_SIZE	4099	/* nextprime(4096) */
#define NR_INLINE_TERMS 6

/*}}}  */
/*{{{  types */

typedef struct
{
	char *pat;
	ATerm term;
} at_entry;

/*}}}  */
/*{{{  globals */

static Symbol symbol_int;
static Symbol symbol_str;
static Symbol symbol_real;
static Symbol symbol_appl;
static Symbol symbol_list;
static Symbol symbol_blob;
static Symbol symbol_placeholder;
static at_entry pattern_table[TABLE_SIZE];

/*}}}  */
/*{{{  function declarations */

extern char *strdup(const char *s);
static ATerm makePlaceholder(ATermPlaceholder pat, va_list *args);
static ATermAppl makeArguments(ATermAppl appl, char *name, ATbool quoted,
							   va_list *args);
static ATerm AT_vmakeTerm(ATerm pat, va_list *args);

/*}}}  */

/*{{{  void AT_initMake(int argc, char *argv[]) */
void AT_initMake(int argc, char *argv[])
{
	int	lcv;
	for (lcv=0; lcv < TABLE_SIZE; lcv++)
	{
		pattern_table[lcv].pat  = NULL;
		pattern_table[lcv].term = NULL;
	}
	symbol_int  = ATmakeSymbol("int",  0, ATfalse);
	symbol_str  = ATmakeSymbol("str",  0, ATfalse);
	symbol_real = ATmakeSymbol("real", 0, ATfalse);
	symbol_appl = ATmakeSymbol("appl", 0, ATfalse);
	symbol_list = ATmakeSymbol("list", 0, ATfalse);
	symbol_blob = ATmakeSymbol("blob", 0, ATfalse);
	symbol_placeholder = ATmakeSymbol("placeholder", 0, ATfalse);
}
/*}}}  */

/*{{{  ATerm ATmake(const char *pat, ...) */
ATerm ATmake(const char *pat, ...)
{
	ATerm t;
	va_list args;

	va_start(args, pat);
	t = ATvmake(pat, args);
	va_end(args);

	return t;
}
/*}}}  */

/*{{{  ATerm ATmakeTerm(ATerm pat, ...) */
ATerm ATmakeTerm(ATerm pat, ...)
{
	ATerm t;
	va_list args;

	va_start(args, pat);
	t = AT_vmakeTerm(pat, &args);
	va_end(args);

	return t;
}
/*}}}  */

/*{{{  ATerm ATvmake(const char *pat, va_list args) */
ATerm ATvmake(const char *pat, va_list args)
{
	unsigned int hash_val;
	char        *walk = (char *) pat;
	at_entry    *bucket;

	for(hash_val = 0; *walk; walk++)
		hash_val = 251 * hash_val + *walk;
	hash_val %= TABLE_SIZE;
	
	bucket = &(pattern_table[hash_val]);
	if (bucket->pat)
	{
		if (streq(bucket->pat, pat))
			return AT_vmakeTerm(bucket->term, &args);
		else
			free(bucket->pat);
	}
	else
		ATprotect(&(bucket->term));

	bucket->pat = strdup(pat);
	if (!bucket->pat)
		ATerror("ATvmake: no room for pattern.\n");

	bucket->term = ATreadFromString(pat);
	return AT_vmakeTerm(bucket->term, &args);
}
/*}}}  */

/*{{{  ATerm ATvmakeTerm(ATerm pat, va_list args) */
ATerm ATvmakeTerm(ATerm pat, va_list args)
{
	return AT_vmakeTerm(pat, &args);
}
/*}}}  */

/*{{{  ATerm AT_vmakeTerm(ATerm pat, va_list *args) */

static ATerm
AT_vmakeTerm(ATerm pat, va_list *args)
{
	int nr_args;
	ATermAppl appl;
	ATermList list = NULL;
	ATermList arglist = NULL;
	ATerm term;
	ATerm type;
	Symbol sym;

	switch (ATgetType(pat))
	{
		case AT_INT:
		case AT_REAL:
		case AT_BLOB:
			return pat;
		break;

		case AT_APPL:
			appl = (ATermAppl) pat;
			sym = ATgetSymbol(appl);
			return (ATerm) makeArguments(appl, ATgetName(sym), ATisQuoted(sym),
										 args);
		break;

		case AT_LIST:
			/*{{{  Handle list */
			list = (ATermList) pat;
			nr_args = ATgetLength(list);
			arglist = ATmakeList0();
			while (--nr_args > 0)
			{
				term = ATgetFirst(list);
				arglist = ATinsert(arglist, AT_vmakeTerm(term, args));
				list = ATgetNext(list);
			}
			term = ATgetFirst(list);
			if (ATgetType(term) == AT_PLACEHOLDER)
			{
				type = ATgetPlaceholder((ATermPlaceholder) term);
				if (ATgetType(type) == AT_APPL &&
					ATgetSymbol((ATermAppl)type) == symbol_list)
				{
					list = va_arg(*args, ATermList);
				}
				else
					list = ATmakeList1(AT_vmakeTerm(term, args));
			}
			while (!ATisEmpty(arglist))
			{
				 list = ATinsert(list, ATgetFirst(arglist));
				 arglist = ATgetNext(arglist);
			}

			return (ATerm) list;
			/*}}}  */
		break;

		case AT_PLACEHOLDER:
			return makePlaceholder((ATermPlaceholder)pat, args);
		break;

		default:
			ATerror("AT_vmakeTerm: illegal type %d.\n", ATgetType(pat));
			return (ATerm) NULL;
		break;
	}
}

/*}}}  */

/*{{{  static ATermAppl makeArguments(ATermAppl appl, name, quoted, *args) */
static ATermAppl
makeArguments(ATermAppl appl, char *name, ATbool quoted, va_list *args)
{
	Symbol sym = ATgetSymbol(appl);
	int nr_args = ATgetArity(sym);
	int cur;
	ATerm terms[NR_INLINE_TERMS];
	ATerm term;
	ATerm type;
	ATermList list;
	ATermList arglist;

	if(nr_args == 0)
    {
	    sym = ATmakeSymbol(name, 0, quoted);
		return ATmakeAppl0(sym);
	} else if (nr_args-- <= NR_INLINE_TERMS)
	{
		for (cur = 0; cur < nr_args; cur++)
			terms[cur] = AT_vmakeTerm(ATgetArgument(appl, cur), args);
		terms[nr_args] = ATgetArgument(appl, nr_args);
		if (ATgetType(terms[nr_args]) == AT_PLACEHOLDER)
		{
			type = ATgetPlaceholder((ATermPlaceholder)terms[nr_args]);
			if (ATgetType(type) == AT_APPL &&
				ATgetSymbol((ATermAppl)type) == symbol_list)
			{
				list = va_arg(*args, ATermList);
				for (--cur; cur >= 0; cur--)
					list = ATinsert(list, terms[cur]);
				sym = ATmakeSymbol(name, ATgetLength(list), quoted);
				return ATmakeApplList(sym, list);
			}
		}
		sym = ATmakeSymbol(name, ATgetArity(sym), quoted);
		return ATmakeApplArray(sym, terms);
	}
	term = ATgetArgument(appl, nr_args);
	if (ATgetType(term) == AT_PLACEHOLDER)
	{
		type = ATgetPlaceholder((ATermPlaceholder)term);
		if (ATgetType(type) == AT_APPL &&
			ATgetSymbol((ATermAppl)type) == symbol_list)
		{
			list = va_arg(*args, ATermList);
		}
	}
	if (list == NULL)
		list = ATmakeList1(AT_vmakeTerm(term, args));

	arglist = ATmakeList0();
	for (cur = nr_args-1; cur >= 0; --cur)
		arglist = ATinsert(arglist, AT_vmakeTerm(
					ATgetArgument(appl, cur), args));
	while (!ATisEmpty(arglist))
	{
		list = ATinsert(list, ATgetFirst(arglist));
		arglist = ATgetNext(arglist);
	}

	sym = ATmakeSymbol(name, ATgetLength(list), quoted);
	return ATmakeApplList(sym, list);

}
/*}}}  */

/*{{{  ATerm makePlaceholder(ATermPlaceholder pat, va_list *args) */

ATerm
makePlaceholder(ATermPlaceholder pat, va_list *args)
{
    ATerm type = ATgetPlaceholder(pat);
    if (ATgetType(type) == AT_APPL) 
	{
	    ATermAppl appl = (ATermAppl) type;
		Symbol sym = ATgetSymbol(appl);
		if (sym == symbol_int && ATgetArity(sym) == 0)
			return (ATerm) ATmakeInt(va_arg(*args, int));
		else if (sym == symbol_real && ATgetArity(sym) == 0)
			return (ATerm) ATmakeReal(va_arg(*args, double));
		else if (sym == symbol_blob)
			return (ATerm) ATmakeBlob(va_arg(*args, int),
									  va_arg(*args, void *));
		else if(sym == symbol_placeholder)
			return (ATerm) ATmakePlaceholder(va_arg(*args, ATerm));
		else if (streq(ATgetName(sym), "appl") || 
				 streq(ATgetName(sym), "str"))
		{
			ATbool quoted = (sym == symbol_str ? ATtrue : ATfalse);
			return (ATerm) makeArguments(appl, va_arg(*args, char *),
											 quoted, args);
		}
	}
	ATerror("makePlaceholder: illegal pattern %t\n", pat);
	return (ATerm) NULL;
}

/*}}}  */
