#ifndef _ADT__UTIL_H
#define _ADT__UTIL_H

#include <ADT.h>

typedef void     (*ADT_EntryFunc) (ADT_Entry, void *);
typedef ADT_Entry (*ADT_SubstFunc) (ADT_Entry, void *);

void           ADT_foreachEntry   (ADT_Entries    entries,
				  ADT_EntryFunc  func,
				  void         *user_data);

ADT_Entries     ADT_substitute     (ADT_Entries    entries,
				  ADT_SubstFunc  func,
				  void         *user_data);

#endif /* _ADT__UTIL_H */
