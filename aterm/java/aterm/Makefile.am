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

PRODUCT		= @PRODUCT@
VERSION		= @VERSION@
CLEAN		= @CLEAN@
@SET_MAKE@

srcdir		= @srcdir@
top_srcdir	= @top_srcdir@
prefix		= @prefix@
exec_prefix	= @exec_prefix@
bindir		= @bindir@
libdir		= @libdir@
includedir	= $(prefix)/include
classdir	= $(prefix)/classes

INSTALL		= @INSTALL@
INSTALL_DAT	= @INSTALL_DAT@
INSTALL_PRG	= @INSTALL_PRG@

SUBDIRS		= native pure
DISTFILES	= Makefile.in ${SOURCE}

SOURCE		= ATerm.java ATermAppl.java ATermBlob.java ATermInt.java \
		  ATermList.java ATermPlaceholder.java ATermReal.java \
		  ParseError.java

CLASSES		= $(patsubst %.java,%.class,$(SOURCE))

all: ${CLASSES}
	@for dir in ${SUBDIRS}; \
	do \
		echo "make all in $$dir"; \
		cd $$dir; ${MAKE} -s all; \
		cd ..; \
	done

%.class: %.java
	javac -classpath ..:$$CLASSPATH $<

clean:
	@rm -f ${CLASSES} *~ core
	@for dir in ${SUBDIRS}; \
	do \
		echo "make clean in $$dir"; \
		cd $$dir; ${MAKE} -s clean; \
		cd ..; \
	done

install:
	@mkdir -p ${classdir}/aterm
	@for file in ${CLASSES}; \
	do \
		${INSTALL_DAT} $$file ${classdir}/aterm; \
	done
	@for dir in ${SUBDIRS}; \
	do \
		echo "make install in $$dir"; \
		cd $$dir; ${MAKE} -s install; \
		cd ..; \
	done

dist:
	@cp ${DISTFILES} ${DISTDIR}
	@for dir in ${SUBDIRS}; \
	do \
		echo "make dist in $$dir"; \
		mkdir ${DISTDIR}/$$dir; \
		cd $$dir; ${MAKE} -s DISTDIR=${DISTDIR}/$$dir dist; \
		cd ..; \
	done
