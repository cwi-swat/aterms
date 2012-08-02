#define MEMBLOCK 256 /* size of a memory block */

#define SUCCESS       0
#define FILENOTFOUND -1
#define EMPTYTERM    -2
#define MEMERROR     -3
#define DATAERROR    -4

#define XMLATTVALUE 1
#define PLAINTEXT   2

typedef enum { false, true } bool;

/* the file in which we're going to store the data represented as xml */
static FILE *xmlfp;

/* whether we shoud use tags for <list>,<blob> and/or <placeholder> or not */
static bool expand = false;

/* whether to use verbose mode */
static bool verbose = false;

/* whether to use textmode */
static bool textmode = false;

int aterm2xml(ATerm at, char* filename);

void axXMLStringConcat(char* str);
void axXMLStringSafeConcat(char* str, int type);

int axParseTerm(ATerm at);
int axParseInt(ATermInt ai);
int axParseReal(ATermReal ar);
int axParsePlaceholder(ATermPlaceholder ap);
int axParseList(ATermList al);
int axParseBLOB(ATermBlob ab);
int axParseAppl(ATermAppl aa);
int axParseAttributes(ATerm at);
int axPrintErrorMsg(int msg);







