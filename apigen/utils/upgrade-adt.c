#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include <ADT.h>
#include <ADT10.h>
#include <aterm2.h>

static char myname[]    = "upgrade-adt";
static char myversion[] = "1.0";
static char myarguments[] = "hi:lo:V";

static ATbool checkForListIdioms = ATfalse;

/*{{{  void usage(void) */

void usage(void)
{
    fprintf(stderr,
	"\n%s converts ADT files in old style to new style ADT files\n\n"
        "Usage: %s -i <input> -o <output> -[Vhl]\n"
        "Options:\n"
        "\t-h              display help information (usage)\n"
	"\t-i              ADT file in old format (default stdin)\n"
	"\t-l              Check for list idioms and convert them (default off)\n"
        "\t-o filename     ADT file in new format (default stdout)\n"
        "\t-V              reveal program version (i.e. %s)\n"
        "\n",
	myname, myname,
        myversion);
}

/*}}}  */

/*{{{  ATbool isEmptyListPattern(ATerm pattern) */

ATbool isEmptyListPattern(ATerm pattern)
{
  return ATisEqual(pattern, ATempty);
}

/*}}}  */
/*{{{  ADT_Entry detectListIdiom(ATerm listType, ATerm pattern) */

ADT_Entry detectListIdiom(ATerm listType, ATerm pattern)
{
  if (ATgetType(pattern) == AT_LIST) {
    ATermList listPattern = (ATermList) pattern;

    if (ATgetLength(pattern) == 2) {
      ATerm head = ATgetFirst(listPattern);
      ATerm tail = ATgetFirst(ATgetNext(listPattern));
      ATerm sort = NULL;
      ATerm elemSort = NULL;

      if (ATgetType(head) == AT_PLACEHOLDER) {
	ATermPlaceholder ph = (ATermPlaceholder) head;
	ATerm type = ATgetPlaceholder(ph);

	if (ATgetType(type) == AT_APPL) {
	  ATermList args = ATgetArguments((ATermAppl) type);

	  if (ATgetLength(args) == 1) {
	    elemSort = ATgetFirst(args);
	  }
	}
      }

      if (elemSort != NULL 
	  && ATgetType(tail) == AT_PLACEHOLDER) {
	ATermPlaceholder ph = (ATermPlaceholder) tail;
	ATerm type = ATgetPlaceholder(ph);

	if (ATgetType(type) == AT_LIST) {
	  ATermList list = (ATermList) type;
	  if (ATgetLength(list) == 1) {
	    ATerm tailType = ATgetFirst(list);

	    if (ATgetType(tailType) == AT_APPL) {
	      ATermList args = ATgetArguments((ATermAppl) tailType);
	      if (ATgetLength(args) == 1) {
		sort = ATgetFirst(args);
		if (ATisEqual(sort, listType)) {
		   return ADT_makeEntryList(listType, elemSort);
		}
	      }
	    }
	  }
	}
      }
    }
  }

  return NULL;
}

/*}}}  */
/*{{{  ADT_Entry convertEntry(ADT10_Entry old) */

ADT_Entry convertEntry(ADT10_Entry old)
{
  ATerm pattern = ADT10_getEntryTermPattern(old);
  ADT_Entry new = NULL;

  if (!checkForListIdioms || !isEmptyListPattern(pattern)) {
    if (checkForListIdioms) {
      new = detectListIdiom(ADT10_getEntrySort(old), pattern);
    }

    if (new == NULL) {
      return ADT_makeEntryConstructor(ADT10_getEntrySort(old),
				      ADT10_getEntryAlternative(old),
				      ADT10_getEntryTermPattern(old));
    }
  }

  return new;
}

/*}}}  */
/*{{{  ADT_Entries convertEntries(ADT10_Entries old) */

ADT_Entries convertEntries(ADT10_Entries old)
{
  ADT_Entries new = ADT_makeEntriesEmpty();

  for (; !ADT10_isEntriesEmpty(old); old = ADT10_getEntriesTail(old)) {
    ADT10_Entry oldEntry = ADT10_getEntriesHead(old);
    ADT_Entry newEntry = convertEntry(oldEntry);

    if (newEntry != NULL) {
      new = ADT_makeEntriesList(convertEntry(oldEntry), new);
    }
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
  ADT10_Entries oldEntries;

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
    case 'l':
      checkForListIdioms = ATtrue;
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
  ADT10_initADT10Api();

  oldEntries = ADT10_EntriesFromTerm(ATreadFromNamedFile(input));

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
