ATbool result;
ATerm  list;
int    ival;

/* Sets result to ATtrue and rval to 16. */
result = ATmatch(ATmake("f(16)"), "f(<int>)", &ival);

/* Sets result to ATtrue and rval to 3.14. */
result = ATmatch(ATmake("3.14"), "<real>", &rval);

/* Sets result to ATfalse because f(g) != g(f) */
result = ATmatch(ATmake("f(g)"), "g(f)");

/* fills ival with 1 and list with [2,3] */
result = ATmatch(ATmake("[1,2,3]"), "[<int>,<list>]", &ival, &list);
