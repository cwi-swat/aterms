
#include <stdio.h>

#include "assert-equals.h"


int assertEquals(int n, char *name, ATerm aterm1, ATerm aterm2) {
  fprintf(stderr, "[%03d] Test \"%s\" ", n, name);
  if (ATR_isEqual(aterm1, aterm2)) {
    fprintf(stderr, "succeeded.\n");
    return 1;
  }
  else {
    fprintf(stderr, "failed: ");
    if (ATR_isSet(aterm1)) {
      aterm1 = (ATerm)ATR_toList(aterm1);
    }
    if (ATR_isSet(aterm2)) {
      aterm2 = (ATerm)ATR_toList(aterm2);
    }
    ATfprintf(stderr, "expected %t, got: %t.\n", aterm2, aterm1);
    return 0;
  }
}

