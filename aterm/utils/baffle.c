/*{{{  includes */

#include <stdio.h>
#include <assert.h>
#include <time.h>
#include <stdlib.h>

#include "bafio.h"
#include "util.h"
#include "aterm2.h"

/*}}}  */

/*{{{  defines */

#define AUTODETECT  0
#define TEXT        1
#define SHARED_TEXT 2
#define BINARY      3

/*}}}  */

char baffle_id[] = "$Id$";

/*{{{  static void usage(char *prg) */

static void usage(char *prg)
{
  fprintf(stderr,
	  "Usage: %s [-i <input>] [-o <output> | -c] [-v] [-rb | -rt | -rs] [-wb | -wt | -ws]\n\n"
	  "    -i <input>    - Read input from file <input>        (Default: stdin)\n"
	  "    -o <output>   - Write output to file <output>       (Default: stdout)\n"
	  "    -c            - Check validity of input-term\n"
	  "    -v            - Print version information\n"
	  "    -h            - Display help\n"
	  "    -rb, -rt, -rs - Choose between BAF, TEXT, and TAF input   (Default: autodetect)\n"
	  "    -wb, -wt, -ws - Choose between BAF, TEXT, and TAF output  (Default: -wb)\n", 
	  prg);
}

/*}}}  */

/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
  int lcv;
  ATbool check = ATfalse;
  int input_format = AUTODETECT;
  int output_format = BINARY;
  ATbool result = ATfalse;
  ATerm term = NULL;
  FILE *input = stdin;
  FILE *output = stdout;

  /* Initialize ATerm-library */
  ATerm bottomOfStack;

  ATinit(argc, argv, &bottomOfStack);

  /* Make sure <term> doesn't get cleaned up */
  ATprotect(&term);

  /* Parse commandline arguments */
  for (lcv = 1; lcv < argc; lcv++) {
    if (streq(argv[lcv], "-i")) {
      input = fopen(argv[++lcv], "rb");
      if (input == NULL)
	ATerror("%s: unable to open %s for reading.\n", argv[0], argv[lcv]);
    }
    else if (streq(argv[lcv], "-o")) {
      output = fopen(argv[++lcv], "wb");
      if (output == NULL)
	ATerror("%s: unable to open %s for writing.\n", argv[0], argv[lcv]);
    }
    else if (streq(argv[lcv], "-c")) {
      check = ATtrue;
    }
    else if (streq(argv[lcv], "-v")) {
      int major, minor;

      AT_getBafVersion(&major, &minor);
      ATfprintf(stderr, "%s - Version: %d.%d\n", argv[0], major, minor);
      exit(0);
    }
    else if (streq(argv[lcv], "-h")) {
      usage(argv[0]);
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

  if (!check) {
    switch (output_format) {
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
  }

  return 0;
}

/*}}}  */
