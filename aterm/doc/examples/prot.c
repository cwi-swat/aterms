/*

    ATerm -- The ATerm (Annotated Term) library
    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
                             The  Netherlands.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/
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
