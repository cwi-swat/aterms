#include <stdio.h>
#include <stdlib.h>
#include <aterm2.h>
#include "util.h"

static const char version[] = "0.1";

int main(int argc, char *argv[])
{
  ATerm bottom;
  int lcv;
  char *input = "-";
  char *output = "-";
  ATerm term = NULL;

  ATinit(argc, argv, &bottom);

  /* Parse commandline arguments */
  for (lcv = 1; lcv < argc; lcv++) {
    if (streq(argv[lcv], "-i")) {
      input = argv[++lcv];
    }
    else if (streq(argv[lcv], "-o")) {
      output = argv[++lcv];
    }
    else if (streq(argv[lcv], "-v")) {
      ATfprintf(stderr, "%s - version: %s\n", argv[0], version);
      exit(0);
    }
    else if (streq(argv[lcv], "-h")) {
      ATwarning("%s -i <file> -o <file> [vh]\n", argv[0]);
      exit(0);
    }
  }

  term = ATreadFromNamedFile(input);

  if (term != NULL && ATgetType(term) == AT_LIST) {
    term = (ATerm) ATreverse((ATermList) term);
  }

  if (term != NULL) {
    ATwriteToNamedBinaryFile(term, output);
  }
  else {
    ATerror("failed");
  }

  return 0;

}
