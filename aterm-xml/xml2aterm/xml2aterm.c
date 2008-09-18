#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <aterm2.h>

#include <xmlmemory.h>
#include <tree.h>
#include <parser.h>
#include <parserInternals.h>

#include "globals.h"
#include "xml2aterm.h"

static char myname[] = "xml2aterm";
static char myversion[] = "0.1";
static char myarguments[] = "hei:o:vV";
static char* outfile = "-";

/*{{{  void xaMemError(char *var) */

void xaMemError(char *var)
{
  fprintf(stderr, "Memory error: malloc failed for %s", var);
  return;
}

/*}}}  */

/*{{{  static xmlEntityPtr xaGetEntity (xmlParserCtxtPtr ctxt, const xmlChar *name) */

static xmlEntityPtr xaGetEntity (xmlParserCtxtPtr ctxt, const xmlChar *name)
{
  fprintf(stderr, "xaGetEntity\n");
  return(NULL);
}

/*}}}  */

/*{{{  static void xaEntityDecl (xmlParserCtxtPtr ctxt, const xmlChar *name, int type, const xmlChar *publicID, \ */

static void xaEntityDecl (xmlParserCtxtPtr ctxt, const xmlChar *name, int type, const xmlChar *publicID, \
			  const xmlChar *systemID, xmlChar *content)
{
  fprintf(stderr, "xaEntityDecl\n");
  return;
}

/*}}}  */

/*{{{  static void xaStartDocument (xmlParserCtxtPtr ctxt) */

static void xaStartDocument (xmlParserCtxtPtr ctxt)
{
  if(verbose)
    {
      fprintf(stderr, "xaStartDocument\n");
    }

  /* INIT */

  stack = ATempty;

  if(verbose)
    {
      ATfprintf(stderr, "stack = %t\n", stack);
    }

  return;
}

/*}}}  */

/*{{{  static void xaEndDocument (xmlParserCtxtPtr ctxt) */

static void xaEndDocument (xmlParserCtxtPtr ctxt)
{
  if(verbose)
    {
      fprintf(stderr, "xaEndDocument\n");
    }

  atermdata = (ATermAppl) ATgetFirst(stack);

  if(verbose)
    {
      fprintf(stderr, "we created the following ATerm:\n");
    }
  
  ATwriteToNamedBinaryFile((ATerm) atermdata, outfile);
  
  return;
}

/*}}}  */

/*{{{  static void xaStartElement (xmlParserCtxtPtr ctxt, xmlChar *fullname, xmlChar **atts) */

static void xaStartElement (xmlParserCtxtPtr ctxt, xmlChar *fullname, xmlChar **atts)
{
  int i=0; int j=0;
  ATermList attr;
  ATermList annos = ATempty;
  ATermList attrs = ATempty;
  ATermAppl appl;
  
  if(verbose)
    {
      fprintf(stderr, "[START] %s\n", fullname);
    }

  if(atts)
    {
      while(atts[i])
	{
 	  annos = ATinsert(annos, (ATerm) ATmakeAppl0(ATmakeAFun((const char*) atts[i++],0,ATfalse)));
 	  annos = ATinsert(annos, (ATerm) ATmakeAppl0(ATmakeAFun((const char*) atts[i++],0,ATtrue)));
	}

      for(j=0; j < ATgetLength(annos); j+=2)
	{
	  attr = ATempty;
	  attr = ATinsert(attr, ATelementAt(annos,j));
	  attr = ATinsert(attr, ATelementAt(annos,j+1));

	  attrs = ATinsert(attrs, (ATerm) attr);
	}
    }

  if(verbose)
    {
      ATfprintf(stderr, "attrs = %t\n", attrs);
    }

  appl = ATmakeAppl0(ATmakeAFun((const char*) fullname,0,ATfalse));

  if(!ATisEmpty(attrs))
  {
    appl = (ATermAppl) AT_setAnnotations((ATerm) appl, (ATerm) attrs);
  }

  if(verbose)
    {
      ATfprintf(stderr, "appl = %t\n", appl);
    }

  stack = ATinsert(stack,(ATerm) appl);

  if(verbose)
    {
      ATfprintf(stderr, "stack = %t\n", stack);
    }

  return;
}

/*}}}  */

/*{{{  static void xaParseQuotedAFun(int len, int i) */

static void xaParseQuotedAFun(int len, int i)
{
  ATermAppl appl;
  ATerm anns;

  anns = AT_getAnnotations(ATelementAt(stack,i));
  appl = (ATermAppl) ATgetLast((ATermList) ATgetFirst((ATermList) anns));

  if(anns)
    {
      anns = (ATerm) ATgetNext((ATermList) anns);
      if(ATisEmpty((ATermList) anns))
	{
	  anns = NULL;
	}
    }

  if(anns)
    {
      if(i == 0)
	{
	  appl = (ATermAppl) AT_setAnnotations((ATerm) appl, anns);
  	}
      else
	{
	  appl = ATmakeApplList(ATmakeAFun(ATgetName(ATgetAFun(appl)),i,ATtrue), ATgetSlice(stack,0,i));
	  appl = (ATermAppl) AT_setAnnotations((ATerm) appl, anns);
	}
    }
  else
    {
      if(i != 0)
	{
	  appl = ATmakeApplList(ATmakeAFun(ATgetName(ATgetAFun(appl)),i,ATtrue), ATgetSlice(stack,0,i));
	}
    }
  
  stack = ATgetSlice(stack, i+1, ATgetLength(stack));
  stack = ATinsert(stack, (ATerm) appl);

  return;
}

/*}}}  */

/*{{{  static void xaParseATermList(int len, int i) */

static void xaParseATermList(int len, int i)
{
  int j;
  ATermList atl = ATempty;

  for(j=0;j<i;j++)
    {
      atl = ATinsert(atl, ATelementAt(stack,j));
    }

  stack = ATgetSlice(stack,i+1,ATgetLength(stack));
  stack = ATinsert(stack, (ATerm) atl);

  return;
}

/*}}}  */

/*{{{  static void xaEndElement (xmlParserCtxtPtr ctxt, const xmlChar *name) */

static void xaEndElement (xmlParserCtxtPtr ctxt, const xmlChar *name)
{
  int i,len;
  ATermAppl appl;
  ATerm anns;
  bool foundit;
  ATermList tmp;

  if(verbose)
    {
      fprintf(stderr, "[END] %s\n", name);
    }

  len = ATgetLength(stack);
  i = 0;
  foundit = false;

  tmp = stack;

  while(i < len && !foundit)
    {
      if(!strcmp((const char*) name, ATgetName(ATgetAFun(ATgetFirst(tmp)))))
	{
	  foundit = true;
	}
      else
	{
	  i++;
	  tmp = ATgetNext(tmp);
	}
    }

  if(verbose)
    {
      ATfprintf(stderr, "stack = %t\n", stack);
      fprintf(stderr, "len = %d\n", len);
      fprintf(stderr, "i   = %d\n", i);
    }

  if(!strcmp((const char*) name, quoted_ATermAppl_name))
    {
      xaParseQuotedAFun(len, i);
      return;
    }

  if(!strcmp((const char*) name, ax_list_name))
    {
      xaParseATermList(len, i);
      return;
    }

  anns = AT_getAnnotations(ATelementAt(stack,i));

  if(i != 0)
    {
      appl = ATmakeApplList(ATmakeAFun((char*) name,i,ATfalse), ATreverse(ATgetSlice(stack,0,i)));
    }
  else
    {
      appl = ATmakeAppl0(ATmakeAFun((char*) name,0,ATfalse));
    }

  if(anns)
    {
      appl = (ATermAppl) AT_setAnnotations((ATerm) appl, anns);
    }

  if(verbose)
    {
      ATfprintf(stderr, "appl = %t\n", appl);
    }

  stack = ATgetSlice(stack,i+1,len);
  stack = ATinsert(stack, (ATerm) appl);

  if(verbose)
    {
      ATfprintf(stderr, "stack = %t\n", stack);
    }

  return;
}

/*}}}  */

/*{{{  static void xaCharacters (xmlParserCtxtPtr ctxt, const xmlChar *ch, int len) */

static void xaCharacters (xmlParserCtxtPtr ctxt, const xmlChar *ch, int len)
{
  static char layout[] = "\n\t";
  char *clean  = NULL;
  int i,j;
  bool foundSpecialChar = false;
  char c;

  clean = malloc(sizeof(char)*len);

  if(!clean)
    {
      fprintf(stderr, "mem error blaat\n");
      return;
    }
  
  for(i = 0, j = 0; i < len; i++)
    {
      c = ch[i];

      if(!strchr(layout, c))
	{
	  if(c == ' ')
	    {
	      if(!foundSpecialChar)
		{
                  clean[j++] = c;
		  foundSpecialChar = true;
		}
	    }
	  else
	    {
              clean[j++] = c;
	      foundSpecialChar = false;
	    }
	}
      else
	{
	  foundSpecialChar = true;
	}
    }

  clean[j] = '\0';

  j = len;

  if(strcmp(clean,""))
    {
      while(strchr(" \n\t",clean[j]))
	{
	  clean[j--] = '\0';
	}
    }
    
  if(strcmp(clean,""))
    {
      len = strlen(clean);
      for(j = 0; j < len && strchr(" \n\t",clean[j]); j++);
      for(i = 0; i < len - j; i++)
        {
          clean[i] = clean[i+j];
        }
      clean[i] = '\0';
    }

  if(strcmp(clean,""))
    {
      if(verbose)
	{
	  fprintf(stderr, "[CHARS] %s\n", clean);
	}
  
      /*    
      for(i=0;i<ATgetLength(stack);i++)
	{
	  if(!strcmp(ATgetName(ATgetAFun(ATelementAt(stack,i))), ax_list_name))
	    {
	      foundlist = true;
	      i = ATgetLength(stack);
	    }
	}
      */

      /*
      if(foundlist)
	{
	  tok = strtok(clean, " ");
	  
	  while(tok)
	    {
	      
	      at = ATparse(tok);
	      
	      if(at)
		{
		  switch(ATgetType(at))
		    {
		      
		    case AT_INT:
		    case AT_REAL:
		      stack = ATinsert(stack,(ATerm) ATmakeAppl0(ATmakeAFun(tok,0,ATfalse)));
		      break;
		      
		    case AT_APPL:
		      stack = ATinsert(stack,(ATerm) ATmakeAppl0(ATmakeAFun(tok,0,ATfalse)));
		      break;
		      
		    default:
		      break;
		      
		    }
		}
	      
	      tok = strtok(NULL, " ");
	    }
	  
	  free(tok);
	  
	}      
      */

      stack = ATinsert(stack,(ATerm) ATmakeAppl0(ATmakeAFun(clean,0,ATtrue)));
    }
  
  if(verbose)
    {
      ATfprintf(stderr, "stack = %t\n",stack);
    }
  
  free(clean);  
  return;
}

/*}}}  */

/*{{{  static void xaComment (xmlParserCtxtPtr ctxt, const xmlChar *value) */

static void xaComment (xmlParserCtxtPtr ctxt, const xmlChar *value)
{
  fprintf(stderr, "Comment: %s\n", value);
  return;
}

/*}}}  */

/*{{{  static void xaWarning (xmlParserCtxtPtr ctxt, const xmlChar *value) */

static void xaWarning (xmlParserCtxtPtr ctxt, const xmlChar *value)
{
  fprintf(stderr, "Warning: %s\n", value);
  return;
}

/*}}}  */

/*{{{  static void xaError (xmlParserCtxtPtr ctxt, const xmlChar *value) */

static void xaError (xmlParserCtxtPtr ctxt, const xmlChar *value)
{
  fprintf(stderr, "Error: %s\n", value);
  return;
}

/*}}}  */

/*{{{  static void xaFatalError (xmlParserCtxtPtr ctxt, const xmlChar *value) */

static void xaFatalError (xmlParserCtxtPtr ctxt, const xmlChar *value)
{
  fprintf(stderr, "Fatal Error: %s\n", value);
  return;
}

/*}}}  */

/*{{{  The SAX handler structure */

static xmlSAXHandler my_handler = {
  0, /* internalSubset */
  0, /* isStandalone */
  0, /* hasInternalSubset */
  0, /* hasExternalSubset */
  0, /* resolveEntity */
  (getEntitySAXFunc) xaGetEntity, /* getEntity */
  (entityDeclSAXFunc) xaEntityDecl, /* entityDecl */
  0, /* notationDecl */
  0, /* attributeDecl */
  0, /* elementDecl */
  0, /* unparsedEntityDecl */
  0, /* setDocumentLocator */
  (startDocumentSAXFunc) xaStartDocument,  /* startDocument */
  (endDocumentSAXFunc) xaEndDocument, /* endDocument */
  (startElementSAXFunc) xaStartElement, /* startElement */
  (endElementSAXFunc) xaEndElement, /* endElement */
  0, /* reference */
  (charactersSAXFunc) xaCharacters, /* characters */
  0, /* ignorableWhiteSpace */
  0, /* processingInstruction */
  (commentSAXFunc) xaComment, /* comment */
  (warningSAXFunc) xaWarning, /* warning */
  (errorSAXFunc) xaError, /* error */
  (fatalErrorSAXFunc) xaFatalError, /* fatalError */
  0, /* (getParameterEntitySAXFunc) xaGetParameterEntity */ /* getParameterEntity */
  0, /* (cdataBlockSAXFunc) xaCdataBlock */ /* cdataBlock */
  0, /* (externalSubsetSAXFunc) xaExternalSubset */ /* externalSubset */
};

/*}}}  */

/*{{{  int xml2aterm(char *filename) */

int xml2aterm(char *filename)
{
  xmlParserCtxt my_state;
  
  return mySAXParseFile(&my_handler, &my_state, filename);
}

/*}}}  */

/*{{{  void usage(void)  */

void usage(void) 
{
  fprintf(stderr,
          "Usage: %s -[etvV] -i arg -o arg\n"
          "Options:\n"
          "\t-e             Enable expand option (default off)\n"
          "\t-h             Display usage information\n"
          "\t-i filename    Read XML from filename (default stdin)\n"
          "\t-o filename    Write ATerm to filename (default stdout)\n"
          "\t-v             Verbose mode\n"
          "\t-V             Reveal version (i.e. %s)\n", myname, myversion);
}

/*}}}  */

/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
  int err;
  char* infile = "-";
  int c;

  ATerm bottomOfStack;

  ATinit(argc, argv, &bottomOfStack);

  /* protect the global variables from garbage collection */
  ATprotect((ATerm*)(void*)&stack);
  ATprotect((ATerm*)(void*)&atermdata);
  ATprotect((ATerm*)(void*)&entities);

  while ((c = getopt(argc, argv, myarguments)) != EOF) {
    switch (c) {
      case 'h':
	usage();
	exit(0);
      case 'e':
	expand = true;
	break;
      case 'i':
	infile = strdup(optarg);
	break;
      case 'o':
	outfile = strdup(optarg);
	break;
      case 'v':
	verbose = true;
	break;
      case 'V':
	fprintf(stderr,"%s v%s\n", myname, myversion);
	exit(0);
      default:
	usage();
	exit(1);
    }
  }

  err = xml2aterm(infile);

  switch(err) {
    case 0:
      break;
    case -1:
      fprintf(stderr, "there was an error parsing %s\n", infile);
      break;
    case -2:
      fprintf(stderr, "oke, parsing of %s was successful\n", infile);
      break;
    default:
      fprintf(stderr, "unknown error while parsing %s\n", infile);
  }

  return err;
}

/*}}}  */

/*{{{  int mySAXParseFile(xmlSAXHandlerPtr sax, void *user_data, char *filename) */

int mySAXParseFile(xmlSAXHandlerPtr sax, void *user_data, char *filename)
{
  int ret;
  xmlParserCtxtPtr ctxt;

  ctxt = xmlCreateFileParserCtxt(filename);

  if(!ctxt) { return(-1); }

  ctxt->sax = sax;
  ctxt->userData = user_data;

  xmlParseDocument(ctxt);

  if(ctxt->wellFormed) { ret = 0; }
  else { ret = -1; }

  if(sax != NULL) { ctxt->sax = NULL; }

  xmlFreeParserCtxt(ctxt);

  return(ret);
}

/*}}}  */


