/*
    ATerm -- The ATerm (Annotated Term) library
    Copyright (C) 1998-2001  Stichting Mathematisch Centrum, Amsterdam,
                             The  Netherlands.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

    $Id$
*/

#include <stdio.h>
#include <aterm2.h>

static char version[] = "1.0";

#define arity(appl) (ATgetArity(ATgetAFun((ATermAppl) appl)))
#define streq(str1, str2) (!strcmp(str1, str2))
#define MAX_LABELS 256

typedef enum { AUTODETECT, TEXT, SHARED_TEXT, BINARY } Format;

/*{{{  ATerm visitATerm(ATerm tree, ATerm (*accept)(ATerm t, ATerm data), ATerm data) */

static ATerm
visitATerm(ATerm tree, ATerm (*accept)(ATerm t, ATerm data), ATerm data)
{
  switch(ATgetType(tree)) {
  case AT_APPL:
    /*{{{  Handle function application */

    {
      int arity = ATgetArity(ATgetAFun((ATermAppl)appl));
      ATerm arg;
      int i;

      for (i = 0; i < arity; i++) {
        arg  = ATgetArgument((ATermAppl) tree, i);
        arg  = visitATerm(arg, accept, data);
        tree = (ATerm) ATsetArgument((ATermAppl) tree, arg, i);
      }
    }

    /*}}}  */
    break;
  case AT_LIST:
    /*{{{  handle lists */

    {
      ATermList list = (ATermList) tree;
      ATermList newlist;
      ATerm annos = AT_getAnnotations(tree);

      for (newlist = ATempty; !ATisEmpty(list); list = ATgetNext(list)) {
        newlist = ATinsert(newlist, visitATerm(ATgetFirst(list), accept, data));
      }

      tree = (ATerm) ATreverse(newlist);

      if (annos) {
        tree = AT_setAnnotations(tree, annos);
      }
    }

    /*}}}  */
    break;
  default:
    break;
  }

  return accept(tree, data);
}

/*}}}  */

/*{{{  static ATerm removeAllAnnotations(ATerm tree, ATerm data) */

static ATerm removeAllAnnotations(ATerm tree, ATerm data)
{
  return ATremoveAllAnnotations(tree);
}

/*}}}  */
/*{{{  static ATerm removeAllAnnotationsRecursive(ATerm tree) */

static ATerm removeAllAnnotationsRecursive(ATerm tree)
{
   return visitATerm(tree, removeAllAnnotations, NULL);
}

/*}}}  */
/*{{{  static ATerm removeAnnotationRecursive(ATerm tree, ATerm label) */

static ATerm removeAnnotationRecursive(ATerm tree, ATerm label)
{
  return visitATerm(tree, ATremoveAnnotation, label);
}

/*}}}  */

/*{{{  static void usage(const char *myname, const char* myversion) */

static void usage(const char *myname, const char* myversion)
{
    fprintf (stderr,
        "Usage: %s [<options>]\n"
        "Options:\n"
        "  -i <input>     - Read input from file <input>            (Default: stdin)\n"
        "  -o <output>    - Write output to file <output>           (Default: stout)\n"
        "  -a             - Remove all annotations\n"
        "  -h             - Display help information (usage)\n"
        "  -l <label>     - Label of annotation to remove\n"
        "  -rb, -rt, -rs  - Choose between BAF, TEXT, and TAF input (Default: autodetect)\n"
        "  -wb, -wt, -ws  - Choose between BAF, TEXT, and TAF output(Default: -wb)\n"
        "  -v             - Print version information (i.e. %s)\n"
        "\n"
        "Use -l <label> multiple times to remove multiple labels.\n\n",
        myname, myversion);
}

/*}}}  */
/*{{{  static void requireArgument(int argc, char *argv[], int arg) */

static void requireArgument(int argc, char *argv[], int arg)
{
   if (arg > argc) {
     fprintf(stderr,
             "%s: %s option requires an argument.\n", argv[0], argv[arg]);
     exit(1);
   }
}

/*}}}  */

/*{{{  int main (int argc, char *argv[]) */

int main (int argc, char *argv[])
{
  ATerm  bottomOfStack;
  ATerm  term = NULL;
  ATbool remove_all = ATfalse;
  Format input_format = AUTODETECT;
  Format output_format = BINARY;
  ATermList labels;
  ATerm  label = NULL;
  FILE   *input  = stdin;
  FILE   *output = stdout;
  int    lcv;
  int    result = 0;
   
  ATinit(argc, argv, &bottomOfStack);    
  labels = ATempty;

  if (argc == 1) {
    /* no arguments */
    usage(argv[0], version);
    exit(1);
  }

  /* Parse commandline arguments */
  for (lcv = 1; lcv < argc; lcv++) {
    if (streq(argv[lcv], "-i")) {
      requireArgument(argc, argv, lcv); 
      input = fopen(argv[++lcv], "rb");
      if (input == NULL) {
        ATerror("%s: unable to open %s for reading.\n", argv[0], argv[lcv]);
      }
    }
    else if (streq(argv[lcv], "-o")) {
      requireArgument(argc, argv, lcv); 
      output = fopen(argv[++lcv], "wb");
      if (output == NULL) {
        ATerror("%s: unable to open %s for writing.\n", argv[0], argv[lcv]);
      }
    }
    else if (streq(argv[lcv], "-a")) {
      remove_all = ATtrue;
    }
    else if (streq(argv[lcv], "-l")) {
      requireArgument(argc, argv, lcv);
      label = NULL;
      label = ATparse(argv[++lcv]);
      if (label != NULL) {
        labels = ATinsert(labels, label);
      }
      else {
        ATerror("%s: %s is not a valid label.\n", argv[0], argv[lcv]);
        exit(1);
      }
    }
    else if (streq(argv[lcv], "-v")) {
      fprintf(stderr, "%s - Version: %s\n", argv[0], version);
      exit(0);
    }
    else if (streq(argv[lcv], "-h")) {
      usage(argv[0], version);
      exit(0);
    }
    else if (streq(argv[lcv], "-rb")) {
      input_format = BINARY;
    }
    else if (streq(argv[lcv], "-rt")) {
      input_format = TEXT;
    }
    else if (streq(argv[lcv], "-rs")) {
      input_format = SHARED_TEXT;
    }
    else if (streq(argv[lcv], "-wb")) {
      output_format = BINARY;
    }
    else if (streq(argv[lcv], "-wt")) {
      output_format = TEXT;
    }
    else if (streq(argv[lcv], "-ws")) {
      output_format = SHARED_TEXT;
    }
  }
 
  switch (input_format) {
    case AUTODETECT:
      term = ATreadFromFile(input);
      break;
    case TEXT:
      term = ATreadFromTextFile(input);
      break;
    case SHARED_TEXT:
      term = ATreadFromSharedTextFile(input);
      break;
    case BINARY:
      term = ATreadFromBinaryFile(input);
      break;
  }

  if (term == NULL) {
    ATerror("%s: illegal input!\n", argv[0]);
  }

  if (remove_all) {
    if (!ATisEmpty(labels)) {
      ATwarning("%s: -l option overruled by -a option.\n", argv[0]);
    }
    term = removeAllAnnotationsRecursive(term);
  } 
  else {
    for(; !ATisEmpty(labels); labels = ATgetNext(labels)) {
      term = removeAnnotationRecursive(term, ATgetFirst(labels));
    }
  }

  switch (output_format) {
      case AUTODETECT:
        /* We don't autodetect the output format, default is BINARY. */
      case BINARY:
        result = ATwriteToBinaryFile(term, output);
        break;
      case TEXT:
        result = ATwriteToTextFile(term, output);
        fprintf(output, "\n");
        break;
      case SHARED_TEXT:
        result = ATwriteToSharedTextFile(term, output) > 0;
        break;
  }

  if (!result) {
    ATerror("%s: write failed!\n", argv[0]);
  }

  return 0;
}

/*}}}  */
