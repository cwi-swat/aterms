
/**
  * aterm.c
  */

#include <stdio.h>
#include "aterm2.h"

/*{{{  ATbool ATwriteToTextFile(ATerm t, FILE *f) */

/**
  * Write a term in text format to file.
  */

ATbool ATwriteToTextFile(ATerm t, FILE *f)
{
  Symbol sym;
  ATerm arg;
  int i, arity;
  ATermAppl appl;

  switch(ATgetType(t)) {
    case AT_APPL:
      /*{{{  Print application */
      appl = (ATermAppl)t;

      sym = ATgetSymbol(appl);
      AT_printSymbol(sym, f);
      arity = ATgetArity(sym);
      if(arity > 0) {
	fprintf(f, "(");
	for(i=0; i<arity; i++) {
	  if(i != 0)
	    fprintf(f, ",");
	  arg = ATgetArgument(appl, i);
	  if(ATgetType(arg) == AT_LIST) {
	    fprintf(f, "[");
	    ATwriteToTextFile(arg, f);
	    fprintf(f, "]");
	  } else {
	    ATwriteToTextFile(arg, f);
	  }
	}
	fprintf(f, ")");
      }

      /*}}}  */
      break;

    default:
      ATerror("ATwriteToTextFile: type %d not implemented.", ATgetType(t));
      return ATfalse;  
  }
  return ATtrue;
}

/*}}}  */
