#ifndef __Expr2_H
#define __Expr2_H

#include "Expr2_dict.h"

typedef struct _Expr *Expr;
typedef struct _Num *Num;

extern ATbool isExprPlus(Expr arg);
extern ATbool isExprMinus(Expr arg);

extern ATbool hasExprLhs(Expr arg);
extern Expr getExprLhs(Expr arg);
extern Expr setExprLhs(Expr arg, Expr lhs);
extern ATbool hasExprRhs(Expr arg);
extern Expr getExprRhs(Expr arg);
extern Expr setExprRhs(Expr arg, Expr rhs);

extern Expr makeExprPlus(Expr lhs, 
                         SDFLayout wsAfterLhs,
                         SDFLayout wsAfterPlus,
                         Expr rhs);

extern ATbool hasExprWsAfterLhs(Expr arg);
extern SDFLayout getExprWsAfterLhs(Expr arg);
extern Expr setExprWsAfterLhs(Expr arg, 
                              SDFLayout wsAfterLhs);
...
#endif
