#include <assert.h>
#include <aterm2.h>

/* This example demonstrates the use of an ATermPlaceholder. It creates
 * the function application "add" defined on two integers without actually
 * using a specific integer:  add(<int>,<int>).
 */
void demo_placeholder()
{
    Symbol           sym_int, sym_add;
    ATermAppl        app_add;
    ATermPlaceholder ph_int;

    /* Construct placeholder <int> using zero-arity function symbol "int" */
    sym_int = ATmakeSymbol("int", 0, ATfalse);
    ph_int = ATmakePlaceholder((ATerm)ATmakeAppl0(sym_int));

    /* Construct add(<int>,<int>) using function symbol "add" with 2 args */
    sym_add = ATmakeSymbol("add", 2, ATfalse);
    app_add = ATmakeAppl2(sym_add, (ATerm)ph_int, (ATerm)ph_int);

    /* Equal to constructing it using the level one interface */
    assert(ATisEqual(app_add, ATparse("add(<int>,<int>)")));

    /* Prints: Placeholder <int> is of type: int */
    ATprintf("Placeholder %t is of type: %t\n", ph_int, ATgetPlaceholder(ph_int));
}

int main(int argc, char *argv[])
{
    ATerm bottomOfStack;

    ATinit(argc, argv, &bottomOfStack);
    demo_placeholder();
    return 0;
}
