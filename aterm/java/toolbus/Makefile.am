# SUBDIRS		= pure test

javadir		= ${prefix}/classes/aterm

EXTRA_DIST	= test.tifs

java_JAVA	= Tool.java AbstractTool.java TestTool.java JavaTif.java \
			Testing.java

JAVAROOT	= ${top_builddir}/java/

# This is some kind of hack to make sure that generated Java files are
# generated before compiling all other java files but that it will
# not be included in the distribution nor that it will be generated
# during a make dist.
noinst_SCRIPTS = TestingTif.java TestingTool.java TestingBridge.java

${noinst_SCRIPTS}: test.tifs JavaTif.class
		CLASSPATH=${top_srcdir}/java:${CLASSPATH} \
			${top_srcdir}/java/toolbus/tifstojava \
				-tifs test.tifs -tool testing -package toolbus

test.tifs: test.tb
	${TOOLBUS} -gentifs test.tb

%.class: %.java
	$(CLASSPATH_ENV) $(JAVAC) $(JAVACFLAGS) -O -d $(JAVAROOT) $<



