# SUBDIRS		= pure test

#javadir		= ${prefix}/classes/aterm

# This is some kind of hack to make sure that Testing.java is
# generated before compiling all other java files but that it will
# not be included in the distribution nor that it will be generated
# during a make dist.
noinst_SCRIPTS = TestTifs.java

EXTRA_DIST	= test.tifs

java_JAVA	= Tool.java AbstractTool.java TestTool.java JavaTif.java \
			TestTifs.java Testing.java

JAVAROOT	= ${top_builddir}/java/

TestTifs.java: test.tifs JavaTif.class
		CLASSPATH=${top_srcdir}/java:${CLASSPATH} \
			${top_srcdir}/java/toolbus/tifstojava \
				-tifs test.tifs -tool testing -class TestTifs -package toolbus

test.tifs: test.tb
	${TOOLBUS} -gentifs test.tb

%.class: %.java
	$(CLASSPATH_ENV) $(JAVAC) $(JAVACFLAGS) -O -d $(JAVAROOT) $<



