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
  * memory.h: Memory allocation.
  */

#ifndef MEMORY_H
#define MEMORY_H

#include "aterm2.h"

#define MAX_ARITY            256
#define MIN_TERM_SIZE          2
#define MAX_TERM_SIZE       (MAX_ARITY+3)
#define MAX_BLOCKS_PER_SIZE 1024

#define MAX_INLINE_ARITY       6

/* To change the block size, modify BLOCK_SHIFT only! */
#define BLOCK_SHIFT      16
#define BLOCK_SIZE       (1<<BLOCK_SHIFT)
#define BLOCK_TABLE_SIZE 4099     /* nextprime(4096) */

typedef struct Block
{
  int size;
  struct Block *next_by_size;
  struct Block *next_before;
  struct Block *next_after;
  header_type data[BLOCK_SIZE];
} Block;

typedef struct BlockBucket
{
  struct Block *first_before;
  struct Block *first_after;
} BlockBucket;

extern Block *at_blocks[MAX_TERM_SIZE];
extern int at_nrblocks[MAX_TERM_SIZE];
extern ATerm at_freelist[MAX_TERM_SIZE];
extern BlockBucket block_table[BLOCK_TABLE_SIZE];

void AT_initMemory(int argc, char *argv[]);
void AT_cleanupMemory();
unsigned int AT_hashnumber(ATerm t);
ATerm AT_allocate(int size);
void  AT_freeTerm(int size, ATerm t);
void  AT_collect(int size);
ATbool AT_isValidTerm(ATerm term);
void  AT_validateFreeList(int size);
int AT_inAnyFreeList(ATerm t);
void AT_printAllTerms(FILE *file);
void AT_printAllAFunCounts(FILE *file);

#endif
