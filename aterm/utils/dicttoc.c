#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <ctype.h>
#include <string.h>

#include "aterm2.h"

#define ROW_LENGTH 16

static char *code_prefix = NULL;
static char *file_prefix = NULL;

/* should we output a generation-timestamp */
static int opt_gen_date = 1;

/*{{{  static void usage(char *prg, int exit_code) */

static void usage(char *prg, int exit_code)
{
  fprintf(stderr, "usage: %s -dict <dict-file> [-code_prefix <code_prefix>]\n", prg);
  fprintf(stderr, "<dict-file> must be of the following format:\n");
  fprintf(stderr, "\t[afuns([alias3,symbol1],[alias4,symbol2]),\n");
  fprintf(stderr, "\t terms([[alias1,term1],[alias2,term2],...])]\n");
  exit(exit_code);
}

/*}}}  */

/*{{{  static void checkAFun(ATerm afun) */

static void checkAFun(ATerm afun)
{
  if (ATgetType(afun) != AT_APPL) {
    ATfprintf(stderr, "wrong afun spec: %t\n", afun);
    exit(1);
  }
}

/*}}}  */
/*{{{  static void checkAlias(ATerm alias) */

static void checkAlias(ATerm alias)
{
  if (ATgetType(alias) == AT_APPL) {
    ATermAppl appl = (ATermAppl)alias;
    AFun afun = ATgetAFun(appl);
    if (ATgetArity(afun) == 0 && !ATisQuoted(afun)) {
      return;
    }
  }

  ATfprintf(stderr, "incorrect alias: %t\n", alias);
  exit(1);
}

/*}}}  */

/*{{{  static void generateHeader(FILE *file, ATermList terms, ATermList afuns) */

static void generateHeader(FILE *file, ATermList terms, ATermList afuns)
{
  if (opt_gen_date) {
    time_t now = time(NULL);
    fprintf(file, "/*\n * Generated at %s", ctime(&now));
    fprintf(file, " */\n\n");
  }

  fprintf(file, "#ifndef __%s_H\n", code_prefix);
  fprintf(file, "#define __%s_H\n\n", code_prefix);
  fprintf(file, "#include <aterm2.h>\n\n");

  while (!ATisEmpty(afuns)) {
    ATerm afun, alias, pair = ATgetFirst(afuns);
    afuns = ATgetNext(afuns);

    if (!ATmatch(pair, "[<term>,<term>]", &alias, &afun)) {
      ATfprintf(stderr, "malformed [alias,afun] pair: %t\n", pair);
      exit(1);
    }

    checkAlias(alias);
    checkAFun(afun);

    ATfprintf(file, "extern AFun %t;\n", alias);
  }

  fprintf(file, "\n");

  while (!ATisEmpty(terms)) {
    ATerm term, alias, pair = ATgetFirst(terms);
    terms = ATgetNext(terms);

    if (!ATmatch(pair, "[<term>,<term>]", &alias, &term)) {
      ATfprintf(stderr, "malformed [alias,term] pair: %t\n", pair);
      exit(1);
    }

    checkAlias(alias);

    ATfprintf(file, "extern ATerm %t;\n", alias);
  }

  fprintf(file, "\nextern void init_%s();\n", code_prefix);

  fprintf(file, "\n#endif /* __%s_H */\n", code_prefix);
}

/*}}}  */
/*{{{  static void generateSource(FILE *file, ATermList terms, ATermList afuns) */

static void generateSource(FILE *file, ATermList terms, ATermList afuns)
{
  int len, row, col, index;
  unsigned char *data;
  ATerm all;
  ATermList list, term_aliases, afun_aliases;
  ATermList term_values, afun_values;

  if (opt_gen_date) {
    time_t now = time(NULL);
    fprintf(file, "/*\n * Generated at %s", ctime(&now));
    fprintf(file, " */\n\n");
  }

  fprintf(file, "#include \"%s.h\"\n\n", file_prefix);

  /*{{{  unzip term and afun lists */

  term_aliases = ATempty;
  afun_aliases = ATempty;
  term_values  = ATempty;
  afun_values  = ATempty;

  list = afuns;
  while (!ATisEmpty(list)) {
    ATerm alias, value;
    ATerm pair = ATgetFirst(list);
    list = ATgetNext(list);
    if (!ATmatch(pair, "[<term>,<term>]", &alias, &value)) {
      ATfprintf(stderr, "malformed [alias,afun] pair: %t\n", pair);
      exit(1);
    }
    afun_aliases = ATinsert(afun_aliases, alias);
    afun_values  = ATinsert(afun_values, value);
  }

  afun_aliases = ATreverse(afun_aliases);
  afun_values  = ATreverse(afun_values);

  list = terms;
  while (!ATisEmpty(list)) {
    ATerm alias, value;
    ATerm pair = ATgetFirst(list);
    list = ATgetNext(list);
    if (!ATmatch(pair, "[<term>,<term>]", &alias, &value)) {
      ATfprintf(stderr, "malformed [alias,term] pair: %t\n", pair);
      exit(1);
    }
    term_aliases = ATinsert(term_aliases, alias);
    term_values  = ATinsert(term_values, value);
  }

  term_aliases = ATreverse(term_aliases);
  term_values  = ATreverse(term_values);

  /*}}}  */

  /*{{{  generate symbol declarations */

  list = afun_aliases;
  while (!ATisEmpty(list)) {
    ATerm alias = ATgetFirst(list);
    list = ATgetNext(list);

    checkAlias(alias);

    ATfprintf(file, "AFun %t;\n", alias);
  }

  fprintf(file, "\n");

  /*}}}  */
  /*{{{  generate term declarations */

  list = term_aliases;
  while (!ATisEmpty(list)) {
    ATerm alias = ATgetFirst(list);
    list = ATgetNext(list);

    checkAlias(alias);

    ATfprintf(file, "ATerm %t = NULL;\n", alias);
  }

  /*}}}  */
  /*{{{  generate BAF data */

  ATfprintf(file, "\n/*\n");

  list = afuns;
  while (!ATisEmpty(list)) {
    ATermList pair = (ATermList)ATgetFirst(list);
    list = ATgetNext(list);
    ATfprintf(file, " * %t = %t\n", ATelementAt(pair, 0), ATelementAt(pair, 1));
  }
  ATfprintf(file, " *\n");

  list = terms;
  while (!ATisEmpty(list)) {
    ATermList pair = (ATermList)ATgetFirst(list);
    list = ATgetNext(list);
    ATfprintf(file, " * %t = %t\n", ATelementAt(pair, 0), ATelementAt(pair, 1));
  }
  ATfprintf(file, " *\n");
  ATfprintf(file, " */\n");

  ATfprintf(file, "\nstatic ATermList _%s = NULL;\n\n", code_prefix);

  all = ATmake("[<term>,<term>]", afun_values, term_values);
  data = (unsigned char *)ATwriteToBinaryString(all, &len);

  ATfprintf(file, "#define _%s_LEN %d\n\n", code_prefix, len);
  ATfprintf(file, "static char _%s_baf[_%s_LEN] = {\n", code_prefix, code_prefix, len);

  index = 0;

  for (row=0; index<len; row++) {
    for (col=0; col<ROW_LENGTH && index<len; col++) {
      fprintf(file, "0x%02X", data[index++]);
      if (index < len) {
	fprintf(file, ",");
      }
    }
    fprintf(file, "\n");
  }

  ATfprintf(file, "};\n\n");

  /*}}}  */
  /*{{{  generate init function */

  ATfprintf(file, "void init_%s()\n", code_prefix);
  ATfprintf(file, "{\n");
  ATfprintf(file, "  ATermList afuns, terms;\n\n");

  ATfprintf(file, "  _%s = (ATermList)ATreadFromBinaryString(_%s_baf, _%s_LEN);\n\n", 
	    code_prefix, code_prefix, code_prefix);
  ATfprintf(file, "  ATprotect((ATerm *)&_%s);\n\n", code_prefix);

  ATfprintf(file, "  afuns = (ATermList)ATelementAt(_%s, 0);\n\n", code_prefix);

  list = afuns;
  while (!ATisEmpty(list)) {
    ATerm alias;
    ATermList pair = (ATermList)ATgetFirst(list);
    list = ATgetNext(list);
    alias = ATelementAt(pair, 0);
    ATfprintf(file, "  %t = ATgetAFun((ATermAppl)ATgetFirst(afuns));\n", alias);
    ATfprintf(file, "  afuns = ATgetNext(afuns);\n");
  }

  ATfprintf(file, "\n  terms = (ATermList)ATelementAt(_%s, 1);\n\n", code_prefix);

  list = terms;
  index = 0;
  while (!ATisEmpty(list)) {
    ATerm alias;
    ATermList pair = (ATermList)ATgetFirst(list);
    list = ATgetNext(list);
    alias = ATelementAt(pair, 0);
    ATfprintf(file, "  %t = ATgetFirst(terms);\n", alias);
    ATfprintf(file, "  terms = ATgetNext(terms);\n");
  }

  ATfprintf(file, "}\n");

  /*}}}  */

}

/*}}}  */

/*{{{  static void generateCode(ATermList terms, ATermList afuns) */

static void generateCode(ATermList terms, ATermList afuns)
{
  FILE *source, *header;
  char path_source[BUFSIZ];
  char path_header[BUFSIZ];

  sprintf(path_source, "%s.c", file_prefix);
  sprintf(path_header, "%s.h", file_prefix);

  source = fopen(path_source, "wb");
  header = fopen(path_header, "wb");

  if (!source || !header) {
    fprintf(stderr, "could not open source and/or header files (%s,%s)\n",
	    path_source, path_header);
    exit(1);
  }

  generateHeader(header, terms, afuns);
  generateSource(source, terms, afuns);

  fclose(header);
  fclose(source);
}

/*}}}  */

/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
  int i;
  ATerm bottomOfStack;
  char *dict_name = NULL;
  ATerm dict;
  ATermList terms, afuns;
  static char code_buf[BUFSIZ];
  static char file_buf[BUFSIZ];

  for (i=1; i<argc; i++) {
    if (strcmp(argv[i], "-h") == 0) {
      usage(argv[0], 0);
    }
    else if (strcmp(argv[i], "-no-date") == 0) {
      opt_gen_date = 0;
    }
    else if (strcmp(argv[i], "-dict") == 0) {
      dict_name = argv[++i];
    }
    else if (strcmp(argv[i], "-code-prefix") == 0) {
      code_prefix = argv[++i];
    }
    else if (strcmp(argv[i], "-file-prefix") == 0) {
      file_prefix = argv[++i];
    }
  }

  if (dict_name == NULL) {
    usage(argv[0], 1);
  }

  /*{{{  Build code prefix */

  if (code_prefix == NULL) {
    char *ptr = strrchr(dict_name, '/');
    int index = 0;

    if (!ptr) {
      ptr = dict_name;
    }
    else {
      ptr++; /* skip '/' itself */
    }

    for(; *ptr; ptr++) {
      if (!isalnum((int)*ptr)) {
	code_buf[index++] = '_';
      } else {
	code_buf[index++] = *ptr;
      }
    }
    code_buf[index++] = '\0';
    code_prefix = code_buf;
  }

  /*}}}  */
  /*{{{  Build file prefix */

  if (file_prefix == NULL) {
    char *ptr = strrchr(dict_name, '/');
    int index = 0;

    if (!ptr) {
      ptr = dict_name;
    }
    else {
      ptr++; /* skip '/' itself */
    }

    for(; *ptr; ptr++) {
      if (!isalnum((int)*ptr) && *ptr != '-') {
	file_buf[index++] = '_';
      } else {
	file_buf[index++] = *ptr;
      }
    }
    file_buf[index++] = '\0';
    file_prefix = file_buf;
  }

  /*}}}  */

  ATinit(argc, argv, &bottomOfStack);

  dict = ATreadFromNamedFile(dict_name);

  if (!dict) {
    fprintf(stderr, "could not read dictionary from file: %s\n", dict_name);
    exit(1);
  }

  if (!ATmatch(dict, "[afuns([<list>]),terms([<list>])]", &afuns, &terms)) {
    usage(argv[0], 1);
  }

  generateCode(terms, afuns);

  return 0;
}

/*}}}  */
