/*
 * $Id$
 */

#include <stdio.h>
#include <aterm1.h>
#include <aterm2.h>
#include <stdlib.h>
#include <unistd.h>

static char myname[]    = "atremove-annotation";
static char myversion[] = "1.2";
static char myarguments[] = "abhi:l:o:tV";

#define arity(appl) (ATgetArity(ATgetAFun((ATermAppl) appl)))
#define MAX_ANNOS 256

ATerm visitATerm(ATerm tree, ATerm (*accept)(ATerm t, ATerm data), ATerm data)
{
  switch(ATgetType(tree)) {
  case AT_APPL:
    {
      ATerm arg;
      int i;

      for(i = 0; i < arity(tree); i++) {
        arg  = ATgetArgument((ATermAppl) tree, i);
        arg  = visitATerm(arg, accept, data);
        tree = (ATerm) ATsetArgument((ATermAppl) tree, arg, i);
      }
    }
    break;
  case AT_LIST:
    {
      ATermList list = (ATermList) tree;
      ATermList newlist;
      ATerm annos = AT_getAnnotations(tree);

      for(newlist = ATempty; !ATisEmpty(list); list = ATgetNext(list)) {
        newlist = ATinsert(newlist, visitATerm(ATgetFirst(list), accept, data));
      }

      tree = (ATerm) ATreverse(newlist);

      if (annos) {
        tree = AT_setAnnotations(tree, annos);
      }
    }
    break;
  default:
    break;
  }

  return accept(tree, data);
}

ATerm removeAllAnnotations(ATerm tree, ATerm data)
{
  return ATremoveAllAnnotations(tree);
}

ATerm removeAllAnnotationsRecursive(ATerm tree)
{
   return visitATerm(tree, removeAllAnnotations, NULL);

}

ATerm removeAnnotationRecursive(ATerm tree, ATerm label)
{
  return visitATerm(tree, ATremoveAnnotation, label);
}

/*
    Usage: displays helpful usage information
 */
void usage(void)
{
    fprintf(stderr,
        "Usage: %s -abh -i <file> -l <label> -o <file> -tV . . .\n"
        "Options:\n"
        "\t-a              remove all annotations\n"
        "\t-b              binary output mode (default)\n"
        "\t-h              display help information (usage)\n"
        "\t-i filename     input from file (default stdin)\n"
        "\t-l              label of annotation to remove\n"
        "\t-o filename     output to file (default stdout)\n"
        "\t-t              text output mode\n"
        "\t-V              reveal program version (i.e. %s)\n"
        "\n"
        "Use -l <label> multiple times to remove multiple labels.\n\n",
        myname, myversion);
}

int main (int argc, char **argv)
{
  int c; /* option character */
  ATerm bottomOfStack;
  ATerm t;
  ATbool txtout = ATfalse;
  ATbool remove_all = ATfalse;
  char  *ATlibArgv[] = { "", "-silent"};
  char *annotation[256] = { "" };
  int count_annos = 0;
  char   *input_file_name  = "-";
  char   *output_file_name = "-";
 
  if(argc == 1) { /* no arguments */
    usage();
    exit(1);
  }

  while ((c = getopt(argc, argv, myarguments)) != EOF)
    switch (c) {
    case 'a':  remove_all = ATtrue;          break;
    case 'b':  txtout = ATfalse;             break;
    case 'h':  usage();                      exit(0);
    case 'i':  input_file_name  = optarg;    break;
    case 'l':  
      if(count_annos < MAX_ANNOS) {
        annotation[count_annos++] = optarg; 
      } else {
        ATwarning("%s: Maximum number of labels (%d) exceeded.\n",myname,MAX_ANNOS);
      }
      break;    
    case 'o':  output_file_name = optarg;    break;
    case 't':  txtout = ATtrue;              break;
    case 'V':  fprintf(stdout, "%s %s\n", myname, myversion);
      exit(0);
    default :  usage();                      exit(1);
    }
 
  ATinit(2, ATlibArgv, &bottomOfStack);    /* Initialize Aterm library */
  
  t = ATreadFromNamedFile(input_file_name);
  if(!t) {
    ATerror("%s: could not read term from input file %s\n", myname, input_file_name);
  }

  if (remove_all) {
    t = removeAllAnnotationsRecursive(t);
  } 
  else {
    if(!annotation[0] || !strcmp(annotation[0], "")) {
      usage();
      exit(1);
    } else {
      while(--count_annos >= 0) {
ATwarning("removing: %s\n", annotation[count_annos]);
        t = removeAnnotationRecursive(t, ATparse(annotation[count_annos]));
      }
    }
  }

  if(txtout) {
    ATwriteToNamedTextFile(t, output_file_name);
  } else {
    ATwriteToNamedBinaryFile(t, output_file_name);
  }

  return 0;
}
