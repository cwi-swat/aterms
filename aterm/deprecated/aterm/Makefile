

SOURCE	= World.java ATerm.java ATermList.java ATermAppl.java ATermInt.java \
		ATermPlaceholder.java ATermReal.java ATermBlob.java \
		ATermChannel.java ParseError.java \
		ATermSize.java ATest.java Primes.java
CLASSES	= $(patsubst %.java,%.class,$(SOURCE))

all: ${CLASSES}

%.class: %.java
	javac -classpath ..:$$CLASSPATH $<

test: ${CLASSES}
	java -classpath ..:$$CLASSPATH aterm.ATest

clean:
	rm -f ${CLASSES}

