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
#include <AsFix-expand.h>
#include <AsFix-init-patterns.h>
#include <AsFix.h>

char asfixtest_id[] = "$Id$";

int main(int argc, char **argv)
{
  char *fname;
  ATerm mod, expmod;
  FILE *output, *input;

  ATerm bottomOfStack;

  fname = "Test.asfix";

  AFinit(argc, argv, &bottomOfStack);
  AFinitExpansionTerms();
  AFinitAsFixPatterns();

  input = fopen(fname,"r");
  if(!input)
	ATerror("cannot open file %s for reading, giving up.\n", fname);
  mod = ATreadFromTextFile(input);
  fclose(input);
  fname = "Test.asfix.asfix";
  expmod = AFexpandModuleToAsFix(mod,fname, "Naam");
  ATprintf("Writing: %s\n", fname);
  output = fopen(fname,"w");
  if(!output)
    ATerror("cannot open file %s for writing, giving up.\n", fname);
  ATwriteToTextFile(expmod, output);
  fclose(output);

  ATprintf("AsFixTest was successful\n");

  return 0;
}
