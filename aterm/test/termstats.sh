#!/bin/sh
#
#    ATerm -- The ATerm (Annotated Term) library
#    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
#                             The  Netherlands.
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation; either version 2 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
#
cat | tee /tmp/test.trm | termstats -silent -bafsize -at-symboltable 256019
/usr/bin/time gzip -f /tmp/test.trm 2> /tmp/time.out
TIME=`grep user /tmp/time.out | awk '{ print $2 }'`
GZIPSIZE=`ls -l /tmp/test.trm.gz | awk '{ print $5 }'`
printf "gzipped size    : %8d\n" $GZIPSIZE
printf "  gzip time     : %8s\n" $TIME
