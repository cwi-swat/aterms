#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include <ADT.h>
#include <aterm2.h>

static char myname[]    = "concat-adt";
static char myversion[] = "1.0";
static char myarguments[] = "ho:V";


void usage(void)
{
    fprintf(stderr,
	"\nConcat-adt concatenates lists of adt entries to form a single adt\n"
        "Usage: concat-adt -V -o <output> -h <file-1> ... <file-n>"
        "Options:\n"
        "\t-h              display help information (usage)\n"
        "\t-o filename     output to file (default stdout)\n"
        "\t-V              reveal program version (i.e. %s)\n"
        "\n",
        myversion);
}

#define MAX_ADTS 2500

int 
main (int argc, char **argv)
{
  int c; /* option character */
  ATerm bottomOfStack;
  char *inputs[MAX_ADTS] = { "-" };
  int  nInputs = 0;
  char *output = "-";
  int i;
  ADT_Entries total;

  if(argc == 1) { /* no arguments */
    usage();
    exit(1);
  }

  while ((c = getopt(argc, argv, myarguments)) != EOF) {
    switch (c) {
    case 'h':  
      usage();                      
      exit(0);
    case 'o':  
      output = strdup(optarg);    
      break;
    case 'V':  fprintf(stderr, "%s %s\n", myname, myversion);
      exit(0);
    default:
      usage();
      exit(1);
    }
  }

  /* The optind variable indicates where getopt has stopped */
  for(i = optind; i < argc; i++) {
    if (nInputs < MAX_ADTS) {
      inputs[nInputs++] = strdup(argv[i]);  
    } else {
      ATerror("Maximum number of %s adt files exceeded.\n", MAX_ADTS);
      exit(1);
    }
  }

  if (nInputs == 0) {
    nInputs = 1;
    inputs[0] = strdup("-");
  }

  ATinit(argc, argv, &bottomOfStack); 
  ADT_initADTApi();
 
  total = ADT_makeEntriesEmpty();
  for (--nInputs; nInputs >= 0; nInputs--) {
    ADT_Entries es = ADT_EntriesFromTerm(ATreadFromNamedFile(inputs[nInputs])); 

    if (es == NULL) {
      ATwarning("concat-adt: Unable to read anything from %s\n", 
		inputs[nInputs]);
    }
    else {
      total = ADT_concatEntries(es, total);
    }

    free(inputs[nInputs]);
  }

  ATwriteToNamedTextFile(ADT_EntriesToTerm(total), output);
 
  return 0;
}
