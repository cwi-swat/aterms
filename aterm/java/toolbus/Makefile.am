# SUBDIRS		= pure test

#javadir		= ${prefix}/classes/aterm

java_JAVA	= Tool.java AbstractTool.java TestTool.java JavaTif.java \
			TestTifs.java

TestTifs.java: test.tifs
		CLASSPATH=${top_srcdir}/java:${CLASSPATH} \
			${top_srcdir}/java/toolbus/tifstojava \
				-tifs test.tifs -tool test -class TestTifs -package toolbus

test.tifs: test.tb
	${TOOLBUS} -gentifs test.tb


JAVAROOT	= ${top_builddir}/java/
