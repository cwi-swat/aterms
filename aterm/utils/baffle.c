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
/**
 * baffle.c
 *
 * Usage:
 *
 * baffle [-i <input>] [-o <output> | -c] [-v] [-rb | -rt] [-wb | -wt]
 *
 * -i <input>    - Read input from file <input>        (Default: stdin)
 * -o <output>   - Write output to file <output>       (Default: stdout)
 * -c            - Check validity of input-term
 * -v            - Print version information
 * -h            - Display help
 * -ri           - Read BAF and write interpretation
 * -rb, -rt      - Choose between BAF and TEXT input   (Default: -rb)
 * -wb, -wt      - Choose between BAF and TEXT output  (Default: -wb)
 *
 */

#include <stdio.h>
#include <assert.h>
#include <time.h>
#include <stdlib.h>

#include "bafio.h"
#include "util.h"
#include "aterm2.h"

char baffle_id[] = "$Id$";

static
void
usage(char *prg)
{
	fprintf(stderr,
"Usage: %s [-i <input>] [-o <output> | -c] [-v] [-rb | -rt] [-wb | -wt]\n\n" \
"    -i <input>    - Read input from file <input>        (Default: stdin)\n" \
"    -o <output>   - Write output to file <output>       (Default: stdout)\n" \
"    -c            - Check validity of input-term\n" \
"    -v            - Print version information\n" \
"    -h            - Display help\n" \
"    -ri           - Write interpretation of BAF-input\n" \
"    -rb, -rt      - Choose between BAF and TEXT input   (Default: -rb)\n" \
"    -wb, -wt      - Choose between BAF and TEXT output  (Default: -wb)\n" \
"", prg);
}

int
main(int argc, char *argv[])
{
	int lcv;
	ATbool check   = ATfalse;
	ATbool binary_input  = ATtrue;
	ATbool binary_output = ATtrue;
	ATbool interpret = ATfalse;
	ATbool result = ATfalse;
	ATerm term = NULL;
	FILE *input  = stdin;
	FILE *output = stdout;
	time_t curtime;

	/* Initialize ATerm-library */
	ATerm bottomOfStack;
	ATinit(argc, argv, &bottomOfStack);

	/* Make sure <term> doesn't get cleaned up */
	ATprotect(&term);

	/* Parse commandline arguments */
	for (lcv=1; lcv < argc; lcv++)
	{
		if (streq(argv[lcv], "-i"))
		{
			input = fopen(argv[++lcv], "rb");
			if (input == NULL)
				ATerror("%s: unable to open %s for reading.\n",
						argv[0], argv[lcv]);
		}
		else if(streq(argv[lcv], "-o"))
		{
			output = fopen(argv[++lcv], "wb");
			if (output == NULL)
				ATerror("%s: unable to open %s for writing.\n",
						argv[0], argv[lcv]);
		}
		else if (streq(argv[lcv], "-c"))
		{
			check = ATtrue;
		}
		else if (streq(argv[lcv], "-v"))
		{
			int major, minor;
			AT_getBafVersion(&major, &minor);
			ATfprintf(stderr, "%s - Version: %d.%d\n", argv[0], major, minor);
			exit (0);
		}
		else if (streq(argv[lcv], "-h"))
		{
			usage(argv[0]);
			exit(0);
		}
		else if (streq(argv[lcv], "-ri"))
		{
			interpret = ATtrue;
		}
		else if (streq(argv[lcv], "-rb"))
		{
			binary_input = ATtrue;
		}
		else if (streq(argv[lcv], "-rt"))
		{
			binary_input = ATfalse;
		}
		else if (streq(argv[lcv], "-wb"))
		{
			binary_output = ATtrue;
		}
		else if (streq(argv[lcv], "-wt"))
		{
			binary_output = ATfalse;
		}
	}

	/* Baffle user */
	if (interpret)
	{
		/*AT_interpretBaf(input, output);*/
		exit(0);
	}

	curtime = time(NULL);
	term = ATreadFromFile(input);
	
	if (term == NULL)
		ATerror("%s: illegal input!\n", argv[0]);
	
	if (!check)
	{
        curtime = time(NULL);
		if (binary_output)
			result = ATwriteToBinaryFile(term, output);
		else {
			result = ATwriteToTextFile(term, output);
			fprintf(output, "\n");
		}
        curtime = time(NULL);

		if (!result)
			ATerror("%s: write failed!\n", argv[0]);
	}

	return 0;
}
