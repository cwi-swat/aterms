# SUBDIRS		= pure test

javadir		= ${prefix}/classes/toolbus

EXTRA_DIST	= test.tb test.tifs example.tifs

java_JAVA	= Tool.java AbstractTool.java TestTool.java JavaTif.java \
			Testing.java Example.java

JAVAROOT	= ${top_builddir}/java/

# This is some kind of hack to make sure that generated Java files are
# generated before compiling all other java files but that it will
# not be included in the distribution nor that it will be generated
# during a make dist.
TESTCLASSES	= TestingTif.java TestingTool.java TestingBridge.java 
EXAMPLECLASSES	= ExampleTif.java ExampleTool.java ExampleBridge.java 

noinst_SCRIPTS	= ${TESTCLASSES} ${EXAMPLECLASSES}

bin_SCRIPTS		= tifstojava

${EXAMPLECLASSES}: example.tifs JavaTif.class
		CLASSPATH=${top_srcdir}/java:${CLASSPATH} \
			${top_srcdir}/java/toolbus/tifstojava \
				-tifs example.tifs -tool example -package toolbus

${TESTCLASSES}: test.tifs JavaTif.class
		CLASSPATH=${top_srcdir}/java:${CLASSPATH} \
			${top_srcdir}/java/toolbus/tifstojava \
				-tifs test.tifs -tool testing -package toolbus

test.tifs: test.tb
	${TOOLBUS} -gentifs test.tb

example.tifs: example.tb
	${TOOLBUS} -gentifs example.tb

%.class: %.java
	$(CLASSPATH_ENV) $(JAVAC) $(JAVACFLAGS) -O -d $(JAVAROOT) $<



