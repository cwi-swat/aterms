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
	$(CLASSPATH_ENV) javac -O -d $(JAVAROOT) $<
