#include <stdio.h>

static void
usage(const char *prg)
{
  fprintf(stderr, "Usage: %s <generated api-file>\n", prg);
}

int
main(int argc, char *argv[])
{
  FILE *in, *out;
  char fileName[BUFSIZ];

  if (argc < 2) {
    usage(argv[0]);
    exit(1);
  }

  if (strcmp(argv[1], "-") == 0) {
    in = stdin;
  }
  else if (!(in=fopen(argv[1], "r"))) {
    perror(argv[1]);
    exit(1);
  }

  while (!feof(in)) {
    if (fscanf(in, "%s:", fileName) == 1) {
      fileName[strlen(fileName)-1] = '\0'; /* Remove ':' */
      fprintf(stderr, "extracting %s\n", fileName);
      out = fopen(fileName, "w");
      if (out == NULL) {
	perror(fileName);
	exit(1);
      }
      while (!feof(in)) {
	char c = fgetc(in);
        if (c == '\n') {
	  continue;
	} else if (c == '$') {
	  char lookahead = fgetc(in);
	  if (lookahead != '$') {
	    ungetc(lookahead, in);
	    continue;
	  }
	  fclose(out);
	  break;
	} else if (c == '#') {
	  fputc('\n', out);
	} else if (c == '@') {
	  fputc('\n', out);
	  continue;
	}
	fputc(c, out);
      }
    }
  }

  fclose(in);

  return 0;
}
