typedef enum { false, true } bool;

/* whether we should expand entities or not */
static bool expand = false;

/* whether to use verbose mode */
static bool verbose = false;

/* Global stack to store data */
static ATermList stack;
static ATermAppl atermdata;

/* global list to store enitities */
static ATermList entities;



void xaMemError(char *var);
static xmlEntityPtr xaGetEntity (xmlParserCtxtPtr ctxt, const xmlChar *name);
static void xaEntityDecl (xmlParserCtxtPtr ctxt, const xmlChar *name, int type, const xmlChar *publicID,			  const xmlChar *systemID, xmlChar *content);
static void xaStartDocument (xmlParserCtxtPtr ctxt);
static void xaEndDocument (xmlParserCtxtPtr ctxt);
static void xaStartElement (xmlParserCtxtPtr ctxt, xmlChar *fullname, xmlChar **atts);
static void xaEndElement (xmlParserCtxtPtr ctxt, const xmlChar *name);
static void xaCharacters (xmlParserCtxtPtr ctxt, const xmlChar *ch, int len);
static void xaComment (xmlParserCtxtPtr ctxt, const xmlChar *value);
static void xaWarning (xmlParserCtxtPtr ctxt, const xmlChar *value);
static void xaError (xmlParserCtxtPtr ctxt, const xmlChar *value);
static void xaFatalError (xmlParserCtxtPtr ctxt, const xmlChar *value);

static void xaParseQuotedAFun(int len, int i);
static void xaParseATermList(int len, int i);

int mySAXParseFile(xmlSAXHandlerPtr sax, void *user_data, char *filename);
int xml2aterm(char *filename);
