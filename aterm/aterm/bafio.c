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
#include "afun.h"
#include "util.h"

/*}}}  */
/*{{{  defines */

#define	BAF_MAGIC	0xbaf
#define BAF_VERSION	0x0300			/* version 3.0 */

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

/*}}}  */
/*{{{  types */

typedef struct trm_bucket
{
	struct trm_bucket *next;
	ATerm t;
} trm_bucket;

typedef struct top_symbol
{
	struct top_symbol *next;
	AFun s;

	int index;
	int count;
	
	int code_width;
	int code;
} top_symbol;

typedef struct
{
	int         nr_symbols;
	top_symbol *symbols;
	
	int toptable_size;
	top_symbol **toptable;
} top_symbols;

typedef struct
{
	AFun id;
	int	arity;

	int nr_terms;
	trm_bucket *terms;

	top_symbols *top_symbols; /* top symbols occuring in this symbol */
	
	int termtable_size;
	trm_bucket **termtable;

	int term_width;

	int cur_index;
	int nr_times_top; /* # occurences of this symbol as topsymbol */
} sym_entry;

typedef struct
{
  AFun   sym;
	int    arity;
	int    nr_terms;
	int    term_width;
	ATerm *terms;
	int   *nr_topsyms;
	int   *sym_width;
	int  **topsyms;
} sym_read_entry;

/*}}}  */
/*{{{  variables */

char bafio_id[] = "$Id$";

static int nr_unique_symbols = -1;
static sym_read_entry *read_symbols;
static sym_entry *sym_entries = NULL;

static char *text_buffer = NULL;
static int   text_buffer_size = 0;

static char bit_buffer     = '\0';
static int  bits_in_buffer = 0; /* how many bits in bit_buffer are used */

/*}}}  */
/*{{{  void AT_initBafIO(int argc, char *argv[]) */

/**
	* Initialize BafIO code.
	*/

void AT_initBafIO(int argc, char *argv[])
{
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
/*{{{  static int writeBitsToFile(unsigned int val, int nr_bits, FILE *file) */

static
int
writeBitsToFile(unsigned int val, int nr_bits, FILE *file)
{
	int cur_bit;

	for (cur_bit=0; cur_bit<nr_bits; cur_bit++) {
		bit_buffer <<= 1;
		bit_buffer |= (val & 0x01);
		val >>= 1;
		if (++bits_in_buffer == 8) {
			if (fputc((int)bit_buffer, file) == EOF)
				return -1;
			bits_in_buffer = 0;
			bit_buffer = '\0';
		}
	}

	/* Ok */
	return 0;
}

/*}}}  */
/*{{{  static int flushBitsToFile(FILE *file) */

static
int
flushBitsToFile(FILE *file)
{
	int result = 0;
	if(bits_in_buffer > 0) {
		int left = 8-bits_in_buffer;
		bit_buffer <<= left;
		result = (fputc((int)bit_buffer, file) == EOF) ? -1 : 0;
		bits_in_buffer = 0;
		bit_buffer = '\0';
	}
	
	return result;
}

/*}}}  */
/*{{{  static int readBitsFromFile(unsigned int *val, int nr_bits, FILE *file */

static
int
readBitsFromFile(unsigned int *val, int nr_bits, FILE *file)
{
	int cur_bit, mask = 1;

	*val = 0;
	for (cur_bit=0; cur_bit<nr_bits; cur_bit++) {
		if (bits_in_buffer == 0) {
			int val = fgetc(file);
			if (val == EOF)
				return -1;
			bit_buffer = (char) val;
			bits_in_buffer = 8;
		}
		*val |= (bit_buffer & 0x80 ? mask : 0);
		mask <<= 1;
		bit_buffer <<= 1;
		bits_in_buffer--;
	}

	/* Ok */
	return 0;
}

/*}}}  */
/*{{{  static int flushBitsFromFile(FILE *file) */

static
int
flushBitsFromFile(FILE *file)
{
	bits_in_buffer = 0;
	return 0;
}

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
/*{{{  static void print_sym_entries() */

void
AT_print_sym_entries()
{
	int cur_sym, cur_arg;
	
	for(cur_sym=0; cur_sym<nr_unique_symbols; cur_sym++) {
		sym_entry *cur_entry = &sym_entries[cur_sym];
		ATfprintf(stderr, "symbol %y: #=%d, width: %d\n",
							cur_entry->id, cur_entry->nr_terms, cur_entry->term_width);
#if 0
		{
			int cur_trm;
			for(cur_trm=0; cur_trm<cur_entry->nr_terms; cur_trm++)
				ATfprintf(stderr, "%t, ", cur_entry->terms[cur_trm].t);
			ATfprintf(stderr, "\n");
		}
#endif
		
		ATfprintf(stderr, "  arity: %d\n", cur_entry->arity);
		for (cur_arg=0; cur_arg<cur_entry->arity; cur_arg++) {
			int sym;
			top_symbols *tss = &cur_entry->top_symbols[cur_arg];
			ATfprintf(stderr, "    %d symbols: ", tss->nr_symbols);
			for (sym=0; sym<tss->nr_symbols; sym++) {
				top_symbol *ts = &tss->symbols[sym];
				ATfprintf(stderr, "%y: #=%d, width: %d, ",
									sym_entries[ts->index].id, ts->count, ts->code_width);
			}
			ATfprintf(stderr, "\n");
		}
	}
}

/*}}}  */
/*{{{  static sym_entry *get_top_symbol(ATerm t) */

/**
	* Retrieve the top symbol of a term. Could be a special symbol
  * (AS_INT, AS_REAL, etc) when the term is not an AT_APPL.
	*/

static sym_entry *get_top_symbol(ATerm t, ATbool anno_done)
{
	Symbol sym;

	if (HAS_ANNO(t->header) && !anno_done)
		sym = AS_ANNOTATION;
	else { 
		switch (ATgetType(t)) {
		  case AT_INT:
				sym = AS_INT;
				break;
		  case AT_REAL:
				sym = AS_REAL;
				break;
		  case AT_BLOB:
				sym = AS_BLOB;
				break;
		  case AT_PLACEHOLDER:
				sym = AS_PLACEHOLDER;
				break;
		  case AT_LIST:
				sym = (ATisEmpty((ATermList)t) ? AS_EMPTY_LIST : AS_LIST);
				break;
		  case AT_APPL:
				sym = ATgetAFun((ATermAppl)t);
				break;
		  default:
				ATerror("get_top_symbol: illegal term (%n)\n", t);
				sym = -1;
				break;
		}
	}
	
	return &sym_entries[at_lookup_table[sym]->index];
}

/*}}}  */
/*{{{  static int bit_width(int val) */

/* How many bits are needed to represent <val> */
static int bit_width(int val)
{
	int nr_bits = 0;
	
	if (val <= 1)
		return 0;

	while (val) {
		val>>=1;
		nr_bits++;
	}
	
	return nr_bits;
}

/*}}}  */
/*{{{  static void build_arg_tables() */

/**
	* Build argument tables given the fact that the
  * terms have been sorted by symbol.
	*/

static void build_arg_tables()
{
	int cur_sym, cur_arg, cur_trm;
	int index, lcv;
	top_symbols *tss;
	unsigned int hnr;
	
	for(cur_sym=0; cur_sym<nr_unique_symbols; cur_sym++) {
		sym_entry *cur_entry = &sym_entries[cur_sym];
		int arity = cur_entry->arity;

		assert(arity == ATgetArity(cur_entry->id));
		
		if(arity == 0)
			cur_entry->top_symbols = NULL;
		else {
			cur_entry->top_symbols = (top_symbols *)calloc(arity, sizeof(top_symbols));
			if(!cur_entry->top_symbols)
				ATerror("build_arg_tables: out of memory (arity: %d)\n", arity);
		}

		for(cur_arg=0; cur_arg<arity; cur_arg++) {
			int total_top_symbols = 0;
			for(cur_trm=0; cur_trm<cur_entry->nr_terms; cur_trm++) {
				ATerm term = cur_entry->terms[cur_trm].t;
				ATerm arg = NULL;
				if (sym_entries[cur_sym].id == AS_ANNOTATION) {
					assert(arity == 2);
					if (cur_arg == 0)
						arg = term;
					else
						arg = AT_getAnnotations(term);
				} else {
					switch(ATgetType(term)) {
						case AT_LIST:
							{
								ATermList list = (ATermList)term;
								assert(!ATisEmpty(list));
								assert(arity == 2);
								if (cur_arg == 0)
									arg = ATgetFirst(list);
								else
									arg = (ATerm)ATgetNext(list);
							}
							break;
						case AT_PLACEHOLDER:
							assert(arity == 1);
							arg = ATgetPlaceholder((ATermPlaceholder)term);
							break;
						case AT_APPL:
							arg = ATgetArgument((ATermAppl)term, cur_arg);
							break;
						default:
							ATerror("build_arg_tables: illegal term\n");
							break;
					}
				}
				if (!get_top_symbol(arg, 
						 sym_entries[cur_sym].id == AS_ANNOTATION)->nr_times_top++)
					total_top_symbols++;
			}
			tss = &cur_entry->top_symbols[cur_arg];
			tss->nr_symbols = total_top_symbols;
			tss->symbols = (top_symbol *) calloc(total_top_symbols,
																						sizeof(top_symbol));
			if (!tss->symbols)
				ATerror("build_arg_tables: out of memory (top_symbols: %d)\n",
								total_top_symbols);
			tss->toptable_size = (total_top_symbols*5)/4;
			tss->toptable = (top_symbol **) calloc(tss->toptable_size,
																						 sizeof(top_symbol *));
			if (!tss->toptable)
				ATerror("build_arg_tables: out of memory (table_size: %d)\n",
								tss->toptable_size);

			for(lcv=index=0; lcv<nr_unique_symbols; lcv++)
			{
				if (sym_entries[lcv].nr_times_top > 0)
				{
					top_symbol *ts;
					ts = &cur_entry->top_symbols[cur_arg].symbols[index];
					ts->index = lcv;
					ts->count = sym_entries[lcv].nr_times_top;
					ts->code_width = bit_width(total_top_symbols);
					ts->code = index;
					ts->s = sym_entries[lcv].id;
					
					hnr = ts->s % tss->toptable_size;
					ts->next = tss->toptable[hnr];
					tss->toptable[hnr] = ts;

					sym_entries[lcv].nr_times_top = 0;
					index++;
				}
			}
		}
	}
}

/*}}}  */
/*{{{  static void add_term(sym_entry *entry, ATerm t) */

/**
	* Add a term to the termtable of a symbol.
	*/
static void add_term(sym_entry *entry, ATerm t)
{
	unsigned int hnr = AT_hashnumber(t) % entry->termtable_size;
	entry->terms[entry->cur_index].t = t;
	entry->terms[entry->cur_index].next = entry->termtable[hnr];
	entry->termtable[hnr] = &entry->terms[entry->cur_index];
	entry->cur_index++;
}

/*}}}  */
/*{{{  static void collect_terms(ATerm t) */

/**
	* Collect all terms in the appropriate symbol table.
	*/

static void collect_terms(ATerm t)
{
	AFun sym = -1;
	ATerm annos;
	sym_entry *entry;

	if (!IS_MARKED(t->header)) {
		switch(ATgetType(t)) {
			case AT_INT:
				sym = AS_INT;
				break;
			case AT_REAL:
				sym = AS_REAL;
				break;
			case AT_BLOB:
				sym = AS_BLOB;
				break;
			case AT_PLACEHOLDER:
				sym = AS_PLACEHOLDER;
				collect_terms(ATgetPlaceholder((ATermPlaceholder)t));
				break;
			case AT_LIST:
				{
					ATermList list = (ATermList)t;
					if(ATisEmpty(list))
						sym = AS_EMPTY_LIST;
					else {
						sym = AS_LIST;
						collect_terms(ATgetFirst(list));
						collect_terms((ATerm)ATgetNext(list));
					}
				}
			#if 0 /* <PO> buggy */
					while(!ATisEmpty(list) && !IS_MARKED(list->header)) {
						SET_MARK(list->header);
						collect_terms(ATgetFirst(list));
						entry = &sym_entries[at_lookup_table[AS_LIST]->index];
						assert(entry->id == AS_LIST);
						add_term(entry, (ATerm)list);						
						list = ATgetNext(list);
					}
					if(!IS_MARKED(ATempty->header)) {
						entry = &sym_entries[at_lookup_table[AS_EMPTY_LIST]->index];
						add_term(entry, (ATerm)ATempty);
					}

					/* handle annotation */
					annos = AT_getAnnotations(t);
					if (annos) {
						entry = &sym_entries[at_lookup_table[AS_ANNOTATION]->index];
						assert(entry->id == AS_ANNOTATION);
						collect_terms((ATerm)annos);
						add_term(entry, t);
					}
					return;
		#endif
				break;
			case AT_APPL:
				{
					ATermAppl appl = (ATermAppl)t;
					int cur_arity, cur_arg;

					sym = ATgetAFun(appl);
					cur_arity = ATgetArity(sym);
					for(cur_arg=0; cur_arg<cur_arity; cur_arg++)
						collect_terms(ATgetArgument(appl, cur_arg));
				}
				break;
			default:
				ATerror("collect_terms: illegal term\n");
				break;
		}
		entry = &sym_entries[at_lookup_table[sym]->index];
		/*if(entry->id != sym)
			ATfprintf(stderr, "sym=%y, entry->id = %y\n", sym, entry->id);*/

		assert(entry->id == sym);
		add_term(entry, t);
		
		/* handle annotation */
		annos = AT_getAnnotations(t);
		if (annos) {
			entry = &sym_entries[at_lookup_table[AS_ANNOTATION]->index];
			assert(entry->id == AS_ANNOTATION);
			collect_terms((ATerm)annos);
			add_term(entry, t);
		}

		SET_MARK(t->header);
	}
}

/*}}}  */
/*{{{  static ATbool write_symbols(FILE *file) */

/**
	* Write all symbols in a term to file.
	*/

static ATbool write_symbols(FILE *file)
{
	int sym_idx, arg_idx, top_idx;
	
	for(sym_idx=0; sym_idx<nr_unique_symbols; sym_idx++) {
		sym_entry *cur_sym = &sym_entries[sym_idx];
		if (!write_symbol(cur_sym->id, file))
			return ATfalse;
		if (writeIntToFile(cur_sym->nr_terms, file) < 0)
			return ATfalse;

		for(arg_idx=0; arg_idx<cur_sym->arity; arg_idx++) {
			int nr_symbols = cur_sym->top_symbols[arg_idx].nr_symbols;
			if(writeIntToFile(nr_symbols, file)<0)
				return ATfalse;
			for(top_idx=0; top_idx<nr_symbols; top_idx++) {
				top_symbol *ts = &cur_sym->top_symbols[arg_idx].symbols[top_idx];
				if (writeIntToFile(ts->index, file)<0)
					return ATfalse;
				/* <PO> count not needed! 
					 if (writeIntToFile(ts->count, file)<0)
					 return ATfalse;
					 */
			}
		}
	}
	
	return ATtrue;
}

/*}}}  */
/*{{{  static int find_term(sym_entry *entry, ATerm t) */

/**
	* Find a term in a sym_entry.
	*/

static int find_term(sym_entry *entry, ATerm t)
{
	unsigned int hnr = AT_hashnumber(t) % entry->termtable_size;
	trm_bucket *cur = entry->termtable[hnr];
	
	assert(cur);
	while (cur->t != t) {
		cur = cur->next;
		assert(cur);
	}
	
	return cur - entry->terms;
}

/*}}}  */
/*{{{  static top_symbol *find_top_symbol(top_symbols *syms, AFun sym) */

/**
	* Find a top symbol in a topsymbol table.
	*/

static top_symbol *find_top_symbol(top_symbols *syms, AFun sym)
{
	unsigned int hnr = sym % syms->toptable_size;
	top_symbol *cur = syms->toptable[hnr];
	
	assert(cur);
	while (cur->s != sym) {
		cur = cur->next;
		assert(cur);
	}
	
	return cur;
}

/*}}}  */
/*{{{  static ATbool write_arg(sym_entry *trm_sym, ATerm arg, arg_idx, file, anno_done) */

/**
	* Write an argument to file.
	*/

/* forward declaration */
static ATbool write_term(ATerm, FILE *, ATbool);

static ATbool write_arg(sym_entry *trm_sym, ATerm arg, int arg_idx, 
												FILE *file, ATbool anno_done)
{
	top_symbol *ts;
	sym_entry *arg_sym;
	int arg_trm_idx;
	AFun sym;
	
	sym = get_top_symbol(arg, anno_done)->id;
	ts = find_top_symbol(&trm_sym->top_symbols[arg_idx], sym);

	/*ATfprintf(stderr, "writing topsymbol index of %y = %d\n", ts->s, ts->code);*/
	if(writeBitsToFile(ts->code, ts->code_width, file)<0)
		return ATfalse;
	
	arg_sym = &sym_entries[ts->index];
	
	arg_trm_idx = find_term(arg_sym, arg);
/*	ATfprintf(stderr, "writing arg term index of %t = %d\n",
						arg, arg_trm_idx);*/
	if(writeBitsToFile(arg_trm_idx, arg_sym->term_width, file)<0)
		return ATfalse;

	/*ATfprintf(stderr, "argument %t at index %d (cur_index of %y = %d)\n",
						arg, arg_trm_idx, arg_sym->id, arg_sym->cur_index);*/
	if(arg_trm_idx >= arg_sym->cur_index && 
		 !write_term(arg, file, anno_done))
			return ATfalse;
	
	return ATtrue;
}

/*}}}  */
/*{{{  static ATbool write_term(ATerm t, FILE *file, ATbool anno_done) */

/**
	* Write a term to file.
	*/

static ATbool write_term(ATerm t, FILE *file, ATbool anno_done)
{
	int arg_idx;
	sym_entry *trm_sym = NULL;
	ATerm annos;

	annos = AT_getAnnotations(t);

	/*ATfprintf(stderr, "write term: %t (%d)\n", t, anno_done);*/
	if(!anno_done && annos) {
		/*ATfprintf(stderr, "  writing annotated term, term=%t, annos=%t\n",
							t, annos);*/
		trm_sym = &sym_entries[at_lookup_table[AS_ANNOTATION]->index];
		if(!write_arg(trm_sym, t, 0, file, ATtrue))
			return ATfalse;
		if(!write_arg(trm_sym, annos, 1, file, ATfalse)) 
			return ATfalse;
	} else {
		switch(ATgetType(t)) {
		  case AT_INT:
				if(writeBitsToFile(ATgetInt((ATermInt)t), HEADER_BITS, file) < 0)
					return ATfalse;
#if 0
				if (flushBitsToFile(file)<0)
					return ATfalse;
				if (writeIntToFile(ATgetInt((ATermInt)t), file)<0)
					return ATfalse;
#endif
				trm_sym = &sym_entries[at_lookup_table[AS_INT]->index];
				break;
		  case AT_REAL:
				{
					static char buf[64]; /* must be able to hold str-rep of double */
					sprintf(buf, "%f", ATgetReal((ATermReal)t));
					if (flushBitsToFile(file)<0)
						return ATfalse;
					if (writeStringToFile(buf, strlen(buf), file)<0)
						return ATfalse;
					trm_sym = &sym_entries[at_lookup_table[AS_REAL]->index];
				}
			break;
		  case AT_BLOB:
				{
					ATermBlob blob = (ATermBlob)t;
					if (flushBitsToFile(file)<0)
						return ATfalse;
					if (writeStringToFile(ATgetBlobData(blob), ATgetBlobSize(blob),file)<0)
						return ATfalse;
					trm_sym = &sym_entries[at_lookup_table[AS_BLOB]->index];
				}
			break;
		  case AT_PLACEHOLDER:
				{
					ATerm type = ATgetPlaceholder((ATermPlaceholder)t);
					trm_sym = &sym_entries[at_lookup_table[AS_PLACEHOLDER]->index];
					if(!write_arg(trm_sym, type, 0, file, ATfalse))
						return ATfalse;
				}
			break;
		  case AT_LIST:
				{
					ATermList list = (ATermList)t;
					if (ATisEmpty(list))
						trm_sym = &sym_entries[at_lookup_table[AS_EMPTY_LIST]->index];
					else {
						trm_sym = &sym_entries[at_lookup_table[AS_LIST]->index];
						if(!write_arg(trm_sym, ATgetFirst(list), 0, file, ATfalse))
							return ATfalse;
						if(!write_arg(trm_sym, (ATerm)ATgetNext(list), 1, file, ATfalse))
							return ATfalse;
					}
				}
			break;
		  case AT_APPL:
				{
					int arity;
					AFun sym = ATgetAFun(t);
					trm_sym = &sym_entries[at_lookup_table[sym]->index];
					assert(sym == trm_sym->id);
					arity = ATgetArity(sym);
					for (arg_idx=0; arg_idx<arity; arg_idx++) {
						ATerm cur_arg = ATgetArgument((ATermAppl)t, arg_idx);
						if(!write_arg(trm_sym, cur_arg, arg_idx, file, ATfalse))
							return ATfalse;
					}
				}
			break;
		  default:
				ATerror("write_term: illegal term\n");
				break;
		}
	}
  if(trm_sym->terms[trm_sym->cur_index].t != t) {
		ATerror("terms out of sync at pos %d of sym %y, "
						"term in table was %d, expected %t\n", trm_sym->cur_index,
						trm_sym->id, trm_sym->terms[trm_sym->cur_index].t, t);
	}
		trm_sym->cur_index++;
	/*ATfprintf(stderr, "term=%t, trm_sym=%y, cur_index=%d\n", t, trm_sym->id,
						trm_sym->cur_index);*/
	
	return ATtrue;
}

/*}}}  */

/*{{{  static void free_write_space() */

/**
	* Free all space allocated by the bafio write functions.
	*/

static void free_write_space()
{
	int i, j;

	for(i=0; i<nr_unique_symbols; i++) {
		sym_entry *entry = &sym_entries[i];

		free(entry->terms);
		entry->terms = NULL;
		free(entry->termtable);
		entry->termtable = NULL;

		for(j=0; j<entry->arity; j++) {
			top_symbols *topsyms = &entry->top_symbols[j];
			if(topsyms->symbols) {
				free(topsyms->symbols);
				topsyms->symbols = NULL;
			}
			if(topsyms->toptable) {
				free(topsyms->toptable);
				topsyms->toptable = NULL;
			}
			/*free(topsyms);*/
		}

		if(entry->top_symbols) {
			free(entry->top_symbols);
			entry->top_symbols = NULL;
		}
	}
	free(sym_entries);

	sym_entries = NULL;
}

/*}}}  */
/*{{{  ATbool ATwriteToBinaryFile(ATerm t, FILE *file) */

ATbool
ATwriteToBinaryFile(ATerm t, FILE *file)
{
	int nr_unique_terms = 0;
	int nr_symbols = AT_symbolTableSize();
	int lcv, cur;
	int nr_bits;
	AFun sym;
	
	/* Initialize bit buffer */
	bit_buffer     = '\0';
	bits_in_buffer = 0; /* how many bits in bit_buffer are used */

	for(lcv=0; lcv<nr_symbols; lcv++) {
		if(!SYM_IS_FREE(at_lookup_table[lcv]))
			at_lookup_table[lcv]->count = 0;
	}
	nr_unique_symbols = AT_calcUniqueSymbols(t);

	sym_entries = (sym_entry *) calloc(nr_unique_symbols, sizeof(sym_entry));
	if(!sym_entries)
		ATerror("ATwriteToBinaryFile: out of memory (%d unique symbols!\n",
						nr_unique_symbols);
	
	nr_bits = bit_width(nr_unique_symbols);

	/*{{{  Collect all unique symbols in the input term */

	for(lcv=cur=0; lcv<nr_symbols; lcv++) {
		SymEntry entry = at_lookup_table[lcv];
		if(!SYM_IS_FREE(entry) && entry->count>0) {
			assert(lcv == entry->id);
			nr_unique_terms += entry->count;

			sym_entries[cur].term_width = bit_width(entry->count);
			sym_entries[cur].id = lcv;
			sym_entries[cur].arity = ATgetArity(lcv);
			sym_entries[cur].nr_terms = entry->count;
			sym_entries[cur].terms = (trm_bucket *) calloc(entry->count,
																										sizeof(trm_bucket));
			if (!sym_entries[cur].terms)
				ATerror("ATwriteToBinaryFile: out of memory (sym: %d, terms: %d)\n",
								lcv, entry->count);
			sym_entries[cur].termtable_size = (entry->count*5)/4;
			sym_entries[cur].termtable =
				(trm_bucket **) calloc(sym_entries[cur].termtable_size,
															sizeof(trm_bucket *));
			if (!sym_entries[cur].termtable)
				ATerror("ATwriteToBinaryFile: out of memory (termtable_size: %d\n",
								sym_entries[cur].termtable_size);
			
			entry->index = cur;
			entry->count = 0; /* restore invariant that symbolcount is zero */

			cur++;
		}
	}

	assert(cur == nr_unique_symbols);

	/*}}}  */
	
	
	/*ATfprintf(stderr, "writing %d symbols, %d terms.\n",
						nr_unique_symbols, nr_unique_terms);*/
	
	collect_terms(t);
	AT_unmarkTerm(t);
	
	/* reset cur_index */
	for(lcv=0; lcv < nr_unique_symbols; lcv++)
		sym_entries[lcv].cur_index = 0;
	
	build_arg_tables();
	/*print_sym_entries();*/
	
	/*{{{  write header */

	if(writeIntToFile(0, file) < 0)
		return ATfalse;

	if(writeIntToFile(BAF_MAGIC, file) < 0)
		return ATfalse;

	if(writeIntToFile(BAF_VERSION, file) < 0)
		return ATfalse;

	if(writeIntToFile(nr_unique_symbols, file) < 0)
		return ATfalse;

	if(writeIntToFile(nr_unique_terms, file) < 0)
		return ATfalse;

	/*}}}  */
	
	if(!write_symbols(file))
		return ATfalse;

	/* Write the top symbol */
	sym = get_top_symbol(t, ATfalse)->id;
	if(writeIntToFile(get_top_symbol(t, ATfalse)-sym_entries, file) < 0)
		return ATfalse;

	if (!write_term(t, file, ATfalse)) /* sym == AS_ANNOTATION)) */
		return ATfalse;
	
	if (flushBitsToFile(file)<0)
		return ATfalse;

	free_write_space();

	return ATtrue;
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

/*{{{  ATbool read_all_symbols(FILE *file) */

/**
	* Read all symbols from file.
	*/

ATbool read_all_symbols(FILE *file)
{
	unsigned int val;
	int i, j, k, arity;

	for(i=0; i<nr_unique_symbols; i++) {
		/*{{{  Read the actual symbol */

		Symbol sym = read_symbol(file);
		if(sym < 0)
			ATerror("read_symbols: error reading symbol, giving up.\n");

		read_symbols[i].sym = sym;
		ATprotectSymbol(sym);
		arity = ATgetArity(sym);
		read_symbols[i].arity = arity;

		/*}}}  */
		/*{{{  Read term count and allocate space */

		if(readIntFromFile(&val, file) < 0)
			return ATfalse;
		read_symbols[i].nr_terms = val;
		read_symbols[i].term_width = bit_width(val);
		if(val == 0)
			read_symbols[i].terms = NULL;
		else
			read_symbols[i].terms = (ATerm *)calloc(val, sizeof(ATerm));
		if(!read_symbols[i].terms)
			ATerror("read_symbols: could not allocate space for %d terms.\n", val);
		ATprotectArray(read_symbols[i].terms, val);

		/*}}}  */
		
		/*{{{  Allocate space for topsymbol information */

   if(arity == 0) {
		 read_symbols[i].nr_topsyms = NULL;
		 read_symbols[i].sym_width = NULL;
		 read_symbols[i].topsyms = NULL;
	 } else {
		 read_symbols[i].nr_topsyms = (int *)calloc(arity, sizeof(int));
		 if(!read_symbols[i].nr_topsyms)
			 ATerror("read_all_symbols: out of memory trying to allocate "
							 "space for %d arguments.\n", arity);

		 read_symbols[i].sym_width = (int *)calloc(arity, sizeof(int));
		 if(!read_symbols[i].sym_width)
			 ATerror("read_all_symbols: out of memory trying to allocate "
							 "space for %d arguments .\n", arity);

		 read_symbols[i].topsyms = (int **)calloc(arity, sizeof(int *));
		 if(!read_symbols[i].topsyms)
			 ATerror("read_all_symbols: out of memory trying to allocate "
							 "space for %d arguments.\n", arity);
	 }

		/*}}}  */

		for(j=0; j<read_symbols[i].arity; j++) {
			if(readIntFromFile(&val, file) < 0)
				return ATfalse;

			read_symbols[i].nr_topsyms[j] = val;
			read_symbols[i].sym_width[j] = bit_width(val);
			read_symbols[i].topsyms[j] = (int *)calloc(val, sizeof(int));
			if(!read_symbols[i].topsyms[j])
				ATerror("read_symbols: could not allocate space for %d top symbols.\n",
								val);

			for(k=0; k<read_symbols[i].nr_topsyms[j]; k++) {
				if(readIntFromFile(&val, file) < 0)
					return ATfalse;
				read_symbols[i].topsyms[j][k] = val;
			}
		}

/*		ATfprintf(stderr, "symbol %y read, with %d terms and top symbol counts: ",
							sym, read_symbols[i].nr_terms);
		for(j=0; j<arity; j++)
			ATfprintf(stderr, "%d, ", read_symbols[i].nr_topsyms[j]);
		ATfprintf(stderr, "\n");
		*/
	}

	return ATtrue;
}

/*}}}  */
/*{{{  ATerm read_term(sym_read_entry *sym, FILE *file) */

ATerm read_term(sym_read_entry *sym, FILE *file)
{
	unsigned int val;
	int i, arity = sym->arity;
	sym_read_entry *arg_sym;
	ATerm inline_args[MAX_INLINE_ARITY];
	ATerm *args = inline_args;
	ATerm result;

	if(arity > MAX_INLINE_ARITY) {
		args = calloc(arity, sizeof(ATerm));
		ATprotectArray(args, arity);
		if(!args)
			ATerror("could not allocate space for %d arguments.\n", arity);
	}

	/*ATfprintf(stderr, "reading term over symbol %y\n", sym->sym);*/
	for(i=0; i<arity; i++) {
		/*ATfprintf(stderr, "  reading argument %d (%d)", i, sym->sym_width[i]);*/
	  if(readBitsFromFile(&val, sym->sym_width[i], file) < 0)
			return NULL;
		arg_sym = &read_symbols[sym->topsyms[i][val]];
/*		ATfprintf(stderr, "argument %d, symbol index = %d, symbol = %y\n", 
							i, val, arg_sym->sym);*/

		/*ATfprintf(stderr, "  argsym = %y (term width = %d)\n",
							arg_sym->sym, arg_sym->term_width);*/
		if(readBitsFromFile(&val, arg_sym->term_width, file) < 0)
			return NULL;
/*		ATfprintf(stderr, "arg term index = %d\n", val);*/
		if(!arg_sym->terms[val]) {
			arg_sym->terms[val] = read_term(arg_sym, file);
			if(!arg_sym->terms[val])
				return NULL;
			/*ATfprintf(stderr, "sym=%y, index=%d, t=%t\n", arg_sym->sym, 
								val, arg_sym->terms[val]);				*/
		}

		args[i] = arg_sym->terms[val];
	}

	switch(sym->sym) {
		case AS_INT:
			/*{{{  Read an integer */

			if(readBitsFromFile(&val, HEADER_BITS, file) < 0)
				return NULL;

			result = (ATerm)ATmakeInt((int)val);

			/*}}}  */
			break;
		case AS_REAL:
			/*{{{  Read a real */

			{
				double real;

				if(flushBitsFromFile(file) < 0)
					return ATfalse;
				if(readStringFromFile(file) < 0)
					return ATfalse;
				sscanf(text_buffer, "%lf", &real);
				result = (ATerm)ATmakeReal(real);
			}

			/*}}}  */
			break;
		case AS_BLOB:
			/*{{{  Read a blob */

			{
				int len;
				char *data;

				if(flushBitsFromFile(file) < 0)
					return ATfalse;
				if((len = readStringFromFile(file)) < 0)
					return ATfalse;

				data = malloc(len);
				if(!data)
					ATerror("could not allocate space for blob of size %d\n", len);

				result = (ATerm)ATmakeBlob(len, data);
			}

			/*}}}  */
			break;
		case AS_PLACEHOLDER:
			result = (ATerm)ATmakePlaceholder(args[0]);
			break;
		case AS_LIST:
			result = (ATerm)ATinsert((ATermList)args[1], args[0]);
			break;
		case AS_EMPTY_LIST:
			result = (ATerm)ATempty;
			break;
		case AS_ANNOTATION:
			result = AT_setAnnotations(args[0], args[1]);
			break;
		default:
			/* Must be a function application */
			result = (ATerm)ATmakeApplArray(sym->sym, args);

			/*
			ATfprintf(stderr, "building application from the arguments:\n");
			for(i=0; i<arity; i++)
				ATfprintf(stderr, "  %d = %t\n", i, args[i]);

			ATfprintf(stderr, "result = %t\n", result);
			*/
	}

	if(arity > MAX_INLINE_ARITY) {
		ATunprotectArray(args);
		free(args);
	}

	return result;
}

/*}}}  */

/*{{{  static void free_read_space() */

/**
	* Free all temporary space allocated by the baf read functions.
	*/

static void free_read_space()
{
	int i, j;

	for(i=0; i<nr_unique_symbols; i++) {
		sym_read_entry *entry = &read_symbols[i];

		ATunprotectArray(entry->terms);
		if(entry->terms)
			free(entry->terms);
		if(entry->nr_topsyms)
			free(entry->nr_topsyms);
		if(entry->sym_width)
			free(entry->sym_width);

		for(j=0; j<entry->arity; j++)
			free(entry->topsyms[j]);
		if(entry->topsyms)
			free(entry->topsyms);
	}
}

/*}}}  */

/*{{{  ATerm ATreadFromBinaryFile(FILE *file) */

/**
	* Read a term from a BAF file.
	*/

ATerm
ATreadFromBinaryFile(FILE *file)
{
	unsigned int val, nr_unique_terms;
	ATerm result = NULL;

	/* Initialize bit buffer */
	bit_buffer     = '\0';
	bits_in_buffer = 0; /* how many bits in bit_buffer are used */

	/*{{{  Read header */

	if(readIntFromFile(&val,   file) < 0)
		return NULL;

	if(val == 0) {
		if(readIntFromFile(&val,   file) < 0)
			return NULL;
	}

	if(val != BAF_MAGIC) {
		fprintf(stderr, "ATreadFromBinaryFile: not a BAF file!\n");
		return NULL;
	}

	if(readIntFromFile(&val, file) < 0)
		return NULL;

	if(val != BAF_VERSION) {
		fprintf(stderr, "ATreadFromBinaryFile: wrong BAF version, giving up!\n");
		return NULL;
	}

  if(readIntFromFile(&val, file) < 0)
	  return NULL;
  nr_unique_symbols = val;
		
	if(readIntFromFile(&nr_unique_terms, file) < 0)
		return NULL;

	if(!silent)
		fprintf(stderr, "reading %d unique symbols and %d unique terms.\n",
						nr_unique_symbols, nr_unique_terms);

	/*}}}  */
	/*{{{  Allocate symbol space */

	read_symbols = (sym_read_entry *)calloc(nr_unique_symbols,
																					sizeof(sym_read_entry));
	if(!read_symbols)
		ATerror("ATreadFromBinaryFile: out of memory when allocating %d syms.\n",
						nr_unique_symbols);

	/*}}}  */

	if(!read_all_symbols(file))
		return NULL;

	if(readIntFromFile(&val, file) < 0)
		return NULL;

	result = read_term(&read_symbols[val], file);
 
	free_read_space();

	return result;
}

/*}}}  */






