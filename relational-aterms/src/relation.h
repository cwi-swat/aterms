
#ifndef __RELATION__H
#define __RELATION__H 1


typedef ATbbtree

#define ATR_union(r1, r2) (ATbbtreeUnion(r1,r2, ATR_Comparator))
#define ATR_intersection(r1, r2) (ATbbtreeIntersection(r1,r2, ATR_Comparator))
#define ATR_difference(r1, r2) (ATbbtreeDifference(r1,r2, ATR_Comparator))
#define ATR_cardinality(r) (ATbbtreeSize(r))
#define ATR_member(r, e) (ATbbtree(r,e, ATR_Comparator))





#endif
