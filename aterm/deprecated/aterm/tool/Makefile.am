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
javadir   = $(prefix)/classes/aterm/tool


bin_SCRIPTS = tifstojava java-adapter

# This is some kind of hack to make sure that Testing.java is
# generated before compiling all other java files but that it will
# not be included in the distribution nor that it will be generated
# during a make dist.
noinst_SCRIPTS = Testing.java

EXTRA_DIST  = test.tb Test.java TestTool.java TestingTool.java Tool.java \
              ToolBusChannel.java ToolException.java tifstojava java-adapter

java_JAVA   = JavaTif.java ToolBusChannel.java TestTool.java \
              TestingTool.java ToolException.java \
              Test.java Tool.java 

JAVAROOT    = ${top_builddir}/java1.2/

CLEANFILES  = test.tifs Testing.java

Testing.java: test.tifs JavaTif.class
	$(CLASSPATH_ENV) java aterm.tool.JavaTif \
			   -tool testing -tifs ${srcdir}/test.tifs \
                           -package aterm.tool -class Testing

%.tifs: %.tb
	$(TOOLBUS) -gentifs $<

%.class: %.java
	$(CLASSPATH_ENV) $(JAVAC) $(JAVACFLAGS) -O -d $(JAVAROOT) $<
