/**
 * bafio.c
 */

#define BAF_DEBUGx

/*{{{  includes */

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "_aterm.h"
#include "aterm2.h"
#include "memory.h"
#include "asymbol.h"
#include "util.h"

/*}}}  */
/*{{{  defines */

#define	BAF_MAGIC	0xbaf
#define BAF_VERSION	0x0200			/* version 2.0 */

#define BAF_DEFAULT_TABLE_SIZE      1024

#define BAF_LIST_BATCH_SIZE 64

#define PLAIN_INT		      0
#define ANNO_INT	        (PLAIN_INT | 1)

#define PLAIN_REAL        2
#define ANNO_REAL	        (PLAIN_REAL | 1)

#define PLAIN_LIST	      4
#define ANNO_LIST	        (PLAIN_LIST | 1)

#define PLAIN_PLAC	      6
#define ANNO_PLAC	        (PLAIN_PLAC | 1)

#define PLAIN_BLOB	      8
#define ANNO_BLOB	        (PLAIN_BLOB | 1)

#define SYMBOL_OFFSET     10

#define IS_ANNOTATED(n)   ((n) & 1 ? ATtrue : ATfalse)
#define SYM_INDEX(n)      (((n)-SYMBOL_OFFSET)/2)
#define SYM_COMMAND(n)    ((n)*2 + SYMBOL_OFFSET)
#define PLAIN_CMD(n)      ((n) & ~1)

#if 0
#define TRM_STACK_REL(idx)	(term_stack_depth - (idx) + 10) /* skip CMD's */
#define SYM_STACK_REL(idx)	(sym_stack_depth - (idx))

#define DEFAULT_ARG_STACK_SIZE 1024
#endif

/*}}}  */
/*{{{  types */

typedef struct sym_entry {
	Symbol sym;
	struct sym_entry *next;
	unsigned int count;
	unsigned int index;
} sym_entry;

typedef struct trm_entry {
	ATerm t;
	struct trm_entry *next; /* Next in hash bucket  */
} trm_entry;

/*}}}  */
/*{{{  variables */

char bafio_id[] = "$Id$";

static int cur_symbol = -1;
static sym_entry *symbols = NULL;
static unsigned int symbol_table_size = 0;
static sym_entry **symbol_table = NULL;
static sym_entry **sorted_symbols = NULL;

static trm_entry *table = NULL;
static int table_size = -1;
static int hashtable_size = 0;
static trm_entry **hashtable = NULL;
static int next_free;

static Symbol *read_symbols = NULL;
static ATerm *read_table = NULL;

/*}}}  */
/*{{{  global variables */

#if 0
static int			term_stack_depth;
static ATermTable	term_stack;

static int			sym_stack_depth;
static ATermTable	sym_stack;

static ATerm *arg_stack;
static int    arg_stack_depth;
static int    arg_stack_size;

static ATerm empty_args[MAX_ARITY] = { NULL };
#endif

static char *text_buffer = NULL;
static int   text_buffer_size = 0;


/*}}}  */
/*{{{  void AT_initBafIO(int argc, char *argv[]) */

/**
	* Initialize BafIO code.
	*/

void AT_initBafIO(int argc, char *argv[])
{
#if 0
	int i;

	for(i=0; i<MAX_ARITY; i++)
		empty_args[i] = (ATerm)ATempty;

	ATprotectArray(empty_args, MAX_ARITY);

	arg_stack_size = DEFAULT_ARG_STACK_SIZE;
	arg_stack = (ATerm *)calloc(arg_stack_size,sizeof(ATerm));
	if(!arg_stack)
		ATerror("cannot allocate initial argument stack of size %d\n",
						DEFAULT_ARG_STACK_SIZE);
	arg_stack_depth = 0;
        ATprotectArray(arg_stack, DEFAULT_ARG_STACK_SIZE);
#endif
}

/*}}}  */

/*{{{  void AT_getBafVersion(int *major, int *minor) */

void
AT_getBafVersion(int *major, int *minor)
{
	*major = BAF_VERSION >> 8;
	*minor = BAF_VERSION & 0xff;
}

/*}}}  */

/*{{{  static int writeIntToBuf(unsigned int val, unsigned char *buf) */

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

/*}}}  */
/*{{{  static int readIntFromBuf(unsigned int *val, unsigned char *buf) */

#if 0

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

#endif

/*}}}  */
/*{{{  static int writeIntToFile(unsigned int val, FILE *file) */

static
int
writeIntToFile(unsigned int val, FILE *file)
{
	int nr_items;
	unsigned char buf[8];

	nr_items = writeIntToBuf(val, buf);
	if(fwrite(buf, 1, nr_items, file) != nr_items)
		return -1;

	/* Ok */
	return 0;
}

/*}}}  */
/*{{{  static int readIntFromFile(unsigned int *val, FILE *file) */

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

/*}}}  */
/*{{{  static int writeStringToFile(const char *str, int len, FILE *f) */

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

/*}}}  */
/*{{{  static int readStringFromFile(FILE *f) */

static
int
readStringFromFile(FILE *f)
{
	unsigned int len;

	/* Get length of string */
	if (readIntFromFile(&len, f) < 0)
		return -1;

	/* Assure buffer can hold the string */
	if (text_buffer_size < (len+1))
	{
		text_buffer_size = len*1.5;
		text_buffer = (char *) realloc(text_buffer, text_buffer_size);
		if(!text_buffer)
			ATerror("out of memory in readStringFromFile (%d)\n", text_buffer_size);
	}

	/* Read the actual string */
	if (fread(text_buffer, 1, len, f) != len)
		return -1;

	/* Ok, return length of string */
	return len;
}

/*}}}  */

#if 0
/*{{{  static int writeListToFile(ATermList list, FILE *file) */

/* Forward declaration */
static int writeToBinaryFile(ATerm, FILE *);

static
int
writeListToFile(ATermList list, FILE *file)
{

#define BATCH_SIZE 16

	int       cur_batch, cur_elem;
	int       nr_elems, nr_batches;
	int       base_index, idx;
	ATerm     elems[BATCH_SIZE];
	ATermInt  index_term;
	ATermList l;

	/* First, write all elements of the list to file */
	for (l = list; !ATisEmpty(l); l = ATgetNext(l))
		if (writeToBinaryFile(ATgetFirst(l), file) < 0)
			return -1;

	/* Calculate number of batches we need to do. */
	nr_elems   = ATgetLength(list);
	nr_batches = nr_elems / BATCH_SIZE;

	/* Handle the last (incomplete) batch */

	/* Fast forward to start of batch */
	base_index = nr_batches * BATCH_SIZE;
	for (l = list, idx = base_index; idx; --idx)
		l = ATgetNext(l);

	/* Fill array of batch */
	for (cur_elem = 0; !ATisEmpty(l); ++cur_elem)
	{
		elems[cur_elem] = ATgetFirst(l);
		l = ATgetNext(l);
	}

	/* Write term_stack index to file (reverse order) */
	for (cur_elem = nr_elems-base_index-1; cur_elem >= 0; cur_elem--)
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

/*}}}  */
/*{{{  static int writeSymToFile(Symbol sym, FILE *f) */

static
int
writeSymToFile(Symbol sym, FILE *f)
{
	char *    name;
	ATerm     sym_appl;
	ATermInt  index_sym;

	sym_appl = (ATerm)ATmakeApplArray(sym, empty_args);
	index_sym = (ATermInt) ATtableGet(sym_stack, sym_appl);
	if (index_sym != NULL)
		return ATgetInt(index_sym);

	index_sym = ATmakeInt(sym_stack_depth);
	ATtablePut(sym_stack, sym_appl, (ATerm) index_sym);
	if(writeIntToFile(ATisQuoted(sym) ? CMD_QSYM : CMD_SYM, f) < 0)
		return -1;
	if(writeIntToFile(ATgetArity(sym), f) < 0)
		return -1;
	name = ATgetName(sym);
	writeStringToFile(name, strlen(name)+1, f);

	return sym_stack_depth++;
}

/*}}}  */
/*{{{  static int writeToBinaryFile(ATerm t, FILE *f) */

static
int
writeToBinaryFile(ATerm t, FILE *f)
{
	int       lcv, arity;
	int       index;
	static    char buf[64]; /* Must be large enough to fit a double */
	Symbol    sym;
	ATerm     plac, annos;
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
			arity = ATgetArity(sym);
			for(lcv=0; lcv<arity; lcv++)
			{
				ATerm arg = ATgetArgument(appl, lcv);
				writeToBinaryFile(arg, f);
			}

			/* Now issue push commands for each argument */
			for(lcv=0; lcv<arity; lcv++)
			{
				ATerm index_term = ATtableGet(term_stack, ATgetArgument(appl, lcv));
				index = ATgetInt((ATermInt)index_term);
				if (index < 0)
					return index;
				if (writeIntToFile(TRM_STACK_REL(index), f) < 0)
					return -1;
			}
			index = writeSymToFile(sym, f);
			if (index < 0)
				return index;
			if (writeIntToFile(CMD_APPL, f) < 0)
				return -1;
			if (writeIntToFile(SYM_STACK_REL(index), f) < 0)
				return -1;
			break;

		case AT_INT:
			if (writeIntToFile(CMD_INT, f) < 0)
				return -1;
			if (writeIntToFile(ATgetInt((ATermInt)t), f) < 0)
				return -1;
			break;

		case AT_REAL:
			if (writeIntToFile(CMD_REAL, f) < 0)
				return -1;
			sprintf(buf, "%f", ATgetReal((ATermReal)t));
			if (writeStringToFile(buf, strlen(buf)+1, f) < 0)
				return -1;
			break;

		case AT_LIST:
			if(writeListToFile((ATermList)t, f) < 0)
				return -1;
			break;

		case AT_PLACEHOLDER:
			plac = ATgetPlaceholder((ATermPlaceholder)t);
			index = writeToBinaryFile(plac, f);
			if (index < 0)
				return index;
			if (writeIntToFile(TRM_STACK_REL(index), f) < 0)
					return -1;
			if (writeIntToFile(CMD_PLAC, f) <0)
				return -1;
			break;

		case AT_BLOB:
			if (writeIntToFile(CMD_BLOB, f) < 0)
				return -1;
			blob = (ATermBlob)t;
			if (writeStringToFile((const char *)ATgetBlobData(blob),
								  ATgetBlobSize(blob), f) < 0)
				return -1;
			break;

		default:
			ATerror("ATwriteToBinaryFile: writing free term at %p\n", t);
	}

	annos = AT_getAnnotations(t);
	if(annos) {
		int idx_annos;
		int idx_plain_t;

		ATerm plain_t = AT_removeAnnotations(t);
		idx_plain_t   = term_stack_depth++;
		index_term = ATmakeInt(idx_plain_t);
		ATtablePut(term_stack, plain_t, (ATerm) index_term);

		idx_annos = writeToBinaryFile(annos, f);
		if(idx_annos < 0)
			return -1;

		/* Push index of annotation */
		if(writeIntToFile(TRM_STACK_REL(idx_annos), f) < 0)
			return -1;
		/* Push index of term */
		if(writeIntToFile(TRM_STACK_REL(idx_plain_t), f) < 0)
			return -1;
		if(writeIntToFile(CMD_ANNO, f) < 0)
			return -1;
	}
	index_term = ATmakeInt(term_stack_depth);
	ATtablePut(term_stack, t, (ATerm) index_term);
	return term_stack_depth++;
}

/*}}}  */
/*{{{  ATbool ATwriteToBinaryFile(ATerm t, FILE *file) */

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
	result = result && (writeIntToFile(CMD_RESET,   file) >= 0);
	result = result && (writeIntToFile(BAF_MAGIC,   file) >= 0);
	result = result && (writeIntToFile(BAF_VERSION, file) >= 0);
	result = result && (writeIntToFile(nr_terms,    file) >= 0);

	result = result && writeToBinaryFile(t, file);

	/* Destroy stacks */
	ATtableDestroy(sym_stack);
	ATtableDestroy(term_stack);

	return result;
}

/*}}}  */
/*{{{  static ATerm readFromBinaryFile(FILE *f) */

/**
	* Read a term from a BAF file.
	*/

static ATerm readFromBinaryFile(FILE *f)
{
	unsigned int cmd, len;
	unsigned int value[4];
	int i, major, minor, old_size;
	double real;
	ATerm t, annos, idx, sym_appl;
	ATermList list;
	Symbol sym;
	void *data;

	while(!feof(f)) {
		t = NULL;
		if (readIntFromFile(&cmd, f) < 0)
			break;

		switch(cmd)
		{
			case CMD_RESET:
				/*{{{  Handle RESET command */

				readIntFromFile(&value[0], f);
				readIntFromFile(&value[1], f);
				readIntFromFile(&value[2], f);
				if(value[0] != BAF_MAGIC)
					ATerror("illegal magic number %X, expecting %X\n", 
									value[0], BAF_MAGIC);
				major = BAF_VERSION >> 8;
				minor = BAF_VERSION & 0xFF;
				if(value[1] >> 8 != major)
					ATerror("incompatible BAF version %d.xx (expecting %d.xx)\n", 
									value[1] >> 8, major);
				if((value[1] & 0xFF) > minor)
					ATerror("BAF version %d.%d file newer than expected " \
									"version %d.%d\n", value[1] >> 8, value[1] & 0xFF,
									major, minor);

				/*}}}  */
				break;
	
			case CMD_APPL:
				/*{{{  Handle APPL command */
				
				readIntFromFile(&value[0], f);
				sym_appl = ATtableGet(sym_stack, 
															(ATerm)ATmakeInt(SYM_STACK_REL(value[0])));
				assert(sym_appl && ATgetType(sym_appl) == AT_APPL);
				sym = ATgetSymbol((ATermAppl)sym_appl);
				t = (ATerm)ATmakeApplArray(sym, 
													&arg_stack[arg_stack_depth-ATgetArity(sym)]);
				arg_stack_depth -= ATgetArity(sym);

				/*}}}  */
				break;

			case CMD_INT:
			  /*{{{  Handle INT command */

				readIntFromFile(&value[0], f);
				t = (ATerm)ATmakeInt(value[0]);

				/*}}}  */
				break;

			case CMD_REAL:
				/*{{{  Handle REAL command */

				len = readStringFromFile(f);
				if(sscanf(text_buffer, "%lf", &real) != 1)
					ATerror("CMD_REAL: not a real: %s\n", text_buffer);
				t = (ATerm)ATmakeReal(real);

				/*}}}  */
				break;

			case CMD_LIST:
				/*{{{  Handle LIST command */

				readIntFromFile(&len, f);
				list = ATempty;
        i = len;

        for(--i; i>=0; i--)
					list = ATinsert(list, arg_stack[arg_stack_depth-i-1]);

				t = (ATerm)list;
				arg_stack_depth -= len;

				/*}}}  */
				break;

			case CMD_PLAC:
				/*{{{  Handle PLAC command */

			  t = (ATerm)ATmakePlaceholder(arg_stack[--arg_stack_depth]);
				arg_stack[arg_stack_depth] = NULL;

				/*}}}  */
				break;

			case CMD_BLOB:
			  /*{{{  Handle BLOB command */

			  len = readStringFromFile(f);
				data = (void *)malloc(len);
				if(!data)
				  ATerror("cannot allocate blob of size %d\n", len);
				memcpy(data, text_buffer, len);
				t = (ATerm)ATmakeBlob(len, data);

				/*}}}  */
				break;

			case CMD_SYM:
				/*{{{  Handle SYM command */

			  readIntFromFile(&value[0], f);
				len = readStringFromFile(f);
				sym = ATmakeSymbol(text_buffer, value[0], ATfalse);
				sym_appl  = (ATerm)ATmakeApplArray(sym, empty_args);
				idx = (ATerm)ATmakeInt(sym_stack_depth++);
				ATtablePut(sym_stack, idx, sym_appl);

				/*}}}  */
				break;

			case CMD_QSYM:
			  /*{{{  Handle QSYM command */

			  readIntFromFile(&value[0], f);
				len = readStringFromFile(f);
				sym = ATmakeSymbol(text_buffer, value[0], ATtrue);
				sym_appl  = (ATerm)ATmakeApplArray(sym, empty_args);
				idx = (ATerm)ATmakeInt(sym_stack_depth++);
				ATtablePut(sym_stack, idx, sym_appl);

				/*}}}  */
				break;

			case CMD_ANNO:
			  /*{{{  Handle ANNO command */

			  t = arg_stack[--arg_stack_depth];
				annos = arg_stack[--arg_stack_depth];
				t = AT_setAnnotations(t, annos);

				/*}}}  */
				break;

			default:
				if (cmd < 10)
					ATerror("readFromBinaryFile: illegal BAF!\n");
				/*{{{  Handle PUSH command */

				idx = (ATerm)ATmakeInt(term_stack_depth - (cmd - 10));
				t = ATtableGet(term_stack, idx);
				if(!t)
				  ATerror("element %t not on stack.\n", idx);

				if(arg_stack_depth >= arg_stack_size) {
					/* We need to resize the argument stack */
					ATunprotectArray(arg_stack);
					old_size = arg_stack_size;
					arg_stack_size *= 2;
					arg_stack = realloc(arg_stack, arg_stack_size*sizeof(ATerm));
					for(i=old_size; i<arg_stack_size; i++)
					  arg_stack[i] = NULL;
					ATprotectArray(arg_stack, arg_stack_size);
				}

				arg_stack[arg_stack_depth++] = t;
				t = NULL;

				/*}}}  */
				break;
		}
		if(t) {
			idx = (ATerm)ATmakeInt(term_stack_depth++);
			ATtablePut(term_stack, idx, t);
		}
	}
	idx = (ATerm)ATmakeInt(term_stack_depth-1);
	return ATtableGet(term_stack, idx);
}

/*}}}  */
/*{{{  ATerm AT_readFromBinaryFile(FILE *f) */

/**
	* Read an ATerm from a file, CMD_RESET must have been read already.
	*/

ATerm AT_readFromBinaryFile(FILE *f)
{
	ATerm t;
	unsigned int magic, version, major, minor;
	unsigned int nr_unique_subterms;

	readIntFromFile(&magic, f);
	if(magic != BAF_MAGIC)
		ATerror("illegal magic number %X, expecting %X\n", magic, BAF_MAGIC);

	readIntFromFile(&version, f);
	major = BAF_VERSION >> 8;
	minor = BAF_VERSION & 0xFF;
	if(version >> 8 != major)
		ATerror("incompatible BAF version %d.xx (expecting %d.xx)\n", 
						version >> 8, major);
	if((version & 0xFF) > minor)
		ATerror("BAF version %d.%d file newer than expected " \
						"version %d.%d\n", version >> 8, version & 0xFF, major, minor);	

	readIntFromFile(&nr_unique_subterms, f);

	/* Initialize stacks */
	term_stack_depth = 0;
	term_stack = ATtableCreate( (nr_unique_subterms*3)/4+1, 75 );

	sym_stack_depth = 0;
	sym_stack = ATtableCreate(512, 75);

	arg_stack_depth = 0;
	
	t = readFromBinaryFile(f);

	/* Destroy stacks */
	ATtableDestroy(sym_stack);
	ATtableDestroy(term_stack);

	return t;
}

/*}}}  */
/*{{{  ATerm ATreadFromBinaryFile(FILE *f) */

/**
	* Read a term from a BAF file.
	*/

ATerm
ATreadFromBinaryFile(FILE *f)
{
	unsigned int command;

	readIntFromFile(&command, f);
	if(command != CMD_RESET)
		ATerror("not a BAF file (no CMD_RESET)\n");

	return AT_readFromBinaryFile(f);
}

/*}}}  */
/*{{{  ATbool AT_interpretBaf(FILE *in, FILE *out) */

ATbool
AT_interpretBaf(FILE *in, FILE *out)
{
	unsigned int value[4];
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
					ATerror("CMD_REAL: not a real: %s!\n", text_buffer);
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

			case CMD_QSYM:
				readIntFromFile(&value[1], in);
				len = readStringFromFile(in);
				ATfprintf(out, "CMD_QSYM: %d \"%s\"\n", value[1], text_buffer);
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

/*}}}  */
#endif

/*{{{  static void add_symbol(Symbol sym) */

/**
	* Add a symbol to the symbol table.
	*/

static void add_symbol(Symbol sym)
{
	sym_entry *cur;
	unsigned int hash_val = AT_hashSymbol(ATgetName(sym), ATgetArity(sym));

	hash_val %= symbol_table_size;
	cur = symbol_table[hash_val];

	while(cur && cur->sym != sym)
		cur = cur->next;

	if(cur) {
		cur->count++;
	} else {
		cur = &symbols[cur_symbol];
		cur->next = symbol_table[hash_val];
		symbol_table[hash_val] = cur;
		cur->sym = sym;
		cur->count = 1;
		cur_symbol++;
	}
}

/*}}}  */
/*{{{  static void find_symbol(Symbol sym) */

/**
	* Find a symbol in the symbol table.
	*/

static int find_symbol(Symbol sym)
{
	sym_entry *cur;
	unsigned int hash_val = AT_hashSymbol(ATgetName(sym), ATgetArity(sym));

	hash_val %= symbol_table_size;
	cur = symbol_table[hash_val];

	while(cur && cur->sym != sym)
		cur = cur->next;

	assert(cur);
	return cur->index;
}

/*}}}  */
/*{{{  static void fill_symbols(ATerm t) */

/**
	* Fill symbol table.
	*/

static void fill_symbols(ATerm t)
{
	Symbol sym;
	ATermList list;
	int i, arity;

	if(IS_MARKED(t->header))
		return;

	SET_MARK(t->header);

	switch(ATgetType(t)) {
		case AT_INT:
		case AT_REAL:
		case AT_BLOB:
			return;

		case AT_APPL:
			sym = ATgetSymbol((ATermAppl)t);
			add_symbol(sym);
			arity = ATgetArity(sym);
			for(i=0; i<arity; i++)
				fill_symbols(ATgetArgument((ATermAppl)t, i));
			break;

		case AT_LIST:
			list = (ATermList)t;
			while(!ATisEmpty(list)) {
				fill_symbols(ATgetFirst(list));
				list = ATgetNext(list);
			}
			break;

		case AT_PLACEHOLDER:
			fill_symbols(ATgetPlaceholder((ATermPlaceholder)t));
			break;
			
		default:
			ATerror("fill_symbols: illegal term type: %d\n", ATgetType(t));
			break;
	}

	if(HAS_ANNO(t->header))
		fill_symbols(AT_getAnnotations(t));
}

/*}}}  */
/*{{{  static int compare_symbols(void *sym1, void *sym2) */

/**
	* Compare two symbols on behalve of quicksort.
	*/

static int compare_symbols(const void *e1, const void *e2)
{
	sym_entry *sym1 = *((sym_entry **)e1);
	sym_entry *sym2 = *((sym_entry **)e2);

	return sym2->count - sym1->count;
}

/*}}}  */
/*{{{  static ATbool write_symbol(Symbol sym, FILE *file) */

/**
	* Write a symbol to file.
	*/

static ATbool write_symbol(Symbol sym, FILE *file)
{
	char *name = ATgetName(sym);
	if(writeStringToFile(name, strlen(name), file) < 0)
		return ATfalse;

	if(writeIntToFile(ATgetArity(sym), file) < 0)
		return ATfalse;

	if(writeIntToFile(ATisQuoted(sym), file) < 0)
		return ATfalse;

	return ATtrue;
}

/*}}}  */

/*{{{  static int find_term(ATerm t) */

/**
	* Find a term in the hashqueue, and update queue priority.
	*/

static int find_term(ATerm t)
{
	trm_entry *cur;
	unsigned int hnr = AT_hashnumber(t) % hashtable_size;

	cur = hashtable[hnr];
	assert(cur);
	while(cur->t != t) {
		cur = cur->next;
		assert(cur);
	}

	return cur - table;
}

/*}}}  */

/*{{{  static void fill_terms(ATerm t) */

/**
	* static void fill_terms(ATerm t)
	*/

static void fill_terms(ATerm t)
{
	unsigned int hnr;
	ATerm annos;

	if(IS_MARKED(t->header))
		return;
	SET_MARK(t->header);

	annos = AT_getAnnotations(t);
	if(annos)
		fill_terms(annos);

	switch(ATgetType(t)) {
		case AT_INT:
		case AT_REAL:
		case AT_BLOB:
			break;

		case AT_APPL:
			{
				ATermAppl appl = (ATermAppl)t;
				Symbol sym     = ATgetSymbol(appl);
				int i, arity   = ATgetArity(sym);
				for(i=0; i<arity; i++)
					fill_terms(ATgetArgument(appl, i));
			}
			break;

		case AT_LIST:
			{
				ATermList list = (ATermList)t;
				while(!ATisEmpty(list)) {
					fill_terms(ATgetFirst(list));
					list = ATgetNext(list);
				}
			}
			break;

		case AT_PLACEHOLDER:
			fill_terms(ATgetPlaceholder((ATermPlaceholder)t));
			break;
	}

	hnr = AT_hashnumber(t) % hashtable_size;
	table[next_free].t = t;
	table[next_free].next = hashtable[hnr];
	hashtable[hnr] = &table[next_free];
	next_free++;
}

/*}}}  */
/*{{{  static ATbool write_terms(FILE *file) */

/**
	* Write a term to file.
	*/

static ATbool write_terms(FILE *file)
{
	ATerm *list_elems = NULL;
	int    list_size = 0;
	int i;

	for(i=0; i<table_size; i++) {
		ATerm t = table[i].t;
		switch(ATgetType(t)) {
			case AT_INT:
				/*{{{  Write an integer */

				if(writeIntToFile(PLAIN_INT | (HAS_ANNO(t->header) ? 1 : 0), file) < 0)
					return ATfalse;
				if(writeIntToFile(ATgetInt((ATermInt)t), file) < 0)
					return ATfalse;

				/*}}}  */
				break;
			case AT_REAL:
				/*{{{  Write a real */

				{
					static char buf[64]; /* Must be large enough to fit a double */

					if(writeIntToFile(PLAIN_REAL | (HAS_ANNO(t->header) ? 1 : 0), file) < 0)
						return ATfalse;
					sprintf(buf, "%f", ATgetReal((ATermReal)t));
					if(writeStringToFile(buf, strlen(buf)+1, file) < 0)
						return ATfalse;
				}

				/*}}}  */
				break;
			case AT_BLOB:
				/*{{{  Write a blob */

				{
					int size   = ATgetBlobSize((ATermBlob)t);
					void *data = ATgetBlobData((ATermBlob)t);

					if(writeIntToFile(PLAIN_BLOB | (HAS_ANNO(t->header) ? 1 : 0), file) < 0)
						return ATfalse;
					if(writeIntToFile(size, file) < 0)
						return ATfalse;
					if(writeStringToFile((char *)data, size, file) < 0)
						return ATfalse;
				}

				/*}}}  */
				break;
			case AT_PLACEHOLDER:
				/*{{{  Write a placeholder */

				{
					int plac_index = find_term(ATgetPlaceholder((ATermPlaceholder)t));

					if(writeIntToFile(PLAIN_PLAC | (HAS_ANNO(t->header) ? 1 : 0), file) < 0)
						return ATfalse;
					if(writeIntToFile(plac_index, file) < 0)
						return ATfalse;
				}

				/*}}}  */
				break;
			case AT_APPL:
				/*{{{  Write an application */

				{
					ATermAppl appl = (ATermAppl)t;
					Symbol sym     = ATgetSymbol(appl);
					int arg, arity = ATgetArity(sym);

					int sym_index  = find_symbol(sym);
					int sym_cmd    = SYM_COMMAND(sym_index);

					if(writeIntToFile(sym_cmd | (HAS_ANNO(t->header) ? 1 : 0), file) < 0)
						return ATfalse;

					for(arg=0; arg<arity; arg++) {
						int arg_index = find_term(ATgetArgument(appl, arg));
						if(writeIntToFile(arg_index, file) < 0)
							return ATfalse;
					}
				}				

				/*}}}  */
				break;
			case AT_LIST:
				/*{{{  Write a list */

				{
					int size, cur;
					ATermList list = (ATermList)t;

					size = ATgetLength(list);

					if(writeIntToFile(PLAIN_LIST | (HAS_ANNO(t->header) ? 1 : 0), file) < 0)
						return ATfalse;
					if(writeIntToFile(size, file) < 0)
						return ATfalse;

					if(size > list_size) {
						list_elems = (ATerm *)realloc(list_elems, sizeof(ATerm)*size);
						if(!list_elems)
							ATerror("write_terms: out of memory.");
						list_size = size;
					}

					for(cur=0; cur<size; cur++) {
						list_elems[cur] = ATgetFirst(list);
						list = ATgetNext(list);
					}
					
					for(cur=size-1; cur>=0; cur--) {
						int elem_index = find_term(list_elems[cur]);
						if(writeIntToFile(elem_index, file) < 0)
							return ATfalse;
					}
				}

				/*}}}  */
				break;
		}

		if(HAS_ANNO(t->header)) {
			int anno_index = find_term(AT_getAnnotations(t));
			if(writeIntToFile(anno_index, file) < 0)
				return ATfalse;
		}
	}

	return ATtrue;
}

/*}}}  */

/*{{{  ATbool ATwriteToBinaryFile(ATerm t, FILE *file) */

ATbool
ATwriteToBinaryFile(ATerm t, FILE *file)
{
	int i, nr_syms;
	ATbool result;

	nr_syms = AT_calcUniqueSymbols(t);
	table_size = AT_calcUniqueSubterms(t);

	if(!silent) {
		ATfprintf(stderr, "writing %d symbols and %d terms.\n", 
							nr_syms, table_size);
	}

	/*{{{  Write header */

	if(writeIntToFile(0,   file) < 0)
		return ATfalse;

	if(writeIntToFile(BAF_MAGIC,   file) < 0)
		return ATfalse;

	if(writeIntToFile(BAF_VERSION, file) < 0)
		return ATfalse;

	if(writeIntToFile(nr_syms, file) < 0)
		return ATfalse;

	if(writeIntToFile(table_size, file) < 0)
		return ATfalse;

	/*}}}  */

	/*{{{  allocate symbol space */

	symbols = (sym_entry *)calloc(nr_syms, sizeof(sym_entry));
	if(!symbols)
		ATerror("alloc_symbol_space: "
						"cannot allocate space for %d symbols.\n", nr_syms);
	cur_symbol = 0;

	symbol_table_size = (nr_syms*4)/3;
	symbol_table = (sym_entry **)calloc(symbol_table_size, 
																			sizeof(sym_entry *));
	if(!symbol_table)
		ATerror("alloc_symbol_space: cannot allocate space for "
						"symbol table of size: %d\n", symbol_table_size);

	sorted_symbols = (sym_entry **)calloc(nr_syms, sizeof(sym_entry *));
	if(!sorted_symbols)
		ATerror("alloc_symbol_space: cannot allocate space for "
						"sorted symbol table of size: %d\n", nr_syms);

	/*}}}  */
	/*{{{  allocate term space */

	table = (trm_entry *)calloc(table_size, sizeof(trm_entry));
	if(!table)
		ATerror("ATwriteToBinaryFile: out of memory (%d terms)\n", table_size);

	hashtable_size = (table_size*5)/4;
	hashtable = (trm_entry **)calloc(hashtable_size, sizeof(trm_entry *));
	if(!hashtable)
		ATerror("ATwriteToBinaryFile: out of memory (%d buckets)\n", 
						hashtable_size);

	next_free = 0;

	/*}}}  */
	/*{{{  Gather, sort, and write symbols */

	fill_symbols(t);
	AT_unmarkTerm(t);

	assert(nr_syms == cur_symbol);

	for(i=0; i<nr_syms; i++)
		sorted_symbols[i] = &symbols[i];
 
	qsort((void *)sorted_symbols, nr_syms, 
				sizeof(sym_entry *), compare_symbols);

	for(i=0; i<nr_syms; i++) {
		assert(i==0 || (sorted_symbols[i-1]->count >= sorted_symbols[i]->count));
		sorted_symbols[i]->index = i;
		if(!write_symbol(sorted_symbols[i]->sym, file))
			return ATfalse;
	}

	/*}}}  */

	fill_terms(t);
	assert(next_free == table_size);
	AT_unmarkTerm(t);
	result = write_terms(file);

	/*{{{  Free symbol space */

	free(symbols);
	symbols = NULL;

	free(symbol_table);
	free(sorted_symbols);

	symbol_table = NULL;
	sorted_symbols = NULL;

	cur_symbol = -1;
	symbol_table_size = 0;

	/*}}}  */
	/*{{{  Free term size */

	free(table);
	free(hashtable);

	/*}}}  */

	return result;
}

/*}}}  */

/*{{{  static ATerm read_terms(FILE *file) */

/**
	* Read a list of terms from file.
	*/

static ATerm read_terms(FILE *file)
{
	int i;
	ATerm result = NULL;

	for(i=0; i<table_size; i++) {
		ATbool has_anno = ATfalse;
		unsigned int cmd;

		if(readIntFromFile(&cmd, file) < 0)
			return NULL;

		if(IS_ANNOTATED(cmd)) {
			has_anno = ATtrue;
			cmd = PLAIN_CMD(cmd);
		}

		switch(cmd) {
			case PLAIN_INT:
				/*{{{  Read an integer term */

				{
					unsigned int val;
					
					if(readIntFromFile(&val, file) < 0)
						return NULL;

					result = (ATerm)ATmakeInt(val);
				}

				/*}}}  */
				break;
			case PLAIN_REAL:
				/*{{{  Read a real term */

				{
					double val;

					if(readStringFromFile(file) < 0)
						return NULL;

					sscanf(text_buffer, "%lf", &val); 
					
					result = (ATerm)ATmakeReal(val);
				}

				/*}}}  */
				break;
			case PLAIN_LIST:
				/*{{{  Read one batch of a list. */

				{
					unsigned int lcv, index, len;
					ATermList list = ATempty;
					
					if(readIntFromFile(&len, file) < 0)
						return NULL;

					for(lcv=0; lcv<len; lcv++) {
						if(readIntFromFile(&index, file) < 0)
							return NULL;

						assert(index < i);
						list = ATinsert(list, read_table[index]);
					}
					result = (ATerm)list;
				}

				/*}}}  */
				break;
			case PLAIN_PLAC:
				/*{{{  Read a placeholder */

				{
					unsigned int index;

					if(readIntFromFile(&index, file) < 0)
						return NULL;

					assert(index < i);

					result = (ATerm)ATmakePlaceholder(read_table[index]);
				}

				/*}}}  */
				break;
			case PLAIN_BLOB:
				/*{{{  Read a BLOB term */

				{
					void *data;
					int len = readStringFromFile(file);
					if(len < 0)
						return NULL;
					
					data = malloc(len);
					if(!data)
						ATerror("read_term: out of memory when allocating "
										"blob of size %d\n", len);
					
					memcpy(data, text_buffer, len);

					result = (ATerm)ATmakeBlob(len, data);
				}

				/*}}}  */
				break;
			default:
				/*{{{  Must be an appl */

				{
					int arg;
					unsigned int arity, arg_index, symbol_index;
					Symbol sym;
					ATerm args[MAX_ARITY];
					
					symbol_index = SYM_INDEX(cmd);
					assert(symbol_index < symbol_table_size);
					
					sym = read_symbols[symbol_index];
					arity = ATgetArity(sym);
					
					for(arg=0; arg<arity; arg++) {
						if(readIntFromFile(&arg_index, file) < 0)
							return NULL;
						assert(arg_index < i);
						args[arg] = read_table[arg_index];
						assert(args[arg]);
					}
					
					result = (ATerm)ATmakeApplArray(sym, args);
				}

				/*}}}  */
				break;
		}

		if(has_anno) {
			unsigned int anno_index;

			if(readIntFromFile(&anno_index, file) < 0)
				return NULL;

			assert(anno_index < i);
			result = AT_setAnnotations(result, read_table[anno_index]);
		}

		read_table[i] = result;
	}

	return result;
}

/*}}}  */
/*{{{  Symbol read_symbol(FILE *file) */

/**
	* Read a single symbol from file.
	*/

Symbol read_symbol(FILE *file)
{
	unsigned int arity, quoted;
	int len;

	if((len = readStringFromFile(file)) < 0)
		return -1;

	text_buffer[len] = '\0';

	if(readIntFromFile(&arity, file) < 0)
		return -1;

	if(readIntFromFile(&quoted, file) < 0)
		return -1;

	return ATmakeSymbol(text_buffer, arity, quoted ? ATtrue : ATfalse);
}

/*}}}  */

/*{{{  ATerm ATreadFromBinaryFile(FILE *file) */

/**
	* Read a term from a BAF file.
	*/

ATerm
ATreadFromBinaryFile(FILE *file)
{
	int i;
	unsigned int val;
	ATerm result;

	/*{{{  Read header */

	if(readIntFromFile(&val,   file) < 0)
		return ATfalse;

	if(val == 0) {
		if(readIntFromFile(&val,   file) < 0)
			return ATfalse;
	}

	if(val != BAF_MAGIC) {
		fprintf(stderr, "ATreadFromBinaryFile: not a BAF file!\n");
		return ATfalse;
	}

	if(readIntFromFile(&val, file) < 0)
		return ATfalse;

	if(val != BAF_VERSION) {
		fprintf(stderr, "ATreadFromBinaryFile: old BAF version, giving up!\n");
		return ATfalse;
	}

	if(readIntFromFile(&symbol_table_size, file) < 0)
		return ATfalse;

	if(readIntFromFile(&val, file) < 0)
		return ATfalse;

	table_size = (int)val;

	if(!silent)
		ATfprintf(stderr, "reading %d symbols and %d terms.\n", 
							symbol_table_size, table_size);

	/*}}}  */
	/*{{{  Allocate symbol table */

	read_symbols = (Symbol *)calloc(symbol_table_size, sizeof(Symbol));
	if(!read_symbols)
		ATerror("ATreadFromBinaryFile: could not allocate table of size %d\n",
						symbol_table_size);

	/*}}}  */
	/*{{{  Allocate and protect term space */

	read_table = (ATerm *)calloc(table_size, sizeof(ATerm));
	if(!read_table)
		ATerror("ATwriteToBinaryFile: out of memory (%d terms)\n", table_size);

	next_free = 0;

	ATprotectArray(read_table, table_size);

	/*}}}  */

	/*{{{  Read and protect symbols */

	for(i=0; i<symbol_table_size; i++) {
		read_symbols[i] = read_symbol(file);
		if(read_symbols[i] < 0)
			ATerror("ATreadFromBinaryFile: corrupt symbol section in BAF file!\n");
	  ATprotectSymbol(read_symbols[i]);
	}

	/*}}}  */

  result = read_terms(file);

	/*{{{  Unprotect and free term table */

	ATunprotectArray(read_table);
	free(read_table);
	read_table = NULL;
	next_free = -1;

	/*}}}  */
	/*{{{  Unprotect and free symboltable */

	for(i=0; i<symbol_table_size; i++)
		ATunprotectSymbol(read_symbols[i]);

	free(read_symbols); /* Free symbol table */
	read_symbols = NULL;
	symbol_table_size = -1;

	/*}}}  */
 
	return result;
}

/*}}}  */



