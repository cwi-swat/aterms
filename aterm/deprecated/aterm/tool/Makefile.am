javadir   = $(prefix)/classes/aterm/tool


bin_SCRIPTS = tifstojava java-adapter

EXTRA_DIST  = test.tb Test.java TestTool.java TestingTool.java Tool.java \
              ToolBusChannel.java ToolException.java tifstojava java-adapter

java_JAVA   = JavaTif.java ToolBusChannel.java TestTool.java \
              TestingTool.java ToolException.java \
              Test.java Tool.java 

JAVAROOT    = ${top_builddir}/java1.2/

CLEANFILES  = test.tifs Testing.java

BUILT_SOURCES = test.tifs Testing.java

${srcdir}/TestingTool.java : Testing.java

Testing.java: test.tifs JavaTif.class
	$(CLASSPATH_ENV) java aterm.tool.JavaTif \
			   -tool testing -tifs ${srcdir}/test.tifs \
                           -package aterm.tool -class Testing

%.tifs: %.tb
	$(TOOLBUS) -gentifs $<

%.class: %.java
	$(CLASSPATH_ENV) javac -O -d $(JAVAROOT) $<
