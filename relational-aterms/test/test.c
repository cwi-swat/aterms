
#include <relational-aterms.h>

int assertEquals(int n, char *name, ATerm aterm1, ATerm aterm2) {
  fprintf(stderr, "[%03d] Test \"%s\" ", n, name);
  if (ATR_isEqual(aterm1, aterm2)) {
    fprintf(stderr, "succeeded.\n");
    return 1;
  }
  else {
    fprintf(stderr, "failed: ");
    if (ATR_isSet(aterm1)) {
      aterm1 = (ATerm)ATR_toList(aterm1);
    }
    if (ATR_isSet(aterm2)) {
      aterm2 = (ATerm)ATR_toList(aterm2);
    }
    ATfprintf(stderr, "expected %t, got: %t.\n", aterm2, aterm1);
    return 0;
  }
}


int test() {
  ATRelationStore store;
  int n = 1;
  int success = 1;
  store = ATR_createRelationStore();

  ATR_put(store, ATparse("ONE"), ATR_fromString("1"));
  ATR_put(store, ATparse("TWO"), ATR_fromString("2"));
  ATR_put(store, ATparse("THREE"), ATR_fromString("3"));
  ATR_put(store, ATparse("ONE-TWO"), ATR_fromString("(1, 2)"));
  ATR_put(store, ATparse("EMPTYSETSET"), ATR_fromString("[]"));
  ATR_put(store, ATparse("Set1"), ATR_fromString("[1, 2, 3]"));
  ATR_put(store, ATparse("Set2"), ATR_fromString("[3, 4, 5]"));
  ATR_put(store, ATparse("Relation1"), ATR_fromString("[(1, 10), (2, 20), (3, 30)]"));
  ATR_put(store, ATparse("Relation2"), ATR_fromString("[(3, 30), (4, 40), (5, 50)]"));
  success = success && assertEquals(n++, "set 1", ATR_fromString("[1]"), ATR_fromString("[1]"));
  success = success && assertEquals(n++, "set 2", ATR_fromString("[1, 2]"), ATR_fromString("[1, 2]"));
  success = success && assertEquals(n++, "set 3", ATR_fromString("[1, 2, 3]"), ATR_fromString("[1, 2, 3]"));
  success = success && assertEquals(n++, "set 4", ATR_fromString("[1, 2, 3]"), ATR_get(store, ATparse("Set1")));
  success = success && assertEquals(n++, "set 5", ATR_get(store, ATparse("Set1")), ATR_get(store, ATparse("Set1")));
  success = success && assertEquals(n++, "rel 1", ATR_fromString("[(1, 10)]"), ATR_fromString("[(1, 10)]"));
  success = success && assertEquals(n++, "rel 2", ATR_fromString("[(1, 10), (2, 20)]"), ATR_fromString("[(1, 10), (2, 20)]"));
  success = success && assertEquals(n++, "rel 3", ATR_fromString("[(1, 10), (2, 20), (3, 30)]"), ATR_fromString("[(1, 10), (2, 20), (3, 30)]"));
  success = success && assertEquals(n++, "rel 4", ATR_fromString("[(1, 10), (2, 20), (3, 30)]"), ATR_get(store, ATparse("Relation1")));
  success = success && assertEquals(n++, "rel 5", ATR_get(store, ATparse("Relation1")), ATR_get(store, ATparse("Relation1")));
  success = success && assertEquals(n++, "rel 11", ATR_fromString("[(1, (2, 3)), (4, (5, 6))]"), ATR_fromString("[(4, (5, 6)), (1, (2, 3))]"));
  success = success && assertEquals(n++, "true 1", ATR_fromString("true"), ATR_fromString("true"));
  success = success && assertEquals(n++, "false 1", ATR_fromString("false"), ATR_fromString("false"));
  success = success && assertEquals(n++, "== 1", ATR_fromString("1"), ATR_fromString("1"));
  success = success && assertEquals(n++, "== 2", ATR_fromString("-1"), ATR_fromString("-1"));
  success = success && assertEquals(n++, "== 3", ATR_fromString("\"a\""), ATR_fromString("\"a\""));
  success = success && assertEquals(n++, "== 4", ATR_fromString("[]"), ATR_fromString("[]"));
  success = success && assertEquals(n++, "== 5", ATR_fromString("[1]"), ATR_fromString("[1]"));
  success = success && assertEquals(n++, "== 6", ATR_fromString("[1, 2]"), ATR_fromString("[1, 2]"));
  success = success && assertEquals(n++, "== 7", ATR_fromString("[1, 2]"), ATR_fromString("[2, 1]"));
  success = success && assertEquals(n++, "== 8", ATR_fromString("[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"), ATR_fromString("[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"));
  success = success && assertEquals(n++, "== 9", ATR_fromString("[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"), ATR_fromString("[10, 2, 3, 4, 5, 6, 7, 8, 9, 1]"));
  success = success && assertEquals(n++, "== 10", ATR_fromString("[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"), ATR_fromString("[10, 9, 3, 4, 5, 6, 7, 8, 2, 1]"));
  success = success && assertEquals(n++, "== 11", ATR_fromString("[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"), ATR_fromString("[10, 9, 7, 4, 5, 6, 3, 8, 2, 1]"));
  success = success && assertEquals(n++, "== 12", ATR_fromString("[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"), ATR_fromString("[10, 9, 7, 6, 5, 4, 3, 8, 2, 1]"));
  success = success && assertEquals(n++, "== 13", ATR_fromString("[(1, 2)]"), ATR_fromString("[(1, 2)]"));
  success = success && assertEquals(n++, "== 14", ATR_fromString("[(1, 2), (3, 4)]"), ATR_fromString("[(1, 2), (3, 4)]"));
  success = success && assertEquals(n++, "== 15", ATR_fromString("[(1, 2), (3, 4)]"), ATR_fromString("[(3, 4), (1, 2)]"));
  success = success && assertEquals(n++, "== 16", ATR_fromString("[(1, (2, 3)), (4, (5, 6))]"), ATR_fromString("[(4, (5, 6)), (1, (2, 3))]"));
  success = success && assertEquals(n++, "== 17", ATR_fromString("[(1, (2, (3, 4))), (4, (5, (6, 7)))]"), ATR_fromString("[(4, (5, (6, 7))), (1, (2, (3, 4)))]"));
  success = success && assertEquals(n++, "== 18", ATR_fromString("[]"), ATR_fromString("[]"));
  success = success && assertEquals(n++, "== 19", ATR_fromString("[(1, [1, 2, 3]), (2, [2, 3, 4])]"), ATR_fromString("[(1, [1, 2, 3]), (2, [2, 3, 4])]"));
  success = success && assertEquals(n++, "== 20", ATR_fromString("[(1, [1, 2, 3]), (2, [2, 3, 4])]"), ATR_fromString("[(2, [2, 3, 4]), (1, [1, 2, 3])]"));
  success = success && assertEquals(n++, "== 21", ATR_fromString("[[1], [2]]"), ATR_fromString("[[2], [1]]"));
  success = success && assertEquals(n++, "== 22", ATR_fromString("[[]]"), ATR_fromString("[[]]"));
  success = success && assertEquals(n++, "== 23", ATR_fromString("[[], []]"), ATR_fromString("[[], []]"));
  ATR_put(store, ATparse("SmallSet"), ATR_fromString("[1, 2, 3, 2, 4]"));
  ATR_put(store, ATparse("BigSet"), ATR_fromString("[5, 4, 3, 2, 1, 2]"));
  success = success && assertEquals(n++, "union 1", ATR_union(ATR_fromString("[7]"), 
				    ATR_get(store, ATparse("Set1"))), ATR_fromString("[7, 1, 2, 3]"));
  success = success && assertEquals(n++, "union 2", ATR_union(ATR_get(store, ATparse("Set1")), 
				    ATR_fromString("[7]")), ATR_fromString("[1, 2, 3, 7]"));
  success = success && assertEquals(n++, "union 3", ATR_union(ATR_fromString("[]"), 
				    ATR_get(store, ATparse("Set1"))), ATR_get(store, ATparse("Set1")));
  success = success && assertEquals(n++, "union 4", ATR_union(ATR_get(store, ATparse("Set1")), 
				    ATR_fromString("[]")), ATR_get(store, ATparse("Set1")));
  success = success && assertEquals(n++, "union 5", ATR_union(ATR_fromString("[]"), 
				    ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "union 6", ATR_union(ATR_get(store, ATparse("Set1")), 
				    ATR_get(store, ATparse("Set2"))), ATR_fromString("[1, 2, 3, 4, 5]"));
  success = success && assertEquals(n++, "union 7", ATR_union(ATR_fromString("[(7, 70)]"), 
				    ATR_get(store, ATparse("Relation1"))), ATR_fromString("[(7, 70), (1, 10), (2, 20), (3, 30)]"));
  success = success && assertEquals(n++, "union 8", ATR_union(ATR_get(store, ATparse("Relation1")), 
				    ATR_fromString("[(7, 70)]")), ATR_fromString("[(1, 10), (2, 20), (3, 30), (7, 70)]"));
  success = success && assertEquals(n++, "union 9", ATR_union(ATR_fromString("[(1, (2, (3, 4))), (2, (3, (4, 5)))]"), 
				    ATR_fromString("[(2, (3, (4, 5))), (3, (4, (5, 6)))]")), ATR_fromString("[(1, (2, (3, 4))), (2, (3, (4, 5))), (3, (4, (5, 6)))]"));
  success = success && assertEquals(n++, "union 10", ATR_union(ATR_fromString("[]"), 
				     ATR_get(store, ATparse("Relation2"))), ATR_get(store, ATparse("Relation2")));
  success = success && assertEquals(n++, "union 11", ATR_union(ATR_get(store, ATparse("Relation1")), 
				     ATR_fromString("[]")), ATR_get(store, ATparse("Relation1")));
  success = success && assertEquals(n++, "union 12", ATR_union(ATR_fromString("[]"), 
				     ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "union 13", ATR_union(ATR_fromString("[1, 2, 3, 2, 1, 4]"), 
				     ATR_fromString("[1, 2, 2, 3, 3, 3]")), ATR_fromString("[1, 1, 2, 2, 3, 3, 3, 4]"));
  success = success && assertEquals(n++, "inter 1", ATR_intersection(ATR_fromString("[]"), 
					   ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 2", ATR_intersection(ATR_fromString("[]"), 
					   ATR_fromString("[7]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 3", ATR_intersection(ATR_get(store, ATparse("Set1")), 
					   ATR_fromString("[7]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 4", ATR_intersection(ATR_fromString("[7]"), 
					   ATR_get(store, ATparse("Set1"))), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 5", ATR_intersection(ATR_get(store, ATparse("Set1")), 
					   ATR_fromString("[2]")), ATR_fromString("[2]"));
  success = success && assertEquals(n++, "inter 6", ATR_intersection(ATR_fromString("[2]"), 
					   ATR_get(store, ATparse("Set1"))), ATR_fromString("[2]"));
  success = success && assertEquals(n++, "inter 7", ATR_intersection(ATR_fromString("[]"), 
					   ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 8", ATR_intersection(ATR_get(store, ATparse("Set1")), 
					   ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 9", ATR_intersection(ATR_fromString("[]"), 
					   ATR_get(store, ATparse("Set1"))), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 10", ATR_intersection(ATR_get(store, ATparse("Set1")), 
					    ATR_get(store, ATparse("Set2"))), ATR_fromString("[3]"));
  success = success && assertEquals(n++, "inter 11", ATR_intersection(ATR_get(store, ATparse("Set2")), 
					    ATR_get(store, ATparse("Set1"))), ATR_fromString("[3]"));
  success = success && assertEquals(n++, "inter 12", ATR_intersection(ATR_fromString("[(7, 70)]"), 
					    ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 13", ATR_intersection(ATR_fromString("[]"), 
					    ATR_fromString("[(7, 70)]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 14", ATR_intersection(ATR_fromString("[(2, 20)]"), 
					    ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 15", ATR_intersection(ATR_get(store, ATparse("Relation1")), 
					    ATR_fromString("[(7, 70)]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 16", ATR_intersection(ATR_fromString("[(2, 20)]"), 
					    ATR_get(store, ATparse("Relation1"))), ATR_fromString("[(2, 20)]"));
  success = success && assertEquals(n++, "inter 17", ATR_intersection(ATR_fromString("[(1, (2, (3, 4))), (2, (3, (4, 5)))]"), 
					    ATR_fromString("[(2, (3, (4, 5))), (3, (4, (5, 6)))]")), ATR_fromString("[(2, (3, (4, 5)))]"));
  success = success && assertEquals(n++, "inter 18", ATR_intersection(ATR_fromString("[]"), 
					    ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 19", ATR_intersection(ATR_get(store, ATparse("Relation1")), 
					    ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 20", ATR_intersection(ATR_fromString("[]"), 
					    ATR_get(store, ATparse("Relation1"))), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inter 21", ATR_intersection(ATR_get(store, ATparse("Relation1")), 
					    ATR_get(store, ATparse("Relation2"))), ATR_fromString("[(3, 30)]"));
  success = success && assertEquals(n++, "inter 22", ATR_intersection(ATR_get(store, ATparse("Relation2")), 
					    ATR_get(store, ATparse("Relation1"))), ATR_fromString("[(3, 30)]"));
  success = success && assertEquals(n++, "inter 23", ATR_intersection(ATR_fromString("[1, 2, 3, 2, 1]"), 
					    ATR_fromString("[1, 2, 2, 3, 3, 3]")), ATR_fromString("[1, 2, 2, 3]"));
  success = success && assertEquals(n++, "inter 24", ATR_intersection(ATR_fromString("[1, 2, 3, 2, 1, 4]"), 
					    ATR_fromString("[1, 2, 2, 3, 3, 3]")), ATR_fromString("[1, 2, 2, 3]"));
  success = success && assertEquals(n++, "diff 1", ATR_difference(ATR_get(store, ATparse("Set1")), 
					ATR_fromString("[]")), ATR_get(store, ATparse("Set1")));
  success = success && assertEquals(n++, "diff 2", ATR_difference(ATR_fromString("[]"), 
					ATR_get(store, ATparse("Set1"))), ATR_fromString("[]"));
  success = success && assertEquals(n++, "diff 3", ATR_difference(ATR_get(store, ATparse("Set1")), 
					ATR_get(store, ATparse("Set2"))), ATR_fromString("[1, 2]"));
  success = success && assertEquals(n++, "diff 4", ATR_difference(ATR_get(store, ATparse("Set2")), 
					ATR_get(store, ATparse("Set1"))), ATR_fromString("[4, 5]"));
  success = success && assertEquals(n++, "diff 5", ATR_difference(ATR_get(store, ATparse("Relation1")), 
					ATR_fromString("[]")), ATR_get(store, ATparse("Relation1")));
  success = success && assertEquals(n++, "diff 6", ATR_difference(ATR_fromString("[]"), 
					ATR_get(store, ATparse("Relation1"))), ATR_fromString("[]"));
  success = success && assertEquals(n++, "diff 7", ATR_difference(ATR_get(store, ATparse("Relation1")), 
					ATR_get(store, ATparse("Relation2"))), ATR_fromString("[(1, 10), (2, 20)]"));
  success = success && assertEquals(n++, "diff 8", ATR_difference(ATR_get(store, ATparse("Relation2")), 
					ATR_get(store, ATparse("Relation1"))), ATR_fromString("[(4, 40), (5, 50)]"));

  success = success && assertEquals(n++, "comp 1", ATR_compose(ATR_fromString("[]"), 
				     ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "comp 2", ATR_compose(ATR_fromString("[(1, 10), (2, 20)]"), 
				     ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "comp 3", ATR_compose(ATR_fromString("[]"), 
				     ATR_fromString("[(10, 100), (20, 200)]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "comp 4", ATR_compose(ATR_fromString("[(1, 10), (2, 20)]"), 
				     ATR_fromString("[(10, 100), (20, 200)]")), ATR_fromString("[(1, 100), (2, 200)]"));
  success = success && assertEquals(n++, "product 1", ATR_product(ATR_fromString("[]"), 
					ATR_get(store, ATparse("Set1"))), ATR_fromString("[]"));
  success = success && assertEquals(n++, "product 2", ATR_product(ATR_get(store, ATparse("Set1")), 
					ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "product 3", ATR_product(ATR_fromString("[9]"), 
					ATR_get(store, ATparse("Set1"))), ATR_fromString("[(9, 1), (9, 2), (9, 3)]"));
  success = success && assertEquals(n++, "product 4", ATR_product(ATR_get(store, ATparse("Set1")), 
					ATR_fromString("[9]")), ATR_fromString("[(1, 9), (2, 9), (3, 9)]"));
  success = success && assertEquals(n++, "product 5", ATR_product(ATR_get(store, ATparse("Set1")), 
					ATR_get(store, ATparse("Set2"))), ATR_fromString("[(1, 3), (1, 4), (1, 5), (2, 3), (2, 4), (2, 5), (3, 3), (3, 4), (3, 5)]"));
  ATR_put(store, ATparse("Relation3"), ATR_fromString("[(1, 10), (2, 20), (1, 11), (3, 30), (2, 21)]"));
  success = success && assertEquals(n++, "image 1", ATR_leftImage(ATR_get(store, ATparse("Relation3")),
					ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "image 2", ATR_rightImage(ATR_get(store, ATparse("Relation3")),
					ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "image 3", ATR_rightImage(ATR_get(store, ATparse("Relation3")),
					ATR_fromString("[1]")), ATR_fromString("[10, 11]"));
  success = success && assertEquals(n++, "image 4", ATR_leftImage(ATR_get(store, ATparse("Relation3")),
					 ATR_fromString("[10, 11]")), ATR_fromString("[1]"));
  success = success && assertEquals(n++, "image 5", ATR_rightImage(ATR_get(store, ATparse("Relation3")),
					ATR_fromString("[1, 2]")), ATR_fromString("[10, 11, 20, 21]"));
  success = success && assertEquals(n++, "trans 1", ATR_transitiveClosure(ATR_fromString("[(1, 2), (2, 3), (3, 4)]")), ATR_fromString("[(1, 2), (2, 3), (3, 4), (1, 3), (2, 4), (1, 4)]"));
  success = success && assertEquals(n++, "trans 2", ATR_transitiveReflexiveClosure(ATR_fromString("[(1, 2), (2, 3), (3, 4)]")), ATR_fromString("[(1, 2), (2, 3), (3, 4), (1, 3), (2, 4), (1, 4), (1, 1), (2, 2), (3, 3), (4, 4)]"));
  success = success && assertEquals(n++, "trans 3", ATR_transitiveClosure(ATR_fromString("[(1, 2), (2, 3), (3, 4), (4, 2), (4, 5)]")), ATR_fromString("[(1, 2), (2, 3), (3, 4), (4, 2), (4, 5), (1, 3), (2, 4), (3, 2), (3, 5), (4, 3), (1, 4), (2, 2), (2, 5), (3, 3), (4, 4), (1, 5)]"));
  success = success && assertEquals(n++, "trans 4", ATR_transitiveReflexiveClosure(ATR_fromString("[(1, 2), (2, 3), (3, 4), (4, 2), (4, 5)]")), ATR_fromString("[(1, 2), (2, 3), (3, 4), (4, 2), (4, 5), (1, 3), (2, 4), (3, 2), (3, 5), (4, 3), (1, 4), (2, 2), (2, 5), (3, 3), (4, 4), (1, 5), (1, 1), (5, 5)]"));
  success = success && assertEquals(n++, "size 1", (ATerm)ATR_cardinality(ATR_fromString("[]")), ATR_fromString("0"));
  success = success && assertEquals(n++, "size 2", (ATerm)ATR_cardinality(ATR_fromString("[]")), ATR_fromString("0"));
  success = success && assertEquals(n++, "size 3", (ATerm)ATR_cardinality(ATR_get(store, ATparse("Set1"))), ATR_fromString("3"));
  success = success && assertEquals(n++, "size 4", (ATerm)ATR_cardinality(ATR_get(store, ATparse("Relation1"))), ATR_fromString("3"));


  ATR_put(store, ATparse("Rel1"), ATR_fromString("[(1, 10)]"));
  ATR_put(store, ATparse("Rel2"), ATR_fromString("[(1, 10), (2, 20), (3, 30)]"));
  ATR_put(store, ATparse("Rel3"), ATR_fromString("[(1, 10), (2, 20), (3, 30), (2, 21), (1, 11)]"));
  ATR_put(store, ATparse("Rel4"), ATR_fromString("[(1, 10), (2, 20), (3, 10), (2, 10), (1, 20)]"));
  success = success && assertEquals(n++, "id-1", ATR_identity(ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "id-2", ATR_identity(ATR_fromString("[1, 2, 3]")), ATR_fromString("[(1, 1), (2, 2), (3, 3)]"));
  success = success && assertEquals(n++, "domain-1", ATR_domain(ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "domain-2", ATR_domain(ATR_get(store, ATparse("Rel1"))), ATR_fromString("[1]"));
  success = success && assertEquals(n++, "domain-3", ATR_domain(ATR_get(store, ATparse("Rel2"))), ATR_fromString("[1, 2, 3]"));
  success = success && assertEquals(n++, "domain-4", ATR_domain(ATR_get(store, ATparse("Rel3"))), ATR_fromString("[1, 2, 3, 2, 1]"));
  success = success && assertEquals(n++, "range-1", ATR_range(ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "range-2", ATR_range(ATR_get(store, ATparse("Rel1"))), ATR_fromString("[10]"));
  success = success && assertEquals(n++, "range-3", ATR_range(ATR_get(store, ATparse("Rel2"))), ATR_fromString("[10, 20, 30]"));
  success = success && assertEquals(n++, "range-4", ATR_range(ATR_get(store, ATparse("Rel4"))), ATR_fromString("[10, 20, 10, 10, 20]"));
  ATR_put(store, ATparse("range4"), ATR_range(ATR_get(store, ATparse("Rel4"))));
  success = success && assertEquals(n++, "carrier-1", ATR_carrier(ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "carrier-2", ATR_carrier(ATR_get(store, ATparse("Rel2"))), ATR_fromString("[1, 2, 3, 10, 20, 30]"));
  success = success && assertEquals(n++, "carrier-3", ATR_carrier(ATR_get(store, ATparse("Rel3"))), ATR_fromString("[1, 10, 2, 20, 3, 30, 2, 21, 1, 11]"));
  ATR_put(store, ATparse("G"), ATR_fromString("[(1, 2), (1, 3), (2, 4), (3, 4)]"));
  success = success && assertEquals(n++, "top-1", ATR_sources(ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "top-2", ATR_sources(ATR_get(store, ATparse("G"))), ATR_fromString("[1]"));
  success = success && assertEquals(n++, "bottom-1", ATR_sinks(ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "bottom-2", ATR_sinks(ATR_get(store, ATparse("G"))), ATR_fromString("[4]"));
  success = success && assertEquals(n++, "inv-1", ATR_inverse(ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "inv-2", ATR_inverse(ATR_get(store, ATparse("Rel2"))), ATR_fromString("[(10, 1), (20, 2), (30, 3)]"));
  success = success && assertEquals(n++, "compl-1", ATR_complement(ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "compl-2", ATR_complement(ATR_get(store, ATparse("Rel1"))), ATR_fromString("[(1, 1), (10, 1), (10, 10)]"));
  success = success && assertEquals(n++, "domainR-1", ATR_domainRestriction(
						  ATR_fromString("[]"), 
						  ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "domainR-2", ATR_domainRestriction(
						  ATR_fromString("[]"), 
						  ATR_fromString("[1]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "domainR-3", ATR_domainRestriction(
						  ATR_get(store, ATparse("Rel2")), 
						  ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "domainR-4", ATR_domainRestriction(
						  ATR_get(store, ATparse("Rel2")), 
						  ATR_fromString("[1]")), ATR_fromString("[(1, 10)]"));
  success = success && assertEquals(n++, "domainR-5", ATR_domainRestriction(
						  ATR_get(store, ATparse("Rel2")), 
						  ATR_fromString("[3, 1]")), ATR_fromString("[(1, 10), (3, 30)]"));
  success = success && assertEquals(n++, "domainR-6", ATR_domainRestriction(
						  ATR_get(store, ATparse("Rel2")), 
						  ATR_fromString("[7, 17]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "domainR-7", ATR_domainRestriction(
						  ATR_get(store, ATparse("Rel3")), 
						  ATR_fromString("[1, 2]")), ATR_fromString("[(1, 10), (2, 20), (2, 21), (1, 11)]"));
  success = success && assertEquals(n++, "rangeR-1", ATR_rangeRestriction(
						ATR_fromString("[]"), 
						ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "rangeR-2", ATR_rangeRestriction(
						ATR_fromString("[]"), 
						ATR_fromString("[10]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "rangeR-3", ATR_rangeRestriction(
						ATR_get(store, ATparse("Rel2")), 
						ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "rangeR-4", ATR_rangeRestriction(
						ATR_get(store, ATparse("Rel2")), 
						ATR_fromString("[10]")), ATR_fromString("[(1, 10)]"));
  success = success && assertEquals(n++, "rangeR-5", ATR_rangeRestriction(
						ATR_get(store, ATparse("Rel2")), 
						ATR_fromString("[30, 10]")), ATR_fromString("[(1, 10), (3, 30)]"));
  success = success && assertEquals(n++, "rangeR-6", ATR_rangeRestriction(
						ATR_get(store, ATparse("Rel2")), 
						ATR_fromString("[7, 17]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "rangeR-7", ATR_rangeRestriction(
						ATR_get(store, ATparse("Rel4")), 
						ATR_fromString("[10]")), ATR_fromString("[(1, 10), (3, 10), (2, 10)]"));
  success = success && assertEquals(n++, "carrierR-1", ATR_carrierRestriction(
						    ATR_fromString("[]"), 
						    ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "carrierR-2", ATR_carrierRestriction(
						    ATR_fromString("[]"), 
						    ATR_fromString("[10]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "carrierR-3", ATR_carrierRestriction(
						    ATR_get(store, ATparse("Rel2")), 
						    ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "carrierR-4", ATR_carrierRestriction(
						    ATR_get(store, ATparse("Rel2")), 
						    ATR_fromString("[10, 1, 20]")), ATR_fromString("[(1, 10)]"));
  success = success && assertEquals(n++, "carrierR-5", ATR_carrierRestriction(
						    ATR_get(store, ATparse("Rel2")), 
						    ATR_fromString("[30, 1, 3, 10]")), ATR_fromString("[(1, 10), (3, 30)]"));
  success = success && assertEquals(n++, "carrierR-6", ATR_carrierRestriction(
						    ATR_get(store, ATparse("Rel2")), 
						    ATR_fromString("[7, 17]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "domainX-1", ATR_domainExclusion(
						ATR_fromString("[]"), 
						ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "domainX-2", ATR_domainExclusion(
						ATR_fromString("[]"), 
						ATR_fromString("[1]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "domainX-3", ATR_domainExclusion(
						ATR_get(store, ATparse("Rel2")), 
						ATR_fromString("[]")), ATR_get(store, ATparse("Rel2")));
  success = success && assertEquals(n++, "domainX-4", ATR_domainExclusion(
						ATR_get(store, ATparse("Rel2")), 
						ATR_fromString("[1]")), ATR_fromString("[(2, 20), (3, 30)]"));
  success = success && assertEquals(n++, "domainX-5", ATR_domainExclusion(
						ATR_get(store, ATparse("Rel2")), 
						ATR_fromString("[3, 1]")), ATR_fromString("[(2, 20)]"));
  success = success && assertEquals(n++, "domainX-6", ATR_domainExclusion(
						ATR_get(store, ATparse("Rel2")), 
						ATR_fromString("[7, 17]")), ATR_get(store, ATparse("Rel2")));
  success = success && assertEquals(n++, "rangeX-1", ATR_rangeExclusion(
					      ATR_fromString("[]"), 
					      ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "rangeX-2", ATR_rangeExclusion(
					      ATR_fromString("[]"), 
					      ATR_fromString("[10]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "rangeX-3", ATR_rangeExclusion(
					      ATR_get(store, ATparse("Rel2")), 
					      ATR_fromString("[]")), ATR_get(store, ATparse("Rel2")));
  success = success && assertEquals(n++, "rangeX-4", ATR_rangeExclusion(
					      ATR_get(store, ATparse("Rel2")), 
					      ATR_fromString("[10]")), ATR_fromString("[(2, 20), (3, 30)]"));
  success = success && assertEquals(n++, "rangeX-5", ATR_rangeExclusion(
					      ATR_get(store, ATparse("Rel2")), 
					      ATR_fromString("[30, 10]")), ATR_fromString("[(2, 20)]"));
  success = success && assertEquals(n++, "rangeX-6", ATR_rangeExclusion(
					      ATR_get(store, ATparse("Rel2")), 
					      ATR_fromString("[7, 17]")), ATR_get(store, ATparse("Rel2")));
  success = success && assertEquals(n++, "carrierX-1", ATR_carrierExclusion(
						  ATR_fromString("[]"), 
						  ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "carrierX-2", ATR_carrierExclusion(
						  ATR_fromString("[]"), 
						  ATR_fromString("[10]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "carrierX-3", ATR_carrierExclusion(
						  ATR_get(store, ATparse("Rel2")), 
						  ATR_fromString("[]")), ATR_get(store, ATparse("Rel2")));
  success = success && assertEquals(n++, "carrierX-4", ATR_carrierExclusion(
						  ATR_get(store, ATparse("Rel2")), 
						  ATR_fromString("[10, 1, 20]")), ATR_fromString("[(3, 30)]"));
  success = success && assertEquals(n++, "carrierX-5", ATR_carrierExclusion(
						  ATR_get(store, ATparse("Rel2")), 
						  ATR_fromString("[30, 1, 3, 10]")), ATR_fromString("[(2, 20)]"));
  success = success && assertEquals(n++, "carrierX-6", ATR_carrierExclusion(
						  ATR_get(store, ATparse("Rel2")), 
						  ATR_fromString("[7, 17]")), ATR_get(store, ATparse("Rel2")));

/*   success = success && assertEquals(n++, "reachX-1", ATR_reachExclusion( */
/* 					      ATR_fromString("[]"),  */
/* 					      ATR_fromString("[]"), */
/* 					      ATR_fromString("[]")), ATR_fromString("[]")); */
/*   success = success && assertEquals(n++, "reachX-2", ATR_reachExclusion( */
/* 					      ATR_fromString("[1]"),  */
/* 					      ATR_fromString("[]"), */
/* 					      ATR_get(store, ATparse("G"))), ATR_fromString("[(1, 2), (1, 3), (1, 4)]")); */
/*   success = success && assertEquals(n++, "reachX-3", ATR_reachExclusion( */
/* 					      ATR_fromString("[1]"),  */
/* 					      ATR_fromString("[2]"), */
/* 					      ATR_get(store, ATparse("G"))), ATR_fromString("[(1, 3), (1, 4)]")); */
/*   success = success && assertEquals(n++, "reachX-4", ATR_reachExclusion( */
/* 					      ATR_fromString("[1]"),  */
/* 					      ATR_fromString("[2, 3]"), */
/* 					      ATR_get(store, ATparse("G"))), ATR_fromString("[]")); */
/*   success = success && assertEquals(n++, "reachX-5", ATR_reachExclusion( */
/* 					      ATR_fromString("[1]"),  */
/* 					      ATR_fromString("[4]"), */
/* 					      ATR_get(store, ATparse("G"))), ATR_fromString("[(1, 2), (1, 3)]")); */
/*   success = success && assertEquals(n++, "reachR-1", ATR_reachRestriction( */
/* 						ATR_fromString("[]"),  */
/* 						ATR_fromString("[]"), */
/* 						ATR_fromString("[]")), ATR_fromString("[]")); */
/*   success = success && assertEquals(n++, "reachR-2", ATR_reachRestriction( */
/* 						ATR_fromString("[1]"),  */
/* 						ATR_fromString("[]"), */
/* 						ATR_get(store, ATparse("G"))), ATR_fromString("[]")); */
/*   success = success && assertEquals(n++, "reachR-3", ATR_reachRestriction( */
/* 						ATR_fromString("[1]"),  */
/* 						ATR_fromString("[1, 2]"), */
/* 						ATR_get(store, ATparse("G"))), ATR_fromString("[(1, 2)]")); */
/*   success = success && assertEquals(n++, "reachR-4", ATR_reachRestriction( */
/* 						ATR_fromString("[1]"),  */
/* 						ATR_fromString("[1, 2, 3]"), */
/* 						ATR_get(store, ATparse("G"))), ATR_fromString("[(1, 2), (1, 3)]")); */
/*   success = success && assertEquals(n++, "reachR-5", ATR_reachRestriction( */
/* 						ATR_fromString("[1]"),  */
/* 						ATR_fromString("[1, 2, 4]"), */
/* 						ATR_get(store, ATparse("G"))), ATR_fromString("[(1, 2), (1, 4)]")); */
  success = success && assertEquals(n++, "power0-1", ATR_powerSet0(ATR_fromString("[]")), ATR_fromString("[[]]"));
  success = success && assertEquals(n++, "power0-2", ATR_powerSet0(ATR_fromString("[1]")), ATR_fromString("[[], [1]]"));
  success = success && assertEquals(n++, "power0-3", ATR_powerSet0(ATR_fromString("[1, 2]")), ATR_fromString("[[], [1], [2], [1, 2]]"));
  success = success && assertEquals(n++, "power0-4", ATR_powerSet0(ATR_fromString("[1, 2, 3, 4]")), ATR_fromString("[[], [1], [2], [3], [4], [1, 2], [1, 3], [1, 4], [2, 3], [2, 4], [3, 4], [1, 2, 3], [1, 2, 4], [1, 3, 4], [2, 3, 4], [1, 2, 3, 4]]"));
  success = success && assertEquals(n++, "powerSet1-1", ATR_powerSet1(ATR_fromString("[]")), ATR_fromString("[]"));
  success = success && assertEquals(n++, "powerSet1-2", ATR_powerSet1(ATR_fromString("[1]")), ATR_fromString("[[1]]"));
  success = success && assertEquals(n++, "powerSet1-3", ATR_powerSet1(ATR_fromString("[1, 2]")), ATR_fromString("[[1], [2], [1, 2]]"));
  success = success && assertEquals(n++, "powerSet1-4", ATR_powerSet1(ATR_fromString("[1, 2, 3, 4]")), ATR_fromString("[[1], [2], [3], [4], [1, 2], [1, 3], [1, 4], [2, 3], [2, 4], [3, 4], [1, 2, 3], [1, 2, 4], [1, 3, 4], [2, 3, 4], [1, 2, 3, 4]]"));

  success = success && assertEquals(n++, "sum-1", (ATerm)ATR_sum(ATR_fromString("[]")), ATparse("0"));
  success = success && assertEquals(n++, "sum-2", (ATerm)ATR_sum(ATR_fromString("[1]")), ATparse("1"));
  success = success && assertEquals(n++, "sum-3", (ATerm)ATR_sum(ATR_fromString("[1, 2]")), ATparse("3"));
  success = success && assertEquals(n++, "sum-4", (ATerm)ATR_sum(ATR_fromString("[1, 2, 3]")), ATparse("6"));
  success = success && assertEquals(n++, "sum-5", (ATerm)ATR_sum(ATR_fromString("[1, -2, 3]")), ATparse("2"));
  success = success && assertEquals(n++, "sum-6", (ATerm)ATR_sum(ATR_fromString("[1, 1, 1]")), ATparse("1"));
  success = success && assertEquals(n++, "average-1", (ATerm)ATR_average(ATR_fromString("[]")), ATparse("0"));
  success = success && assertEquals(n++, "average-2", (ATerm)ATR_average(ATR_fromString("[1]")), ATparse("1"));
  success = success && assertEquals(n++, "average-3", (ATerm)ATR_average(ATR_fromString("[1, 3]")), ATparse("2"));
  success = success && assertEquals(n++, "max-1", (ATerm)ATR_max(ATR_fromString("[]")), ATparse("0"));
  success = success && assertEquals(n++, "max-2", (ATerm)ATR_max(ATR_fromString("[1]")), ATparse("1"));
  success = success && assertEquals(n++, "max-3", (ATerm)ATR_max(ATR_fromString("[1, 2, 3]")), ATparse("3"));
  success = success && assertEquals(n++, "max-4", (ATerm)ATR_max(ATR_fromString("[3, 2, 1]")), ATparse("3"));
  success = success && assertEquals(n++, "max-5", (ATerm)ATR_max(ATR_fromString("[3, -2, 1]")), ATparse("3"));
  success = success && assertEquals(n++, "max-6", (ATerm)ATR_max(ATR_fromString("[1, 2, 1, 2]")), ATparse("2"));
  success = success && assertEquals(n++, "min-1", (ATerm)ATR_min(ATR_fromString("[]")), ATparse("0"));
  success = success && assertEquals(n++, "min-2", (ATerm)ATR_min(ATR_fromString("[1]")), ATparse("1"));
  success = success && assertEquals(n++, "min-3", (ATerm)ATR_min(ATR_fromString("[1, 2, 3]")), ATparse("1"));
  success = success && assertEquals(n++, "min-4", (ATerm)ATR_min(ATR_fromString("[3, 2, 1]")), ATparse("1"));
  success = success && assertEquals(n++, "min-5", (ATerm)ATR_min(ATR_fromString("[3, -2, 1]")), ATparse("-2"));
  success = success && assertEquals(n++, "min-6", (ATerm)ATR_min(ATR_fromString("[1, 2, 1, 2]")), ATparse("1"));

  success = success && assertEquals(n++, "matching-terms-1", 
	       ATR_matchingTerms(ATR_fromString("[]"), ATparse("3")), 
	       ATR_fromString("[]"));
  success = success && assertEquals(n++, "matching-terms-1", 
	       ATR_matchingTerms(ATR_fromString("[1,2,3]"), ATparse("3")), 
	       ATR_fromString("[3]"));
  success = success && assertEquals(n++, "matching-terms-3", 
	       ATR_matchingTerms(ATR_fromString("[1,2,3]"), ATparse("4")), 
	       ATR_fromString("[]"));
  success = success && assertEquals(n++, "matching-terms-4", 
	       ATR_matchingTerms(ATR_fromString("[1,2,3]"), ATparse("<int>")), 
	       ATR_fromString("[1,2,3]"));

  success = success && assertEquals(n++, "matching-subterms-1", 
	       ATR_matchingSubTerms(ATR_fromString("[]"), ATparse("<term>")), 
	       ATR_fromString("[]"));
  success = success && assertEquals(n++, "matching-subterms-2", 
	       ATR_matchingSubTerms(ATR_fromString("[f(a,b),g(b,c),f(d,e)]"), 
				 ATparse("f(<term>,<term>)")), 
				 ATR_fromString("[[a,b],[d,e],[]]")); 
  success = success && assertEquals(n++, "matching-subterms-3", 
	       ATR_matchingSubTerms(ATR_fromString("[1,2,3]"), ATparse("<term>")), 
	       ATR_fromString("[[1],[2],[3]]"));

  success = success && assertEquals(n++, "matching-subterms-4", 
	       ATR_matchingSubTerms(ATR_fromString("[1,2,3]"), ATparse("<appl>")), 
	       ATR_fromString("[[]]"));

  success = success && assertEquals(n++, "matching-subterms-flattened-1", 
	       ATR_matchingSubTermsFlattened(ATR_fromString("[]"), ATparse("<term>")), 
	       ATR_fromString("[]"));
  success = success && assertEquals(n++, "matching-subterms-flattened-2", 
	       ATR_matchingSubTermsFlattened(ATR_fromString("[f(a,b),g(b,c),f(d,e)]"), 
				 ATparse("f(<term>,<term>)")), 
				 ATR_fromString("[a,b,d,e]")); 
  success = success && assertEquals(n++, "matching-subterms-flattened-3", 
	       ATR_matchingSubTermsFlattened(ATR_fromString("[1,2,3]"), ATparse("<term>")), 
	       ATR_fromString("[1,2,3]"));

  success = success && assertEquals(n++, "matching-subterms-flattened-4", 
	       ATR_matchingSubTermsFlattened(ATR_fromString("[1,2,3]"), ATparse("<appl>")), 
	       ATR_fromString("[]"));

  return success;
}


int main(int argc, char **argv) {
  ATerm bottomOfStack;
  ATinit(argc, argv, &bottomOfStack);
  ATR_init();
  if (test()) {
    return 0;
  }
  return 1;
}
