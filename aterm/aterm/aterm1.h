
/**
 * aterm1.h: Definition of the level 1 interface
 * of the ATerm library.
 */

#ifndef ATERM1_H
#define ATERM1_H

#include <stdio.h>
#include <stdarg.h>
#include "encoding.h"

#define T_INT          0
#define T_REAL         1
#define T_APPL         4
#define T_TERMS        2
#define T_LIST         3
#define T_PLACEHOLDER  5
#define T_BLOB         6

/* This assumes 32 bit ints */
typedef unsigned int ATerm;

/** The following functions implement the operations of
  * the 'standard' ATerm interface, and should appear
  * in some form in every implementation of the ATerm
  * datatype.
  * The prefix T is used to make these functions
  * relatively unique.
  */

ATerm *Tmake(char *pattern, ...);
ATerm *Tmatch(ATerm *t, char *pattern, ...);
ATerm *TreadFromString(char *string);
ATerm *TreadFromFile(FILE *file);
ATerm *TreadFromBinaryFile(FILE *file);
int    TgetType(ATerm *t);
int    TwriteToFile(ATerm *t, FILE *file);
int    TwriteToBinaryFile(ATerm *t, FILE *file);
char  *TwriteToString(ATerm *t);
void   TsetAnnotation(ATerm *t, ATerm *label, ATerm *anno);
ATerm *TgetAnnotation(ATerm *t, ATerm *label);
void   TremoveAnnotation(ATerm *t, ATerm *label);

/**
  * We also define some functions that are specific
  * for the C implementation of ATerms, but are part
  * of the level 1 interface nevertheless.
  */

void Tinit(int argc, char *argv[], void (*error)(const char *format, va_list args), int *bottomOfStack);
void Terror(const char *format, ...);
int Tprintf(const char *format, ...);
int Tfprintf(FILE *stream, const char *format, ...);
int Tvfprintf(FILE *stream, const char *format, va_list args);

#endif
