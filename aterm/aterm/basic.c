
/**
  * Basic ATerm support.
  */

#include "aterm2.h"
#include "memory.h"

#include <assert.h>

/* error_handler is called when a fatal error is detected */
static void (*error_handler)(const char *format, va_list args) = NULL;

/*{{{  void Tinit(int argc, char *argv[], error, int *bottomOfStack) */

/**
  * Initialize the ATerm library.
  */

void Tinit(int argc, char *argv[], 
	   void (*error)(const char *format, va_list args), int *bottomOfStack)
{
  /* Check for reasonably sized ATerm (32 bits, 4 bytes)     */
  /* This check might break on perfectly valid architectures */
  /* that have char == 2 bytes, and sizeof(ATerm) == 2       */
  assert(sizeof(ATerm) == 4);

  error_handler = error;
  T_initMemory(argc, argv);
/*  TinitGC(argc, argv, bottomOfStack);
*/
}

/*}}}  */
/*{{{  void Terror(const char *format, ...) */

/**
  * A fatal error was detected.
  */

void Terror(const char *format, ...)
{
  va_list args;

  va_start(args, format);
  if(error_handler)
    error_handler(format, args);
  else {
    vfprintf(stderr, format, args);
    exit(1);
  }
}

/*}}}  */
