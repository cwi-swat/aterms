/*

    ATerm -- The ATerm (Annotated Term) library
    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
                             The  Netherlands.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/

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
#define AT_APPL         1
#define AT_INT          2 
#define AT_REAL         3
#define AT_LIST         4
#define AT_PLACEHOLDER  5
#define AT_BLOB         6

#define AT_SYMBOL       7

typedef struct _ATerm
{
	header_type   header;
	struct _ATerm *next;
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
ATerm ATreadFromTextFile(FILE *file);
ATerm ATreadFromSharedTextFile(FILE *f);
ATerm ATreadFromBinaryFile(FILE *file);
ATerm ATreadFromFile(FILE *file);
ATerm ATreadFromNamedFile(const char *name);
ATerm ATreadFromString(const char *string);
ATerm ATreadFromSharedString(char *s, int size);
ATerm ATreadFromBinaryString(char *s, int size);

/* Abbreviation for ATreadFromString */
#define ATparse(s)	ATreadFromString((s))

/* int    ATgetType(ATerm t); */
#define ATgetType(t) GET_TYPE((t)->header)

/* ATbool ATisEqual(ATerm t1, ATerm t2); */
#ifdef NO_SHARING
extern ATbool AT_isEqual(ATerm t1, ATerm t2);
#define ATisEqual(t1,t2) (AT_isEqual((ATerm)(t1), (ATerm)(t2)))
/* The casts are needed because we want to allow the user
   to easily check the equality of other ATerm types like 
	 ATermList, ATermAppl, etc. */
#else
#define ATisEqual(t1,t2) ((ATbool)((ATerm)(t1) == (ATerm)(t2)))
#endif

ATbool ATwriteToTextFile(ATerm t, FILE *file);
long   ATwriteToSharedTextFile(ATerm t, FILE *f);
ATbool ATwriteToBinaryFile(ATerm t, FILE *file);
ATbool ATwriteToNamedTextFile(ATerm t, const char *name);
ATbool ATwriteToNamedBinaryFile(ATerm t, const char *name);
char  *ATwriteToString(ATerm t);
char  *ATwriteToSharedString(ATerm t, int *len);
char  *ATwriteToBinaryString(ATerm t, int *len);
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
void ATinitialize(int argc, char *argv[]);
void ATsetWarningHandler(void (*handler)(const char *format, va_list args));
void ATsetErrorHandler(void (*handler)(const char *format, va_list args));
void ATsetAbortHandler(void (*handler)(const char *format, va_list args));
void ATwarning(const char *format, ...);
void ATerror(const char *format, ...);
void ATabort(const char *format, ...);
int  ATprintf(const char *format, ...);
int  ATfprintf(FILE *stream, const char *format, ...);
int  ATvfprintf(FILE *stream, const char *format, va_list args);

#endif
