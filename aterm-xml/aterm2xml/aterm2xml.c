#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <aterm2.h>

#include "aterm2xml.h"
#include <globals.h>

static char myname [] = "aterm2xml";
static char myversion[] = "0.1";
static char myarguments[] = "hi:o:evVt";

/*{{{  void axXMLStringSafeConcat(char* str, int type) */

void axXMLStringSafeConcat(char* str, int type)
{
  int i;
  char *forbiddenchars = NULL;

  switch(type)
    {
    case XMLATTVALUE:

      forbiddenchars = strdup("&^<\"");
      break;

    case PLAINTEXT:

      forbiddenchars = strdup("&^<");
      break;
    }

  if(!forbiddenchars)
    {
      (void) axPrintErrorMsg(MEMERROR);
      return;
    }
  
  for(i=0; i < strlen(str); i++)
    {
      if(strchr(forbiddenchars,str[i]))
	{
	  fprintf(xmlfp,"&#%04d;", (int) str[i]);
	}
      
      else
	{
	  fprintf(xmlfp,"%c", str[i]);
	}
    }
  
  free(forbiddenchars);
  return;
}

/*}}}  */
/*{{{  void axXMLStringConcat(char* str) */

void axXMLStringConcat(char* str)
{
  fprintf(xmlfp, str);

  return;
}

/*}}}  */

/*{{{  int axParseTerm(ATerm at) */

int axParseTerm(ATerm at)
{
  int ret = 0;      /* the return code for error handling */

  /* check for an empty ATerm */
  if(!at)
    {
      return(axPrintErrorMsg(EMPTYTERM));
    }

  switch(ATgetType(at)) {

  case AT_APPL:
    ret = axParseAppl((ATermAppl) at);
    break;
  case AT_INT:
    ret = axParseInt((ATermInt) at);
    break;
  case AT_REAL:
    ret = axParseReal((ATermReal) at);
    break;
  case AT_LIST:
    ret = axParseList((ATermList) at);
    break;
  case AT_PLACEHOLDER:
    ret = axParsePlaceholder((ATermPlaceholder) at);
    break;
  case AT_BLOB:
    ret = axParseBLOB((ATermBlob) at);
    break;

  }

  return(ret);
}

/*}}}  */
/*{{{  int axParseInt(ATermInt ai) */

int axParseInt(ATermInt ai)
{
  if(verbose) { ATprintf("[INT ] %t\n", ai); }
  axXMLStringConcat(ATwriteToString((ATerm) ai));
  return(SUCCESS);
}

/*}}}  */
/*{{{  int axParseReal(ATermReal ar) */

int axParseReal(ATermReal ar)
{
  if(verbose) { ATprintf("[REAL] %t\n", ar); }
  axXMLStringConcat(ATwriteToString((ATerm) ar));
  return(SUCCESS);
}

/*}}}  */
/*{{{  int axParsePlaceholder(ATermPlaceholder ap) */

int axParsePlaceholder(ATermPlaceholder ap)
{
  int ret;

  if(verbose) { ATprintf("[PLHD] %t\n", ap); }

  if(expand)
    {
      axXMLStringConcat("<");
      axXMLStringConcat(ax_ph_name);
      axXMLStringConcat(">");

      ret = axParseTerm(ATgetPlaceholder(ap));
 
      axXMLStringConcat("</");
      axXMLStringConcat(ax_ph_name);
      axXMLStringConcat(">");
    }
  
  else
    {
      return(axPrintErrorMsg(DATAERROR));
    }

  return(axPrintErrorMsg(ret));
}

/*}}}  */
/*{{{  int axParseList(ATermList al) */

int axParseList(ATermList al)
{
  int i, ret = 0;

  if(verbose) { ATprintf("[LIST] %t\n", al); }

  if(expand)
    {
      axXMLStringConcat("<");
      axXMLStringConcat(ax_list_name);
      axXMLStringConcat(">");
    }

  /* for now, we simply traverse the list and parse all elements */
  for(i=0; i < ATgetLength(al); i++)
    {
      ret = axParseTerm(ATelementAt(al,i));
      axPrintErrorMsg(ret);
      
      axXMLStringConcat(" ");
    }

  if(expand)
    {
      axXMLStringConcat("</");
      axXMLStringConcat(ax_list_name);
      axXMLStringConcat(">");
    }

  return(ret);
}

/*}}}  */
/*{{{  int axParseBLOB(ATermBlob ab) */

int axParseBLOB(ATermBlob ab)
{
  if(verbose) { ATprintf("[BLOB] %t\n", ab); }

  if(expand)
    {
      axXMLStringConcat("<");
      axXMLStringConcat(ax_blob_name);
      axXMLStringConcat(">");
    }

  axXMLStringConcat("<![CDATA[");

  axXMLStringConcat(ATwriteToString((ATerm) ab));

  axXMLStringConcat("]]>");

  if(expand)
    {
      axXMLStringConcat("</");
      axXMLStringConcat(ax_blob_name);
      axXMLStringConcat(">");
    }

  return(SUCCESS);
}

/*}}}  */
/*{{{  int axParseAppl(ATermAppl aa) */

int axParseAppl(ATermAppl aa)
{
  int i, arity;
  int ret = 0;      /* the return code for error handling */
  char* afname;     /* the name of the ATermAppl */  
  AFun af;
  bool quoted;

  if(verbose) { ATprintf("[APPL] %t\n", aa); }

  af = ATgetAFun(aa);
  
  afname = ATgetName(af);
  arity  = ATgetArity(af);

  quoted = ATisQuoted(af);

  if(verbose) { printf("[APPL] name: %s, quoted: %d, arity: %d\n", afname, quoted, arity); }

  if(!quoted)
    {
      /* it seems the same constraints are applicable to both unquoted AFuns and TagNames
       * so we DONT NEED TO CHECK for illegal chars here
       * (a name begins with a letter and can contain alphanumeric and _ and -)
       */

      axXMLStringConcat("<");	  
      axXMLStringConcat(afname);
      axParseAttributes((ATerm) aa);

      if(arity == 0)
	{
	  axXMLStringConcat("/>");
	}

      else
	{
	  axXMLStringConcat(">");
	  
	  /* for each child do parseterm(child) */
	  for(i=0; i < arity; i++)
	    {
	      ret = axParseTerm(ATgetArgument(aa,i));
	    }
	  
	  axXMLStringConcat("</");	  
	  axXMLStringConcat(afname);
	  axXMLStringConcat(">");
	}
    }
  
  if(quoted)
    {
      /* here afname is a quoted AFun and can contain just about any characters.
       * in XML we are bound to AttValue characters, so we NEED TO CHECK THIS HERE!
       */	
      
      if(textmode && !(arity || AT_getAnnotations((ATerm) aa)))
	{
	  axXMLStringSafeConcat(afname, PLAINTEXT);
	  axXMLStringConcat(" ");
	}

      else
	{
	  axXMLStringConcat("<");
	  axXMLStringConcat(quoted_ATermAppl_name);
	  
	  axXMLStringConcat(" ");
	  
	  axXMLStringConcat(quoted_AFun_name);
	  axXMLStringConcat("=\"");
	  axXMLStringSafeConcat(afname, XMLATTVALUE);
	  axXMLStringConcat("\"");
	  
	  axParseAttributes((ATerm) aa);
	
	  if(!arity)
	    {  
	      axXMLStringConcat("/>");
	    }

	  else
	    {
	      axXMLStringConcat(">");
	      
	      /* for each child do parseterm(child) */
	      for(i=0; i < arity; i++)
		{
		  ret = axParseTerm(ATgetArgument(aa,i));
		}
	      
	      axXMLStringConcat("</");
	      axXMLStringConcat(quoted_ATermAppl_name);
	      axXMLStringConcat(">");
	    }
	}
    } 

  return(ret);
}

/*}}}  */
/*{{{  int axParseAttributes(ATerm at) */

int axParseAttributes(ATerm at)
{
  int i;
  ATerm annos, label, value;
  ATbool quoted_value;
  char *labelstr, *valuestr;

  annos = AT_getAnnotations(at);

  /* attribute-value in XML MUST be quoted with either " or '
   * Value cannot contain any of ^<&["']
   *
   * the ATerm can be either quoted or unquoted
   *
   * let's try quoted -> "" and unquoted -> ''
   *
   */

  if(annos)
    {
      if(verbose) { ATprintf("[ANNO] %t\n", annos); }

      for (i=0; i < ATgetLength(annos); i++)
	{
	  label = ATgetFirst((ATermList) ATelementAt((ATermList) annos,i));
	  value = ATgetLast( (ATermList) ATelementAt((ATermList) annos,i));

	  labelstr = ATwriteToString(label);

	  axXMLStringConcat(" ");
	  axXMLStringConcat(labelstr);
	  axXMLStringConcat("=");
	
	  quoted_value = ATisQuoted(ATgetAFun((ATermAppl) value));

	  quoted_value ? axXMLStringConcat("\"") : axXMLStringConcat("'");

	  switch(ATgetType(value))
	    {
	    case AT_INT:
	      valuestr = ATwriteToString(value);
	      break;
	    case AT_REAL:
	      valuestr = ATwriteToString(value);
	      break;
	    default:
	      valuestr = ATgetName(ATgetAFun((ATermAppl) value));
	    }

	  axXMLStringSafeConcat(valuestr, XMLATTVALUE);

	  quoted_value ? axXMLStringConcat("\"") : axXMLStringConcat("'");

	  if(verbose) { ATprintf("[ANNO] label: %t, value: %t, quoted_value: %d\n", label, value, quoted_value); }
	}
    }    

  return(SUCCESS);
}

/*}}}  */

/*{{{  int axPrintErrorMsg(int msg) */

int axPrintErrorMsg(int msg)
{
  switch(msg)
    {
    case SUCCESS:
      break;
    case FILENOTFOUND:
      fprintf(stderr, "Error: file not found\n");
      break;
    case EMPTYTERM:
      fprintf(stderr, "Error: empty ATerm\n");
      break;
    case MEMERROR:
      fprintf(stderr, "Error: memory error\n");
      break;
    case DATAERROR:
      fprintf(stderr, "Error: unexpected data\n");
      break;
    }
  return(msg);
}

/*}}}  */

/*{{{  int aterm2xml(ATerm at, char *filename) */

int aterm2xml(ATerm at, char *filename)
{
  FILE *tmpfp;
  int error = 0;


  if (strcmp(filename,"-") == 0) {
    tmpfp = stdout;
  }
  else {
    tmpfp = fopen(filename, "w");
  }

  if(tmpfp) {
    xmlfp = tmpfp;
  }

  error = axPrintErrorMsg(axParseTerm(at));

  fprintf(xmlfp, "\n");

  tmpfp = xmlfp;
  fclose(xmlfp);

  xmlfp = NULL;

  /* all done */

  return(error);
}

/*}}}  */

/*{{{  void usage(void) */

void usage(void)
{
  fprintf(stderr,
	  "Usage: %s -[etvV] -i arg -o arg\n"
	  "Options:\n"
	  "\t-e             Enable expand option (default off)\n"
	  "\t-h             Display usage information\n"
	  "\t-i filename    Read ATerm from filename (default stdin)\n"
	  "\t-t             Enable text option (default off)\n"
	  "\t-o filename    Write XML to filename (default stdout)\n"
	  "\t-v             Verbose mode\n"
	  "\t-V             Reveal version (i.e. %s)\n",
	  myname, myversion);
}

/*}}}  */

/*{{{  int main(int argc,char *argv[]) */

int main(int argc,char *argv[])
{
  ATerm aterm;
  char *infile  = "-";
  char *outfile = "-";
  int error = 0;
  int c;

  /* init */
  ATerm bottomOfStack;
  ATinit(argc, argv, &bottomOfStack);

  while ((c = getopt(argc, argv, myarguments)) != EOF) {
    switch (c) {
      case 'e':
	expand = true;
	break;
      case 'h':
	usage(); 
	exit(0);
      case 'i':
	infile = strdup(optarg); 
	break;
      case 'o':
	outfile = strdup(optarg);
	break;
      case 't':
	textmode = true;
	break;
      case 'V':
	fprintf(stderr,"%s v%s\n", myname, myversion);
	exit(0);
      default:
	usage();
	exit(1);
    }
  }

  /* load the aterm to be parsed from the inputfile */

  aterm = ATreadFromNamedFile(infile);
  
  if(verbose) { printf("ATerm read from %s\n", infile); }

  /* parse the aterm */
  error = aterm2xml(aterm, outfile);

  if(verbose) {
    printf("done\n");
  }

  return(error);
}

/*}}}  */

/* return codes
***************
SUCCESS       0
FILENOTFOUND -1
EMPTYTERM    -2
MEMERROR     -3
DATAERROR    -4
*/


