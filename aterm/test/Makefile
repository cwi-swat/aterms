
PRODUCT		= aterm
VERSION		= 0.0.1

CC		= gcc
CFLAGS		= -g -ansi -pedantic -Wall
INCLUDES	= -I../aterm
DEFS		=
LFLAGS		= -g
LIBS		= -L../aterm -lATerm -L$(EFENCE) -lefence
AR		= ar

EFENCE		= /home/olivierp/ElectricFence-2.0.5
TESTS		= test-stress
TESTPRGS	= stress
TESTSRCS	= stress.c
TESTOBJS	= stress.o

all: clean $(TESTPRGS) $(TESTS)

stress: stress.o
	$(CC) $(LFLAGS) -o stress stress.o $(LIBS)

include ../implicit-rules

test-stress: stress
	stress

clean:
	rm -f $(TESTPRGS) $(TESTOBJS) *~ core

depend:
	$(CC) -MM $(CFLAGS) $(DEFS) $(INCLUDES) $(TESTSRCS) > .depend

.depend:
	$(CC) -MM $(CFLAGS) $(DEFS) $(INCLUDES) $(TESTSRCS) > .depend

include .depend

