#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "aterm2.h"

/*{{{  static void usage(const char *prg) */

static void usage(const char *prg)
{
  fprintf(stderr, "Usage: %s [<options>] <file1> <file2>\n", prg);
  fprintf(stderr, "Options:\n");
  fprintf(stderr, "    --nodiffs | --diffs <diff-file>              (default: stdout)\n");
  fprintf(stderr, "    --notemplate | --template <template-file>    (default: stdout)\n");
  exit(1);
}

/*}}}  */

/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
  ATerm t1 = NULL, t2 = NULL;
  ATerm template = NULL, diffs = NULL;
  ATerm bottomOfStack;
  int i;
  FILE *diff_file = stdout;
  FILE *template_file = stdout;

  ATinit(argc, argv, &bottomOfStack);

  ATprotect(&t1);
  ATprotect(&t2);
  ATprotect(&template);
  ATprotect(&diffs);

  if (argc < 3) {
    usage(argv[0]);
  }

  for (i=1; i<argc-2; i++) {
    if (strcmp(argv[i], "--nodiffs") == 0) {
      diff_file = NULL;
    } else if (strcmp(argv[i], "--diffs") == 0) {
      diff_file = fopen(argv[++i], "wb");
      if (!diff_file) {
	fprintf(stderr, "could not open file %s for writing.\n", argv[i]);
	exit(1);
      }
    } else if (strcmp(argv[i], "--notemplate") == 0) {
      template_file = NULL;
    } else if (strcmp(argv[i], "--template") == 0) {
      template_file = fopen(argv[++i], "wb");
      if (!template_file) {
	fprintf(stderr, "could not open file %s for writing.\n", argv[i]);
	exit(1);
      }
    }
  }

  t1 = ATreadFromNamedFile(argv[argc-2]);
  if (!t1) {
    fprintf(stderr, "parse error in %s, giving up\n", argv[argc-2]);
    exit(1);
  }

  t2 = ATreadFromNamedFile(argv[argc-1]);
  if (!t2) {
    fprintf(stderr, "parse error in %s, giving up\n", argv[argc-1]);
    exit(1);
  }

  if (ATisEqual(t1, t2)) {
    return 0;
  }

  ATdiff(t1, t2, template_file ? &template : NULL, diff_file ? &diffs : NULL);

  if (template_file) {
    ATfprintf(template_file, "%t\n", template);
    fflush(template_file);
  }

  if (diff_file) {
    ATfprintf(diff_file, "%t\n", diffs);
    fflush(diff_file);
  }

  return 1;
}

/*}}}  */
