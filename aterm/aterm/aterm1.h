
/**
 * aterm1.h: Definition of the level 1 interface
 * of the ATerm library.
 */

#ifndef ATERM1_H
#define ATERM1_H

#include <stdio.h>
#include <stdarg.h>
#include "encoding.h"
#include "abool.h"

#define	AT_FREE         0
#define AT_INT          1 
#define AT_REAL         2
#define AT_APPL         3
#define AT_LIST         4
#define AT_PLACEHOLDER  5
#define AT_BLOB         6

#define AT_SYMBOL       7

typedef struct ATerm
{
	header_type   header;
	struct ATerm *next;
} *ATerm;

/** The following functions implement the operations of
  * the 'standard' ATerm interface, and should appear
  * in some form in every implementation of the ATerm
  * datatype.
  * The prefix AT is used to make these functions
  * relatively unique.
  */

ATerm ATmake(const char *pattern, ...);
ATbool ATmatch(ATerm t, const char *pattern, ...);
ATerm ATreadFromString(const char *string);
ATerm ATreadFromTextFile(FILE *file);
ATerm ATreadFromBinaryFile(FILE *file);

/* Abbreviation for ATreadFromString */
#define ATparse(s)	ATreadFromString((s))

/* int    ATgetType(ATerm t); */
#define ATgetType(t) GET_TYPE((t)->header)

/* ATbool ATisEqual(ATerm t1, ATerm t2); */
#define ATisEqual(t1,t2) ((ATerm)(t1) == (ATerm)(t2))

ATbool ATwriteToTextFile(ATerm t, FILE *file);
ATbool ATwriteToBinaryFile(ATerm t, FILE *file);
char  *ATwriteToString(ATerm t);
ATerm  ATsetAnnotation(ATerm t, ATerm label, ATerm anno);
ATerm  ATgetAnnotation(ATerm t, ATerm label);
ATerm  ATremoveAnnotation(ATerm t, ATerm label);

void ATprotect(ATerm *atp);
void ATunprotect(ATerm *atp);
void ATprotectArray(ATerm *start, int size);
void ATunprotectArray(ATerm *start);

/**
  * We also define some functions that are specific
  * for the C implementation of ATerms, but are part
  * of the level 1 interface nevertheless.
  */

void ATinit(int argc, char *argv[], ATerm *bottomOfStack);
void ATsetErrorHandler(void (*handler)(const char *format, va_list args));
void ATerror(const char *format, ...);
int  ATprintf(const char *format, ...);
int  ATfprintf(FILE *stream, const char *format, ...);
int  ATvfprintf(FILE *stream, const char *format, va_list args);

#endif
