/*{{{  includes */

#include <stdio.h>

#include "ADT-utils.h"

/*}}}  */

/*{{{  ADTforeachEntry(ADTEntries entries, ADTEntryFunc func, void *user_data) */

void
ADTforeachEntry(ADTEntries entries, ADTEntryFunc func, void *user_data)
{
  while (ADThasEntriesHead(entries)) {
    ADTEntry entry = ADTgetEntriesHead(entries);
    func(entry, user_data);
    entries = ADTgetEntriesTail(entries);
  }
}

/*}}}  */

/*{{{  ADTsubstitute(ADTEntries entries, ADTSubstFunc substitute, void *user_data) */

ADTEntries
ADTsubstitute(ADTEntries entries, ADTSubstFunc substitute, void *user_data)
{
  ATermList todo  = (ATermList)ADTEntriesToTerm(entries);
  ATermList done = ATempty;

  while (!ATisEmpty(todo)) {
    ADTEntry before = ADTEntryFromTerm(ATgetFirst(todo));
    ADTEntry after  = substitute(before, user_data);

    if (after != NULL) {
      done = ATinsert(done, ADTEntryToTerm(after));
    }

    todo = ATgetNext(todo);
  }

  return ADTEntriesFromTerm((ATerm)ATreverse(done));
}

/*}}}  */
