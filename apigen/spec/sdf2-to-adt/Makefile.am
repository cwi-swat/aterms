#    Meta-Environment - An environment for language prototyping.
#    Copyright (C) 2001  Stichting Mathematisch Centrum,
#                        Amsterdam, The Netherlands.
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation; either version 2 of the License, or (at
#    your option) any later version.
#
#    This program is distributed in the hope that it will be useful, but
#    WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#    General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
#    USA

INCLUDES		= -I$(ASC_SUPPORT)/include                           -I$(PT_SUPPORT)/include                           -I$(ATERM)/include                           -I$(ASF_SUPPORT)/include

LDFLAGS			= -L${ASC_SUPPORT}/lib                           -L${PT_SUPPORT}/lib                           -L${ASF_SUPPORT}/lib                           -L${ATERM}/lib

LDLIBS          	= -lasc-support -lPT -lASF -lATB -lATerm

noinst_LIBRARIES	= libSdf2-to-ADT.a

bin_PROGRAMS		= Sdf2-to-ADT

Sdf2_to_ADT_SOURCES		= init.c
Sdf2_to_ADT_LDADD		= libSdf2-to-ADT.a ${LDLIBS} ${SOCKLIBS}

libSdf2_to_ADT_a_SOURCES	= $(SPEC_SRCS)
libSdf2_to_ADT_a_LIBADD	= init.o

# Old versions of automake put all dependencies on a single line which
# causes several tools to crash (sed for instance) because of line length
# limitations. We therefore split all dependencies and define them among
# several SPEC_SRCSx variables. These are all combined in SPEC_SRCS which
# forms the concatenation of all sub varibales ( SPEC_SRCS = $(SPEC_SRCS1)
# ... $(SPEC_SRCSn)
#
SPEC_SRCS1=\
   AUX-Sdf2-to-ADT29.c AUX-Sdf2-to-ADT28.c AUX-Sdf2-to-ADT27.c\
   AUX-Sdf2-to-ADT26.c AUX-Sdf2-to-ADT25.c AUX-Sdf2-to-ADT24.c\
   AUX-Sdf2-to-ADT23.c AUX-Sdf2-to-ADT22.c AUX-Sdf2-to-ADT21.c\
   AUX-Sdf2-to-ADT20.c AUX-Sdf2-to-ADT19.c AUX-Sdf2-to-ADT18.c\
   AUX-Sdf2-to-ADT17.c AUX-Sdf2-to-ADT16.c AUX-Sdf2-to-ADT15.c\
   AUX-Sdf2-to-ADT14.c AUX-Sdf2-to-ADT13.c AUX-Sdf2-to-ADT12.c\
   AUX-Sdf2-to-ADT11.c AUX-Sdf2-to-ADT10.c AUX-Sdf2-to-ADT9.c\
   AUX-Sdf2-to-ADT8.c AUX-Sdf2-to-ADT7.c AUX-Sdf2-to-ADT6.c\
   AUX-Sdf2-to-ADT5.c AUX-Sdf2-to-ADT4.c AUX-Sdf2-to-ADT3.c\
   AUX-Sdf2-to-ADT2.c AUX-Sdf2-to-ADT1.c AUX-Sdf21.c AUX-Cons-Options1.c\
   AUX-ATerm-Utils8.c AUX-ATerm-Utils7.c AUX-ATerm-Utils6.c\
   AUX-ATerm-Utils5.c AUX-ATerm-Utils4.c AUX-ATerm-Utils3.c\
   AUX-ATerm-Utils2.c AUX-ATerm-Utils1.c AUX-Sdf2-Syntax1.c AUX-Layout1.c\
   AUX-ATerm-Syntax1.c AUX-Alias-Sdf-Syntax1.c AUX-Restrictions-Sdf-Syntax1.c\
   AUX-Label-Sdf-Syntax1.c AUX-Literals-Sdf-Syntax1.c AUX-Sorts-Sdf-Syntax1.c\
   AUX-CC-Sdf-Syntax1.c AUX-Priority-Sdf-Syntax1.c AUX-Regular-Sdf-Syntax1.c\
   AUX-Modular-Sdf-Syntax1.c AUX-Basic-Sdf-Syntax1.c AUX-Kernel-Sdf-Syntax1.c\
   AUX-Integer-Syntax1.c AUX-Literal-Syntax1.c AUX-Literals1.c\
   AUX-Character-Class-Syntax1.c AUX-Renaming-Sdf-Syntax1.c\
   AUX-Grammar-Syntax1.c AUX-Symbols1.c AUX-Character-Syntax1.c

SPEC_SRCS = $(SPEC_SRCS1)

init.c: $(SPEC_SRCS) Makefile
	@(echo "/*GENERATED AUTOMATICALLY, DO NOT MODIFY */" ;\
	SPEC_SRCS="$(SPEC_SRCS)" ;\
	for file in $$SPEC_SRCS; do \
		echo "extern void register_$$file();" ;\
		echo "extern void resolve_$$file();" ;\
		echo "extern void init_$$file();" ;\
	done ;\
	echo "void register_all() {" ;\
	for file in $$SPEC_SRCS; do \
		echo "  register_$$file();" ;\
	done ;\
	echo "}" ;\
	echo "void resolve_all() {" ;\
	for file in $$SPEC_SRCS; do \
		echo "  resolve_$$file();" ;\
	done ;\
	echo "}" ;\
	echo "void init_all() {" ;\
	for file in $$SPEC_SRCS; do \
		echo "  init_$$file();" ;\
	done ;\
	echo "}" ) | sed 's/.c()/()/g;s/-/_/g;' > $@
