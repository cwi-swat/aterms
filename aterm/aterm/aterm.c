
/**
  * aterm.c
  */

#include <stdio.h>
#include "aterm2.h"

/*{{{  ATbool ATwriteToTextFile(ATerm t, FILE *f) */

/**
  * Write a term in text format to file.
  */

ATbool writeToTextFile(ATerm t, FILE *f)
{
  Symbol sym;
  ATerm arg, trm;
  int i, arity;
  ATermAppl appl;
  ATermList list;
  ATermBlob blob;

  switch(ATgetType(t)) {
    case AT_INT:
      fprintf(f, "%d", ((ATermInt)t)->value);
      break;
    case AT_REAL:
      fprintf(f, "%f", ((ATermReal)t)->value);
      break;
    case AT_APPL:
      /*{{{  Print application */

      appl = (ATermAppl)t;

      sym = ATgetSymbol(appl);
      AT_printSymbol(sym, f);
      arity = ATgetArity(sym);
      if(arity > 0) {
	fputc('(', f);
	for(i=0; i<arity; i++) {
	  if(i != 0)
	    fputc(',', f);
	  arg = ATgetArgument(appl, i);
	  if(ATgetType(arg) == AT_LIST) {
	    fputc('[', f);
	    writeToTextFile(arg, f);
	    fputc(']', f);
	  } else {
	    writeToTextFile(arg, f);
	  }
	}
	fputc(')', f);
      }

      /*}}}  */
      break;
    case AT_LIST:
      /*{{{  Print list */

      list = (ATermList)t;
      if(ATisEmpty(list))
	break;

      trm = ATgetFirst(list);
      if(ATgetType(trm) == AT_LIST) {
	fputc('[', f);
	writeToTextFile(trm, f);
	fputc(']', f);
      } else {
	writeToTextFile(trm, f);
      }

      list = ATgetNext(list);
      if(!ATisEmpty(list)) {
	fputc(',', f);
	writeToTextFile((ATerm)list, f);
      }

      /*}}}  */
      break;
    case AT_PLACEHOLDER:
      /*{{{  Print placeholder */
      
      fputc('<', f);
      writeToTextFile(ATgetPlaceholder((ATermPlaceholder)t), f);
      fputc('>', f);

      /*}}}  */
      break;
    case AT_BLOB:
      /*{{{  Print blob */

      blob = (ATermBlob)t;
      fprintf(f, "%08d:", ATgetBlobSize(blob));
      fwrite(ATgetBlobData(blob), ATgetBlobSize(blob), 1, f);

      /*}}}  */
      break;

    default:
      ATerror("ATwriteToTextFile: type %d not implemented.", ATgetType(t));
      return ATfalse;  
  }
  return ATtrue;
}

ATbool ATwriteToTextFile(ATerm t, FILE *f)
{
  ATbool result;

  if(ATgetType(t) == AT_LIST) {
    fputc('[', f);

    if(!ATisEmpty((ATermList)t))
      result = writeToTextFile(t, f);

    fputc(']', f);
  } else {
    result = writeToTextFile(t, f);
  }
  return result;
}

/*}}}  */
