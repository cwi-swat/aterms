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
static Symbol symbol_term;
static at_entry pattern_table[TABLE_SIZE];

/*}}}  */
/*{{{  function declarations */

extern char *strdup(const char *s);
static ATerm makePlaceholder(ATermPlaceholder pat, va_list *args);
static ATermAppl makeArguments(ATermAppl appl, char *name, ATbool quoted,
							   va_list *args);
static ATerm AT_vmakeTerm(ATerm pat, va_list *args);

static ATbool AT_vmatchTerm(ATerm t, ATerm pat, va_list *args);
static ATbool matchPlaceholder(ATerm t, ATermPlaceholder pat, va_list *args);
static ATbool matchArguments(ATermAppl appl, ATermAppl applpat, va_list *args);

/*}}}  */

/*{{{  ATerm AT_getPattern(const char *pat) */

/**
  * Retrieve a pattern using hash techniques.
  */

ATerm AT_getPattern(const char *pat)
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
		return bucket->term;
	  else
		free(bucket->pat);
	}
  else
	ATprotect(&(bucket->term));
  
  bucket->pat = strdup(pat);
  if (!bucket->pat)
	ATerror("ATvmake: no room for pattern.\n");
  
  bucket->term = ATreadFromString(pat);
  return bucket->term;
}

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
	symbol_term = ATmakeSymbol("term", 0, ATfalse);
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

/**
  * Make a new term using a string pattern and a list of arguments.
  */

ATerm ATvmake(const char *pat, va_list args)
{
  return AT_vmakeTerm(AT_getPattern(pat), &args);
}

/*}}}  */
/*{{{  ATerm ATvmakeTerm(ATerm pat, va_list args) */

/**
  * Match a term pattern against a list of arguments.
  */

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
	int cur = -1;
	ATerm terms[NR_INLINE_TERMS];
	ATerm term = NULL;
	ATerm type = NULL;
	ATermList list = NULL;
	ATermList arglist = NULL;

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
		terms[nr_args] = AT_vmakeTerm(terms[nr_args], args);
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
/*{{{  static ATerm makePlaceholder(ATermPlaceholder pat, va_list *args) */

static ATerm
makePlaceholder(ATermPlaceholder pat, va_list *args)
{
    ATerm type = ATgetPlaceholder(pat);
	
	ATprintf("makePlaceholder: %t\n", pat);
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
		else if(sym == symbol_term)
		    return va_arg(*args, ATerm);
		else {
		  ATbool makeAppl = ATfalse, quoted = ATfalse;
		  char *name = ATgetName(sym);
		  

		  if(streq(name, "appl") || streq(name, "id"))
			makeAppl = ATtrue;
		  else if(streq(name, "str")) {
			makeAppl = ATtrue;
			quoted = ATtrue;
		  }
		  if(makeAppl)
			return (ATerm) makeArguments(appl, va_arg(*args, char *),
										 quoted, args);
		}
	}
	ATerror("makePlaceholder: illegal pattern %t\n", pat);
	return (ATerm) NULL;
}

/*}}}  */

/*{{{  ATbool ATvmatch(ATerm t, const char *pat, va_list args) */

/**
  * Match a string pattern against a term using a list of arguments.
  */

ATbool ATvmatch(ATerm t, const char *pat, va_list args)
{
  return AT_vmatchTerm(t, AT_getPattern(pat), &args);
}

/*}}}  */
/*{{{  ATbool ATmatch(ATerm t, const char *pat, ...) */

/**
  * Match a term against a string pattern.
  */

ATbool ATmatch(ATerm t, const char *pat, ...)
{
  ATbool result;
  va_list args;

  va_start(args, pat);
  result = ATvmatch(t, pat, args);
  va_end(args);

  return result;
}

/*}}}  */
/*{{{  ATbool ATmatchTerm(ATerm t, ATerm pat, ...) */

/**
  * Match a term against a pattern using a list of arguments.
  */

ATbool ATmatchTerm(ATerm t, ATerm pat, ...)
{
  ATbool result;
  va_list args;

  va_start(args, pat);
  result = ATvmatchTerm(t, pat, args);
  va_end(args);

  return result;
}

/*}}}  */
/*{{{  ATbool ATvmatchTerm(ATerm t, ATerm pat, va_list args) */

/**
  * Match a term against a string pattern using a list of arguments.
  */

ATbool ATvmatchTerm(ATerm t, ATerm pat, va_list args)
{
  return AT_vmatchTerm(t, pat, &args);
}

/*}}}  */
/*{{{  ATbool AT_vmatchTerm(ATerm t, ATerm pat, va_list *args) */

/**
  * Match a term against a term pattern using a list of arguments.
  */

ATbool AT_vmatchTerm(ATerm t, ATerm pat, va_list *args)
{
  ATermList listpat, list;
  Symbol sym, psym;
  int len;

  if(ATgetType(pat) == AT_PLACEHOLDER)
	return matchPlaceholder(t, (ATermPlaceholder)pat, args);

  if(ATgetType(pat) != ATgetType(t))
	return ATfalse;

  switch(ATgetType(pat)) {
	case AT_INT:
	case AT_REAL:
	case AT_BLOB:
	  return ATisEqual(t, pat);
	  break;

	case AT_APPL:
	  sym = ATgetSymbol((ATermAppl)t);
	  psym = ATgetSymbol((ATermAppl)pat);
	  if(!streq(ATgetName(sym), ATgetName(psym)) ||
		 ATisQuoted(sym) != ATisQuoted(psym))
		return ATfalse;
	  return matchArguments((ATermAppl)t, (ATermAppl)pat, args);
	  break;

	case AT_LIST:
	  /*{{{  Match a list */

	  listpat = (ATermList)pat;
	  list = (ATermList)t;
	  len = ATgetLength(listpat);
	  while(--len > 0) {
		if(ATisEmpty(list))
		  return ATfalse;

		pat     = ATgetFirst(listpat);
		t       = ATgetFirst(list);
		listpat = ATgetNext(listpat);
		list    = ATgetNext(list);

		if(!AT_vmatchTerm(t, pat, args))
		  return ATfalse;
	  }

	  /* We now reached the last element of the pattern list */
	  pat = ATgetFirst(listpat);
	  if(ATgetType(pat) == AT_PLACEHOLDER) {
		ATerm type = ATgetPlaceholder((ATermPlaceholder)pat);
		if(ATgetType(type) == AT_APPL && 
		   ATgetSymbol((ATermAppl)type) == symbol_list) {
		  ATermList *listarg = va_arg(*args, ATermList *);
		  if(listarg)
			*listarg = list;
		  return ATtrue;
		}
	  }
	  /* Last element was not a <list> pattern */
	  if(ATgetLength(list) != 1)
		return ATfalse;

	  return AT_vmatchTerm(ATgetFirst(list), pat, args);

	  /*}}}  */
	  break;

	default:
	  ATerror("AT_vmatchTerm: illegal type %d\n", ATgetType(pat));
	  return ATfalse;
	  break;
  }
}

/*}}}  */
/*{{{  static ATbool matchArguments(ATermAppl appl, applpat, va_list *args) */

/**
  * Match the arguments of a function application against a term pattern.
  */

static ATbool matchArguments(ATermAppl appl, ATermAppl applpat, 
							 va_list *args) 
{
  Symbol sym = ATgetSymbol(appl);
  Symbol psym = ATgetSymbol(applpat);
  int i, arity = ATgetArity(sym), parity = ATgetArity(psym)-1;
  ATerm pat;

  if(parity == -1)
	return arity == 0 ? ATtrue : ATfalse;

  for(i=0; i<parity; i++) {
	if(!AT_vmatchTerm(ATgetArgument(appl,i), ATgetArgument(applpat,i), args))
	  return ATfalse;
  }

  /* We now reached the last argument */
  pat = ATgetArgument(applpat, parity);
  if(ATgetType(pat) == AT_PLACEHOLDER) {
	ATerm type = ATgetPlaceholder((ATermPlaceholder)pat);
	if(ATgetType(type) == AT_APPL &&
	   ATgetSymbol((ATermAppl)type) == symbol_list)
	{
	  ATermList *listarg = va_arg(*args, ATermList *);
	  if(listarg) {
		*listarg = ATmakeList0();
		for(i=arity-1; i>parity; i--) {
		  *listarg = ATinsert(*listarg, ATgetArgument(appl, i));
		}
	  }
	  return ATtrue;
	}
  }

  /* Last pattern was not '<list>' */
  if((arity-1) != parity)
	return ATfalse;

  /* Match the last argument */
  return AT_vmatchTerm(ATgetArgument(appl, parity),
					   ATgetArgument(applpat, parity), args);
}

/*}}}  */
/*{{{  static ATbool matchPlaceholder(ATerm t, pat, va_list *args) */

/**
  * Match a term against a placeholder term.
  */

static ATbool matchPlaceholder(ATerm t, ATermPlaceholder pat, va_list *args)
{
  ATerm type = ATgetPlaceholder(pat);
  ATbool matchAppl = ATfalse, matchStr = ATfalse, matchId = ATfalse;

  if(ATgetType(type) == AT_APPL) {
	ATermAppl pappl = (ATermAppl)type;
	Symbol psym = ATgetSymbol(pappl);
	if(psym == symbol_int && ATgetArity(psym) == 0) {	  
	  /*{{{  handle pattern <int> */

	  if(ATgetType(t) == AT_INT) {
		int *arg = va_arg(*args, int *);
		if(arg)
		  *arg = ATgetInt((ATermInt)t);
		return ATtrue;
	  }
	  return ATfalse;

	  /*}}}  */
	} else if(psym == symbol_real && ATgetArity(psym) == 0) {
	  /*{{{  handle pattern <real> */

	  if(ATgetType(t) == AT_REAL) {
		double *arg = va_arg(*args, double *);
		if(arg)
		  *arg = ATgetReal((ATermReal)t);
		return ATtrue;
	  }
	  return ATfalse;

	  /*}}}  */
	} else if(psym == symbol_blob) {
	  /*{{{  handle pattern <blob> */

	  if(ATgetType(t) == AT_BLOB) {
		int *size  = va_arg(*args, int *);
		void **data = va_arg(*args, void **);
		if(size)
		  *size = ATgetBlobSize((ATermBlob)t);
		if(data)
		  *data = ATgetBlobData((ATermBlob)t);
		return ATtrue;
	  }
	  return ATfalse;

	  /*}}}  */
	} else if(psym == symbol_placeholder) {
	  /*{{{  handle pattern <placeholder> */

	  if(ATgetType(t) == AT_PLACEHOLDER) {
		ATerm *type = va_arg(*args, ATerm *);
		if(type)
		  *type = ATgetPlaceholder((ATermPlaceholder)t);
		return ATtrue;
	  }
	  return ATfalse;

	  /*}}}  */
	} else if(psym == symbol_term) {
	  ATerm *term = va_arg(*args, ATerm *);
	  if(term)
		*term = t;
	  return ATtrue;
	} else if(streq(ATgetName(psym), "appl")) {
	  matchAppl = ATtrue;
	} else if(streq(ATgetName(psym), "str")) {
	  matchStr = ATtrue;
	  matchAppl = ATtrue;
	} else if(streq(ATgetName(psym), "id")) {
	  matchId = ATtrue;
	  matchAppl = ATtrue;
	}
	 
	if(matchAppl) {
	  /*{{{  handle patterns <appl> and <str> */
	  Symbol sym;
	  ATermAppl appl;
	  int arity, parity;
	  char **name;

	  if(ATgetType(t) != AT_APPL)
		return ATfalse;

	  appl = (ATermAppl)t;
	  sym = ATgetSymbol(appl);
	  arity = ATgetArity(sym);
	  parity = ATgetArity(psym)-1;

	  if(ATisQuoted(sym)) {
		if(matchId)
		  return ATfalse;
	  } else if(matchStr) {
		return ATfalse;
	  }

	  name = va_arg(*args, char **);
	  if(name)
		*name = ATgetName(sym);

	  return matchArguments(appl, pappl, args);

	  /*}}}  */
	}
  }
  ATerror("matchPlaceholder: illegal pattern %t\n", pat);
  return ATfalse;
}

/*}}}  */
