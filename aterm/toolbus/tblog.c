/*{{{  includes */

#include <stdio.h>

#include "atb-tool.h"
#include "logger.tif.h"
#include "tblog_dict.h"

/*}}}  */

/*{{{  variables */

FILE *logfile = NULL;

/*}}}  */

/*{{{  void rec_terminate(int conn, ATerm arg) */

void rec_terminate(int conn, ATerm arg)
{
  ATfprintf(logfile, "\n]\n");
  fclose(logfile);
  exit(0);
}

/*}}}  */
/*{{{  void rec_monitor(int conn, ATerm monitor_event) */

void rec_monitor(int conn, ATerm monitor_event)
{
  static ATbool is_first = ATtrue;

  if (ATgetType(monitor_event) == AT_APPL) {
    ATermAppl appl = (ATermAppl)monitor_event;
    if (ATgetAFun(appl) == afun_logpoint) {
      ATerm trm_pid1, trm_atom_fun, trm_atom_args, trm_coords;
      ATerm trm_env, trm_subs, trm_notes, trm_pid2, trm_proc_expr;

      trm_pid1      = ATgetArgument(appl, 0);
      trm_atom_fun  = ATgetArgument(appl, 1);
      trm_atom_args = ATgetArgument(appl, 2);
      trm_coords    = ATgetArgument(appl, 3);
      trm_env       = ATgetArgument(appl, 4);
      trm_subs      = ATgetArgument(appl, 5);
      trm_notes     = ATgetArgument(appl, 6);
      trm_pid2      = ATgetArgument(appl, 7);
      trm_proc_expr = ATgetArgument(appl, 8);

      if (is_first) {
	is_first = ATfalse;
      } else {
	ATfprintf(logfile, ",\n");
      }

      ATfprintf(logfile, "%t(%,l)", trm_atom_fun, trm_atom_args);
      fflush(logfile);

      ATBwriteTerm(conn, (ATerm)ATmakeAppl1(afun_snd_continue, trm_pid1));
    }
  }
}

/*}}}  */
/*{{{  int main(int argc, char *argv[]) */

int main(int argc, char *argv[])
{
  int i;
  char *mode = "w";
  ATerm bottomOfStack;

  for (i=1; i<argc; i++) {
    if (strcmp(argv[i], "-append") == 0) {
      mode = "a";
    } else if (strcmp(argv[i], "-logfile") == 0) {
      i++;
      if (strcmp(argv[i], "-") == 0) {
	logfile = stdout;
      } else {
	logfile = fopen(argv[i], mode);
	if (logfile == NULL) {
	  perror(argv[i]);
	  exit(1);
	}
      }
    }
  }

  if (logfile == NULL) {
    logfile = stderr;
  }

  ATfprintf(logfile, "[\n");

  ATBinit(argc, argv, &bottomOfStack);

  init_tblog_dict();

  ATBconnect(NULL, NULL, -1, logger_handler);

  ATBeventloop();

  return 0;
}

/*}}}  */
