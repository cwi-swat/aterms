#include <aterm2.h>
#include <memory.h>
#include <util.h>
#include <stdlib.h>
#include <time.h>
#include <_aterm.h>

/*{{{  checks for rand48() suite */

#if HAVE_CONFIG_H
#  include "config.h"
#endif

#if HAVE_LRAND48 && HAVE_SRAND48
  /* Use the rand48() suite */
#else
#  ifdef WIN32
#  define lrand48()   rand()
#  define srand48(s)  srand(s)
#  else
#  define lrand48()   random()
#  define srand48(s)  srandom(s)
#  endif
#endif

/*}}}  */

#define MAX_SYMBOLS   1024;

/*{{{  variables */

static int nr_symbols = 5;
static int nr_terms   = 100;
static int open;
static int term_count;
static int magic_perc = 40;

static ATbool unique_leaves = ATtrue;
static ATbool binary = ATfalse;

static AFun  *symbols;

/*}}}  */

/*{{{  ATerm genterm(ATerm t) */

ATerm genterm(ATerm t)
{
  ATerm args[MAX_ARITY];
  int i, arity, index, maxarity, todo;
  static int next_leave = 0;

  maxarity = nr_terms-(term_count+open);
  if(nr_symbols < maxarity)
    maxarity = nr_symbols;

  /*arity = lrand48() % (maxarity);*/
  if(maxarity == 1) {
    term_count++;		
    if(t)
      return (ATerm)ATmakeAppl1(symbols[1], t);
    else {
      if(unique_leaves)
	return (ATerm)ATmakeInt(next_leave++);
      else
	return (ATerm)ATmakeAppl0(symbols[0]);
    }
  }

  arity = 1+(lrand48() % (maxarity-1));

  /*arity = lrand48() % nr_symbols;*/

  for(i=0; i<arity; i++)
    args[i] = NULL;

  /* Place the input term */
  args[lrand48() % arity] = t;
  if(t)
    todo = arity-1;
  else
    todo = arity;
  open += todo;


  for(i=0; i<todo; i++) {
    do {
      index = lrand48() % arity;
    } while(args[index] != NULL);

    if((term_count+open+1) < nr_terms && ((lrand48()%100) < magic_perc)) {
      args[index] = genterm(NULL);
    } else {
      if(unique_leaves)
	args[index] = (ATerm)ATmakeInt(next_leave++);
      else
	args[index] = (ATerm)ATmakeAppl0(symbols[0]);
      term_count++;
    }
    open--;
  }
  term_count++;
  return (ATerm)ATmakeApplArray(symbols[arity], args);
}

/*}}}  */
/*{{{  ATerm randgen() */

ATerm randgen()
{
  int i;
  ATerm t = NULL;

  symbols = (AFun *)calloc(nr_symbols, sizeof(AFun));

  if(!symbols)
    ATerror("could not allocate enough memory.\n");

  for(i=0; i<nr_symbols; i++) {
    char buf[16];
    sprintf(buf, "fun-%d", i);
    symbols[i] = ATmakeSymbol(buf, i, ATfalse);
    ATprotectSymbol(symbols[i]);
  }

  open = 0;
  while(term_count < nr_terms)
    t = genterm(t);

  return t;
}

/*}}}  */
/*{{{  void usage(char *prg) */

void usage(char *prg)
{
  fprintf(stderr, "usage: %s [-symbols <nr>] [-terms <nr>] [-wb|-wt] "
	  "[-seed <nr>] [-magic <perc>] [-unique-leaves] [-help]\n",
	  prg);
  exit(1);
}

/*}}}  */

/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
  int i;
  ATbool help = ATfalse;
  long seed = (long)time(NULL);
  ATerm stack, t;

  for(i=1; i<argc; i++) {
    if(streq(argv[i], "-symbols"))
      nr_symbols = atoi(argv[++i]);
    else if(streq(argv[i], "-terms"))
      nr_terms   = atoi(argv[++i]);
    else if(streq(argv[i], "-wb"))
      binary = ATtrue;
    else if(streq(argv[i], "-wt"))
      binary = ATfalse;
    else if(streq(argv[i], "-unique-leaves"))
      unique_leaves = ATtrue;
    else if(streq(argv[i], "-seed"))
      seed = atol(argv[++i]);
    else if(streq(argv[i], "-magic")) {
      magic_perc = atoi(argv[++i]);
      if(magic_perc < 0)
	magic_perc = 0;
      if(magic_perc > 100)
	magic_perc = 100;
    } else if(streq(argv[i], "-help") || streq(argv[i], "-h")) {
      usage(argv[0]);
      help = ATtrue;
    }
  }

  ATinit(argc, argv, &stack);

  if(!silent)
    fprintf(stderr, "seed = %ld\n", seed);

  srand48(seed);

  if(help)
    exit(0);

  t = randgen();

  if(binary)
    ATwriteToBinaryFile(t, stdout);
  else {
    ATfprintf(stdout, "%t\n", t);
  }

  return 0;
}

/*}}}  */
