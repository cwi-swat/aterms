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

/* The checksum this utility prints is calculated using the
 "RSA Data Security, Inc. MD5 Message-Digest Algorithm" (see RFC1321)
 */

#include <stdio.h>
#include "aterm2.h"

static void usage(const char *prg)
{
  fprintf(stderr, "Usage: %s [<file>]\n", prg);
  exit(1);
}

int main(int argc, char *argv[])
{
  ATerm t = NULL;

  ATinit(argc, argv, &t);

  if (argc == 1) {
    t = ATreadFromFile(stdin);
  } else if (strcmp(argv[1], "-h") == 0) {
    usage(argv[0]);
  } else if (argc == 2) {
    t = ATreadFromNamedFile(argv[1]);
  } else {
    usage(argv[0]);
  }

  if (!t) {
    fprintf(stderr, "parse error, giving up\n");
    exit(1);
  }

  ATprintf("%h\n", t);

  return 0;
}
