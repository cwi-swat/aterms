
CC_CC		= cc
CC_GCC		= gcc

CFLAGS_CC	= -O
#CFLAGS_CC	= -g
CFLAGS_GCC	= -g -ansi -pedantic -Wall

INCL		= -I../asfix -I../aterm
DEFS		=
LFLAGS		=

#LIBS_GCC	= -L../aterm -lATerm-dbg -L$(EFENCE) -lefence
LIBS_GCC	= -L../aterm -lATerm-dbg -L$(EFENCE)
LIBS_CC		= -L../aterm -lATerm-cc
AR			= ar

EFENCE		= /home/olivierp/ElectricFence-2.0.5
TESTPRGS	= stress-cc stress-gcc primes-cc primes-gcc asfixtest
TESTSRCS	= stress.c asfixtest.c primes.c
TESTOBJS	= stress.o asfixtest.o primes.o

all: $(TESTPRGS) $(TESTS)

stress-cc: stress.c
	$(CC_CC) $(CFLAGS_CC) $(INCL) $(LFLAGS) -o stress-cc stress.c $(LIBS_CC)

stress-gcc: stress.c
	$(CC_GCC) $(CFLAGS_GCC) $(INCL) $(LFLAGS) -o stress-gcc stress.c $(LIBS_GCC)

asfixtest: asfixtest.c
	$(CC_GCC) $(CFLAGS_GCC) $(INCL) $(LFLAGS) -o asfixtest asfixtest.c \
	-L../asfix -lAsFix $(LIBS_GCC)

primes-cc: primes.c
	$(CC_CC) $(CFLAGS_CC) $(INCL) $(LFLAGS) -o primes-cc primes.c $(LIBS_CC)

primes-gcc: primes.c
	$(CC_GCC) $(CFLAGS_GCC) $(INCL) $(LFLAGS) -o primes-gcc primes.c $(LIBS_GCC)

clean:
	rm -f $(TESTPRGS) $(TESTOBJS) *~ core

depend:
	$(CC_GCC) -MM $(CFLAGS) $(DEFS) $(INCL) $(TESTSRCS) > .depend

.depend:
	$(CC_GCC) -MM $(CFLAGS) $(DEFS) $(INCL) $(TESTSRCS) > .depend

include .depend

