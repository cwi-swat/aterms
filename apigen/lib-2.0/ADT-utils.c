/*{{{  includes */

#include <stdio.h>

#include "ADT-utils.h"

/*}}}  */

/*{{{  ADT_foreachEntry(ADT_Entries entries, ADT_EntryFunc func, void *user_data) */

void
ADT_foreachEntry(ADT_Entries entries, ADT_EntryFunc func, void *user_data)
{
  while (ADT_hasEntriesHead(entries)) {
    ADT_Entry entry = ADT_getEntriesHead(entries);
    func(entry, user_data);
    entries = ADT_getEntriesTail(entries);
  }
}

/*}}}  */

/*{{{  ADT_substitute(ADT_Entries entries, ADT_SubstFunc substitute, void *user_data) */

ADT_Entries
ADT_substitute(ADT_Entries entries, ADT_SubstFunc substitute, void *user_data)
{
  ATermList todo  = (ATermList)ADT_EntriesToTerm(entries);
  ATermList done = ATempty;

  while (!ATisEmpty(todo)) {
    ADT_Entry before = ADT_EntryFromTerm(ATgetFirst(todo));
    ADT_Entry after  = substitute(before, user_data);

    if (after != NULL) {
      done = ATinsert(done, ADT_EntryToTerm(after));
    }

    todo = ATgetNext(todo);
  }

  return ADT_EntriesFromTerm((ATerm)ATreverse(done));
}

/*}}}  */
