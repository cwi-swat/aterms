
PRODUCT		= aterm
VERSION		= 0.0.1

CC		= gcc
CFLAGS		= -g -pedantic -Wall
INCLUDES	= -I../aterm
DEFS		=
LFLAGS		= -g
LIBS		= -L../aterm -lATerm
AR		= ar

TESTS		= tst-alloc
TESTPRGS	= test-alloc
TESTSRCS	= test-alloc.c
TESTOBJS	= test-alloc.o

all: $(TESTPRGS)

test-all: clean all $(TESTS)

test-alloc: test-alloc.o
	$(CC) $(LFLAGS) -o test-alloc test-alloc.o $(LIBS)

include ../implicit-rules

tst-alloc: test-alloc
	test-alloc

clean:
	rm -f $(TESTPRGS) $(TESTOBJS) *~ core

.depend:
	$(CC) -MM $(CFLAGS) $(DEFS) $(INCLUDES) $(TESTSRCS) > .depend

include .depend

