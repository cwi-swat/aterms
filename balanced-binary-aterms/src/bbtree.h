
/* Bounded Balance Trees in ATerms. */

#ifndef __BB_TREE__H
#define __BB_TREE__H 1

#include <stdio.h>
#include <aterm2.h>

typedef ATerm ATermBBTree;

/* A function-type to compare ATerms 
 * Should return:
 * -1 if a1 < a2
 *  0 if a1 = a2
 * +1 if a1 > a2
 * Note: compare(a1,a2) = 0 need not imply ATisEqual(a1,a2).
 */
typedef int (*ATermComparator)(const ATerm,const ATerm);


/* A function type used in accumulators. */
typedef ATerm (*ATermFunction)(const ATerm, const ATerm);


/* The empty tree */
extern ATermBBTree ATemptyBBTree;

/*
 * name   : ATbbtreeInit
 * pre    : true
 * action : initialize tree routines
 * post   : the tree routines are ready to use!
 */
void ATbbtreeInit();


/*
 * name   : ATbbtreeSize
 * pre    : t must be a valid ATermBBTree
 * action : compute the size (# of elements) of t
 * post   : the number of elements in t is returned.
 */
int ATbbtreeSize(ATermBBTree t);

/*
 * name   : ATbbtreeMember
 * pre    : t must be a valid ATermBBTree, elt a random aterm
 * action : check whether elt is in t via the comparator.
 * post   : true is returned when elt is in t.
 */
ATbool ATbbtreeMember(ATermBBTree t, ATerm elt, ATermComparator comparator);

/*
 * name   : ATbbtreeGet
 * pre    : tree is a valid ATermBBTree elt is an element
 * action : find an element in tree that is equal to elt
 *          as defined by ATbbtreeSetComparator.
 * post   : if an element is found this element is returned
 * note   : ATisEqual(ATbbtreeGet(t, e), e) need not hold.
 */
ATerm ATbbtreeGet(ATermBBTree tree, ATerm elt, ATermComparator comparator);

/*
 * name   : ATmakeTree
 * pre    : elt an arbitrary ATerm, l and r valid ATermBBTrees
 * action : create a balanced tree with l and r as siblings
 * post   : the new balanced tree is returned.
 */
ATermBBTree ATmakeBBTree(ATerm elt, ATermBBTree l, ATermBBTree r);

/*
 * name   : ATbbtreeInsert
 * pre    : t a valid ATerm tree, elt an arbitrary ATerm
 * action : insert elt in t
 * post   : the new tree containing elt is returned.
 */
ATermBBTree ATbbtreeInsert(ATermBBTree t, ATerm elt, ATermComparator comparator);

/*
 * name   : ATbbtreeDelete
 * pre    : t a valid ATerm tree, elt an arbitrary ATerm
 * action : delete elt from t if it exists
 * post   : the new tree lacking elt is returned.
 */
ATermBBTree ATbbtreeDelete(ATermBBTree t, ATerm elt, ATermComparator comparator);

/*
 * name   : ATbbtreeUnion
 * pre    : t1, t2 valid ATermBBTrees
 * action : compute the union of t1 and t2 as sets
 * post   : the union of t1 and t2 is returned.
 */
ATermBBTree ATbbtreeUnion(ATermBBTree t1, ATermBBTree t2, ATermComparator comparator);

/*
 * name   : ATbbtreeHUnion
 * pre    : t1, t2 valid ATermBBTrees
 * action : compute the union of t1 and t2 as sets with the hedge algorithm
 * post   : the union of t1 and t2 is returned.
 */
ATermBBTree ATbbtreeHUnion(ATermBBTree t1, ATermBBTree t2, ATermComparator comparator);

/*
 * name   : ATbbtreeDifference
 * pre    : t1, t2 valid ATermBBTrees
 * action : compute the asymmetric set-difference of t1 and t2
 * post   : t1 - t2 is returned.
 */
ATermBBTree ATbbtreeDifference(ATermBBTree t1, ATermBBTree t2, ATermComparator comparator);

/*
 * name   : ATbbtreeIntersection
 * pre    : t1, t2 valid ATermBBTrees
 * action : compute the intersection of sets t1 and t2
 * post   : the intersection of t1 and t2 is returned
 */
ATermBBTree ATbbtreeIntersection(ATermBBTree t1, ATermBBTree t2, ATermComparator comparator);


/*
 * name   : ATbbtreeHead
 * pre    : t valid ATermBBTree
 * action : get the head element of the tree
 * post   : the head element is returned
 */
ATerm ATbbtreeHead(ATermBBTree t);

/*
 * name   : ATbbtreeTail
 * pre    : t valid ATermBBTree, comparator valid comparator
 * action : get the tail set of the tree
 * post   : the tail set is returned
 */
ATermBBTree ATbbtreeTail(ATermBBTree t, ATermComparator comparator);

ATermList ATbbtreeToList(ATermBBTree t);


/*
 * name   : ATftreeToDot
 * pre    : f and open filehandle, t a valid ATermBBTree
 * action : print a tree representation of t to f to be processed by dot(1).
 * post   : the dot representation of t is written to f.
 */
void ATftreeToDot(FILE *f, ATermBBTree t);


ATbool ATbbtreeIsEqual(ATermBBTree set1, ATermBBTree set2, ATermComparator comparator);
ATbool ATbbtreeSubSetOf(ATermBBTree set1, ATermBBTree set2, ATermComparator comparator);

ATermList ATbbtreeToNestedList(ATermBBTree t);

ATbool ATbbtreeIsTree(ATerm t);

void ATbbtreeAccumulate(ATermBBTree source, ATermBBTree *dest, ATermFunction func, 
			ATerm extraArg, ATermComparator comparator);

void ATbbtreeAccumulateFlattened(ATermBBTree source, ATermBBTree *dest, ATermFunction func, 
				 ATerm extraArg, ATermComparator comparator);

ATermBBTree ATbbtreeAccumulateFunctional(ATermBBTree source, ATermFunction func, 
				   ATerm extraArg, ATermComparator comparator);


#endif
