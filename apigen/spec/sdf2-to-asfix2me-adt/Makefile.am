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

noinst_LIBRARIES	= libSdf2-to-AsFix2ME-ADT.a

bin_PROGRAMS		= Sdf2-to-AsFix2ME-ADT

Sdf2_to_AsFix2ME_ADT_SOURCES		= init.c
Sdf2_to_AsFix2ME_ADT_LDADD		= libSdf2-to-AsFix2ME-ADT.a ${LDLIBS} ${SOCKLIBS}

libSdf2_to_AsFix2ME_ADT_a_SOURCES	= $(SPEC_SRCS)
libSdf2_to_AsFix2ME_ADT_a_LIBADD	= init.o

# Old versions of automake put all dependencies on a single line which
# causes several tools to crash (sed for instance) because of line length
# limitations. We therefore split all dependencies and define them among
# several SPEC_SRCSx variables. These are all combined in SPEC_SRCS which
# forms the concatenation of all sub varibales ( SPEC_SRCS = $(SPEC_SRCS1)
# ... $(SPEC_SRCSn)
#
SPEC_SRCS1=\
   AUX-Sdf2-to-AsFix2ME-ADT13.c AUX-Sdf2-to-AsFix2ME-ADT12.c\
   AUX-Sdf2-to-AsFix2ME-ADT11.c AUX-Sdf2-to-AsFix2ME-ADT10.c\
   AUX-Sdf2-to-AsFix2ME-ADT9.c AUX-Sdf2-to-AsFix2ME-ADT8.c\
   AUX-Sdf2-to-AsFix2ME-ADT7.c AUX-Sdf2-to-AsFix2ME-ADT6.c\
   AUX-Sdf2-to-AsFix2ME-ADT5.c AUX-Sdf2-to-AsFix2ME-ADT4.c\
   AUX-Sdf2-to-AsFix2ME-ADT3.c AUX-Sdf2-to-AsFix2ME-ADT2.c\
   AUX-Sdf2-to-AsFix2ME-ADT1.c AUX-Character-Class-Normalization16.c\
   AUX-Character-Class-Normalization15.c\
   AUX-Character-Class-Normalization14.c\
   AUX-Character-Class-Normalization13.c\
   AUX-Character-Class-Normalization12.c\
   AUX-Character-Class-Normalization11.c\
   AUX-Character-Class-Normalization10.c AUX-Character-Class-Normalization9.c\
   AUX-Character-Class-Normalization8.c AUX-Character-Class-Normalization7.c\
   AUX-Character-Class-Normalization6.c AUX-Character-Class-Normalization5.c\
   AUX-Character-Class-Normalization4.c AUX-Character-Class-Normalization3.c\
   AUX-Character-Class-Normalization2.c AUX-Character-Class-Normalization1.c\
   AUX-Build-AsFix2ME-ADT15.c AUX-Build-AsFix2ME-ADT14.c\
   AUX-Build-AsFix2ME-ADT13.c AUX-Build-AsFix2ME-ADT12.c\
   AUX-Build-AsFix2ME-ADT11.c AUX-Build-AsFix2ME-ADT10.c\
   AUX-Build-AsFix2ME-ADT9.c AUX-Build-AsFix2ME-ADT8.c\
   AUX-Build-AsFix2ME-ADT7.c AUX-Build-AsFix2ME-ADT6.c\
   AUX-Build-AsFix2ME-ADT5.c AUX-Build-AsFix2ME-ADT4.c\
   AUX-Build-AsFix2ME-ADT3.c AUX-Build-AsFix2ME-ADT2.c\
   AUX-Build-AsFix2ME-ADT1.c AUX-Character-Class-Membership5.c\
   AUX-Character-Class-Membership4.c AUX-Character-Class-Membership3.c\
   AUX-Character-Class-Membership2.c AUX-Character-Class-Membership1.c\
   AUX-Build-General-AsFix2ME-ADT7.c AUX-Build-General-AsFix2ME-ADT6.c\
   AUX-Build-General-AsFix2ME-ADT5.c AUX-Build-General-AsFix2ME-ADT4.c\
   AUX-Build-General-AsFix2ME-ADT3.c AUX-Build-General-AsFix2ME-ADT2.c

SPEC_SRCS2=\
   AUX-Build-General-AsFix2ME-ADT1.c AUX-Character-Arithmetic10.c\
   AUX-Character-Arithmetic9.c AUX-Character-Arithmetic8.c\
   AUX-Character-Arithmetic7.c AUX-Character-Arithmetic6.c\
   AUX-Character-Arithmetic5.c AUX-Character-Arithmetic4.c\
   AUX-Character-Arithmetic3.c AUX-Character-Arithmetic2.c\
   AUX-Character-Arithmetic1.c AUX-Character-Class-Syntax8.c\
   AUX-Character-Class-Syntax7.c AUX-Character-Class-Syntax6.c\
   AUX-Character-Class-Syntax5.c AUX-Character-Class-Syntax4.c\
   AUX-Character-Class-Syntax3.c AUX-Character-Class-Syntax2.c\
   AUX-Character-Class-Syntax1.c AUX-Cons-Options1.c AUX-Sdf21.c\
   AUX-ATerm-Utils8.c AUX-ATerm-Utils7.c AUX-ATerm-Utils6.c\
   AUX-ATerm-Utils5.c AUX-ATerm-Utils4.c AUX-ATerm-Utils3.c\
   AUX-ATerm-Utils2.c AUX-ATerm-Utils1.c AUX-Integers11.c AUX-Integers10.c\
   AUX-Integers9.c AUX-Integers8.c AUX-Integers7.c AUX-Integers6.c\
   AUX-Integers5.c AUX-Integers4.c AUX-Integers3.c AUX-Integers2.c\
   AUX-Integers1.c AUX-Character-Normalization1.c AUX-Character-Syntax7.c\
   AUX-Character-Syntax6.c AUX-Character-Syntax5.c AUX-Character-Syntax4.c\
   AUX-Character-Syntax3.c AUX-Character-Syntax2.c AUX-Character-Syntax1.c\
   AUX-Layout1.c AUX-Sdf2-Syntax1.c AUX-ATerm-Syntax1.c AUX-Booleans6.c\
   AUX-Booleans5.c AUX-Booleans4.c AUX-Booleans3.c AUX-Booleans2.c\
   AUX-Booleans1.c AUX-Basic-Integers4.c AUX-Basic-Integers3.c\
   AUX-Basic-Integers2.c AUX-Basic-Integers1.c AUX-Alias-Sdf-Syntax1.c\
   AUX-Restrictions-Sdf-Syntax1.c AUX-Label-Sdf-Syntax1.c\
   AUX-Literals-Sdf-Syntax1.c AUX-Sorts-Sdf-Syntax1.c\
   AUX-CC-Sdf-Syntax1.c AUX-Priority-Sdf-Syntax1.c AUX-Regular-Sdf-Syntax1.c\
   AUX-Modular-Sdf-Syntax1.c AUX-Basic-Sdf-Syntax1.c AUX-Kernel-Sdf-Syntax1.c\
   AUX-Integer-Syntax1.c AUX-Literal-Syntax1.c AUX-IntCon1.c AUX-Literals1.c\
   AUX-Renaming-Sdf-Syntax1.c AUX-Grammar-Syntax1.c AUX-Symbols1.c

SPEC_SRCS = $(SPEC_SRCS1) $(SPEC_SRCS2)

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
