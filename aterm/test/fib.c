#include <stdio.h>
#include <stdlib.h>

#include <aterm1.h>
#include <aterm2.h>
#include <assert.h>


/* Rewriting wrt. the TRS

fib(zero) -> succ(zero)
fib(succ(zero)) -> succ(zero)
fib(succ(succ(X))) -> plus(fib(X),fib(succ(X)))

plus(zero,X) -> X
plus(succ(X),Y) -> succ(plus(X,Y))
*/


static AFun f_zero;
static AFun f_suc;
static AFun f_plus;
static AFun f_fib;


#define zero() ((ATerm)ATmakeAppl0(f_zero))
#define suc(x) ((ATerm)ATmakeAppl1(f_suc, x))
#define mkfib(x) ((ATerm)ATmakeAppl1(f_fib, x))

void error(char *ss) {
  fprintf(stderr, "a problem %s\n",ss);
  exit(1);
}

int suc2int(ATerm t) {
  AFun f;
  int res = 0;
  ATerm sub;
  
  f = ATgetAFun(t);
  while(f==f_suc) {
    res++;
    sub = ATgetArgument(t,0);

      /*fprintf(stderr,"t = %x\tres= %d\tsub = %x\n",(unsigned int)t,res,(unsigned int)sub);*/
#ifdef PEM
    assert( GET_AGE(t->header) <= GET_AGE(sub->header)  );
#endif
    
    t = sub;
    f = ATgetAFun(t);
      /*printf("t = %x\n",(unsigned int)t);*/
  }
  
  if(f != f_zero) {
    fprintf(stderr,"top symbol = %s\n",ATgetName(f));
    ATprintf("%t\n",t);
    error("suc2int: no rule applicable");
  }
    /*printf("res = %d\n",res);*/
  return res;
}

ATerm plus(ATerm xx, ATerm yy) {
  ATerm x,y;
  AFun  ff;
  ff = ATgetAFun(xx);
  if (ff == f_zero) {
    x = yy;
    return(x);
  } else if (ff == f_suc) {
    x = ATgetArgument(xx,0);
    y = yy;
    return(suc(plus(x,y)));
  } else {
    error("no rule applicable");
  }
  return NULL;
}

ATerm fib(ATerm xx) {
  ATerm x,xxx;
  AFun  ff,fff;
  ff = ATgetAFun(xx);

  if (ff == f_zero) {
    return(suc(zero()));
  } else if (ff == f_suc) {
    xxx = ATgetArgument(xx,0);
    fff = ATgetAFun(xxx);
    if (fff == f_zero) {
      return(suc(zero()));
    } else if (fff == f_suc) {
      x = ATgetArgument(xxx,0);
      return(plus(fib(x), fib(xxx)));
    } else {
      error("no rule applicable");
    }
  } else {
    error("no rule applicable");
  }
  return NULL;
}

ATerm normalizePlus(ATerm t) {
    ATerm res = t;
    ATerm v0, v1, v2, v3, v4;
    while(1) {
      start:
      v0 = ATgetArgument(res,0);
        /* plus(s(s(s(s(s(x))))),y) => plus(x,s(s(s(s(s(y)))))) */
      if(ATgetAFun(v0) == f_suc) {
        v1 = ATgetArgument(v0,0);
        if(ATgetAFun(v1) == f_suc) {
          v2 = ATgetArgument(v1,0);
          if(ATgetAFun(v2) == f_suc) {
            v3 = ATgetArgument(v2,0);
            if(ATgetAFun(v3) == f_suc) {
              v4 = ATgetArgument(v3,0);
              if(ATgetAFun(v4) == f_suc) {
                res = (ATerm)ATmakeAppl2(f_plus,
                                  ATgetArgument(v4,0),
                                  (ATerm)ATmakeAppl1(f_suc,
                                  (ATerm)ATmakeAppl1(f_suc,
                                  (ATerm)ATmakeAppl1(f_suc,
                                  (ATerm)ATmakeAppl1(f_suc,
                                  (ATerm)ATmakeAppl1(f_suc,
                                  (ATerm)ATgetArgument(res,1)))))));
                  /*continue;*/
                goto start;
              }
            }
          }
        }
      }

        /* plus(0,x) = x */
      if(ATgetAFun(v0) == f_zero) {
        res = ATgetArgument(res,1);
        goto stop;
      }

        /* plus(s(x),y) => plus(x,s(y)) */
      if(ATgetAFun(v0) == f_suc) {
        res = (ATerm)ATmakeAppl2(f_plus,
                          ATgetArgument(v0,0),
                          (ATerm)ATmakeAppl1(f_suc,ATgetArgument(res,1)));
        goto start;
      }
      goto stop;
    }
  stop:
    return res;
   }
    
ATerm normalizeFib(ATerm t) {
  ATerm res = t;
  ATerm v0, v1, v2, fib1, fib2;

    while(1) {
        /* fib(0) = suc(0) */
      v0 = ATgetArgument(res,0);
        /*fprintf(stderr,"***\n");
          fprintf(stderr,"fib( %d )\n",suc2int(v0)); */

      
      if(ATgetAFun(v0) == f_zero) {
        res = (ATerm)ATmakeAppl1(f_suc,v0);
        break;
      }
        /* fib(suc(0)) => suc(0) */
      if(ATgetAFun(v0) == f_suc) {
        v1 = ATgetArgument(v0,0);
        if(ATgetAFun(v1) == f_zero) {
          res = v0;
          break;
        }
      }
        /*  fib(s(s(x))) => plus(fib(x),fib(s(x)))
               v0 v1 */
      if(ATgetAFun(v0) == f_suc) {
        v1 = ATgetArgument(v0,0);
        if(ATgetAFun(v1) == f_suc) {
          v2 = ATgetArgument(v1,0);
          fib2 = normalizeFib((ATerm)ATmakeAppl1(f_fib,v1));
          fib1 = normalizeFib((ATerm)ATmakeAppl1(f_fib,v2));
            /* System.out.println("adding"); */
          res = normalizePlus((ATerm)ATmakeAppl2(f_plus,fib1,fib2));
          break;
        }
      }
      break;
    }
    return res;
  }




ATerm normalize(ATerm t) 
{
  int       i, a;
  AFun      h;
  ATermList subs;
  ATerm     res;

  /* ATprintf("normalizing == %t\n", t) ; */
  h = ATgetAFun(t);
  if (h == f_plus) {
    res = plus(normalize(ATgetArgument(t,0)),normalize(ATgetArgument(t,1)));
  } else if (h == f_fib) {
    res = fib(normalize(ATgetArgument(t,0)));
  } else {
    a = ATgetArity(h);
    subs = ATmakeList0();
    for(i= a-1; i>=0; i--) {
      subs = ATinsert(subs, normalize(ATgetArgument(t, i)));
    }
    res = (ATerm)ATmakeApplList(h, subs);
  }
  /* ATprintf("normal form == %t (of %t)\n", res, t) ; */
  return(res);
}

int nfib(int n) {
  if (n<=1) return(1);
  else return(nfib(n-1)+nfib(n-2));
}

int mmain() {
  ATerm res;
  ATerm query ;
  int i,n;
  
  f_zero = ATmakeAFun("zero", 0, ATfalse);
  f_suc  = ATmakeAFun("suc", 1, ATfalse);
  f_fib  = ATmakeAFun("fib", 1, ATfalse);
  f_plus  = ATmakeAFun("plus", 2, ATfalse);

  ATprotectAFun(f_zero);
  ATprotectAFun(f_suc);
  ATprotectAFun(f_fib);
  ATprotectAFun(f_plus);

    /* printf("enter n: "); scanf("%d",&n); */
  n = 32;
    /* construct the term representation of the n value */
  query = zero();
  for(i=0;i<n;i++) {
    query = suc(query);
  }
    /*  write the original number and its 'fib' */
  for(i=0; i<1; i++) {
      /* res = fib(query); */
    res = normalizeFib(mkfib(query));
  }
    /* ATprintf("fib(%t) == %t\n", query, res) ; */
    printf("fib(%d) == %d\n", suc2int(query), suc2int(res)) ;


  
  return 0;
}


int main(int argc, char **argv)
{
  ATerm     bottomOfStack;
  /*  Initialise the ATerm bits & pieces  */
  ATinit(argc, argv, &bottomOfStack);
  mmain();
  return(0);
}
