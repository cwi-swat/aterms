#ifndef _ADT_UTIL_H
#define _ADT_UTIL_H

#include <ADT.h>

typedef void     (*ADTEntryFunc) (ADTEntry, void *);
typedef ADTEntry (*ADTSubstFunc) (ADTEntry, void *);

void           ADTforeachEntry   (ADTEntries    entries,
				  ADTEntryFunc  func,
				  void         *user_data);

ADTEntries     ADTsubstitute     (ADTEntries    entries,
				  ADTSubstFunc  func,
				  void         *user_data);


#endif /* _ADT_UTIL_H */
