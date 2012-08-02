
/* The checksum this utility prints is calculated using the
 * "RSA Data Security, Inc. MD5 Message-Digest Algorithm" (see RFC1321)
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "aterm2.h"

/*{{{  static void usage(const char *prg) */

static void usage(const char *prg)
{
  fprintf(stderr, "Usage: %s [<file>]\n", prg);
  exit(1);
}

/*}}}  */

/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
  ATerm t = NULL;

  ATinit(argc, argv, &t);

  if (argc == 1) {
    t = ATreadFromFile(stdin);
  } else if (strcmp(argv[1], "-h") == 0) {
    usage(argv[0]);
  } else if (argc == 2) {
    t = ATreadFromNamedFile(argv[1]);
  } else {
    usage(argv[0]);
  }

  if (!t) {
    fprintf(stderr, "parse error, giving up\n");
    exit(1);
  }

  ATprintf("%h\n", t);

  return 0;
}

/*}}}  */
