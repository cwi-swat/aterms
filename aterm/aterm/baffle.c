/**
 * baf.c
 */

void foo();

#if 0 /* NOT FINISHED YET!!! */

#include <stdio.h>
#include <stdlib.h>

#include "_aterm.h"
#include "aterm2.h"

#define	BAF_MAGIC	0xbaf
#define BAF_VERSION	0x0100			/* version 1.0 */

#define CMD_INIT	AT_FREE			/* reuse */
#define CMD_PUSH	AT_APPL			/* reuse */
#define CMD_INT		AT_INT
#define CMD_REAL	AT_REAL
#define CMD_LIST	AT_LIST
#define CMD_PLAC	AT_PLACEHOLDER
#define CMD_BLOB	AT_BLOB
#define CMD_SYM		AT_SYMBOL
#define CMD_ANNO	8				/* new */

static int			term_stack_depth = 0;
static ATermTable	term_stack = NULL;

static int			sym_stack_depth = 0;
static ATermTable	sym_stack = NULL;

static
int
writeIntToBuf(unsigned int val, unsigned char *buf)
{
	if (val < (1 << 7))
	{
		buf[0] = (unsigned char) val;
		return 1;
	}

	if (val < (1 << 14))
	{
		buf[0] = (val >>  8) | 0x80;
		buf[1] = (val >>  0) & 0xff;
		return 2;
	}

	if (val < (1 << 21))
	{
		buf[0] = (val >> 16) | 0xc0;
		buf[1] = (val >>  8) & 0xff;
		buf[2] = (val >>  0) & 0xff;
		return 3;
	}

	if (val < (1 << 28))
	{
		buf[0] = (val >> 24) | 0xe0;
		buf[1] = (val >> 16) & 0xff;
		buf[2] = (val >>  8) & 0xff;
		buf[3] = (val >>  0) & 0xff;
		return 4;
	}

	buf[0] = 0xf0;
	buf[1] = (val >> 24) & 0xff;
	buf[2] = (val >> 16) & 0xff;
	buf[3] = (val >>  8) & 0xff;
	buf[4] = (val >>  0) & 0xff;
	return 5;
}

static
int
readIntFromBuf(unsigned int *val, unsigned char *buf)
{
	if ( (buf[0] & 0x80) == 0 )
	{
		*val = buf[0];
		return 1;
	}
	
	if ( (buf[0] & 0x40) == 0 )
	{
		*val = buf[1] + ((buf[0] & 0xc0) << 8);
		return 2;
	}

	if ( (buf[0] & 0x20) == 0 )
	{
		*val = buf[2] + (buf[1] << 8) + ((buf[0] & 0xe0) << 16);
		return 3;
	}
	
	if ( (buf[0] & 0x10) == 0 )
	{
		*val = buf[3] + (buf[2] << 8) + (buf[1] << 16) +
				((buf[0] & 0xf0) << 24);
		return 4;
	}

	*val = buf[4] + (buf[3] << 8) + (buf[2] << 16) + (buf[1] << 24);
	return 5;
}

static
int
writeIntToFile(unsigned int val, FILE *file)
{
	int nr_items;
	unsigned char buf[8];

	nr_items = writeIntToBuf(val, buf);
	return fwrite(buf, 1, nr_items, file);
}

static
int
readIntFromFile(unsigned int *val, FILE *file)
{
	int buf[8];

	/* Try to read 1st character */
	if ( (buf[0] = fgetc(file)) == EOF )
		return EOF;

	/* Check if 1st character is enough */
	if ( (buf[0] & 0x80) == 0 )
	{
		*val = buf[0];
		return 1;
	}
	
	/* Try to read 2nd character */
	if ( (buf[1] = fgetc(file)) == EOF )
		return EOF;

	/* Check if 2nd character is enough */
	if ( (buf[0] & 0x40) == 0 )
	{
		*val = buf[1] + ((buf[0] & 0xc0) << 8);
		return 2;
	}

	/* Try to read 3rd character */
	if ( (buf[2] = fgetc(file)) == EOF )
		return EOF;

	/* Check if 3rd character is enough */
	if ( (buf[0] & 0x20) == 0 )
	{
		*val = buf[2] + (buf[1] << 8) + ((buf[0] & 0xe0) << 16);
		return 3;
	}
	
	/* Try to read 4th character */
	if ( (buf[3] = fgetc(file)) == EOF )
		return EOF;

	/* Check if 4th character is enough */
	if ( (buf[0] & 0x10) == 0 )
	{
		*val = buf[3] + (buf[2] << 8) + (buf[1] << 16) +
				((buf[0] & 0xf0) << 24);
		return 4;
	}

	/* Try to read 5th character */
	if ( (buf[4] = fgetc(file)) == EOF )
		return EOF;

	/* Now 5th character should be enough */
	*val = buf[4] + (buf[3] << 8) + (buf[2] << 16) + (buf[1] << 24);
	return 5;
}

ATbool
ATwriteToBinaryFile(ATerm t, FILE *file)
{
	int nr_terms = AT_calcUniqueSubterms(t);
	ATbool result = ATtrue;

	/* Initialize stacks */


	/* Write an INIT command */
	result = result && writeIntToFile(CMD_INIT,    file);
	result = result && writeIntToFile(BAF_MAGIC,   file);
	result = result && writeIntToFile(BAF_VERSION, file);
	result = result && writeIntToFile(nr_terms,    file);

	if (result == ATfalse)
		return ATfalse;

	/* Destroy stacks */

	return ATtrue;
}

#endif /* NOT FINISHED YET!!! */
