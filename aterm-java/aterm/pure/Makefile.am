javadir = ${prefix}/classes/aterm/pure

JAVAROOT = ${top_builddir}

CLASSPATH_ENV = CLASSPATH=${JAVAROOT}:${EXTERNAL_JARS}

JAVASOURCES = \
	AFunImpl.java \
	ATermApplImpl.java \
	ATermBlobImpl.java \
	ATermImpl.java \
	ATermIntImpl.java \
	ATermListImpl.java \
	ATermPlaceholderImpl.java \
	ATermRealImpl.java \
	ATermVisitableImpl.java \
	PureFactory.java\
	ATermWriter.java

java_JAVA       = $(JAVASOURCES)
EXTRA_DIST      = $(JAVASOURCES)
