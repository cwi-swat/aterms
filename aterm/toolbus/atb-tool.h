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

#ifndef ATB_TOOL_H
#define ATB_TOOL_H

#include <sys/times.h>
#include <sys/types.h>
#include <string.h>
#include <aterm1.h>

/* ToolBus callback functions */
typedef ATerm (*ATBhandler)(int file_desc, ATerm input_term);

/* ToolBus tool functions */
int   ATBinitialize(int argc, char *argv[]);
int    ATBinit(int argc, char *argv[], ATerm *stack_bottom);
int    ATBconnect(char *tool, char *host, int port, ATBhandler h);
void   ATBdisconnect(int file_desc);
int    ATBeventloop(void);

ATerm ATBpack(ATerm t);

ATerm  ATBreadTerm(int file_desc);
int    ATBwriteTerm(int file_desc, ATerm term);
void   ATBpostEvent(int file_desc, ATerm event);

ATbool ATBpeekOne(int file_desc);
int    ATBpeekAny(void);
int    ATBhandleOne(int file_desc);
int    ATBhandleAny(void);

int    ATBgetDescriptors(fd_set *set);

/* Generic signature checker */
ATerm ATBcheckSignature(ATerm signature, char *sigs[], int nrsigs);

#ifndef streq
#  define streq(s,t)	(!(strcmp(s,t)))
#endif

#ifndef MIN
#  define MIN(a,b)	((a)<(b) ? (a) : (b))
#endif

#ifndef MAX
#  define MAX(a,b)	((a)>(b) ? (a) : (b))
#endif

#endif /* ATB_TOOL_H */
