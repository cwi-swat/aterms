
PRODUCT		= aterm
VERSION		= 0.0.3

ATERMSRCS	= aterm.c list.c make.c memory.c symbol.c gc.c
ATERMLIBS	= libATerm-gcc.a libATerm-cc.a libATerm-dbg.a libATerm-prof.a

CC_CC		= cc
CC_GCC		= gcc
CC_DBG		= ${CC_GCC}
CC_PROF		= ${CC_GCC}

CFLAGS_CC	= -O ${DEFS_CC} ${INCL_CC}
CFLAGS_GCC	= -Wall -pedantic -O4
CFLAGS_DBG	= -Wall -pedantic -g
CFLAGS_PROF	= -Wall -pedantic -pg

DEFS_CC		= -DVERSION="\"$(VERSION)\"" -DNDEBUG -DGC_ON
DEFS_GCC	= -DVERSION="\"$(VERSION)\"" -DNDEBUG -DGC_ON
DEFS_DBG	= -DVERSION="\"$(VERSION)\"" -DDEBUG_ALL -DGC_ON
DEFS_PROF	= -DVERSION="\"$(VERSION)\"" -DDEBUG_ALL -DGC_ON

INCL_CC		=
INCL_GCC	=
INCL_DBG	=
INCL_PROF	=

LFLAGS_CC	=
LFLAGS_GCC	=
LFLAGS_DBG	= -g
LFLAGS_PROF	= -pg

OBJ_CC		= $(patsubst %.c,%-cc.o,$(ATERMSRCS))
OBJ_GCC		= $(patsubst %.c,%-gcc.o,$(ATERMSRCS))
OBJ_DBG		= $(patsubst %.c,%-dbg.o,$(ATERMSRCS))
OBJ_PROF	= $(patsubst %.c,%-prof.o,$(ATERMSRCS))

AR		= ar
AR_FLAGS	= cr

all: $(ATERMLIBS)
	@echo ${OBJ_GCC}

%-cc.o: %.c
	${CC_CC} ${CFLAGS_CC} -c -o $@ $<

%-gcc.o: %.c
	${CC_GCC} ${CFLAGS_GCC} -c -o $@ $<

%-dbg.o: %.c
	${CC_DBG} ${CFLAGS_DBG} -c -o $@ $<

%-prof.o: %.c
	${CC_PROF} ${CFLAGS_PROF} -c -o $@ $<

libATerm-cc.a: ${OBJ_CC}
	${AR} ${AR_FLAGS} $@ $<

libATerm-gcc.a:	${OBJ_GCC}
	${AR} ${AR_FLAGS} $@ $<

libATerm-dbg.a: ${OBJ_DBG}
	${AR} ${AR_FLAGS} $@ $<

libATerm-prof.a: ${OBJ_PROF}
	${AR} ${AR_FLAGS} $@ $<

clean:
	rm -f $(ATERMLIBS) *.o *~ core

tag:
	ctags -t *.[ch]

depend:
	$(CC_GCC) -MM $(CFLAGS_GCC) $(ATERMSRCS) > .depend

.depend:
	$(CC_GCC) -MM $(CFLAGS_GCC) $(ATERMSRCS) > .depend

include .depend
