
/**
  * encoding.h: Low level encoding of ATerm datatype.
  */

#ifndef ENCODING_H
#define ENCODING_H

#define	MASK_QUOTED	(1<<0)
#define	MASK_ANNO	MASK_QUOTED
#define MASK_MARK	(1<<1)
#define MASK_ARITY	((1<<2) | (1<<3) | (1<<4))
#define MASK_TYPE	((1<<5) | (1<<6) | (1<<7))

#define SHIFT_ARITY	2
#define SHIFT_TYPE	5
#define SHIFT_LENGTH	8
#define SHIFT_SYMBOL	SHIFT_LENGTH

#define GET_MARK(h)     ((h) & MASK_MARK)
#define GET_TYPE(h)     (((h) & MASK_TYPE) >> SHIFT_TYPE)
#define HAS_ANNO(h)     ((h) & MASK_ANNO)
#define GET_ARITY(h)	(((h) & MASK_ARITY) >> SHIFT_ARITY)
#define GET_SYMBOL(h)	((h) >> SHIFT_SYMBOL)
#define GET_LENGTH(h)	((h) >> SHIFT_LENGTH)
#define IS_QUOTED(h)	((h) & MASK_QUOTED)

#define SET_MARK(h)			((h) | MASK_MARK)
#define SET_TYPE(h, type)	(((h) & ~MASK_TYPE) | ((type) << SHIFT_TYPE))
#define SET_ANNO(h)			((h) | MASK_ANNO)
#define SET_ARITY(h, ar)	(((h) & ~MASK_ARITY) | ((ar) << SHIFT_ARITY))
#define SET_SYMBOL(h, sym)	(((h) & ~MASK_SYMBOL) | (sym) << SHIFT_SYMBOL)
#define SET_LENGTH(h, len)  (((h) & ~MASK_LENGTH) | (len) << SHIFT_LENGTH)
#define SET_QUOTED(h)		((h) | MASK_QUOTED)

#define CLR_MARK(h)			((h) & ~MASK_MARK)
#define CLR_ANNO(h)			((h) & ~MASK_ANNO)
#define CLR_QUOTED(h)		((h) & ~MASK_QUOTED)

#define APPL_HEADER(anno,ari,sym) ((anno) | ((ari) << SHIFT_ARITY) | \
				   (AT_APPL << SHIFT_TYPE) | \
				   ((header_type)(sym) << SHIFT_SYMBOL))
#define INT_HEADER(anno)          ((anno) | AT_INT << SHIFT_TYPE)
#define REAL_HEADER(anno)         ((anno) | AT_REAL << SHIFT_TYPE)


/* This assumes 32 bits int */
typedef unsigned int header_type;

#endif
