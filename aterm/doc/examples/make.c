#include <aterm2.h>

int    ival = 42;
char  *sval = "example";
char  *blob = "12345678";
double rval = 3.14;
char  *func = "f";

void foo()
{
    ATerm term[4];
    ATerm list[3];
    ATerm appl[3];

    term[0] = ATmake("<int>" , ival);       /* integer value: 42                */
    term[1] = ATmake("<str>" , func);       /* quoted application: "f", no args */
    term[2] = ATmake("<real>", rval);       /* real value: 3.14                 */
    term[3] = ATmake("<blob>", 8, blob);    /* blob of size 8, data: 12345678   */

    list[0] = ATmake("[]");
    list[1] = ATmake("[1,<int>,<real>]", ival, rval);
    list[2] = ATmake("[<int>,<list>]", ival+1, list[1]);

    appl[0] = ATmake("<appl>", func);
    appl[1] = ATmake("<appl(<int>)>", func, ival);
    appl[2] = ATmake("<appl(<int>, <term>, <list>)>", func, 42, term[3], list[2]);

    ATprintf("appl[2] = %t\n", appl[2]);
}

int main(int argc, char *argv[])
{
    ATerm bottomOfStack;

    ATinit(argc, argv, &bottomOfStack);
    foo();
    return 0;
}
