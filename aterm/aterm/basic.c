/**
  * Basic ATerm support.
  */

#include "aterm2.h"
#include "memory.h"
#include "symbol.h"

#include <assert.h>

/* error_handler is called when a fatal error is detected */
static void (*error_handler)(const char *format, va_list args) = NULL;

/*{{{  void ATinit(int argc, char *argv[], error, int *bottomOfStack) */

/**
  * Initialize the ATerm library.
  */

void ATinit(int argc, char *argv[], 
	   void (*error)(const char *format, va_list args), int *bottomOfStack)
{
  /* Check for reasonably sized ATerm (32 bits, 4 bytes)     */
  /* This check might break on perfectly valid architectures */
  /* that have char == 2 bytes, and sizeof(header_type) == 2 */
  assert(sizeof(header_type) == sizeof(ATerm *));
  assert(sizeof(header_type) >= 4);

  error_handler = error;
  AT_initMemory(argc, argv);
  AT_initSymbol(argc, argv);
/*  ATinitGC(argc, argv, bottomOfStack);
*/
}

/*}}}  */
/*{{{  void ATerror(const char *format, ...) */

/**
  * A fatal error was detected.
  */

void ATerror(const char *format, ...)
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
