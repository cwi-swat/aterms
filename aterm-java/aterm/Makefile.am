SUBDIRS = . pure

javadir = ${prefix}/classes/aterm

JAVAROOT = ${top_builddir}

CLASSPATH_ENV = CLASSPATH=${JAVAROOT}:${EXTERNAL_JARS}

JAVASOURCES = \
	AFun.java \
	ATerm.java \
	ATermAppl.java \
	ATermBlob.java \
	ATermFactory.java \
	ATermInt.java \
	ATermList.java \
	ATermPlaceholder.java \
	ATermReal.java \
	Fwd.java \
	Visitable.java \
	Identifiable.java \
	Visitor.java \
	ParseError.java

java_JAVA       = $(JAVASOURCES)
EXTRA_DIST      = $(JAVASOURCES)
