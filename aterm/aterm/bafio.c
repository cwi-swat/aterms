/**
 * bafio.c
 */

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "_aterm.h"
#include "aterm2.h"
#include "memory.h"

#define	BAF_MAGIC	0xbaf
#define BAF_VERSION	0x0100			/* version 1.0 */

#define CMD_RESET	AT_FREE			/* reuse */
#define CMD_APPL	AT_APPL
#define CMD_INT		AT_INT
#define CMD_REAL	AT_REAL
#define CMD_LIST	AT_LIST
#define CMD_PLAC	AT_PLACEHOLDER
#define CMD_BLOB	AT_BLOB
#define CMD_SYM		AT_SYMBOL
#define CMD_QSYM    (CMD_SYM+1)
#define CMD_ANNO	(CMD_QSYM+1)	/* new */

#define TRM_STACK_REL(idx)	(term_stack_depth - (idx) + 10) /* skip CMD's */
#define SYM_STACK_REL(idx)	(sym_stack_depth - (idx))

static int			term_stack_depth;
static ATermTable	term_stack;

static int			sym_stack_depth;
static ATermTable	sym_stack;

static char *text_buffer = NULL;
static int   text_buffer_size = 0;

void
AT_getBafVersion(int *major, int *minor)
{
	*major = BAF_VERSION >> 8;
	*minor = BAF_VERSION & 0xff;
}

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
		*val = buf[1] + ((buf[0] & ~0xc0) << 8);
		return 2;
	}

	if ( (buf[0] & 0x20) == 0 )
	{
		*val = buf[2] + (buf[1] << 8) + ((buf[0] & ~0xe0) << 16);
		return 3;
	}
	
	if ( (buf[0] & 0x10) == 0 )
	{
		*val = buf[3] + (buf[2] << 8) + (buf[1] << 16) +
				((buf[0] & ~0xf0) << 24);
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
		*val = buf[1] + ((buf[0] & ~0xc0) << 8);
		return 2;
	}

	/* Try to read 3rd character */
	if ( (buf[2] = fgetc(file)) == EOF )
		return EOF;

	/* Check if 3rd character is enough */
	if ( (buf[0] & 0x20) == 0 )
	{
		*val = buf[2] + (buf[1] << 8) + ((buf[0] & ~0xe0) << 16);
		return 3;
	}
	
	/* Try to read 4th character */
	if ( (buf[3] = fgetc(file)) == EOF )
		return EOF;

	/* Check if 4th character is enough */
	if ( (buf[0] & 0x10) == 0 )
	{
		*val = buf[3] + (buf[2] << 8) + (buf[1] << 16) +
				((buf[0] & ~0xf0) << 24);
		return 4;
	}

	/* Try to read 5th character */
	if ( (buf[4] = fgetc(file)) == EOF )
		return EOF;

	/* Now 5th character should be enough */
	*val = buf[4] + (buf[3] << 8) + (buf[2] << 16) + (buf[1] << 24);
	return 5;
}

static
int
writeStringToFile(const char *str, int len, FILE *f)
{
	/* Write length. */
	if (writeIntToFile(len, f) < 0)
		return -1;

	/* Write actual string. */
	if (fwrite(str, 1, len, f) != len)
		return -1;

	/* Ok */
	return 0;
}

static
int
writeSymToFile(Symbol sym, FILE *f)
{
	char *    name;
	ATerm     sym_appl;
	ATermInt  index_sym;

	sym_appl = (ATerm) ATmakeAppl0(sym);
	index_sym = (ATermInt) ATtableGet(sym_stack, sym_appl);
	if (index_sym != NULL)
		return ATgetInt(index_sym);

	index_sym = ATmakeInt(sym_stack_depth);
	ATtablePut(sym_stack, sym_appl, (ATerm) index_sym);
	writeIntToFile(CMD_SYM, f);
	writeIntToFile(ATgetArity(sym), f);
	name = ATgetName(sym);
	writeStringToFile(name, strlen(name)+1, f);

	return sym_stack_depth++;
}

/* Forward declaration */
static int writeToBinaryFile(ATerm, FILE *);

static
int
writeListToFile(ATermList list, FILE *file)
{

#define BATCH_SIZE 16

	int       cur_batch, cur_elem;
	int       nr_elems, nr_batches;
	int       base_index;
	ATerm     elems[BATCH_SIZE];
	ATermInt  index_term;
	ATermList l;

	/* First, write all elements of the list to file */
	for (l = list; !ATisEmpty(l); l = ATgetNext(l))
		if (writeToBinaryFile(ATgetFirst(l), file) < 0)
			return -1;

	/* Calculate number of batches we need to do. */
	nr_elems   = ATgetLength(l);
	nr_batches = nr_elems / BATCH_SIZE;

	/* Handle the last (incomplete) batch */

	/* Fast forward to start of batch */
	base_index = nr_batches * BATCH_SIZE;
	for (l = list; base_index; --base_index);
		l = ATgetNext(l);

	/* Fill array of batch */
	for (cur_elem = 0; !ATisEmpty(l); ++cur_elem)
	{
		elems[cur_elem] = ATgetFirst(l);
		l = ATgetNext(l);
	}

	/* Write term_stack index to file (reverse order) */
	for (--cur_elem; cur_elem >= 0; cur_elem--)
	{
		index_term = (ATermInt) ATtableGet(term_stack, elems[cur_elem]);
		assert(index_term != NULL);
		if (writeIntToFile(TRM_STACK_REL(ATgetInt(index_term)), file) < 0)
			return -1;
	}

	/* Handle the other batches, analogous to last (incomplete) batch */
	for (cur_batch = nr_batches-1; cur_batch >= 0; --cur_batch)
	{
		base_index = cur_batch * BATCH_SIZE;
		for (l = list; base_index; --base_index)
			l = ATgetNext(l);

		for (cur_elem = 0; cur_elem < BATCH_SIZE; ++cur_elem)
		{
			elems[cur_elem] = ATgetFirst(l);
			l = ATgetNext(l);
		}

		for (--cur_elem; cur_elem >=0; cur_elem--)
		{
			index_term = (ATermInt) ATtableGet(term_stack, elems[cur_elem]);
			assert(index_term != NULL);
			if (writeIntToFile(TRM_STACK_REL(ATgetInt(index_term)), file) < 0)
				return -1;
		}
	}

	if (writeIntToFile(CMD_LIST, file) < 0)
		return -1;
	
	if (writeIntToFile(nr_elems, file) < 0)
		return -1;

	/* Ok */
	return 0;
}

static
int
writeToBinaryFile(ATerm t, FILE *f)
{
	static int arg_indices[MAX_ARITY];

	int       lcv;
	int       index;
	char      buf[sizeof(double)*4]; /* Must be large enough to fit a double */
	Symbol    sym;
	ATerm     plac;
	ATermInt  index_term;
	ATermAppl appl;
	ATermBlob blob;

	/* If t is already on the stack, return its index. */
	index_term = (ATermInt) ATtableGet(term_stack, t);
	if (index_term != NULL)
		return ATgetInt(index_term);

	/* Otherwise, push t on the stack and return its index. */
	switch(ATgetType(t))
	{
		case AT_APPL:
			appl = (ATermAppl) t;
			sym = ATgetSymbol(appl);

			/* First write all the arguments of the appl */
			for (lcv = ATgetArity(sym)-1; lcv >= 0; --lcv)
			{
				ATerm arg = ATgetArgument(appl, lcv);
				arg_indices[lcv] = writeToBinaryFile(arg, f);
			}

			/* Now issue push commands for each argument */
			for (lcv = ATgetArity(sym)-1; lcv >= 0; --lcv)
			{
				index = arg_indices[lcv];
				if (index < 0)
					return index;
				if (writeIntToFile(TRM_STACK_REL(index), f) <= 0)
					return -1;
			}
			index = writeSymToFile(sym, f);
			if (index < 0)
				return index;
			if (writeIntToFile(CMD_APPL, f) <= 0)
				return -1;
			if (writeIntToFile(SYM_STACK_REL(index), f) <= 0)
				return -1;
			break;

		case AT_INT:
			if (writeIntToFile(CMD_INT, f) <= 0)
				return -1;
			if (writeIntToFile(ATgetInt((ATermInt)t), f) <= 0)
				return -1;
			break;

		case AT_REAL:
			if (writeIntToFile(CMD_REAL, f) <= 0)
				return -1;
			sprintf(buf, "%f", ATgetReal((ATermReal)t));
			if (writeStringToFile(buf, strlen(buf)+1, f) <= 0)
				return -1;
			break;

		case AT_LIST:
			writeListToFile((ATermList)t, f);
			break;

		case AT_PLACEHOLDER:
			plac = ATgetPlaceholder((ATermPlaceholder)t);
			index = writeToBinaryFile(plac, f);
			if (index < 0)
				return index;
			if (writeIntToFile(TRM_STACK_REL(index), f) <= 0)
					return -1;
			if (writeIntToFile(CMD_PLAC, f) <=0)
				return -1;
			break;

		case AT_BLOB:
			if (writeIntToFile(CMD_BLOB, f) <= 0)
				return -1;
			blob = (ATermBlob)t;
			if (writeStringToFile((const char *)ATgetBlobData(blob),
								  ATgetBlobSize(blob), f) <= 0)
				return -1;
			break;

		default:
			ATerror("ATwriteToBinaryFile: writing free term at %p\n", t);
	}

	index_term = ATmakeInt(term_stack_depth);
	ATtablePut(term_stack, t, (ATerm) index_term);
	return term_stack_depth++;
}

ATbool
ATwriteToBinaryFile(ATerm t, FILE *file)
{
	int nr_terms = AT_calcUniqueSubterms(t);
	ATbool result = ATtrue;

	/* Initialize stacks */
	term_stack_depth = 0;
	term_stack = ATtableCreate( (4*nr_terms/3) + 1, 75 );

	sym_stack_depth = 0;
	sym_stack = ATtableCreate(512, 75);

	/* Write an INIT command */
	result = result && writeIntToFile(CMD_RESET,   file);
	result = result && writeIntToFile(BAF_MAGIC,   file);
	result = result && writeIntToFile(BAF_VERSION, file);
	result = result && writeIntToFile(nr_terms,    file);

	result = result && writeToBinaryFile(t, file);

	/* Destroy stacks */
	ATtableDestroy(sym_stack);
	ATtableDestroy(term_stack);

	return result;
}

static
int
readStringFromFile(FILE *f)
{
	unsigned int len;

	/* Get length of string */
	if (readIntFromFile(&len, f) < 0)
		return -1;

	/* Assure buffer can hold the string */
	if (text_buffer_size < len)
	{
		if (text_buffer_size == 0)
			text_buffer_size = 1024;
		else
			text_buffer_size *= 2;
		text_buffer = (char *) realloc(text_buffer, text_buffer_size);
	}

	/* Read the actual string */
	if (fread(text_buffer, 1, len, f) != len)
		return -1;

	/* Ok, return length of string */
	return len;
}

ATerm
ATreadFromBinaryFile(FILE *f)
{
	return NULL;
}

ATbool
AT_interpretBaf(FILE *in, FILE *out)
{
	unsigned int value[16];
	int len;
	double real;

	while (!feof(in))
	{
		if (readIntFromFile(&value[0], in) < 0)
			break;

		switch(value[0])
		{
			case CMD_RESET:
				readIntFromFile(&value[1], in);
				readIntFromFile(&value[2], in);
				readIntFromFile(&value[3], in);
				ATfprintf(out, "CMD_RESET: %x, %d.%d, %d\n",
					value[1], value[2] >> 8, value[2] & 0xff, value[3]);
			break;

			case CMD_APPL:
				readIntFromFile(&value[1], in);
				ATfprintf(out, "CMD_APPL : %d\n", value[1]);
			break;

			case CMD_INT:
				readIntFromFile(&value[1], in);
				ATfprintf(out, "CMD_INT  : %d\n", value[1]);
			break;

			case CMD_REAL:
				len = readStringFromFile(in);
				if (sscanf(text_buffer, "%lf", &real) != 1)
					ATerror("CMD_REAL: no real: %s!\n", text_buffer);
				ATfprintf(out, "CMD_REAL : %f\n", real);
			break;

			case CMD_LIST:
				readIntFromFile(&value[1], in);
				ATfprintf(out, "CMD_LIST : %d\n", value[1]);
			break;

			case CMD_PLAC:
				ATfprintf(out, "CMD_PLAC\n");
			break;

			case CMD_BLOB:
				len = readStringFromFile(in);
				ATfprintf(out, "CMD_BLOB : %d\n", len);
			break;

			case CMD_SYM:
				readIntFromFile(&value[1], in);
				len = readStringFromFile(in);
				ATfprintf(out, "CMD_SYM  : %d %s\n", value[1], text_buffer);
			break;

			case CMD_ANNO:
				ATfprintf(out, "CMD_ANNO\n");
			break;

			default:
				if (value[0] < 10)
					ATerror("ATinterpretBaf: illegal BAF!\n");
				ATfprintf(out, "CMD_PUSH : %d\n",
						  value[0] - 10); /* skip CMD's */
			break;
		}
	}

	return ATtrue;
}
