
PRODUCT		= aterm
VERSION		= 0.0.1

CC		= gcc
CFLAGS		= -g -ansi -pedantic -Wall
DEFS		= -DDEBUG_ALL
INCLUDES	=
LFLAGS		= -g
AR		= ar

ATERMLIBS	= libATerm.a
ATERMSRCS	= aterm.c memory.c symbol.c list.c
ATERMOBJS	= aterm.o memory.o symbol.o list.o

all: $(ATERMLIBS)

include ../implicit-rules

libATerm.a: $(ATERMOBJS)
	$(AR) cr libATerm.a $(ATERMOBJS)

clean:
	rm -f $(ATERMLIBS) $(ATERMOBJS) *.o *~ core

tag:
	ctags -t *.[ch]

depend:
	$(CC) -MM $(CFLAGS) $(DEFS) $(INCLUDES) $(ATERMSRCS) > .depend

.depend:
	$(CC) -MM $(CFLAGS) $(DEFS) $(INCLUDES) $(ATERMSRCS) > .depend

include .depend

