#include <assert.h>

#include "tool2.tif.h"

/*{{{  void rec_terminate(int conn, ATerm t) */

void rec_terminate(int conn, ATerm t)
{
  ATprintf("rec_terminate called: %d, %t\n", conn, t);
  exit(0);
}

/*}}}  */
/*{{{  void rec_ack_event(int conn, ATerm t) */

void rec_ack_event(int conn, ATerm t)
{
  static ATerm big_term;
  ATerm unpacked_big_term;

  ATprintf("rec_ack_event called: %d, %t\n", conn, t);

  if (ATmatch(t, "test-event-2(<list>)", NULL)) {
    big_term = ATparse("f(<t>,1,3.4{[[a,b]]})");
    ATBwriteTerm(conn, ATmake("snd-event(pack-test(<term>))", 
			      ATBpack(big_term)));
  } else if(ATmatch(t, "pack-test(<term>)", &unpacked_big_term)) {
    assert(ATisEqual(big_term, unpacked_big_term));
    ATfprintf(stderr, "packing mechanism is working!\n");
  }
}

/*}}}  */

/*{{{  void start_events(int conn) */

void start_events(int conn)
{
  ATerm t;

  ATprintf("starting events\n");
  ATBwriteTerm(conn, ATparse("snd-event(test-event-1(f(1)))"));
  t = ATparse("snd-event(test-event-2(a,[]))");
  ATprintf("sending event: %t\n", t);
  ATBwriteTerm(conn, t);
  ATprintf("events started\n");
}

/*}}}  */

/*{{{  ATerm test_eval_3(int conn, char *s, int i , ATerm t) */

ATerm test_eval_3(int conn, char *s, int i , ATerm t)
{
  ATprintf("test_eval_3: %d, %s, %d, %t\n", conn, s, i, t);
  return ATmake("snd-value(ok(<str>,<int>,<term>))", s, i, t);
}

/*}}}  */
/*{{{  ATerm test_eval_2(int conn, int i, char *s) */

ATerm test_eval_2(int conn, int i, char *s)
{
  ATprintf("test_eval_2: %d, %d, %s\n", conn, i, s);
  return ATmake("snd-value(ok(<int>,<str>))", i, s);	
}

/*}}}  */
/*{{{  ATerm test_eval_1(int conn, ATerm t) */

ATerm test_eval_1(int conn, ATerm t)
{
  ATprintf("test_eval_1: %d, %t\n", conn, t);
  return ATmake("snd-value(ok(<term>))", t);	
}

/*}}}  */
/*{{{  ATerm test_eval_0(int conn) */

ATerm test_eval_0(int conn)
{
  ATprintf("test_eval_0: %d\n", conn);
  return ATmake("snd-value(ok)");	
}

/*}}}  */

/*{{{  void test_do_3(int conn, ATerm t1, ATerm t2, ATerm t3) */

void test_do_3(int conn, ATerm t1, ATerm t2, ATerm t3)
{
  ATprintf("test_do_3: %d, %t, %t, %t\n", conn, t1, t2, t3);	
}

/*}}}  */
/*{{{  void test_do_2(int conn, char *s, ATerm t) */

void test_do_2(int conn, char *s, ATerm t)
{
  ATprintf("test_do_2: %d, %s, %t\n", conn, s, t);	
}

/*}}}  */
/*{{{  void test_do_1(int conn, int i) */

void test_do_1(int conn, int i)
{
  ATprintf("test_do_1: %d, %d\n", conn, i);	
}

/*}}}  */
/*{{{  void test_do_0(int conn) */

void test_do_0(int conn)
{
  ATprintf("test_do_0: %d\n", conn);	
}

/*}}}  */

/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
  ATerm bottomOfStack;
  int conn;

  ATBinit(argc, argv, &bottomOfStack);
  conn = ATBconnect(NULL, NULL, -1, tool2_handler);

  ATBeventloop();

  return 0;
}

/*}}}  */
