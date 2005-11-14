
#include <limits.h>

#include "relational-aterms.h"

asdasd

#define COMPARATOR ATR_compare

#define RELATION_STORE_SIZE 100
#define RELATION_STORE_LOAD_PCT 50

int ATR_compare(const ATerm t1, const ATerm t2) {
  if (ATisEqual(t1,t2))
    return 0;

  if (ATR_isSet(t1) && ATR_isSet(t2)) {
    ATbool b1, b2;
    if (ATR_cardinality(t1) < ATR_cardinality(t2)) {
      return -1;
    }
    if (ATR_cardinality(t1) > ATR_cardinality(t2)) {
      return +1;
    }  
    if ((b1 = ATR_subSetOf(t1, t2))) {
      return -1;
    }
    if ((b2 = ATR_subSetOf(t2, t1))) {
      return +1;
    }
    if (b1 && b2) {
      return 0;
    }
    return ATcompare((ATerm)ATsort(ATR_toList(t1), ATR_compare), 
		     (ATerm)ATsort(ATR_toList(t2), ATR_compare));
  }
  return ATcompare(t1, t2);
}
    

void ATR_init() {
  ATbbtreeInit();
}

ATRelationStore ATR_createRelationStore() {
  return ATtableCreate(RELATION_STORE_SIZE, RELATION_STORE_LOAD_PCT);
}

void ATR_destroyRelationStore(ATRelationStore store) {
  ATtableDestroy(store);
}

void ATR_put(ATRelationStore store, ATerm key, ATSet set) {
  ATtablePut(store, key, set);
}

ATSet ATR_get(ATRelationStore store, ATerm key) {
  return ATtableGet(store, key);
}

ATSet ATR_fromString(char *string) {
  ATerm aterm = ATparse(string);
  if (ATgetType(aterm) == AT_LIST)
    return ATR_fromList((ATermList)aterm);
  return (ATSet)aterm;
}

ATSet ATR_empty() {
  return ATemptyBBTree;
}

ATTuple ATR_makeTuple(ATerm aterm1, ATerm aterm2) {
  return (ATTuple)ATmakeAppl2(ATmakeAFun("", 2, ATfalse), aterm1, aterm2);
}

ATerm ATR_getFirst(ATTuple tuple) {
  return ATgetArgument((ATermAppl)tuple, 0);
}

ATerm ATR_getSecond(ATTuple tuple) {
  return ATgetArgument((ATermAppl)tuple, 1);
}

ATSet ATR_fromList(ATermList list) {
  ATSet set = ATR_empty();
  while (!ATisEmpty(list)) {
    ATerm first = ATgetFirst(list);
    if (ATgetType(first) == AT_LIST) {
      set = ATR_insert(set, ATR_fromList((ATermList)first));
    }
    else {
      set = ATR_insert(set, first);
    }
    list = ATgetNext(list);
  }
  return set;
}


ATRelation  ATR_union(ATRelation relation1, ATRelation relation2) {
  return ATbbtreeUnion(relation1, relation2, COMPARATOR);
}

ATRelation  ATR_intersection(ATRelation relation1, ATRelation relation2) {
  return ATbbtreeIntersection(relation1, relation2, COMPARATOR);
}


ATRelation  ATR_difference(ATRelation relation1, ATRelation relation2) {
  return ATbbtreeDifference(relation1, relation2, COMPARATOR);
}


ATerm ATR_getHead(ATIterator iterator) {
  return ATgetFirst(iterator);
}


ATIterator ATR_getTail(ATIterator iterator) {
  return ATgetNext(iterator);
}

ATbool ATR_isEmpty(ATIterator iterator) {
  return ATisEmpty((ATermList)iterator);
}


ATermList ATR_toList(ATSet set) {
  return ATbbtreeToNestedList(set);
}

ATIterator ATR_getIterator(ATSet set) {
  return ATbbtreeToList(set);
}


ATermInt ATR_cardinality(ATRelation relation) {
  return ATmakeInt(ATbbtreeSize(relation));
}


ATbool ATR_member(ATRelation relation, ATerm element) {
  return ATbbtreeMember(relation, element, COMPARATOR);
}

ATbool ATR_isEqual(ATSet set1, ATSet set2) {
  return ATbbtreeIsEqual(set1, set2, COMPARATOR);
}

ATbool ATR_subSetOf(ATSet set1, ATSet set2) {
  return ATbbtreeSubSetOf(set1, set2, COMPARATOR);
}



ATSet ATR_insert(ATSet set, ATerm element) {
  return ATbbtreeInsert(set, element, COMPARATOR);
}

ATSet ATR_delete(ATSet set, ATerm element) {
  return ATbbtreeDelete(set, element, COMPARATOR);
}



ATRelation ATR_product(ATSet set1, ATSet set2) {
  ATIterator iter1 = ATR_getIterator(set1);
  ATIterator iter_saved = ATR_getIterator(set2);
  ATRelation relation = ATR_empty();

  while (!ATR_isEmpty(iter1)) {
    ATIterator iter2 = iter_saved;
    while (!ATR_isEmpty(iter2)) {
      relation = ATR_insert(relation, 
			    ATR_makeTuple(ATR_getHead(iter1),
					  ATR_getHead(iter2)));
      iter2 = ATR_getTail(iter2);
    }
    iter1 = ATR_getTail(iter1);
  }
  return relation;
}


ATSet ATR_domain(ATRelation relation) {
  ATIterator iter = ATR_getIterator(relation);
  ATSet set = ATR_empty();
  while (!ATR_isEmpty(iter)) {
    set = ATR_insert(set, ATR_getFirst(ATR_getHead(iter)));
    iter = ATR_getTail(iter);
  }
  return set;
}

ATSet ATR_sources(ATRelation relation) {
  ATSet range = ATR_range(relation);
  ATSet sources = ATR_empty();
  ATIterator iter = ATR_getIterator(ATR_domain(relation));
  while (!ATR_isEmpty(iter)) {
    ATerm head = ATR_getHead(iter);
    if (!ATR_member(range, head)) {
      sources = ATR_insert(sources, head);
    }
    iter = ATR_getTail(iter);
  }
  return sources;
}

ATSet ATR_sinks(ATRelation relation) {
  ATSet domain = ATR_domain(relation);
  ATSet sinks = ATR_empty();
  ATIterator iter = ATR_getIterator(ATR_range(relation));
  while (!ATR_isEmpty(iter)) {
    ATerm head = ATR_getHead(iter);
    if (!ATR_member(domain, head)) {
      sinks = ATR_insert(sinks, head);
    }
    iter = ATR_getTail(iter);
  }
  return sinks;
}


ATRelation ATR_domainRestriction(ATRelation relation, ATSet set) {
  return ATR_intersection(relation, ATR_product(set, ATR_range(relation)));
}

ATRelation ATR_domainExclusion(ATRelation relation, ATSet set) {
  return ATR_difference(relation, ATR_product(set, ATR_range(relation)));
}

ATSet ATR_rightImage(ATRelation relation, ATSet set) {
  return ATR_range(ATR_domainRestriction(relation, set));
}

ATSet ATR_leftImage(ATRelation relation, ATSet set) {
  return ATR_domain(ATR_rangeRestriction(relation, set));
}

ATSet ATR_range(ATRelation relation) {
  ATIterator iter = ATR_getIterator(relation);
  ATSet set = ATR_empty();
  while (!ATR_isEmpty(iter)) {
    set = ATR_insert(set, ATR_getSecond(ATR_getHead(iter)));
    iter = ATR_getTail(iter);
  }
  return set;
}

ATRelation ATR_rangeRestriction(ATRelation relation, ATSet set) {
  return ATR_intersection(relation, ATR_product(ATR_domain(relation), set));
}

ATRelation ATR_rangeExclusion(ATRelation relation, ATSet set) {
  return ATR_difference(relation, ATR_product(ATR_domain(relation), set));
}


ATRelation ATR_inverse(ATRelation relation) {
  ATIterator iter = ATR_getIterator(relation);
  ATRelation newRelation = ATR_empty();
  while (!ATR_isEmpty(iter)) {
    ATTuple tuple = ATR_getHead(iter);
    newRelation = ATR_insert(newRelation, 
			  ATR_makeTuple(
					ATR_getSecond(tuple),
					ATR_getFirst(tuple)));
    iter = ATR_getTail(iter);
  }
  return newRelation;
}

ATRelation ATR_complement(ATRelation relation) {
  ATSet carrier = ATR_carrier(relation);
  return ATR_difference(ATR_product(carrier, carrier), relation);
}


ATRelation ATR_compose(ATRelation relation1, ATRelation relation2) {
  ATIterator iter1 = ATR_getIterator(relation1);
  ATIterator iter_saved = ATR_getIterator(relation2);
  ATRelation composition = ATR_empty();
  while (!ATR_isEmpty(iter1)) {
    ATTuple tuple1 = ATR_getHead(iter1);
    ATIterator iter2 = iter_saved;
    while (!ATR_isEmpty(iter2)) {
      ATTuple tuple2 = ATR_getHead(iter2);
      if (ATisEqual(ATR_getSecond(tuple1), ATR_getFirst(tuple2))) {
	composition = ATR_insert(composition,
				 ATR_makeTuple(ATR_getFirst(tuple1),
					       ATR_getSecond(tuple2)));
      }
      iter2 = ATR_getTail(iter2);
    }
    iter1 = ATR_getTail(iter1);
  }
  return composition;
}

ATRelation ATR_symmetricClosure(ATRelation relation) {
  return ATR_union(relation, ATR_inverse(relation));
}

ATRelation ATR_reflexiveClosure(ATRelation relation) {
  return ATR_union(relation, ATR_identity(ATR_carrier(relation)));
}

ATRelation ATR_transitiveClosure(ATRelation relation) {
  int i;
  ATRelation cursor = relation;
  ATRelation closure = relation;
  for (i = 0; i < ATgetInt(ATR_cardinality(relation)) - 1; i++) {
    cursor = ATR_compose(cursor, relation);
    closure = ATR_union(closure, cursor);
  }
  return closure;
}

ATRelation ATR_transitiveReflexiveClosure(ATRelation relation) {
  return ATR_union(ATR_transitiveClosure(relation), 
		   ATR_reflexiveClosure(relation));
}

ATRelation ATR_identity(ATSet set) {
  ATIterator iter = ATR_getIterator(set);
  ATRelation relation = ATR_empty();
  while (!ATR_isEmpty(iter)) {
    ATerm elem = ATR_getHead(iter);
    relation = ATR_insert(relation, ATR_makeTuple(elem, elem));
    iter = ATR_getTail(iter);
  }
  return relation;
}


static void check_type_is_int_else_abort(ATerm t, char *msg) {
  if (!ATgetType(t) == AT_INT) 
    ATabort("Invalid set type to %s!\n", msg);
}

ATermInt ATR_sum(ATSet set) {
  ATIterator iter = ATR_getIterator(set);
  int n = 0;
  while (!ATR_isEmpty(iter)) {
    ATerm head = ATR_getHead(iter);
    check_type_is_int_else_abort(head, "ATR_sum");
    n += ATgetInt(((ATermInt)head));
    iter = ATR_getTail(iter);
  }
  return ATmakeInt(n);
}

ATermInt ATR_max(ATSet set) {
  ATIterator iter = ATR_getIterator(set);
  int max = 0;
  int n = 0;
  while (!ATR_isEmpty(iter)) {
    ATerm head = ATR_getHead(iter);
    check_type_is_int_else_abort(head, "ATR_max");
    n = ATgetInt(((ATermInt)head));
    max = max > n ? max : n;
    iter = ATR_getTail(iter);
  }
  return ATmakeInt(max);
}

ATermInt ATR_min(ATSet set) {
  ATIterator iter = ATR_getIterator(set);
  int min = INT_MAX;
  int n = 0;
  while (!ATR_isEmpty(iter)) {
    ATerm head = ATR_getHead(iter);
    check_type_is_int_else_abort(head, "ATR_min");
    n = ATgetInt(((ATermInt)head));
    min = min < n ? min : n;
    iter = ATR_getTail(iter);
  }
  if (min == INT_MAX) {
    min = 0;
  }
  return ATmakeInt(min);
}

ATermInt ATR_average(ATSet set) {
  int sum = ATgetInt(ATR_sum(set));
  int count = ATgetInt(ATR_cardinality(set));
  int avg = 0;
  if (count > 0) {
    avg = sum/count;
  }
  return ATmakeInt(avg);
}

ATSet ATR_carrier(ATRelation relation) {
  /* can be optimized using only one iteration. */
  return ATR_union(ATR_domain(relation), ATR_range(relation));
}

ATRelation ATR_carrierRestriction(ATRelation relation, ATSet set) {
  return ATR_intersection(ATR_domainRestriction(relation, set), 
			  ATR_rangeRestriction(relation, set));
}

ATRelation ATR_carrierExclusion(ATRelation relation, ATSet set) {
  return ATR_intersection(ATR_domainExclusion(relation, set), 
			  ATR_rangeExclusion(relation, set));
}



static ATermList mapInsertX(ATerm x, ATermList xs) {
  ATermList result = ATempty;
  while (!ATisEmpty(xs)) {
    result = ATinsert(result, (ATerm)ATinsert((ATermList)ATgetFirst(xs), x));
    xs = ATgetNext(xs);
  }
  return result;
}




/*
powerSet :: [a] -> [[a]]
powerSet [] = [[]]
powerSet (x:xs) = xss ++ map (x:) xss
                where xss = powerSet xs
*/

static ATermList powerList0(ATermList list) {
  if (ATisEmpty(list)) {
    return ATinsert(ATempty, (ATerm)ATempty);
  }
  else {
    ATerm x = ATgetFirst(list);
    ATermList xs = ATgetNext(list);
    ATermList xss = powerList0(xs);
    return ATconcat(xss, mapInsertX(x, xss));
  }
}


ATSet ATR_powerSet1(ATSet set) {
  return ATR_delete(ATR_powerSet0(set), ATR_empty());

}

ATSet ATR_powerSet0(ATSet set) {
  return ATR_fromList(powerList0(ATR_toList(set)));
}


ATbool ATR_isSet(ATerm set) {
  return ATbbtreeIsTree(set);
}


ATSet ATR_comprehend(ATSet set, ATermFunction func, ATerm extraArg) {
  ATSet newSet = ATR_empty();
  ATbbtreeAccumulate(set, &newSet, func, extraArg, COMPARATOR);
  return newSet;
}

ATSet ATR_comprehendFlattened(ATSet set, ATermFunction func, ATerm extraArg) {
  ATSet newSet = ATR_empty();
  ATbbtreeAccumulateFlattened(set, &newSet, func, extraArg, COMPARATOR);
  return newSet;
}

static ATerm matching_term(ATerm term, ATerm pattern) {
  /* Should assert  number of placeholders in pattern <= 10. */
  if (ATmatchTerm(term, pattern, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL))
    return term;
  return NULL;
}

static ATerm matching_subterms_as_list(ATerm term, ATerm pattern) {
  /* Should assert  number of placeholders in pattern <= 10. */
  ATermList list = ATempty;
  ATerm t1 = NULL, 
    t2 = NULL, 
    t3 = NULL, 
    t4 = NULL, 
    t5 = NULL, 
    t6 = NULL, 
    t7 = NULL, 
    t8 = NULL, 
    t9 = NULL, 
    t10 = NULL;
  if (ATmatchTerm(term, pattern, &t1, &t2, &t3, &t4, &t5, &t6, &t7, &t8, &t9, &t10)) {
    if ((t1)) list = ATinsert(list, t1);
    if ((t2)) list = ATinsert(list, t2);
    if ((t3)) list = ATinsert(list, t3);
    if ((t4)) list = ATinsert(list, t4);
    if ((t5)) list = ATinsert(list, t5);
    if ((t6)) list = ATinsert(list, t6);
    if ((t7)) list = ATinsert(list, t7);
    if ((t8)) list = ATinsert(list, t8);
    if ((t9)) list = ATinsert(list, t9);
    if ((t10)) list = ATinsert(list, t10);
  }
  return (ATerm)list;
}

static ATerm matching_subterms_as_set(ATerm term, ATerm pattern) {
  /*  ATwarning("matching %t to pattern %t, list %t, become %t\n", 
	    term, 
	    pattern,
	    matching_subterms_as_list(term, pattern),
	    ATR_fromList((ATermList)matching_subterms_as_list(term, pattern))); */
  return ATR_fromList((ATermList)matching_subterms_as_list(term, pattern));
}

ATSet ATR_matchingTerms(ATSet set, ATerm pattern) {
  return ATR_comprehend(set, matching_term, pattern);
}

ATSet ATR_matchingSubTerms(ATSet set, ATerm pattern) {
  return ATR_comprehend(set, matching_subterms_as_set, pattern);
}


ATSet ATR_matchingSubTermsFlattened(ATSet set, ATerm pattern) {
  return ATR_comprehendFlattened(set, matching_subterms_as_set, pattern);
}

