
/**
  * aterm.c
  */

/*{{{  includes */

#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include <assert.h>

#include "_aterm.h"
#include "memory.h"
#include "asymbol.h"
#include "list.h"
#include "make.h"
#include "gc.h"
#include "util.h"
#include "bafio.h"

/*}}}  */
/*{{{  defines */

#define SILENT_FLAG	"-silent"

#define DEFAULT_BUFFER_SIZE 4096
#define RESIZE_BUFFER(n) if(n > buffer_size) resize_buffer(n)
#define ERROR_SIZE 32
#define INITIAL_MARK_STACK_SIZE   16384
#define MARK_STACK_MARGE          MAX_ARITY

/* Initial number of terms that can be protected (grows as needed) */
#define PROTECT_INITIAL_SIZE 2048
#define PROTECT_EXPAND_SIZE  4096
/* The same for the protected arrays */
#define PROTECT_ARRAY_INITIAL_SIZE 128
#define PROTECT_ARRAY_EXPAND_SIZE  256

/*}}}  */
/*{{{  globals */

char            aterm_id[] = "$Id$";

/* Flag to tell whether to keep quiet or not. */
ATbool silent	= ATfalse;

/* error_handler is called when a fatal error is detected */
static void     (*error_handler) (const char *format, va_list args) = NULL;

/* We need a buffer for printing and parsing */
static int      buffer_size = 0;
static char    *buffer = NULL;

/* Parse error description */
static int      line = 0;
static int      col = 0;
static char     error_buf[ERROR_SIZE];
static int      error_idx = 0;

ATerm         **at_protected = NULL;	/* Holds all protected terms           */
int             at_nrprotected = -1;	/* How many terms are actually
																				 protected */
int             at_maxprotected = -1;	/* How many terms fit in the array     */

ProtectedArray *at_protected_arrays = NULL; /* Holds protected arrays */
int             at_nrprotected_arrays = -1; /* How many arrays */
int             at_maxprotected_arrays = -1; /* How many arrays maximally */

static ATerm   *mark_stack = NULL;
static int      mark_stack_size = 0;
int             mark_stats[3] = {0, MYMAXINT, 0};
int             nr_marks = 0;

/*}}}  */
/*{{{  function declarations */

extern char    *strdup(const char *s);
static ATerm    fparse_term(int *c, FILE * f);
static ATerm    sparse_term(int *c, char **s);

/*}}}  */

/*{{{  void AT_cleanup() */

/**
	* Perform necessary cleanup.
	*/

void
AT_cleanup()
{
    AT_cleanupGC();
    AT_cleanupMemory();
}

/*}}}  */
/*{{{  void ATinit(int argc, char *argv[], ATerm *bottomOfStack) */

/**
  * Initialize the ATerm library.
  */

void
ATinit(int argc, char *argv[], ATerm * bottomOfStack)
{
	int lcv;
	static ATbool initialized = ATfalse;
  ATbool help = ATfalse;

	if (initialized)
		return;

	for (lcv=1; lcv < argc; lcv++) {
		if (streq(argv[lcv], SILENT_FLAG)) {
			silent = ATtrue;
		} else if(strncmp(argv[lcv], "-help", strlen(argv[lcv])) == 0)
			help = ATtrue;
	}

	if (!silent)
		ATfprintf(stderr, "  ATerm Library, version %s, built: %s\n",
							VERSION, CURDATE);

	if(help) {
		fprintf(stderr, "    %-20s: print this help info\n", "-help");
		fprintf(stderr, "    %-20s: suppress runtime gc information.\n",
						"-silent");
	}

	/* Protect novice users that simply pass NULL as bottomOfStack */
	if (bottomOfStack == NULL)
		ATerror("ATinit: illegal bottomOfStack (arg 3) passed.\n");
	
	/* Check for reasonably sized ATerm (32 bits, 4 bytes)     */
	/* This check might break on perfectly valid architectures */
	/* that have char == 2 bytes, and sizeof(header_type) == 2 */
	assert(sizeof(header_type) == sizeof(ATerm *));
	assert(sizeof(header_type) >= 4);
	
	buffer_size = DEFAULT_BUFFER_SIZE;
	buffer = (char *) malloc(DEFAULT_BUFFER_SIZE);
	if (!buffer)
		ATerror("ATinit: cannot allocate string buffer of size %d\n",
						DEFAULT_BUFFER_SIZE);

	/* Allocate memory for PROTECT_INITIAL_SIZE protected terms */
	at_nrprotected = 0;
	at_maxprotected = PROTECT_INITIAL_SIZE;
	at_protected = (ATerm **) calloc(at_maxprotected, sizeof(ATerm *));
	if (!at_protected)
		ATerror("ATinit: cannot allocate space for %d protected terms.\n",
						at_maxprotected);
	
	/* Allocate initial array of protected arrays */
	at_nrprotected_arrays  = 0;
	at_maxprotected_arrays = PROTECT_ARRAY_INITIAL_SIZE;
	at_protected_arrays    = (ProtectedArray *)calloc(at_maxprotected_arrays,
																											sizeof(ProtectedArray));
	if(!at_protected_arrays)
		ATerror("ATinit: cannot allocate space for %d protected arrays.\n",
						at_maxprotected_arrays);

	/* Allocate initial mark stack */
	mark_stack = (ATerm *) malloc(sizeof(ATerm) * INITIAL_MARK_STACK_SIZE);
	if (!mark_stack)
		ATerror("cannot allocate marks stack of %d entries.\n",
						INITIAL_MARK_STACK_SIZE);
	mark_stack_size = INITIAL_MARK_STACK_SIZE;

	/* Initialize other components */
	AT_initMemory(argc, argv);
	AT_initSymbol(argc, argv);
	AT_initList(argc, argv);
	AT_initMake(argc, argv);
	AT_initGC(argc, argv, bottomOfStack);
	AT_initBafIO(argc, argv);
	
	initialized = ATtrue;

	atexit(AT_cleanup);

	if(help) {
		fprintf(stderr, "\n");
		exit(0);
	}
}

/*}}}  */
/*{{{  void ATsetErrorHandler(handler) */

/**
	* Change the error handler.
	*/

void
ATsetErrorHandler(void (*handler) (const char *format, va_list args))
{
    error_handler = handler;
}

/*}}}  */
/*{{{  void ATerror(const char *format, ...) */

/**
  * A fatal error was detected.
  */

void
ATerror(const char *format,...)
{
    va_list         args;

    va_start(args, format);
    if (error_handler)
	error_handler(format, args);
    else
    {
	ATvfprintf(stderr, format, args);
	abort();
	/* exit(1); */
    }

    va_end(args);
}

/*}}}  */

/*{{{  void ATprotect(ATerm *term) */

/**
  * Protect a given term.
  */
void
ATprotect(ATerm * term)
{
	/*
	 * If at_nrprotected < at_maxprotected, then at_nrprotected holds the
	 * exact index of the first free slot. Otherwise, we need to increase the
	 * at_protected array.
	 */

	assert(*term == NULL || AT_isValidTerm(*term)); /* Check the precondition */

	if (at_nrprotected >= at_maxprotected) {
		at_maxprotected += PROTECT_EXPAND_SIZE;
		at_protected = (ATerm **) realloc(at_protected,
										  at_maxprotected * sizeof(ATerm *));
		if (!at_protected)
	    ATerror("ATprotect: no space to hold %d protected terms.\n",
							at_maxprotected);
	}

	at_protected[at_nrprotected++] = term;
}

/*}}}  */
/*{{{  void ATunprotect(ATerm *term) */

/**
 * Unprotect a given term.
 */

void
ATunprotect(ATerm * term)
{
	int lcv;
	
	/*
	 * Traverse array of protected terms. If equal, switch last protected
	 * term into this slot and clear last protected slot. Update number of
	 * protected terms.
	 */
	for (lcv = 0; lcv < at_nrprotected; ++lcv)
	{
		if (at_protected[lcv] == term)
		{
			at_protected[lcv] = at_protected[--at_nrprotected];
			at_protected[at_nrprotected] = NULL;
			break;
		}
	}
}

/*}}}  */
/*{{{  void ATprotectArray(ATerm *start, int size) */

/**
	* Protect an array
	*/

void ATprotectArray(ATerm *start, int size)
{
	/*
	 * If at_nrprotected_arrays < at_maxprotected_arrays, 
	 * then at_nrprotected_arrays holds the exact index of the first free slot. 
	 * Otherwise, we need to resize at_protected_arrays.
	 */
	int idx;

#ifndef NDEBUG
  for(idx=0; idx<size; idx++) {
		assert(start[idx] == NULL || AT_isValidTerm(start[idx]));
	}
#endif

	if (at_nrprotected_arrays >= at_maxprotected_arrays) {
		at_maxprotected_arrays += PROTECT_ARRAY_EXPAND_SIZE;
		at_protected_arrays = (ProtectedArray *) realloc(at_protected_arrays,
														 at_maxprotected_arrays * sizeof(ProtectedArray));
		if (!at_protected_arrays)
	    ATerror("ATprotectArray: no space to hold %d protected arrays.\n",
							at_maxprotected_arrays);
	}

	idx = at_nrprotected_arrays++;
	at_protected_arrays[idx].start = start;
	at_protected_arrays[idx].size  = size;
}

/*}}}  */
/*{{{  void ATunprotectArrray(ATerm *start) */

/**
 * Unprotect an array of terms.
 */

void ATunprotectArray(ATerm *start)
{
	int lcv;
	
	/*
	 * Traverse array of protected term arrays. If equal, switch last protected
	 * array into this slot and clear last protected slot. Update number of
	 * protected arrays.
	 */
	for (lcv = 0; lcv < at_nrprotected_arrays; ++lcv) {
		if (at_protected_arrays[lcv].start == start) {
			at_nrprotected_arrays--;
			at_protected_arrays[lcv].start = 
				at_protected_arrays[at_nrprotected_arrays].start;
			at_protected_arrays[lcv].size = 
				at_protected_arrays[at_nrprotected_arrays].size;
			at_protected_arrays[at_nrprotected_arrays].start = NULL;
			break;
		}
	}
}

/*}}}  */

/*{{{  int ATprintf(const char *format, ...) */

/**
 * Extension of printf() with ATerm-support.
 */

int
ATprintf(const char *format,...)
{
    int             result = 0;
    va_list         args;

    va_start(args, format);
    result = ATvfprintf(stdout, format, args);
    va_end(args);

    return result;
}

/*}}}  */
/*{{{  int ATfprintf(FILE *stream, const char *format, ...) */

/**
 * Extension of fprintf() with ATerm-support.
 */

int
ATfprintf(FILE * stream, const char *format,...)
{
    int             result = 0;
    va_list         args;

    va_start(args, format);
    result = ATvfprintf(stream, format, args);
    va_end(args);

    return result;
}
/*}}}  */
/*{{{  int ATvfprintf(FILE *stream, const char *format, va_list args) */

int
ATvfprintf(FILE * stream, const char *format, va_list args)
{
    const char     *p;
    char           *s;
    char            fmt[16];
    int             result = 0;
    ATerm           t;
    ATermList       l;

    for (p = format; *p; p++)
    {
	if (*p != '%')
	{
	    fputc(*p, stream);
	    continue;
	}

	s = fmt;
	while (!isalpha((int) *p))	/* parse formats %-20s, etc. */
	    *s++ = *p++;
	*s++ = *p;
	*s = '\0';

	switch (*p)
	{
	case 'c':
	case 'd':
	case 'i':
	case 'o':
	case 'u':
	case 'x':
	case 'X':
	    fprintf(stream, fmt, va_arg(args, int));
	    break;

	case 'e':
	case 'E':
	case 'f':
	case 'g':
	case 'G':
	    fprintf(stream, fmt, va_arg(args, double));
	    break;

	case 'p':
	    fprintf(stream, fmt, va_arg(args, void *));
	    break;

	case 's':
	    fprintf(stream, fmt, va_arg(args, char *));
	    break;

	    /*
	     * ATerm specifics start here: "%t" to print an ATerm; "%l" to
	     * print a list; "%y" to print a Symbol; "%n" to print a single
	     * ATerm node
	     */
	case 't':
	    ATwriteToTextFile(va_arg(args, ATerm), stream);
	    break;
	case 'l':
	    l = va_arg(args, ATermList);
	    fmt[strlen(fmt) - 1] = '\0';	/* Remove 'l' */
	    while (!ATisEmpty(l))
	    {
		ATwriteToTextFile(ATgetFirst(l), stream);
		/*
		 * ATfprintf(stream, "\nlist node: %n\n", l);
		 * ATfprintf(stream, "\nlist element: %n\n", ATgetFirst(l));
		 */
		l = ATgetNext(l);
		if (!ATisEmpty(l))
		    fputs(fmt + 1, stream);
	    }
	    break;
	case 'y':
	    AT_printSymbol(va_arg(args, Symbol), stream);
	    break;
	case 'n':
	    t = va_arg(args, ATerm);
	    switch (ATgetType(t))
	    {
	    case AT_INT:
	    case AT_REAL:
	    case AT_BLOB:
		ATwriteToTextFile(t, stream);
		break;

	    case AT_PLACEHOLDER:
		fprintf(stream, "<...>");
		break;

	    case AT_LIST:
		fprintf(stream, "[...(%d)]", ATgetLength((ATermList) t));
		break;

	    case AT_APPL:
		fprintf(stream, "<appl>(...(%d))", GET_ARITY(t->header));
		break;
	    case AT_FREE:
		fprintf(stream, "@");
		break;
	    default:
		fprintf(stream, "#");
		break;
	    }
	    break;

	default:
	    fputc(*p, stream);
	    break;
	}
    }
    return result;
}

/*}}}  */

/*{{{  static void resize_buffer(int n) */

/**
  * Resize the resident string buffer
  */

static void
resize_buffer(int n)
{
    buffer_size = n;
    buffer = (char *) realloc(buffer, buffer_size);
    if (!buffer)
	ATerror("ATinit: cannot allocate string buffer of size %d\n", buffer_size);
}

/*}}}  */

/*{{{  ATbool ATwriteToTextFile(ATerm t, FILE *f) */

/**
  * Write a term in text format to file.
  */

ATbool
writeToTextFile(ATerm t, FILE * f)
{
    Symbol          sym;
    ATerm           arg, trm;
    int             i, arity;
    ATermAppl       appl;
    ATermList       list;
    ATermBlob       blob;

    switch (ATgetType(t))
    {
			case AT_INT:
				fprintf(f, "%d", ((ATermInt) t)->value);
				break;
			case AT_REAL:
				fprintf(f, "%f", ((ATermReal) t)->value);
				break;
			case AT_APPL:
				/*{{{  Print application */

				appl = (ATermAppl) t;
				
				sym = ATgetSymbol(appl);
				AT_printSymbol(sym, f);
				arity = ATgetArity(sym);
				if (arity > 0) {
					fputc('(', f);
					for (i = 0; i < arity; i++) {
						if (i != 0)
							fputc(',', f);
						arg = ATgetArgument(appl, i);
						ATwriteToTextFile(arg, f);
					}
					fputc(')', f);
				}

				/*}}}  */
				break;
			case AT_LIST:
				/*{{{  Print list */
				
				list = (ATermList) t;
				if(!ATisEmpty(list)) {
					ATwriteToTextFile(ATgetFirst(list), f);
					list = ATgetNext(list);
				}
				while(!ATisEmpty(list)) {
					fputc(',', f);				
					ATwriteToTextFile(ATgetFirst(list), f);
					list = ATgetNext(list);
				}

				/*}}}  */
				break;
			case AT_PLACEHOLDER:
				/*{{{  Print placeholder */

	fputc('<', f);
	ATwriteToTextFile(ATgetPlaceholder((ATermPlaceholder) t), f);
	fputc('>', f);

	/*}}}  */
				break;
			case AT_BLOB:
				/*{{{  Print blob */

	blob = (ATermBlob) t;
	fprintf(f, "%08d:", ATgetBlobSize(blob));
	fwrite(ATgetBlobData(blob), ATgetBlobSize(blob), 1, f);

	/*}}}  */
				break;

			case AT_FREE:
				if(AT_inAnyFreeList(t))
					ATerror("ATwriteToTextFile: printing free term at %p!\n", t);
				else
					ATerror("ATwriteToTextFile: free term %p not in freelist?\n", t);
				return ATfalse;

			case AT_SYMBOL:
				ATerror("ATwriteToTextFile: not a term but a symbol: %y\n", t);
				return ATfalse;
    }
    trm = (ATerm) AT_getAnnotations(t);
    if (trm) {
			fputc('{', f);
			writeToTextFile(trm, f);
			fputc('}', f);
		}
    return ATtrue;
}

ATbool
ATwriteToTextFile(ATerm t, FILE * f)
{
    ATbool          result = ATtrue;

    if (ATgetType(t) == AT_LIST)
    {
	fputc('[', f);

	if (!ATisEmpty((ATermList) t))
	    result = writeToTextFile(t, f);

	fputc(']', f);
    }
    else
    {
	result = writeToTextFile(t, f);
    }
    return result;
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

static int
symbolTextSize(Symbol sym)
{
    char           *id = ATgetName(sym);

    if (ATisQuoted(sym))
    {
	int             len = 2;
	while (*id)
	{
	    /* We need to escape special characters */
	    switch (*id)
	    {
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
    }
    else
	return strlen(id);
}

/*}}}  */
/*{{{  static char *writeSymbolToString(Symbol sym, char *buf) */

/**
  * Write a symbol in a string buffer.
  */

static char    *
writeSymbolToString(Symbol sym, char *buf)
{
    char           *id = ATgetName(sym);

    if (ATisQuoted(sym))
    {
	*buf++ = '"';
	while (*id)
	{
	    /* We need to escape special characters */
	    switch (*id)
	    {
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
    }
    else
    {
	strcpy(buf, id);
	return buf + strlen(buf);
    }
}

/*}}}  */
/*{{{  static char *writeToString(ATerm t, char *buf) */

static char    *topWriteToString(ATerm t, char *buf);

static char    *
writeToString(ATerm t, char *buf)
{
    ATerm           trm;
    ATermList       list;
    ATermAppl       appl;
    ATermBlob       blob;
    Symbol          sym;
    int             i, size, arity;

    switch (ATgetType(t))
    {
    case AT_INT:
	/*{{{  write integer */

	sprintf(buf, "%d", ATgetInt((ATermInt) t));
	buf += strlen(buf);

	/*}}}  */
	break;

    case AT_REAL:
	/*{{{  write real */

	sprintf(buf, "%f", ATgetReal((ATermReal) t));
	buf += strlen(buf);

	/*}}}  */
	break;

    case AT_APPL:
	/*{{{  write appl */

	appl = (ATermAppl) t;
	sym = ATgetSymbol(appl);
	arity = ATgetArity(sym);
	buf = writeSymbolToString(sym, buf);
	if (arity > 0)
	{
	    *buf++ = '(';
	    buf = topWriteToString(ATgetArgument(appl, 0), buf);
	    for (i = 1; i < arity; i++)
	    {
		*buf++ = ',';
		buf = topWriteToString(ATgetArgument(appl, i), buf);
	    }
	    *buf++ = ')';
	}

	/*}}}  */
	break;

    case AT_LIST:
	/*{{{  write list */

	list = (ATermList) t;
	if (!ATisEmpty(list))
	{
	    buf = topWriteToString(ATgetFirst(list), buf);
	    list = ATgetNext(list);
	    while (!ATisEmpty(list))
	    {
				*buf++ = ',';
				buf = topWriteToString(ATgetFirst(list), buf);
				list = ATgetNext(list);
	    }
	}

	/*}}}  */
	break;

    case AT_PLACEHOLDER:
	/*{{{  write placeholder */

	trm = ATgetPlaceholder((ATermPlaceholder) t);
	buf = topWriteToString(trm, buf);

	/*}}}  */
	break;

    case AT_BLOB:
	/*{{{  write blob */

	blob = (ATermBlob) t;
	size = ATgetBlobSize(blob);
	sprintf(buf, "%08d:", size);
	memcpy(buf + 9, ATgetBlobData(blob), size);
	buf += 9 + size;

	/*}}}  */
	break;
    }
    return buf;
}

static char    *
topWriteToString(ATerm t, char *buf)
{
    if (ATgetType(t) == AT_LIST)
    {
	*buf++ = '[';
	buf = writeToString(t, buf);
	*buf++ = ']';
    }
    else
    {
	buf = writeToString(t, buf);
    }
    return buf;
}

/*}}}  */
/*{{{  static int textSize(ATerm t) */

/**
  * Calculate the size of a term in text format
  */

static int      topTextSize(ATerm t);

static int
textSize(ATerm t)
{
    char            numbuf[32];
    ATerm           trm;
    ATermList       list;
    ATermAppl       appl;
    Symbol          sym;
    int             i, size, arity;

    switch (ATgetType(t))
    {
    case AT_INT:
	sprintf(numbuf, "%d", ATgetInt((ATermInt) t));
	size = strlen(numbuf);
	break;

    case AT_REAL:
	sprintf(numbuf, "%f", ATgetReal((ATermReal) t));
	size = strlen(numbuf);
	break;

    case AT_APPL:
	appl = (ATermAppl) t;
	sym = ATgetSymbol(appl);
	arity = ATgetArity(sym);
	size = symbolTextSize(sym);
	for (i = 0; i < arity; i++)
	    size += topTextSize(ATgetArgument(appl, i));
	if (arity > 0)
	{
	    /* Add space for the ',' characters */
	    if (arity > 1)
		size += arity - 1;
	    /* and for the '(' and ')' characters */
	    size += 2;
	}
	break;

    case AT_LIST:
	list = (ATermList) t;
	if (ATisEmpty(list))
	    size = 0;
	else
	{
	    size = ATgetLength(list) - 1;	/* Space for the ','
						 * characters */
	    while (!ATisEmpty(list))
	    {
		size += topTextSize(ATgetFirst(list));
		list = ATgetNext(list);
	    }
	}
	break;

    case AT_PLACEHOLDER:
	trm = ATgetPlaceholder((ATermPlaceholder) t);
	size = topTextSize(trm);
	break;

    case AT_BLOB:
	size = 9 + ATgetBlobSize((ATermBlob) t);
	break;

    default:
	ATerror("textSize: Illegal type %d\n", ATgetType(t));
	return -1;
	break;
    }
    return size;
}

static int
topTextSize(ATerm t)
{
    int             size = textSize(t);

    if (ATgetType(t) == AT_LIST)
	size += 2;

    return size;
}

int
AT_calcTextSize(ATerm t)
{
    return topTextSize(t);
}

/*}}}  */

/**
  * Write a term into its text representation.
  */

char           *
ATwriteToString(ATerm t)
{
    int             size = topTextSize(t);
    char           *end;

    RESIZE_BUFFER(size);

    end = topWriteToString(t, buffer);
    *end = '\0';

    assert(end - buffer == size);

    return buffer;
}


/**
	* Write the text representation of a term into a buffer.
	*/

void
AT_writeToStringBuffer(ATerm t, char *buffer)
{
    topWriteToString(t, buffer);
}

/*}}}  */

/*{{{  static void store_char(int char) */

/**
	* Store a single character in the buffer
	*/

static void
store_char(int c, int pos)
{
    if (pos >= buffer_size)
	resize_buffer(buffer_size * 2);	/* Double the space */

    buffer[pos] = c;
}

/*}}}  */
/*{{{  static void fnext_char(int *c, FILE *f) */

/**
  * Read the next character from file.
  */

static void
fnext_char(int *c, FILE * f)
{
    *c = fgetc(f);
    if (*c == '\n')
    {
	line++;
	col = 0;
    }
    else
    {
	col++;
    }
    error_buf[error_idx++] = *c;
    error_idx %= ERROR_SIZE;
}

/*}}}  */
/*{{{  static void fskip_layout(int *c, FILE *f) */

/**
  * Skip layout from file.
  */

static void
fskip_layout(int *c, FILE * f)
{
    while (isspace(*c))
	fnext_char(c, f);
}

/*}}}  */
/*{{{  static void fnext_skip_layout(int *c, FILE *f) */

/**
  * Skip layout from file.
  */

static void
fnext_skip_layout(int *c, FILE * f)
{
	do
  {
		fnext_char(c, f);
	} while (isspace(*c));
}

/*}}}  */
/*{{{  static ATermList fparse_terms(int *c, FILE *f) */

/**
  * Parse a list of arguments.
  */

ATermList
fparse_terms(int *c, FILE * f)
{
	ATermList list;
	ATerm el = fparse_term(c, f);

	list = ATinsert(ATempty, el);

	while(*c == ',') {
		fnext_skip_layout(c, f);
		el = fparse_term(c, f);
		list = ATinsert(list, el);
	}

	return ATreverse(list);
}

/*}}}  */
/*{{{  static ATermAppl fparse_quoted_appl(int *c, FILE *f) */

/**
  * Parse a quoted application.
  */

static          ATermAppl
fparse_quoted_appl(int *c, FILE * f)
{
	int             len = 0;
	ATermList       args = ATempty;
	Symbol          sym;
	char           *name;

	/* First parse the identifier */
	fnext_char(c, f);
	while (*c != '"') {
		switch (*c) {
			case EOF:
				/*	case '\n':
						case '\r':
						case '\t':
				*/
				return NULL;
			case '\\':
				fnext_char(c, f);
				if (*c == EOF)
					return NULL;
				switch (*c) {
					case 'n':
						store_char('\n', len++);
						break;
					case 'r':
						store_char('\r', len++);
						break;
					case 't':
						store_char('\t', len++);
						break;
					default:
						store_char(*c, len++);
						break;
				}
				break;
			default:
				store_char(*c, len++);
				break;
		}
		fnext_char(c, f);
	}

	store_char('\0', len);
	
	name = strdup(buffer);
	if (!name)
		ATerror("fparse_quoted_appl: symbol to long.");
	
	fnext_skip_layout(c, f);

	/* Time to parse the arguments */
	if (*c == '(') {
		fnext_skip_layout(c, f);
		args = fparse_terms(c, f);
		if (args == NULL || *c != ')')
			return NULL;
		fnext_skip_layout(c, f);
	}

	/* Wrap up this function application */
	sym = ATmakeSymbol(name, ATgetLength(args), ATtrue);
	free(name);
	return ATmakeApplList(sym, args);
}

/*}}}  */
/*{{{  static ATermAppl fparse_unquoted_appl(int *c, FILE *f) */

/**
  * Parse a quoted application.
  */

static          ATermAppl
fparse_unquoted_appl(int *c, FILE * f)
{
    int             len = 0;
    Symbol          sym;
    ATermList       args = ATempty;
    char           *name;

    /* First parse the identifier */
    while (isalnum(*c) || *c == '-' || *c == '_')
    {
	store_char(*c, len++);
	fnext_char(c, f);
    }
    store_char('\0', len++);
    name = strdup(buffer);
    if (!name)
	ATerror("fparse_unquoted_appl: symbol to long.");

    fskip_layout(c, f);

    /* Time to parse the arguments */
    if (*c == '(')
    {
	fnext_skip_layout(c, f);
	args = fparse_terms(c, f);
	if (args == NULL || *c != ')')
	    return NULL;
	fnext_skip_layout(c, f);
    }

    /* Wrap up this function application */
    sym = ATmakeSymbol(name, ATgetLength(args), ATfalse);
    free(name);
    return ATmakeApplList(sym, args);
}

/*}}}  */
/*{{{  static void fparse_num_or_blob(int *c, FILE *f) */

/**
  * Parse a number or blob.
  */

static          ATerm
fparse_num_or_blob(int *c, FILE * f, ATbool canbeblob)
{
    char            num[32], *ptr = num;

    if (*c == '-')
    {
	*ptr++ = *c;
	fnext_char(c, f);
    }

    while (isdigit(*c))
    {
	*ptr++ = *c;
	fnext_char(c, f);
    }
    if (canbeblob && *c == ':')
    {
	/*{{{  Must be a blob! */

	int             i, size;
	char           *data;

	*ptr = 0;
	size = atoi(num);
	if (size == 0)
	    data = NULL;
	else
	{
	    data = malloc(size);
	    if (!data)
		ATerror("fparse_num_or_blob: no room for blob of size %d\n", size);
	}
	for (i = 0; i < size; i++)
	{
	    fnext_char(c, f);
	    data[i] = *c;
	}
	fnext_char(c, f);
	return (ATerm) ATmakeBlob(size, data);

	/*}}}  */
    }
    else if (*c == '.' || toupper(*c) == 'E')
    {
	/*{{{  A real number */

	if (*c == '.')
	{
	    *ptr++ = *c;
	    fnext_char(c, f);
	    while (isdigit(*c))
	    {
		*ptr++ = *c;
		fnext_char(c, f);
	    }
	}
	if (toupper(*c) == 'E')
	{
	    *ptr++ = *c;
	    fnext_char(c, f);
	    if (*c == '-')
	    {
		*ptr++ = *c;
		fnext_char(c, f);
	    }
	    while (isdigit(*c))
	    {
		*ptr++ = *c;
		fnext_char(c, f);
	    }
	}
	*ptr = '\0';
	return (ATerm) ATmakeReal(atof(num));

	/*}}}  */
    }
    else
    {
	/*{{{  An integer */

	*ptr = '\0';
	return (ATerm) ATmakeInt(atoi(num));

	/*}}}  */
    }
}

/*}}}  */

/*{{{  static ATerm fparse_term(int *c, FILE *f) */

/**
  * Parse a term from file.
  */

static          ATerm
fparse_term(int *c, FILE * f)
{
    ATerm           t, result = NULL;

    switch (*c)
    {
    case '"':
			result = (ATerm) fparse_quoted_appl(c, f);
			break;
			case '[':
				fnext_skip_layout(c, f);
				if (*c == ']')
					result = (ATerm) ATempty;
				else
					{
						result = (ATerm) fparse_terms(c, f);
						if (result == NULL || *c != ']')
							return NULL;
					}
				fnext_skip_layout(c, f);
				break;
			case '<':
				fnext_skip_layout(c, f);
				t = fparse_term(c, f);
				if (t != NULL && *c == '>')
					{
						result = (ATerm) ATmakePlaceholder(t);
						fnext_skip_layout(c, f);
					}
				break;
			default:
				if (isalpha(*c))
					result = (ATerm) fparse_unquoted_appl(c, f);
				else if (isdigit(*c))
					result = fparse_num_or_blob(c, f, ATtrue);
				else if (*c == '.' || *c == '-')
					result = fparse_num_or_blob(c, f, ATfalse);
				else
					result = NULL;
    }
		
    fskip_layout(c, f);

    if (*c == '{')
    {
			/* Term is annotated */
			fnext_skip_layout(c, f);
			if (*c != '}')
				{
					ATerm annos = (ATerm) fparse_terms(c, f);
					if (annos == NULL || *c != '}')
						return NULL;
					result = AT_setAnnotations(result, annos);
				}
			fnext_skip_layout(c, f);
    }
		
    return result;
}

/*}}}  */
/*{{{  ATerm readFromTextFile(FILE *file) */

/**
	* Read a term from a text file. The first character has been read.
	*/

ATerm
readFromTextFile(int *c, FILE *file)
{
	ATerm term;
	fskip_layout(c, file);
		
	term = fparse_term(c, file);

	if (term)
  {
		ungetc(*c, file);
	}
	else
  {
		int i;
		fprintf(stderr, "readFromTextFile: parse error at line %d, col %d:\n",
						line, col);
		for (i = 0; i < ERROR_SIZE; ++i)
		{
			char c = error_buf[(i + error_idx) % ERROR_SIZE];
			if (c)
				fprintf(stderr, "%c", c);
		}
		fprintf(stderr, "\n");
		fflush(stderr);
	}
		
	return term;
}

/*}}}  */
/*{{{  ATerm ATreadFromTextFile(FILE *file) */

/**
  * Read a term from a text file.
  */

ATerm
ATreadFromTextFile(FILE * file)
{
    int c;

    line = 0;
    col = 0;
    error_idx = 0;
    memset(error_buf, 0, ERROR_SIZE);

		fnext_char(&c, file);
		return readFromTextFile(&c, file);
}

/*}}}  */
/*{{{  ATerm ATreadFromFile(FILE *file) */

/**
	* Read an ATerm from a file that could be binary or text.
	*/

ATerm ATreadFromFile(FILE *file)
{
	int c;

	fnext_char(&c, file);
	if(c == 0) {
		/* Might be a BAF file */
		return AT_readFromBinaryFile(file);
	} else {
		/* Probably a text file */
		return readFromTextFile(&c, file);
	}
}

/*}}}  */


#define snext_char(c,s) ((*c) = *(*s)++)
#define sskip_layout(c,s) while(isspace(*c)) snext_char(c,s)
#define snext_skip_layout(c,s) do { snext_char(c, s); } while(isspace(*c))

/*{{{  static ATermList sparse_terms(int *c, char **s) */

/**
  * Parse a list of arguments.
  */

ATermList
sparse_terms(int *c, char **s)
{
	ATermList list;
	ATerm el = sparse_term(c, s);

	list = ATinsert(ATempty, el);

	while(*c == ',') {
		snext_skip_layout(c, s);
		el = sparse_term(c, s);
		list = ATinsert(list, el);
	}

	return ATreverse(list);
}

/*}}}  */
/*{{{  static ATermAppl sparse_quoted_appl(int *c, char **s) */

/**
  * Parse a quoted application.
  */

static          ATermAppl
sparse_quoted_appl(int *c, char **s)
{
    int             len = 0;
    ATermList       args = ATempty;
    Symbol          sym;
    char           *name;

    /* First parse the identifier */
    snext_char(c, s);
    while (*c != '"')
    {
	switch (*c)
	{
	case EOF:
/*	case '\n':
	case '\r':
	case '\t':
*/
	    return NULL;
	case '\\':
	    snext_char(c, s);
	    if (*c == EOF)
		return NULL;
	    switch (*c)
	    {
	    case 'n':
		store_char('\n', len++);
		break;
	    case 'r':
		store_char('\r', len++);
		break;
	    case 't':
		store_char('\t', len++);
		break;
	    default:
		store_char(*c, len++);
		break;
	    }
	    break;
	default:
	    store_char(*c, len++);
	    break;
	}
	snext_char(c, s);
    }

    store_char('\0', len);

    name = strdup(buffer);
    if (!name)
	ATerror("fparse_quoted_appl: symbol to long.");

    snext_skip_layout(c, s);

    /* Time to parse the arguments */
    if (*c == '(')
    {
	snext_skip_layout(c, s);
	args = sparse_terms(c, s);
	if (args == NULL || *c != ')')
	    return NULL;
	snext_skip_layout(c, s);
    }

    /* Wrap up this function application */
    sym = ATmakeSymbol(name, ATgetLength(args), ATtrue);
    free(name);
    return ATmakeApplList(sym, args);
}

/*}}}  */
/*{{{  static ATermAppl sparse_unquoted_appl(int *c, char **s) */

/**
  * Parse a quoted application.
  */

static          ATermAppl
sparse_unquoted_appl(int *c, char **s)
{
    int             len = 0;
    Symbol          sym;
    ATermList       args = ATempty;
    char           *name;

    /* First parse the identifier */
    while (isalnum(*c) || *c == '-' || *c == '_')
    {
	store_char(*c, len++);
	snext_char(c, s);
    }
    store_char('\0', len);
    name = strdup(buffer);
    if (!name)
	ATerror("fparse_unquoted_appl: symbol to long.");

    sskip_layout(c, s);

    /* Time to parse the arguments */
    if (*c == '(')
    {
	snext_skip_layout(c, s);
	args = sparse_terms(c, s);
	if (args == NULL || *c != ')')
	    return NULL;
	snext_skip_layout(c, s);
    }

    /* Wrap up this function application */
    sym = ATmakeSymbol(name, ATgetLength(args), ATfalse);
    free(name);
    return ATmakeApplList(sym, args);
}

/*}}}  */
/*{{{  static void sparse_num_or_blob(int *c, char **s) */

/**
  * Parse a number or blob.
  */

static          ATerm
sparse_num_or_blob(int *c, char **s, ATbool canbeblob)
{
    char            num[32], *ptr = num;

    if (*c == '-')
    {
	*ptr++ = *c;
	snext_char(c, s);
    }

    while (isdigit(*c))
    {
	*ptr++ = *c;
	snext_char(c, s);
    }
    if (canbeblob && *c == ':')
    {
	/*{{{  Must be a blob! */

	int             i, size;
	char           *data;

	*ptr = 0;
	size = atoi(num);
	if (size == 0)
	    data = NULL;
	else
	{
	    data = malloc(size);
	    if (!data)
		ATerror("fparse_num_or_blob: no room for blob of size %d\n", size);
	}
	for (i = 0; i < size; i++)
	{
	    snext_char(c, s);
	    data[i] = *c;
	}
	snext_char(c, s);
	return (ATerm) ATmakeBlob(size, data);

	/*}}}  */
    }
    else if (*c == '.' || toupper(*c) == 'E')
    {
	/*{{{  A real number */

	if (*c == '.')
	{
	    *ptr++ = *c;
	    snext_char(c, s);
	    while (isdigit(*c))
	    {
		*ptr++ = *c;
		snext_char(c, s);
	    }
	}
	if (toupper(*c) == 'E')
	{
	    *ptr++ = *c;
	    snext_char(c, s);
	    if (*c == '-')
	    {
		*ptr++ = *c;
		snext_char(c, s);
	    }
	    while (isdigit(*c))
	    {
		*ptr++ = *c;
		snext_char(c, s);
	    }
	}
	*ptr = '\0';
	return (ATerm) ATmakeReal(atof(num));

	/*}}}  */
    }
    else
    {
	/*{{{  An integer */

	*ptr = '\0';
	return (ATerm) ATmakeInt(atoi(num));

	/*}}}  */
    }
}

/*}}}  */

/*{{{  static ATerm sparse_term(int *c, char **s) */

/**
  * Parse a term from file.
  */

static          ATerm
sparse_term(int *c, char **s)
{
    ATerm           t, result = NULL;

    switch (*c)
    {
    case '"':
	result = (ATerm) sparse_quoted_appl(c, s);
	break;
    case '[':
	snext_skip_layout(c, s);
	if (*c == ']')
	    result = (ATerm) ATempty;
	else
	{
	    result = (ATerm) sparse_terms(c, s);
	    if (result == NULL || *c != ']')
		return NULL;
	}
	snext_skip_layout(c, s);
	break;
    case '<':
	snext_skip_layout(c, s);
	t = sparse_term(c, s);
	if (t != NULL && *c == '>')
	{
	    result = (ATerm) ATmakePlaceholder(t);
	    snext_skip_layout(c, s);
	}
	break;
    default:
	if (isalpha(*c))
	    result = (ATerm) sparse_unquoted_appl(c, s);
	else if (isdigit(*c))
	    result = sparse_num_or_blob(c, s, ATtrue);
	else if (*c == '.' || *c == '-')
	    result = sparse_num_or_blob(c, s, ATfalse);
	else
	    result = NULL;
    }

    sskip_layout(c, s);

    if (*c == '{')
    {
	/* Term is annotated */
	snext_skip_layout(c, s);
	if (*c != '}')
	{
	    ATerm           annos = (ATerm) sparse_terms(c, s);
	    if (annos == NULL || *c != '}')
		return NULL;
	    result = AT_setAnnotations(result, annos);
	}
	snext_skip_layout(c, s);
    }

    return result;
}

/*}}}  */
/*{{{  ATerm ATreadFromString(const char *string) */

/**
  * Read from a string.
  */

ATerm
ATreadFromString(const char *string)
{
    int             c;
    const char     *orig = string;
    ATerm           term;

    snext_skip_layout(&c, (char **) &string);

    term = sparse_term(&c, (char **) &string);

    if (!term)
    {
	int             i;
	fprintf(stderr, "ATreadFromString: parse error at or near:\n");
	fprintf(stderr, "%s\n", orig);
	for (i = 1; i < string - orig; ++i)
	    fprintf(stderr, " ");
	fprintf(stderr, "^\n");
    }
    else
	string--;

    return term;
}

/*}}}  */

/*{{{  void AT_markTerm(ATerm t) */

/**
  * Mark a term and all of its children.
  */

void
AT_markTerm(ATerm t)
{
	int             i, arity;
	Symbol          sym;
	ATerm          *current = mark_stack + 1;
	ATerm          *limit = mark_stack + mark_stack_size - MARK_STACK_MARGE;
	ATerm          *depth = mark_stack;
	
	mark_stack[0] = NULL;
	*current++ = t;
	
	while (ATtrue) {
		if (current > limit) {
			int current_index, depth_index;
			
			current_index = current - mark_stack;
			depth_index   = depth - mark_stack;
			
			/* We need to resize the mark stack */
			mark_stack_size = mark_stack_size * 2;
			mark_stack = (ATerm *) realloc(mark_stack, sizeof(ATerm) * mark_stack_size);
			if (!mark_stack)
				ATerror("cannot realloc mark stack to %d entries.\n", mark_stack_size);
			limit = mark_stack + mark_stack_size - MARK_STACK_MARGE;
			fprintf(stderr, "resized mark stack to %d entries\n", mark_stack_size);
			
			current = mark_stack + current_index;
			depth   = mark_stack + depth_index;
		}
			
		if (current > depth)
			depth = current;
			
		t = *--current;
			
		if (!t)
			break;

		if (IS_MARKED(t->header))
			continue;
		
		SET_MARK(t->header);
		
		switch (GET_TYPE(t->header)) {
			case AT_INT:
			case AT_REAL:
			case AT_BLOB:
				break;
				
			case AT_APPL:
				sym = ATgetSymbol((ATermAppl) t);
				AT_markSymbol(sym);
				arity = GET_ARITY(t->header);
				if (arity > MAX_INLINE_ARITY)
					arity = ATgetArity(sym);
				for (i = 0; i < arity; i++)
					*current++ = ATgetArgument((ATermAppl) t, i);
				break;
				
			case AT_LIST:
				if (!ATisEmpty((ATermList) t)) {
					*current++ = (ATerm) ATgetNext((ATermList) t);
					*current++ = ATgetFirst((ATermList) t);
				}
				break;
				
			case AT_PLACEHOLDER:
				*current++ = ATgetPlaceholder((ATermPlaceholder) t);
				break;
		}
	}
	STATS(mark_stats, depth - mark_stack);
	nr_marks++;
}

/*}}}  */
/*{{{  void AT_unmarkTerm(ATerm t) */

/**
  * Unmark a term and all of its children.
	* pre: the whole term must be marked.
  */

void
AT_unmarkTerm(ATerm t)
{
	int             i, arity;
	Symbol          sym;
	ATerm          *current = mark_stack + 1;
	ATerm          *limit = mark_stack + mark_stack_size - MARK_STACK_MARGE;
	ATerm          *depth = mark_stack;
	
	mark_stack[0] = NULL;
	*current++ = t;
	
	while (ATtrue) {
		if (current > limit) {
	    int current_index, depth_index;
			
	    current_index = current - mark_stack;
	    depth_index   = depth - mark_stack;
			
	    /* We need to resize the mark stack */
	    mark_stack_size = mark_stack_size * 2;
	    mark_stack = (ATerm *) realloc(mark_stack, sizeof(ATerm) * mark_stack_size);
	    if (!mark_stack)
				ATerror("cannot realloc mark stack to %d entries.\n", mark_stack_size);
	    limit = mark_stack + mark_stack_size - MARK_STACK_MARGE;
	    fprintf(stderr, "resized mark stack to %d entries\n", mark_stack_size);
			
	    current = mark_stack + current_index;
	    depth   = mark_stack + depth_index;
		}
		
		if (current > depth)
	    depth = current;
		
		t = *--current;
		
		if (!t)
	    break;
		
		CLR_MARK(t->header);
		
		switch (GET_TYPE(t->header)) {
			case AT_INT:
			case AT_REAL:
			case AT_BLOB:
				break;
					
			case AT_APPL:
				sym = ATgetSymbol((ATermAppl) t);
				AT_unmarkSymbol(sym);
				arity = GET_ARITY(t->header);
				if (arity > MAX_INLINE_ARITY)
					arity = ATgetArity(sym);
				for (i = 0; i < arity; i++)
					*current++ = ATgetArgument((ATermAppl) t, i);
				break;
				
			case AT_LIST:
				if (!ATisEmpty((ATermList) t)) {
					*current++ = (ATerm) ATgetNext((ATermList) t);
					*current++ = ATgetFirst((ATermList) t);
				}
				break;
				
			case AT_PLACEHOLDER:
				*current++ = ATgetPlaceholder((ATermPlaceholder) t);
				break;
		}
	}
}

/*}}}  */

/*{{{  static int calcCoreSize(ATerm t) */

/**
	* Calculate the term size in bytes.
	*/

static int
calcCoreSize(ATerm t)
{
    int             i, arity, size = 0;
    Symbol          sym;

    if (IS_MARKED(t->header))
	return size;

    SET_MARK(t->header);
    switch (ATgetType(t))
    {
    case AT_INT:
	size = 12;
	break;

    case AT_REAL:
    case AT_BLOB:
	size = 16;
	break;

    case AT_APPL:
	sym = ATgetSymbol((ATermAppl) t);
	arity = ATgetArity(sym);
	size = 8 + arity * 4;
	if (!AT_isMarkedSymbol(sym))
	{
	    size += strlen(ATgetName(sym)) + 1;
	    size += sizeof(struct SymEntry);
	    AT_markSymbol(sym);
	}
	for (i = 0; i < arity; i++)
	    size += calcCoreSize(ATgetArgument((ATermAppl) t, i));
	break;

    case AT_LIST:
	size = 16;
	while (!ATisEmpty((ATermList) t))
	{
		size += 16;
	    size += calcCoreSize(ATgetFirst((ATermList) t));
		t = (ATerm)ATgetNext((ATermList)t);
	}
	break;

    case AT_PLACEHOLDER:
	size = 12;
	size += calcCoreSize(ATgetPlaceholder((ATermPlaceholder) t));
	break;
    }
    return size;
}


/*}}}  */
/*{{{  int AT_calcCoreSize(ATerm t) */

/**
	* Calculate the term size in bytes.
	*/

int
AT_calcCoreSize(ATerm t)
{
    int result = calcCoreSize(t);
    AT_unmarkTerm(t);
    return result;
}


/*}}}  */
/*{{{  static int calcUniqueSubterms(ATerm t) */

/**
	* Calculate the number of unique subterms.
	*/

static int
calcUniqueSubterms(ATerm t)
{
	int    i, arity, nr_unique = 0;
	Symbol sym;
	ATermList list;
	
	if (IS_MARKED(t->header))
		return 0;
	
	switch (ATgetType(t)) {
		case AT_INT:
		case AT_REAL:
		case AT_BLOB:
		case AT_PLACEHOLDER:
			nr_unique = 1;
			break;
			
		case AT_APPL:
			nr_unique = 1;
			sym = ATgetSymbol((ATermAppl) t);
			arity = ATgetArity(sym);
			for (i = 0; i < arity; i++)
				nr_unique += calcUniqueSubterms(ATgetArgument((ATermAppl)t, i));
			break;
			
		case AT_LIST:
			nr_unique = 1;
			list = (ATermList)t;
			while(!ATisEmpty(list) && !IS_MARKED(list->header)) {
			  nr_unique += calcUniqueSubterms(ATgetFirst(list));
			  list = ATgetNext(list);
			}
		  break;
	}
	
	SET_MARK(t->header);

	return nr_unique;
}


/*}}}  */
/*{{{  int AT_calcUniqueSubterms(ATerm t) */

/**
	* Calculate the number of unique subterms.
	*/

int
AT_calcUniqueSubterms(ATerm t)
{
    int result = calcUniqueSubterms(t);
    AT_unmarkTerm(t);
    return result;
}


/*}}}  */
/*{{{  void AT_assertUnmarked(ATerm t) */

void AT_assertUnmarked(ATerm t)
{
	ATermAppl appl;
	Symbol sym;
	int i;

	assert(!IS_MARKED(t->header));
	switch(ATgetType(t)) {
		case AT_APPL:
			appl = (ATermAppl)t;
			sym = ATgetSymbol(appl);
			assert(!AT_isMarkedSymbol(sym));
			for(i=0; i<ATgetArity(sym); i++)
				AT_assertUnmarked(ATgetArgument(appl, i));
			break;

		case AT_LIST:
			if((ATermList)t != ATempty) {
				AT_assertUnmarked(ATgetFirst((ATermList)t));
				AT_assertUnmarked((ATerm)ATgetNext((ATermList)t));
			}
			break;

		case AT_PLACEHOLDER:
			AT_assertUnmarked(ATgetPlaceholder((ATermPlaceholder)t));
			break;
	}
}

/*}}}  */
/*{{{  void AT_assertMarked(ATerm t) */

void AT_assertMarked(ATerm t)
{
	ATermAppl appl;
	Symbol sym;
	int i;

	assert(IS_MARKED(t->header));
	switch(ATgetType(t)) {
		case AT_APPL:
			appl = (ATermAppl)t;
			sym = ATgetSymbol(appl);
			assert(AT_isMarkedSymbol(sym));
			for(i=0; i<ATgetArity(sym); i++)
				AT_assertMarked(ATgetArgument(appl, i));
			break;

		case AT_LIST:
			if((ATermList)t != ATempty) {
				AT_assertMarked(ATgetFirst((ATermList)t));
				AT_assertMarked((ATerm)ATgetNext((ATermList)t));
			}
			break;

		case AT_PLACEHOLDER:
			AT_assertMarked(ATgetPlaceholder((ATermPlaceholder)t));
			break;
	}
}

/*}}}  */

/*{{{  int AT_calcTermDepth(ATerm t) */

/**
	* Calculate the maximum depth of a term.
	*/

int AT_calcTermDepth(ATerm t)
{
	int arity, i, maxdepth, depth;
	ATermAppl appl;
	ATermList list;

  switch(ATgetType(t)) {
		case AT_INT:
		case AT_REAL:
		case AT_BLOB:
			return 1;

		case AT_APPL:
			maxdepth = 0;
			appl = (ATermAppl)t;
			arity = ATgetArity(ATgetSymbol(appl));
			for(i=0; i<arity; i++) {
				depth = AT_calcTermDepth(ATgetArgument(appl, i));
				if(depth > maxdepth)
					maxdepth = depth;
			}
			return maxdepth+1;

		case AT_LIST:
			maxdepth = 0;
			list = (ATermList)t;
			while(!ATisEmpty(list)) {
				depth = AT_calcTermDepth(ATgetFirst(list));
				if(depth > maxdepth)
					maxdepth = depth;
				list = ATgetNext(list);
			}
			return maxdepth+1;

		case AT_PLACEHOLDER:
			return 1+AT_calcTermDepth(ATgetPlaceholder((ATermPlaceholder)t));

		default:
			ATerror("Trying to calculate the depth of a free term.\n");
			return 0;
	}
}

/*}}}  */
