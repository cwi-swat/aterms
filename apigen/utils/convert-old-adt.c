#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include <ADT.h>
#include <OLDADT.h>
#include <aterm2.h>

static char myname[]    = "convert-old-adt";
static char myversion[] = "1.0";
static char myarguments[] = "hi:o:V";


/*{{{  void usage(void) */

void usage(void)
{
    fprintf(stderr,
	"\nConcat-adt concatenates lists of adt entries to form a single adt\n"
        "Usage: concat-adt -V -o <output> -h <file-1> ... <file-n>"
        "Options:\n"
        "\t-h              display help information (usage)\n"
	"\t-i              ADT file in old format (default stdin)\n"
        "\t-o filename     ADT file in new format (default stdout)\n"
        "\t-V              reveal program version (i.e. %s)\n"
        "\n",
        myversion);
}

/*}}}  */

/*{{{  ADT_Entry convertEntry(OLDADT_Entry old) */

ADT_Entry convertEntry(OLDADT_Entry old)
{
  return ADT_makeEntryConstructor(OLDADT_getEntrySort(old),
				  OLDADT_getEntryAlternative(old),
				  OLDADT_getEntryTermPattern(old));
}

/*}}}  */
/*{{{  ADT_Entries convertEntries(OLDADT_Entries old) */

ADT_Entries convertEntries(OLDADT_Entries old)
{
  ADT_Entries new = ADT_makeEntriesEmpty();

  for (; !OLDADT_isEntriesEmpty(old); old = OLDADT_getEntriesTail(old)) {
    OLDADT_Entry oldEntry = OLDADT_getEntriesHead(old);

    new = ADT_makeEntriesList(convertEntry(oldEntry), new);
  }

  return ADT_EntriesFromTerm((ATerm) ATreverse((ATermList) new));
}

/*}}}  */

/*{{{  int main (int argc, char **argv) */

int main (int argc, char **argv)
{
  int c; /* option character */
  ATerm bottomOfStack;
  char *input = "-";
  char *output = "-";
  OLDADT_Entries oldEntries;

  if(argc == 1) { /* no arguments */
    usage();
    exit(1);
  }

  while ((c = getopt(argc, argv, myarguments)) != EOF) {
    switch (c) {
    case 'h':  
      usage();                      
      exit(0);
    case 'i':
      input = strdup(optarg);
      break;
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

  ATinit(argc, argv, &bottomOfStack); 
  ADT_initADTApi();
  OLDADT_initOLDADTApi();

  oldEntries = OLDADT_EntriesFromTerm(ATreadFromNamedFile(input));

  if (oldEntries) {
    ADT_Entries newEntries = convertEntries(oldEntries);
    ATwriteToNamedTextFile(ADT_EntriesToTerm(newEntries), output);

    return 0;
  }
  else {
    return 1;
  }
}

/*}}}  */
