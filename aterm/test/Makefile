
CC_CC		= cc
CC_GCC		= gcc

CFLAGS_CC	= -O
#CFLAGS_CC	= -g
CFLAGS_GCC	= -g -ansi -pedantic -Wall

INCL		= -I../asfix -I../aterm -I../toolbus
DEFS		=
LFLAGS		=

#LIBS_GCC	= -L../aterm -lATerm-dbg -L$(EFENCE) -lefence
LIBS_GCC	= -L../aterm -lATerm-dbg -L$(EFENCE)
LIBS_CC		= -L../aterm -lATerm-cc
AR			= ar

EFENCE		= /home/olivierp/ElectricFence-2.0.5
TESTPRGS	= stress-cc stress-gcc primes-cc primes-gcc asfixtest \
		test-tool
TESTSRCS	= stress.c asfixtest.c primes.c test-tool
TESTOBJS	= stress.o asfixtest.o primes.o test-tool.o

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

test-tool: test-tool.c
	$(CC_GCC) $(CFLAGS_GCC) $(INCL) $(LFLAGS) -o test-tool test-tool.c \
		$(LIBS_GCC) -L../toolbus -lATB-dbg -lsocket -lnsl

clean:
	rm -f $(TESTPRGS) $(TESTOBJS) *~ core

depend:
	$(CC_GCC) -MM $(DEFS) $(INCL) $(TESTSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-cc.o:/"   >  .depend
	cat /tmp/.gendep | sed "s/.o:/-gcc.o:/"  >> .depend
	cat /tmp/.gendep | sed "s/.o:/-dbg.o:/"  >> .depend
	cat /tmp/.gendep | sed "s/.o:/-prof.o:/" >> .depend
	rm -f /tmp/.gendep

.depend:
	$(CC_GCC) -MM $(DEFS) $(INCL) $(TESTSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-cc.o:/"   >  .depend
	cat /tmp/.gendep | sed "s/.o:/-gcc.o:/"  >> .depend
	cat /tmp/.gendep | sed "s/.o:/-dbg.o:/"  >> .depend
	cat /tmp/.gendep | sed "s/.o:/-prof.o:/" >> .depend
	rm -f /tmp/.gendep

include .depend
