
/**
  * aterm.c
  */

/*{{{  includes */

#include <stdio.h>
#include <stdlib.h>
#include "aterm2.h"
#include "memory.h"
#include "symbol.h"
#include "list.h"

#include <assert.h>

/*}}}  */
/*{{{  defines */

#define DEFAULT_BUFFER_SIZE 4096
#define RESIZE_BUFFER(n) if(n > buffer_size) resize_buffer(n)

/*}}}  */
/*{{{  globals */

/* error_handler is called when a fatal error is detected */
static void (*error_handler)(const char *format, va_list args) = NULL;

/* We need a buffer for printing to strings */
static int   buffer_size = 0;
static char *buffer = NULL;

/*}}}  */

/*{{{  void ATinit(int argc, char *argv[], error, int *bottomOfStack) */

/**
  * Initialize the ATerm library.
  */

void ATinit(int argc, char *argv[], 
	   void (*error)(const char *format, va_list args), int *bottomOfStack)
{
  /* Check for reasonably sized ATerm (32 bits, 4 bytes)     */
  /* This check might break on perfectly valid architectures */
  /* that have char == 2 bytes, and sizeof(header_type) == 2 */
  assert(sizeof(header_type) == sizeof(ATerm *));
  assert(sizeof(header_type) >= 4);

  error_handler = error;

  buffer_size = DEFAULT_BUFFER_SIZE;
  buffer = (char *)malloc(DEFAULT_BUFFER_SIZE);
  if(!buffer)
    ATerror("ATinit: cannot allocate string buffer of size %d\n", 
	    DEFAULT_BUFFER_SIZE);

  AT_initMemory(argc, argv);
  AT_initSymbol(argc, argv);
  AT_initList(argc, argv);
/*  ATinitGC(argc, argv, bottomOfStack);
*/
}

/*}}}  */
/*{{{  void ATerror(const char *format, ...) */

/**
  * A fatal error was detected.
  */

void ATerror(const char *format, ...)
{
  va_list args;

  va_start(args, format);
  if(error_handler)
    error_handler(format, args);
  else {
    vfprintf(stderr, format, args);
    exit(1);
  }

  va_end(args);
}

/*}}}  */

/*{{{  static void resize_buffer(int n) */

/**
  * Resize the resident string buffer
  */

static void resize_buffer(int n)
{
  free(buffer);
  buffer_size = n;
  buffer = (char *)malloc(buffer_size);
  if(!buffer)
    ATerror("ATinit: cannot allocate string buffer of size %d\n", buffer_size);
}

/*}}}  */

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
/*{{{  ATbool ATwriteToBinaryFile(ATerm t, FILE *f) */

/**
  * Write a term to file in a compact binary format (using gel)
  */

ATbool ATwriteToBinaryFile(ATerm t, FILE *f)
{
  return ATfalse;
}

/*}}}  */
/*{{{  char *ATwriteToString(ATerm t) */

/**
  * Write a term to a string buffer.
  */

/*{{{  static int symbolTextSize(Symbol sym) */

/**
  * Calculate the size of a symbol in text format.
  */

static int symbolTextSize(Symbol sym)
{
  char *id = ATgetName(sym);
  
  if(ATisQuoted(sym)) {
    int len = 2;
    while(*id) {
      /* We need to escape special characters */
      switch(*id) {
	case '\\':
	case '"':
	case '\n':
	case '\t':
	case '\r':
	  len += 2;
	  break;
	default:
	  len++;
      }
      id++;
    }
    return len;
  } else
    return strlen(id);
}

/*}}}  */
/*{{{  static char *writeSymbolToString(Symbol sym, char *buf) */

/**
  * Write a symbol in a string buffer.
  */

static char *writeSymbolToString(Symbol sym, char *buf)
{
  char *id = ATgetName(sym);
  
  if(ATisQuoted(sym)) {
    *buf++ = '"';
    while(*id) {
      /* We need to escape special characters */
      switch(*id) {
	case '\\':
	case '"':
	  *buf++ = '\\';
	  *buf++ = *id;
	  break;
	case '\n':
	  *buf++ = '\\';
	  *buf++ = 'n';
	  break;
	case '\t':
	  *buf++ = '\\';
	  *buf++ = 't';
	  break;
	case '\r':
	  *buf++ = '\\';
	  *buf++ = 'r';
	  break;
	default:
	  *buf++ = *id;
      }
      id++;
    }
    *buf++ = '"';
    return buf;
  } else {
    strcpy(buf, id);
    return buf+strlen(buf);
  }
}

/*}}}  */
/*{{{  static char *writeToString(ATerm t, char *buf) */

static char *topWriteToString(ATerm t, char *buf);

static char *writeToString(ATerm t, char *buf)
{
  ATerm trm;
  ATermList list;
  ATermAppl appl;
  ATermBlob blob;
  Symbol sym;
  int i, size, arity;

  switch(ATgetType(t)) {
    case AT_INT:
      /*{{{  write integer */

      sprintf(buf, "%d", ATgetInt((ATermInt)t));
      buf += strlen(buf);

      /*}}}  */
      break;

    case AT_REAL:
      /*{{{  write real */

      sprintf(buf, "%f", ATgetReal((ATermReal)t));
      buf += strlen(buf);

      /*}}}  */
      break;

    case AT_APPL:
      /*{{{  write appl */

      appl = (ATermAppl)t;
      sym = ATgetSymbol(appl);
      arity = ATgetArity(sym);
      buf = writeSymbolToString(sym, buf);
      if(arity > 0) {
	*buf++ = '(';
	buf = topWriteToString(ATgetArgument(appl, i), buf);
	for(i=1; i<arity; i++) {
	  *buf++ = ',';
	  buf = topWriteToString(ATgetArgument(appl, i), buf);
	}
	*buf++ = ')';
      }

      /*}}}  */
      break;

    case AT_LIST:
      /*{{{  write list */

      list = (ATermList)t;
      if(!ATisEmpty(list))
	buf = topWriteToString(ATgetFirst(list), buf);
      list = ATgetNext(list);
      while(!ATisEmpty(list)) {
	*buf++ = ',';
	buf = topWriteToString(ATgetFirst(list), buf);
	list = ATgetNext(list);
      }

      /*}}}  */
      break;

    case AT_PLACEHOLDER:
      /*{{{  write placeholder */

      trm = ATgetPlaceholder((ATermPlaceholder)t);
      buf = topWriteToString(trm, buf);

      /*}}}  */
      break;

    case AT_BLOB:
      /*{{{  write blob */

      blob = (ATermBlob)t;
      size = ATgetBlobSize(blob);
      sprintf(buf, "%08d:", size);
      memcpy(buf+9, ATgetBlobData(blob), size);
      buf += 9+size;

      /*}}}  */
      break;
  }
  return buf;
}

static char *topWriteToString(ATerm t, char *buf)
{
  if(ATgetType(t) == AT_LIST) {
    *buf++ = '[';
    buf = writeToString(t, buf);
    *buf++ = ']';
  } else {
    buf = writeToString(t, buf);
  }
  return buf;
}

/*}}}  */
/*{{{  static int textSize(ATerm t) */

/**
  * Calculate the size of a term in text format
  */

static int topTextSize(ATerm t);

static int textSize(ATerm t)
{
  char numbuf[32];
  ATerm trm;
  ATermList list;
  ATermAppl appl;
  Symbol sym;
  int i, size, arity;

  switch(ATgetType(t)) {
    case AT_INT:
      sprintf(numbuf, "%d", ATgetInt((ATermInt)t));
      size = strlen(numbuf);
      break;

    case AT_REAL:
      sprintf(numbuf, "%f", ATgetReal((ATermReal)t));
      size = strlen(numbuf);
      break;

    case AT_APPL:
      appl = (ATermAppl)t;
      sym = ATgetSymbol(appl);
      arity = ATgetArity(sym);
      size = symbolTextSize(sym);
      for(i=0; i<arity; i++)
	size += topTextSize(ATgetArgument(appl, i));
      size += arity-1; /* Add space for the ',' characters */
      break;

    case AT_LIST:
      list = (ATermList)t;
      size += ATgetLength(list)-1; /* Add space for the ',' characters */
      while(!ATisEmpty(list)) {
	size += topTextSize(ATgetFirst(list));
	list = ATgetNext(list);
      }
      break;

    case AT_PLACEHOLDER:
      trm = ATgetPlaceholder((ATermPlaceholder)t);
      size = topTextSize(trm);
      break;

    case AT_BLOB:
      size = 9 + ATgetBlobSize((ATermBlob)t);
      break;
  }
  return size;
}

int topTextSize(ATerm t)
{
  int size = textSize(t);

  if(ATgetType(t) == AT_LIST)
    size += 2;

  return size;
}

/*}}}  */

/**
  * Write a term into its text representation.
  */

char *ATwriteToString(ATerm t)
{
  int size = topTextSize(t);
  char *end;

  RESIZE_BUFFER(size);

  end = topWriteToString(t, buffer);
  *end = '\0';
  
  return buffer;
}

/*}}}  */
