#include <stdio.h>
#include <aterm1.h>

static ATerm global_aterm;                 /* global ATerm */

#define NR_ENTRIES 42                      /* arbitrary number for this example. */
static ATerm global_arr[NR_ENTRIES];       /* global ATerm array. */

int main(int argc, char *argv[])
{
    ATerm bottomOfStack;                    /* Used in initialisation of library    */

    ATinit(argc, argv, &bottomOfStack);     /* Initialise the ATerm library.        */
    ATprotect(&global_aterm);               /* Protect the global aterm variable.   */
    ATprotectArray(global_arr, NR_ENTRIES); /* Protect the global aterm array.      */
    foo();                                  /* Call to code that works with ATerms. */

    /* End of program. */
    return 0;
}
