
PRODUCT		= aterm
VERSION		= 0.0.1

CC		= gcc
CFLAGS		= -g -pedantic -Wall
DEFS		= -DDEBUG_ALL
LFLAGS		= -g
AR		= ar

ATERMLIBS	= libATerm.a
ATERMSRCS	= basic.c memory.c
ATERMOBJS	= basic.o memory.o

all: $(ATERMLIBS)

include ../implicit-rules

libATerm.a: $(ATERMOBJS)
	$(AR) cr libATerm.a $(ATERMOBJS)

clean:
	rm -f $(ATERMLIBS) $(ATERMOBJS) *.o *~ core

.depend:
	$(CC) -MM $(CFLAGS) $(DEFS) $(INCLUDES) $(ATERMSRCS) > .depend

include .depend

