
PRODUCT		= aterm
VERSION		= 0.0.2

CC		= gcc
CFLAGS		= -g -ansi -pedantic -Wall
NOANSI		= -g -pedantic -Wall
DEFS		= -DDEBUG_ALL
INCLUDES	=
LFLAGS		= -g
AR		= ar

ATERMLIBS	= libATerm.a
ATERMSRCS	= aterm.c list.c make.c memory.c symbol.c gc.c
ATERMOBJS	= aterm.o list.o make.o memory.o symbol.o gc.o

all: $(ATERMLIBS)

include ../implicit-rules

libATerm.a: $(ATERMOBJS)
	$(AR) cr libATerm.a $(ATERMOBJS)

gc.o:
	$(CC) -c $(NOANSI) $(INCLUDES) gc.c

clean:
	rm -f $(ATERMLIBS) $(ATERMOBJS) *.o *~ core

tag:
	ctags -t *.[ch]

depend:
	$(CC) -MM $(CFLAGS) $(DEFS) $(INCLUDES) $(ATERMSRCS) > .depend

.depend:
	$(CC) -MM $(CFLAGS) $(DEFS) $(INCLUDES) $(ATERMSRCS) > .depend

include .depend

