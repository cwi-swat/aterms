
PRODUCT		= aterm
VERSION		= 0.0.4

ATERMSRCS	= aterm.c list.c make.c memory.c symbol.c gc.c
ATERMLIBS	= libATerm-gcc.a libATerm-cc.a libATerm-dbg.a libATerm-prof.a

CC_CC		= cc
CC_GCC		= gcc
CC_DBG		= ${CC_GCC}
CC_PROF		= ${CC_GCC}

CFLAGS_CC	= -g -O ${DEFS_CC} ${INCL_CC}
#CFLAGS_CC      = -g ${DEFS_CC} ${INCL_CC}
CFLAGS_GCC	= -Wall -pedantic -O4 ${DEFS_GCC} ${INCL_GCC}
CFLAGS_DBG	= -Wall -pedantic -g ${DEFS_DBG} ${INCL_DBG}
CFLAGS_PROF	= -Wall -pedantic -pg ${DEFS_PROF} ${INCL_PROF}

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

%-cc.o: %.c
	${CC_CC} ${CFLAGS_CC} -c -o $@ $<

%-gcc.o: %.c
	${CC_GCC} ${CFLAGS_GCC} -c -o $@ $<

%-dbg.o: %.c
	${CC_DBG} ${CFLAGS_DBG} -c -o $@ $<

%-prof.o: %.c
	${CC_PROF} ${CFLAGS_PROF} -c -o $@ $<

libATerm-cc.a: ${OBJ_CC}
	${AR} ${AR_FLAGS} $@ ${OBJ_CC}

libATerm-gcc.a:	${OBJ_GCC}
	${AR} ${AR_FLAGS} $@ ${OBJ_GCC}

libATerm-dbg.a: ${OBJ_DBG}
	${AR} ${AR_FLAGS} $@ ${OBJ_DBG}

libATerm-prof.a: ${OBJ_PROF}
	${AR} ${AR_FLAGS} $@ ${OBJ_PROF}

clean:
	rm -f $(ATERMLIBS) *.o *~ core

tag:
	ctags -t *.[ch]

depend:
	$(CC_GCC) -MM $(DEFS_CC) $(INCL_CC) $(ATERMSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-cc.o:/"   >  .depend
	$(CC_GCC) -MM $(DEFS_GCC) $(INCL_GCC) $(ATERMSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-gcc.o:/"  >> .depend
	$(CC_GCC) -MM $(DEFS_DBG) $(INCL_DBG) $(ATERMSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-dbg.o:/"  >> .depend
	$(CC_GCC) -MM $(DEFS_PROF) $(INCL_PROF) $(ATERMSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-prof.o:/" >> .depend
	rm -f /tmp/.gendep

.depend:
	$(CC_GCC) -MM $(DEFS_CC) $(INCL_CC) $(ATERMSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-cc.o:/"   >  .depend
	$(CC_GCC) -MM $(DEFS_GCC) $(INCL_GCC) $(ATERMSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-gcc.o:/"  >> .depend
	$(CC_GCC) -MM $(DEFS_DBG) $(INCL_DBG) $(ATERMSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-dbg.o:/"  >> .depend
	$(CC_GCC) -MM $(DEFS_PROF) $(INCL_PROF) $(ATERMSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-prof.o:/" >> .depend
	rm -f /tmp/.gendep

include .depend
