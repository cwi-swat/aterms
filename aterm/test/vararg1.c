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
#include <stdarg.h>

void va_argtest(va_list *args)
{
	printf("arg: %s\n", va_arg(*args, char *));
}

void va_listtest(int nr, va_list *args)
{
	int i;

	for(i=0; i<nr; i++) {
		va_argtest(args);
	}
	printf("%d arguments.\n", nr);
}

void va_test(int nr, ...)
{
  va_list args;

  va_start(args, nr);
  va_listtest(nr, &args);
  va_end(args);
}


int main(int argc, char *argv[])
{
	va_test(4,"a","b", "c","d");
	return 0;
}
