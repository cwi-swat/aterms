
PRODUCT		= toolbus

ATBSRCS		= atb-tool.c
ATBLIBS		= libATB-gcc.a libATB-cc.a libATB-dbg.a libATB-prof.a
PROGS		= ctif

CC_CC		= cc
CC_GCC		= gcc
CC_DBG		= ${CC_GCC}
CC_PROF		= ${CC_GCC}

CFLAGS_CC	= -g -O ${DEFS_CC} ${INCL_CC}
#CFLAGS_CC      = -g ${DEFS_CC} ${INCL_CC}
CFLAGS_GCC	= -Wall -pedantic -O4 ${DEFS_GCC} ${INCL_GCC}
CFLAGS_DBG	= -Wall -pedantic -g ${DEFS_DBG} ${INCL_DBG}
CFLAGS_PROF	= -Wall -pedantic -pg ${DEFS_PROF} ${INCL_PROF}

DEFS_CC		= -DNDEBUG -DGC_ON
DEFS_GCC	= -DNDEBUG -DGC_ON
DEFS_DBG	= -DDEBUG_ALL -DGC_ON
DEFS_PROF	= -DDEBUG_ALL -DGC_ON

INCL        = -I../aterm
INCL_CC		= $(INCL)
INCL_GCC	= $(INCL)
INCL_DBG	= $(INCL)
INCL_PROF	= $(INCL)

LFLAGS_CC	=
LFLAGS_GCC	=
LFLAGS_DBG	= -g
LFLAGS_PROF	= -pg

OBJ_CC		= $(patsubst %.c,%-cc.o,$(ATBSRCS))
OBJ_GCC		= $(patsubst %.c,%-gcc.o,$(ATBSRCS))
OBJ_DBG		= $(patsubst %.c,%-dbg.o,$(ATBSRCS))
OBJ_PROF	= $(patsubst %.c,%-prof.o,$(ATBSRCS))

AR		= ar
AR_FLAGS	= cr

all: $(ATBLIBS) $(PROGS)

%-cc.o: %.c
	${CC_CC} ${CFLAGS_CC} -c -o $@ $<

%-gcc.o: %.c
	${CC_GCC} ${CFLAGS_GCC} -c -o $@ $<

%-dbg.o: %.c
	${CC_DBG} ${CFLAGS_DBG} -c -o $@ $<

%-prof.o: %.c
	${CC_PROF} ${CFLAGS_PROF} -c -o $@ $<

libATB-cc.a: ${OBJ_CC}
	${AR} ${AR_FLAGS} $@ ${OBJ_CC}

libATB-gcc.a:	${OBJ_GCC}
	${AR} ${AR_FLAGS} $@ ${OBJ_GCC}

libATB-dbg.a: ${OBJ_DBG}
	${AR} ${AR_FLAGS} $@ ${OBJ_DBG}

libATB-prof.a: ${OBJ_PROF}
	${AR} ${AR_FLAGS} $@ ${OBJ_PROF}

ctif: ctif.c atb-tool.h ../aterm/libATerm-dbg.a libATB-dbg.a
	${CC_DBG} ${CFLAGS_DBG} -o ctif ctif.c \
			-L../aterm -lATerm-dbg -L. -lATB-dbg -lsocket -lnsl

clean:
	rm -f $(ATBLIBS) *.o *~ core ctif 

tag:
	ctags -t *.[ch]

depend:
	$(CC_GCC) -MM $(DEFS_CC) $(INCL_CC) $(ATBSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-cc.o:/"   >  .depend
	$(CC_GCC) -MM $(DEFS_GCC) $(INCL_GCC) $(ATBSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-gcc.o:/"  >> .depend
	$(CC_GCC) -MM $(DEFS_DBG) $(INCL_DBG) $(ATBSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-dbg.o:/"  >> .depend
	$(CC_GCC) -MM $(DEFS_PROF) $(INCL_PROF) $(ATBSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-prof.o:/" >> .depend
	rm -f /tmp/.gendep

.depend:
	$(CC_GCC) -MM $(DEFS_CC) $(INCL_CC) $(ATBSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-cc.o:/"   >  .depend
	$(CC_GCC) -MM $(DEFS_GCC) $(INCL_GCC) $(ATBSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-gcc.o:/"  >> .depend
	$(CC_GCC) -MM $(DEFS_DBG) $(INCL_DBG) $(ATBSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-dbg.o:/"  >> .depend
	$(CC_GCC) -MM $(DEFS_PROF) $(INCL_PROF) $(ATBSRCS) > /tmp/.gendep
	cat /tmp/.gendep | sed "s/.o:/-prof.o:/" >> .depend
	rm -f /tmp/.gendep

include .depend
