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
  * Debugging macros
  */

#ifndef DEBUG_H
#define DEBUG_H

#define EXEC(cmd) (cmd); fflush(stdout)

#ifdef DEBUG_ALL
#define DEBUG_MEM
#define DEBUG_ARITY
#endif

#ifdef DEBUG_MEM
#define DBG_MEM(cmd) EXEC(cmd)
#else
#define DBG_MEM(cmd)
#endif

#ifdef DEBUG_ARITY
#define DBG_ARITY(cmd) EXEC(cmd)
#else
#define DBG_ARITY(cmd)
#endif

#endif
