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

#include "_aterm.h"

void termsize(FILE *file)
{
  int core_size, text_size, term_depth;
  
  ATerm t = ATreadFromFile(file);
  core_size = AT_calcCoreSize(t);
  text_size = AT_calcTextSize(t);
  term_depth = AT_calcTermDepth(t);
  printf("internal size: %d bytes, text size: %d bytes, depth: %d\n", 
	 core_size, text_size, term_depth);
}

int main(int argc, char *argv[])
{
  int i;
  ATerm bottomOfStack;

  for(i=1; i<argc; i++) {
    if(strcmp(argv[i], "-h") == 0) {
      ATwarning("usage: %s [aterm-options] < input > output\n", argv[0]);
      ATwarning("  use -at-help to get aterm-options\n");
      exit(0);
    }
  }

  ATinit(argc, argv, &bottomOfStack);
  termsize(stdin);
  
  return 0;
}
