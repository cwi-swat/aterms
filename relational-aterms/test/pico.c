

#include <relational-aterms.h>
#include <aterm2.h>

static ATSet topDownAccumulator(ATerm term, ATermFunction func, ATerm extra, ATSet accu);
static ATSet topDownAccumulatorList(ATermList list, ATermFunction func, ATerm extra, ATSet accu);
static ATerm topDownTransformer(ATerm term, ATermFunction func, ATerm extra);
static ATermList topDownTransformerList(ATermList list, ATermFunction func, ATerm extra);
static ATerm matchDefinedId(ATerm term, ATerm extra);
static ATSet extractDeclaredIds(ATerm term);


static ATSet topDownAccumulator(ATerm term, ATermFunction func, ATerm extra, ATSet accu) {
  ATerm elt = NULL;
  ATSet result = accu;

  if ((elt = func(term, extra))) {
    result = ATR_insert(result, elt);
  }

  if (ATgetType(term) == AT_APPL) {
    return topDownAccumulatorList(ATgetArguments((ATermAppl)term), func, extra, result);
  }

  if (ATgetType(term) == AT_LIST) {
    return topDownAccumulatorList((ATermList)term, func, extra, result);
  }

  return result;
}

static ATSet topDownAccumulatorList(ATermList list, ATermFunction func, ATerm extra, ATSet accu) {
  if (ATisEmpty(list)) {
    return accu;
  }
  return topDownAccumulatorList(ATgetNext(list), func, extra, 
		      topDownAccumulator(ATgetFirst(list), func, extra, accu));
}

static ATerm topDownTransformer(ATerm term, ATermFunction func, ATerm extra) {
  ATerm newTerm = NULL;
  if ((newTerm = func(term, extra))) {
    term = newTerm;
  }

  if (ATgetType(term) == AT_APPL) {
    ATermAppl appl = (ATermAppl)term;
    AFun afun = ATgetAFun(appl);
    ATerm annos = ATgetAnnotations(term);
    ATermList args = ATgetArguments(appl);
    ATermList newArgs = topDownTransformerList(args, func, extra);
    return ATsetAnnotations((ATerm)ATmakeApplList(afun, newArgs), annos);
  }
  if (ATgetType(term) == AT_LIST) {
    ATerm annos = ATgetAnnotations(term);
    return ATsetAnnotations((ATerm)topDownTransformerList((ATermList)term, func, extra), annos);
  }
  ATerror("topDownTransformer reached end of function!\n");
  return NULL;
}

static ATermList topDownTransformerList(ATermList list, ATermFunction func, ATerm extra) {
  if (ATisEmpty(list)) {
    return list;
  }
  return ATinsert(topDownTransformerList(ATgetNext(list), func, extra),
		  topDownTransformer(ATgetFirst(list), func, extra));

}


static ATerm matchDefinedId(ATerm term, ATerm extra) {
  ATerm id = NULL;
  term = ATremoveAnnotations(term);
  if (ATmatch(term, "id-type(<term>, <term>)", &id, NULL)) {
    return id;
  }
  return NULL;
}

static ATerm matchUsedId(ATerm term, ATerm extra) {
  ATerm id = NULL;
  term = ATremoveAnnotations(term);
  if (ATmatch(term, "id(<term>)", &id)) {
    return id;
  }
  return NULL;
}

static ATerm matchAssignedId(ATerm term, ATerm extra) {
  ATerm id = NULL;
  term = ATremoveAnnotations(term);
  if (ATmatch(term, "assign(<term>, <term>)", &id, NULL)) {
    return id;
  }
  return NULL;
}

static ATerm annotateUnique(ATerm term, ATerm extra) {
  static int count = 0;
  ATerm label = ATmake("id");
  ATerm id;
  if (!(id = ATgetAnnotation(term, label))) {
    id = (ATerm)ATmakeInt(++count);
    return ATsetAnnotation(term, label, id);
  }
  return term;
}


static ATSet extractDeclaredIds(ATerm term) {
  return topDownAccumulator(term, matchDefinedId, NULL, ATR_empty());
}

static ATSet extractUsedIds(ATerm term) {
  return topDownAccumulator(term, matchUsedId, NULL, ATR_empty());
}

static ATSet extractAssignedIds(ATerm term) {
  return topDownAccumulator(term, matchAssignedId, NULL, ATR_empty());
}

/*
static ATerm matchNode(ATerm term) {
  ATerm id = ATgetAnnotation(term, ATmake("id"));
  term = ATremoveAnnotations(term); 
  return ATR_makeTuple(stat, id);
  return NULL;
}
*/

static ATerm makeEachNodeUnique(ATerm term) {
  return topDownTransformer(term, annotateUnique, NULL);
}

int main(int argc, char **argv) {
  ATerm bottomOfStack;
  ATerm fac;
  ATinit(argc, argv, &bottomOfStack);
  ATR_init();
  ATsetChecking(ATtrue);

  fac = ATreadFromNamedFile("fac.ast");
  
  ATprintf("fac ast: %t\n\n", fac);
  fac = makeEachNodeUnique(fac);
  ATprintf("fac uniqued: %t\n\n", fac);
  ATprintf("fac decls: %t\n\n", ATR_toList(extractDeclaredIds(fac)));
  ATprintf("fac uses: %t\n\n", ATR_toList(extractUsedIds(fac)));
  ATprintf("fac writes: %t\n\n", ATR_toList(extractAssignedIds(fac)));

  if (1) {
    return 0;
  }
  return 1;
}

