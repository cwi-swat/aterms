/*{{{  includes */

#include <ctype.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <time.h>
#include <assert.h>

#include "aterm2.h"
#include "atb-tool.h"

/*}}}  */

/*{{{  defines */

#define strprefix(a,b)  (!strncmp((a),(b),strlen(b)))
/*}}}  */
/*{{{  globals */

static char *prg = NULL;
static char *prefix = "";

/*}}}  */

/*{{{  char *ident_to_C(char *ident) */

/**
 * Convert a ToolBus identifier to a C identifier
 */

char *ident_to_C(char *ident)
{
  int i;
  static char buf[BUFSIZ];

  strncpy(buf, ident, BUFSIZ-1);
  for(i=0; buf[i]; i++)
    if(buf[i] == '-')
      buf[i] = '_';

  return buf;
}

/*}}}  */
/*{{{  char *uppercase(char *ident) */

/**
 * Convert a string to uppercase
 */

char *uppercase(char *ident)
{
  int i;
  static char buf[BUFSIZ];

  for(i=0; ident[i] && i < BUFSIZ; i++)
    buf[i] = toupper(ident[i]);
  buf[i] = '\0';

  return buf;
}

/*}}}  */

/*{{{  ATermList read_tifs(int fd, const char *tool) */

/**
 * Read some tifs from a file descriptor
 */

ATermList read_tifs(int fd, const char *tool)
{
  ATerm tif;
  ATermList tifs = ATempty;
  ATermPlaceholder ph;
  char *primitive, *name;

  do {
    tif = ATBreadTerm(fd);
    if(ATmatch(tif, "<appl(<placeholder>,<list>)>", &primitive, &ph, NULL)) {
      if(strprefix(primitive, "rec-") &&
	 ATmatch((ATerm)ph, "<appl>", &name) && streq(name, tool))
	tifs = ATinsert(tifs, tif);
    }
  } while(!ATmatch(tif, "end-of-tifs"));

  return tifs;
}

/*}}}  */
/*{{{  ATermList generalize_tifs(ATermList tifs) */

/**
 * Generalize incoming tifs.
 */

ATermList generalize_tifs(ATermList tifs)
{
  ATermList result = ATempty;

  while(!ATisEmpty(tifs)) {
    int i;
    char *name;
    ATermList newargs = ATempty;
    ATerm tif = ATgetFirst(tifs), newtif, tool;
    ATermAppl appl;

    if(ATmatch(tif, "rec-terminate(<term>,<term>)", NULL, NULL) ||
       ATmatch(tif, "rec-ack-event(<term>,<term>)", NULL, NULL)) {
      result = ATinsert(result, tif);
    } else if(ATmatch(tif, "<appl(<term>,<term>)>", &name, &tool, &appl)) {
      Symbol sym = ATgetSymbol(appl);
      for(i=ATgetArity(sym)-1; i>=0; --i) {
	ATerm arg = ATgetArgument(appl, i);
	if(ATisEqual(arg, ATparse("<int>")) ||
	   ATisEqual(arg, ATparse("<str>")))
	  newargs = ATinsert(newargs, arg);
	else
	  newargs = ATinsert(newargs, ATparse("<term>"));
      }

      newtif = ATmake("<appl(<term>,<appl(<list>)>)>", 
		      name, tool, ATgetName(sym), newargs);
      result = ATinsert(result, newtif);
    }

    tifs = ATgetNext(tifs);
  }
  return result;
}

/*}}}  */
/*{{{  ATermList unify_arguments(ATermList args1, ATermList args2) */

/**
 * Unify two lists of arguments.
 */

ATermList unify_arguments(ATermList args1, ATermList args2)
{
  int i = ATgetLength(args1);
  ATermList result = ATempty;


  if(ATgetLength(args2) != i)
    return NULL;

  for(--i; i>=0; i--) {
    ATerm arg1 = ATelementAt(args1, i);
    ATerm arg2 = ATelementAt(args2, i);

    /* When arg1 and arg2 are both <str> or <int>, leave them alone,
       otherwise use <term> */
    if((ATisEqual(arg1, ATparse("<str>")) || 
	ATisEqual(arg1, ATparse("<int>"))) && ATisEqual(arg1, arg2)) {
      result = ATinsert(result, arg1);
    } else {
      result = ATinsert(result, ATparse("<term>"));
    }
  }
  return result;
} 

/*}}}  */
/*{{{  ATermList unify_tifs(ATermList tifs) */

/**
 * Unify a list of tifs.
 */

ATermList unify_tifs(ATermList tifs)
{
  char *primitive;
  ATermList result = ATempty;
  ATerm tool;

  while(!ATisEmpty(tifs)) {
    ATermList rest;
    char *mgu_name, *cur_name;
    ATermList mgu_args, cur_args;
    ATerm mgu, tif = ATgetFirst(tifs);

    if(ATmatch(tif, "rec-terminate(<term>,<term>)", NULL, NULL) ||
       ATmatch(tif, "rec-ack-event(<term>,<term>)", NULL, NULL)) {
      result = ATinsert(result, tif);
      tifs = ATgetNext(tifs);
      continue;
    }

    /* Now built-in primitive, must be primitive rec-do or rec-eval */
    if(!ATmatch(tif, "<appl(<term>,<appl(<list>)>)>",
		&primitive, &tool, &mgu_name, &mgu_args))
      ATerror("illegal tif: %t\n", tif);

    rest = ATgetNext(tifs);
    tifs = ATempty;
    for( ; !ATisEmpty(rest); rest=ATgetNext(rest)) {
      char *cur_prim;
      ATerm cur_tool, cur = ATgetFirst(rest);

      if(!ATmatch(cur, "<appl(<term>,<appl(<list>)>)>", 
		  &cur_prim, &cur_tool, &cur_name, &cur_args) ||
	 !streq(cur_name, mgu_name)) {
	tifs = ATinsert(tifs, cur);
	continue;
      }

      assert(ATisEqual(cur_tool, tool));
      if(!streq(cur_prim, primitive))
	ATerror("primitives collide: %t vs. %t\n", tif, cur);

      mgu_args = unify_arguments(mgu_args, cur_args);
      if(!mgu_args)
	ATerror("arity mismatch: %t vs. %t\n", tif, cur);
    }

    mgu = ATmake("<appl(<list>)>", mgu_name, mgu_args);
    result = ATinsert(result, ATmake("<appl(<term>,<term>)>", 
				     primitive, tool, mgu));
  }

  return result;
}

/*}}}  */

/*{{{  void generate_prologue(FILE *f, char *tool, char *msg) */

/**
 * Generate file prologue (with timestamp!)
 */

void generate_prologue(FILE *f, char *tool, char *msg)
{
  time_t t = time(NULL);

  fprintf(f, "/**\n");
  fprintf(f, " * This file is generated by %s. Do not edit!\n", prg);
  fprintf(f, " * Generated from tifs for tool '%s' (prefix='%s')\n",
	  tool, prefix);
  fprintf(f, " * %s generated at %s", msg, ctime(&t));
  fprintf(f, " */\n\n");
}

/*}}}  */
/*{{{  void generate_argument_list(FILE *f, ATermList args) */

/**
 * Generate a list of arguments given a list of terms.
 */

void generate_argument_list(FILE *f, ATermList args)
{
  fprintf(f, "int conn");
  while(!ATisEmpty(args)) {
    ATerm arg = ATgetFirst(args);
    args = ATgetNext(args);

    fprintf(f, ", ");
    if(ATgetType(arg) != AT_PLACEHOLDER)
      fprintf(f, "ATerm");
    else {
      arg = ATgetPlaceholder((ATermPlaceholder)arg);
      if(ATmatch(arg, "int"))
	fprintf(f, "int");
      else if(ATmatch(arg, "str"))
	fprintf(f, "char *");
      else
	fprintf(f, "ATerm");
    }
  }
}

/*}}}  */
/*{{{  void generate_declarations(FILE *f, ATermList tifs) */

/**
 * Generate function declarations.
 */

void generate_declarations(FILE *f, ATermList tifs)
{
  char *name;
  ATermList args;

  fprintf(f, "/* Prototypes for functions called from the event handler */\n");

  while(!ATisEmpty(tifs)) {
    ATerm tif = ATgetFirst(tifs);
    tifs = ATgetNext(tifs);

    if(ATmatch(tif, "rec-terminate(<term>,<term>)", NULL, NULL)) {
      fprintf(f, "void %srec_terminate(int conn, ATerm);\n", prefix);
    } else if(ATmatch(tif, "rec-monitor(<term>,<term>)", NULL, NULL)) {
      fprintf(f, "void %srec_monitor(int conn, ATerm);\n", prefix);
    } else if(ATmatch(tif, "rec-ack-event(<term>,<term>)", NULL, NULL)) {
      fprintf(f, "void %srec_ack_event(int conn, ATerm);\n", prefix);
    } else if(ATmatch(tif, "rec-do(<term>,<appl(<list>)>)", 
		      NULL, &name, &args)) {
      fprintf(f, "void %s%s(", prefix, ident_to_C(name));
      generate_argument_list(f, args);
      fprintf(f, ");\n");
    } else if(ATmatch(tif, "rec-eval(<term>,<appl(<list>)>)",
		      NULL, &name, &args)) {
      fprintf(f, "ATerm %s%s(", prefix, ident_to_C(name));
      generate_argument_list(f, args);
      fprintf(f, ");\n");
    }
    /* Ignoring other patterns */
  }
}


/*}}}  */
/*{{{  void generate_header(FILE *f, ATermList tifs, char *tool) */

/**
 * Generate the header file.
 */

void generate_header(FILE *f, ATermList tifs, char *tool)
{
  char *protect_def = uppercase(ident_to_C(tool));

  generate_prologue(f, tool, "Headerfile");
  fprintf(f, "#ifndef _%s_H\n", protect_def);
  fprintf(f, "#define _%s_H\n\n", protect_def);
  fprintf(f, "#include <atb-tool.h>\n\n");
  generate_declarations(f, tifs);

  fprintf(f, "extern ATerm %s_handler(int conn, ATerm term);\n", 
	  ident_to_C(tool));
  fprintf(f, "extern ATerm %s_checker(int conn, ATerm sigs);\n", 
	  ident_to_C(tool));
  fprintf(f, "\n#endif\n");
}

/*}}}  */

/*{{{  int generate_signature(FILE *f, ATermList tifs) */

int generate_signature(FILE *f, ATermList tifs)
{
  int count = ATgetLength(tifs);

  fprintf(f, "#define NR_SIG_ENTRIES\t%d\n\n", count);

  fprintf(f, "static char *signature[NR_SIG_ENTRIES] = {\n");

  while(!ATisEmpty(tifs)) {
    ATerm tif = ATgetFirst(tifs);
    tifs = ATgetNext(tifs);
    ATfprintf(f, "  \"%t\",\n", tif);
  }

  fprintf(f, "};\n\n");

  return count;
}

/*}}}  */
/*{{{  void generate_checker(FILE *f, char *tool) */

/**
 * Generate the signature checker.
 */

void generate_checker(FILE *f, char *tool, int nrsigs)
{
  fprintf(f, "/* Check the signature of the tool '%s' */\n", tool);
  fprintf(f, "ATerm %s_checker(int conn, ATerm siglist)\n", 
	  ident_to_C(tool));
  fprintf(f, "{\n");
  fprintf(f, "  return ATBcheckSignature(siglist, signature, "
	  "NR_SIG_ENTRIES);\n");
  fprintf(f, "}\n\n");
}

/*}}}  */

/*{{{  void generate_variables(FILE *f, ATermList tifs) */

/**
 * Generate variable declarations
 */

void generate_variables(FILE *f, ATermList tifs)
{
  ATermList args;
  int i;
  int max_strings = 0;
  int max_ints    = 0;
  int max_terms   = 0;
  int nrints, nrstrings, nrterms;

  /* Calculate the maximum number of ints, strings, and terms. */
  while(!ATisEmpty(tifs)) {
    ATerm tif = ATgetFirst(tifs);
    tifs = ATgetNext(tifs);

    nrints    = 0;
    nrstrings = 0;
    nrterms   = 0;

    if(ATmatch(tif, "rec-terminate(<term>,<term>)", NULL, NULL)) {
      max_terms = MAX(1, max_terms);
    } else if(ATmatch(tif, "rec-ack-event(<term>,<term>)", NULL, NULL)) {
      max_terms = MAX(1, max_terms);
    } else if(ATmatch(tif, "rec-do(<term>,<appl(<list>)>)", 
		      NULL, NULL, &args) ||
	      ATmatch(tif, "rec-eval(<term>,<appl(<list>)>)",
		      NULL, NULL, &args)) {
      while(!ATisEmpty(args)) {
	ATerm arg = ATgetFirst(args);
	args = ATgetNext(args);
	if(ATgetType(arg) != AT_PLACEHOLDER) {
	  nrterms++;
	} else {
	  arg = ATgetPlaceholder((ATermPlaceholder)arg);
	  if(ATmatch(arg, "int"))
	    nrints++;
	  else if(ATmatch(arg, "str"))
	    nrstrings++;
	  else
	    nrterms++;
	}
      }
      max_ints    = MAX(nrints, max_ints);
      max_strings = MAX(nrstrings, max_strings);
      max_terms   = MAX(nrterms, max_terms);
    }
    /* Ignoring other patterns */		
  }

  /* Generate variable declarations */
  fprintf(f, "  /* We need some temporary variables during matching */\n");
  if(max_ints > 0) {
    fprintf(f, "  int i0");
    for(i=1; i<max_ints; i++)
      fprintf(f, ", i%d", i);
    fprintf(f, ";\n");
  }
  if(max_strings > 0) {
    fprintf(f, "  char *s0");
    for(i=1; i<max_strings; i++)
      fprintf(f, ", *s%d", i);
    fprintf(f, ";\n");
  }
  if(max_terms > 0) {
    fprintf(f, "  ATerm t0");
    for(i=1; i<max_terms; i++)
      fprintf(f, ", t%d", i);
    fprintf(f, ";\n");
  }
  fprintf(f, "\n");
}

/*}}}  */
/*{{{  void generate_match(FILE *f, ATerm pat, char *f, args, ATbool ret) */

/**
 * Generate matching code.
 */

void generate_match(FILE *f, ATerm pattern, char *func, 
		    ATermList args, ATbool ret)
{
  ATermList arglist;
  int nrints    = 0;
  int nrstrings = 0;
  int nrterms   = 0;

  ATfprintf(f, "  if(ATmatch(term, \"%t\"", pattern);

  /* Generate arguments of ATmatch */
  for(arglist = args; !ATisEmpty(arglist); arglist = ATgetNext(arglist)) {
    ATerm arg = ATgetFirst(arglist);

    if(ATgetType(arg) != AT_PLACEHOLDER) {
      fprintf(f, ", &t%d", nrterms++);
    } else {
      arg = ATgetPlaceholder((ATermPlaceholder)arg);
      if(ATmatch(arg, "int"))
	fprintf(f, ", &i%d", nrints++);
      else if(ATmatch(arg, "str"))
	fprintf(f, ", &s%d", nrstrings++);
      else
	fprintf(f, ", &t%d", nrterms++);
    }
  }

  fprintf(f, ")) {\n");

  /* Generate call to user function */
  fprintf(f, "    ");
  if(ret)
    fprintf(f, "return ");
  fprintf(f, "%s%s(conn", prefix, func);

  nrints    = 0;
  nrstrings = 0;
  nrterms   = 0;

  /* Generate arguments of user function */
  for(arglist = args; !ATisEmpty(arglist); arglist = ATgetNext(arglist)) {
    ATerm arg = ATgetFirst(arglist);

    if(ATgetType(arg) != AT_PLACEHOLDER)
      fprintf(f, ", t%d", nrterms++);
    else {
      arg = ATgetPlaceholder((ATermPlaceholder)arg);
      if(ATmatch(arg, "int"))
	fprintf(f, ", i%d", nrints++);
      else if(ATmatch(arg, "str"))
	fprintf(f, ", s%d", nrstrings++);
      else
	fprintf(f, ", t%d", nrterms++);
    }
  }

  fprintf(f, ");\n");

  if(!ret)
    fprintf(f, "    return NULL;\n");
  fprintf(f, "  }\n");
}

/*}}}  */
/*{{{  void generate_handler(FILE *f, ATermList tifs, char *tool) */

/**
 * Generate the event handler
 */

void generate_handler(FILE *f, ATermList tifs, char *tool)
{
  char *toolname = ident_to_C(tool);
  char *name;
  ATermList args;

  fprintf(f, "/* Event handler for tool '%s' */\n", tool);
  fprintf(f, "ATerm %s_handler(int conn, ATerm term)\n", toolname);
  fprintf(f, "{\n");
  fprintf(f, "  ATerm in, out;\n");

  generate_variables(f, tifs);

  while(!ATisEmpty(tifs)) {
    ATerm tif = ATgetFirst(tifs);
    tifs = ATgetNext(tifs);

    if(ATmatch(tif, "rec-terminate(<term>,<term>)", NULL, NULL)) {
      generate_match(f, ATparse("rec-terminate(<term>)"), "rec_terminate",
		     (ATermList)ATparse("[<term>]"), ATfalse);
    } else if(ATmatch(tif, "rec-monitor(<term>,<term>)", NULL, NULL)) {
      generate_match(f, ATparse("rec-monitor(<term>)"), "rec_monitor",
		     (ATermList)ATparse("[<term>]"), ATfalse);
    } else if(ATmatch(tif, "rec-ack-event(<term>,<term>)", NULL, NULL)) {
      generate_match(f, ATparse("rec-ack-event(<term>)"), "rec_ack_event",
		     (ATermList)ATparse("[<term>]"), ATfalse);
    } else if(ATmatch(tif, "rec-do(<term>,<appl(<list>)>)", NULL, &name, 
		      &args)) {
      ATerm pattern = ATmake("rec-do(<appl(<list>)>)", name, args);
      generate_match(f, pattern, ident_to_C(name), args, ATfalse);
    } else if(ATmatch(tif, "rec-eval(<term>,<appl(<list>)>)", NULL, &name, 
		      &args)) {
      ATerm pattern = ATmake("rec-eval(<appl(<list>)>)", name, args);
      generate_match(f, pattern, ident_to_C(name), args, ATtrue);
    } else {
      /* Ignoring other patterns */
    }
  }

  fprintf(f, "  if(ATmatch(term, \"rec-do(signature(<term>,<term>))\", "
	  "&in, &out)) {\n");
  fprintf(f, "    ATerm result = %s_checker(conn, in);\n", 
	  ident_to_C(tool));
  fprintf(f, "    if(!ATmatch(result, \"[]\"))\n");
  fprintf(f, "      ATfprintf(stderr, \"warning: not in input signature:"
	  "\\n\\t%%\\n\\tl\\n\", result);\n");
  fprintf(f, "    return NULL;\n");
  fprintf(f, "  }\n\n");
  fprintf(f, "  ATerror(\"tool %s cannot handle term %%t\", term);\n", tool);
  fprintf(f, "  return NULL; /* Silence the compiler */\n");

  fprintf(f, "}\n\n");
}

/*}}}  */
/*{{{  void generate_code(FILE *f, ATermList tifs, orig, char *tool, header) */

/**
 * Generate tif.c code file.
 */

void generate_code(FILE *f, ATermList tifs, ATermList original, 
		   char *tool, char *header)
{
  int count;

  generate_prologue(f, tool, "Implementation");
  fprintf(f, "#include \"%s\"\n\n", header);
  count = generate_signature(f, original);
  generate_handler(f, tifs, tool);
  generate_checker(f, tool, count);
}

/*}}}  */

/*{{{  static void usage(char *prg) */

/**
 * Print usage information and exit.
 */

static void usage(char *prg)
{
  fprintf(stderr, "usage: %s -tool <toolname> [options] <file>.tifs\n", prg);
  fprintf(stderr, "options:\n");
  fprintf(stderr, "    -handler <handler>      "
	  "specify default handler name\n");
  fprintf(stderr, "    -checker <checker>      "
	  "specify default signature checker name\n");
  fprintf(stderr, "    -output <basename>      "
	  "specify which .h and .c files to generate\n");
  fprintf(stderr, "    -prefix <prefix>        "
	  "specify function prefix to disambiguate between tools\n");

  exit(0);
}

/*}}}  */
/*{{{  int main(int argc, char *argv[]) */

/**
 * Process the commandline arguments and start tiffin'.
 */

int main(int argc, char *argv[])
{
  int i, fd;
  ATerm topOfStack;
  char *tool = NULL, *handler = NULL, *checker = NULL, *output = NULL;
  char *tifname = NULL;
  ATermList tifs, original;
  char *p, *codename, *headername;
  FILE *file;
  char buf[2][BUFSIZ];
  int  ATargc    = 2;
  char *ATargv[] = { "", "-silent" };

  prg = argv[0];

  for(i=1; i<argc; i++) {
    if(strprefix("-help", argv[i]))
      usage(argv[0]);
    else if(strprefix("-tool", argv[i]))
      tool = argv[++i];
    else if(strprefix("-handler", argv[i]))
      handler = argv[++i];
    else if(strprefix("-checker", argv[i]))
      checker = argv[++i];
    else if(strprefix("-output", argv[i]))
      output = argv[++i];
    else if (strprefix("-prefix", argv[i]))
      prefix = argv[++i];
    else {
      if(tifname)
	usage(argv[0]);
      tifname = argv[i];
    }
  }
  ATBinit(ATargc, ATargv, &topOfStack);

  if(!tifname || !tool)
    usage(argv[0]);


  strcpy(buf[0], output ? output : tool);

  p = strrchr(buf[0], '.');
  if(p)
    *p = '\0';

  strcpy(buf[1], buf[0]);
  strcat(buf[0], ".tif.c");
  strcat(buf[1], ".tif.h");
  codename = buf[0];
  headername = buf[1];


  fd = open(tifname, O_RDONLY);
  if(fd < 0)
    ATerror("cannot open tifs file %s\n", tifname);

  original = read_tifs(fd, tool);
  close(fd);
  tifs = generalize_tifs(original);
  tifs = unify_tifs(tifs);

  file = fopen(headername, "w"); 
  if(!file)
    ATerror("cannot open output file %s\n", headername);
  generate_header(file, tifs, tool);

  fclose(file);

  file = fopen(codename, "w"); 
  if(!file)
    ATerror("cannot open output file %s\n", codename);

  generate_code(file, tifs, original, tool, headername);
  fclose(file);

  return 0;
}

/*}}}  */
