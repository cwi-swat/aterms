
PRODUCT		= aterm
VERSION		= 0.0.1

CC		= gcc
CFLAGS		= -g -ansi -pedantic -Wall
INCLUDES	= -I ../asfix -I../aterm
DEFS		=
LFLAGS		= -g
LIBS		= -L../asfix -lAsFix -L../aterm -lATerm -L$(EFENCE) -lefence
AR		= ar

EFENCE		= /home/olivierp/ElectricFence-2.0.5
TESTS		= test-stress
TESTPRGS	= stress asfixtest primes
TESTSRCS	= stress.c asfixtest.c primes.c
TESTOBJS	= stress.o asfixtest.o primes.o

all: clean $(TESTPRGS) $(TESTS)

stress: stress.o
	$(CC) $(LFLAGS) -o stress stress.o $(LIBS)

asfixtest: asfixtest.o
	$(CC) $(LFLAGS) -o asfixtest asfixtest.o $(LIBS)

primes: primes.o
	$(CC) $(LFLAGS) -o primes primes.o $(LIBS)

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

