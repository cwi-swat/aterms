
#ifndef __ATERM_RELATIONS__H
#define __ATERM_RELATIONS__H 1

#include <bbtree.h>
#include <aterm2.h>

typedef ATermBBTree ATRelation;
typedef ATermBBTree ATSet;
typedef ATermList ATIterator;
typedef ATerm ATTuple;
typedef ATermTable ATRelationStore;

void ATR_init();

ATRelationStore ATR_createRelationStore();
void ATR_destroyRelationStore(ATRelationStore store);
void ATR_put(ATRelationStore store, ATerm key, ATSet set);
ATSet ATR_get(ATRelationStore store, ATerm key);

ATTuple ATR_makeTuple(ATerm aterm1, ATerm aterm2);

ATerm ATR_getFirst(ATTuple tuple);
ATerm ATR_getSecond(ATTuple tuple);

ATSet ATR_fromString(char *string);

ATSet ATR_empty();

ATSet ATR_insert(ATSet set, ATerm element);
ATSet ATR_delete(ATSet set, ATerm element);

ATSet ATR_fromList(ATermList list);
ATermList ATR_toList(ATSet set);
		   
ATSet ATR_union(ATSet set1, ATSet set2);
ATSet ATR_intersection(ATSet set1, ATSet set2);
ATSet ATR_difference(ATSet set1, ATSet set2);

ATermInt ATR_cardinality(ATSet set);
ATbool ATR_member(ATSet set, ATerm element);
ATbool ATR_isEqual(ATSet set1, ATSet set2);
ATbool ATR_subSetOf(ATSet set1, ATSet set2);


ATIterator ATR_getIterator(ATSet set);
ATbool ATR_isEmpty(ATIterator iterator);
ATerm ATR_getHead(ATIterator iterator);
ATIterator ATR_getTail(ATIterator iterator);

ATRelation ATR_product(ATSet set1, ATSet set2);
ATRelation ATR_identity(ATSet set);

ATSet ATR_domain(ATRelation relation);
ATSet ATR_range(ATRelation relation);
ATSet ATR_carrier(ATRelation relation);

ATSet ATR_sources(ATRelation relation);
ATSet ATR_sinks(ATRelation relation);


ATSet ATR_rightImage(ATRelation relation, ATSet set);
ATSet ATR_leftImage(ATRelation relation, ATSet set);

ATRelation ATR_domainRestriction(ATRelation relation, ATSet set);
ATRelation ATR_domainExclusion(ATRelation relation, ATSet set);

ATRelation ATR_rangeRestriction(ATRelation relation, ATSet set);
ATRelation ATR_rangeExclusion(ATRelation relation, ATSet set);

ATRelation ATR_carrierRestriction(ATRelation relation, ATSet set);
ATRelation ATR_carrierExclusion(ATRelation relation, ATSet set);


ATRelation ATR_compose(ATRelation relation1, ATRelation relation2);

ATRelation ATR_inverse(ATRelation relation);
ATRelation ATR_complement(ATRelation relation);
ATRelation ATR_symmetricClosure(ATRelation relation);
ATRelation ATR_reflexiveClosure(ATRelation relation);
ATRelation ATR_transitiveClosure(ATRelation relation);
ATRelation ATR_transitiveReflexiveClosure(ATRelation relation);


ATermInt ATR_sum(ATSet set);
ATermInt ATR_max(ATSet set);
ATermInt ATR_min(ATSet set);
ATermInt ATR_average(ATSet set);


ATSet ATR_powerSet1(ATSet set);
ATSet ATR_powerSet0(ATSet set);

ATbool ATR_isSet(ATerm set);

int ATR_compare(const ATerm t1, const ATerm t2);



ATSet ATR_comprehend(ATSet set, ATermFunction func, ATerm extraArg);
ATSet ATR_comprehendFlattened(ATSet set, ATermFunction func, ATerm extraArg);

/*
 * pre: pattern contains no more thatn 10 placeholders.
 */
ATSet ATR_matchingTerms(ATSet set, ATerm pattern);
ATSet ATR_matchingSubTerms(ATSet set, ATerm pattern);
ATSet ATR_matchingSubTermsFlattened(ATSet set, ATerm pattern);


#endif
