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

#ifndef _ATERM_H
#define _ATERM_H

#include "aterm2.h"

typedef struct ProtEntry
{
	struct ProtEntry *next;
	ATerm *start;
	int    size;
} ProtEntry;

extern ATbool silent;
extern ProtEntry **at_prot_table;
extern int at_prot_table_size;

void AT_markTerm(ATerm t);
void AT_unmarkTerm(ATerm t);
void AT_unmarkIfAllMarked(ATerm t);
int  AT_calcTextSize(ATerm t);
int  AT_calcCoreSize(ATerm t);
int  AT_calcSubterms(ATerm t);
int  AT_calcUniqueSubterms(ATerm t);
int  AT_calcUniqueSymbols(ATerm t);
int  AT_calcTermDepth(ATerm t);
void AT_writeToStringBuffer(ATerm t, char *buffer);
void AT_assertUnmarked(ATerm t);
void AT_assertMarked(ATerm t);
int AT_calcAllocatedSize();

#endif
