
SOURCE = Testing.java JavaTif.java ToolBusChannel.java TestTool.java \
		TestingTool.java ToolException.java \
		Test.java Testing.java Tool.java 
CLASSES	= $(patsubst %.java,%.class,$(SOURCE))

all: ${CLASSES}

Testing.java: test.tifs JavaTif.class
	java -classpath ../..:$$CLASSPATH aterm.tool.JavaTif \
			-tool testing -tifs test.tifs -package aterm.tool

%.tifs: %.tb
	toolbus -gentifs $<

%.class: %.java
	javac -O -classpath ../..:$$CLASSPATH $<

test: ${CLASSES}

clean:
	rm -f ${CLASSES} Testing.java

