int    ival = 42;
char  *sval = "example";
char  *blob = "12345678";
double rval = 3.14;
char  *func = "f";

ATerm term[4];
term[0] = ATmake("<int>" , ival);       /* integer value: 42                */
term[1] = ATmake("<str>" , func);       /* quoted application: "f", no args */
term[2] = ATmake("<real>", dval);       /* real value: 3.14                 */
term[3] = ATmake("<blob>", 8, blob);    /* blob of size 8, data: 12345678   */

ATerm list[3];
list[0] = ATmake("[]");
list[1] = ATmake("[1,<int>,<real>]", ival, rval);
list[2] = ATmake("[<int>,<list>]", ival+1, list[1]);

ATerm appl[3];
appl[0] = ATmake("<appl>", func);
appl[1] = ATmake("<appl(<int>)>", func, ival);
appl[2] = ATmake("<appl(<int>, <term>, <list>)>", 42, term[3], list[2]);
