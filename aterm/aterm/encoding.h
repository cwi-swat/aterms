
/**
  * encoding.h: Low level encoding of ATerm datatype.
  */

#ifndef ENCODING_H
#define ENCODING_H

#define TYPE_MASK   0x00000007
#define ANNO_MASK   0x00000008
#define ARITY_MASK  0x000000F0
#define SYMBOL_MASK 0xFFFFFF00
#define LENGTH_MASK SYMBOL_MASK

#define ANNO_ON     0x00000008
#define ANNO_OFF    0x00000000

#define ARITY_SHIFT  4
#define LENGTH_SHIFT 8

#define t_get_type(t)      ((t) & TYPE_MASK)
#define t_has_anno(t)      ((t) & ANNO_MASK)
#define t_get_arity(t)     (((t) & ARITY_MASK) >> ARITY_SHIFT)
#define t_get_symbol(appl) ((Symbol *)((appl) & SYMBOL_MASK))
#define t_get_length(list) ((list & LENGTH_MASK) >> LENGTH_SHIFT)

#define t_set_type(t,type) (((t) & ~TYPE_MASK) | (type))
#define t_set_anno(t,on)   (((t) & ~ANNO_MASK) | (on))
#define t_set_arity(t,ar)  (((t) & ~ARITY_MASK) | ((ar) << ARITY_SHIFT))
#define t_set_symbol(appl,sym)  (((appl) & ~SYMBOL_MASK) | (sym))
#define t_set_length(list,len)  (((list) & ~LENGTH_MASK) | ((len) << LENGTH_SHIFT))

#endif
