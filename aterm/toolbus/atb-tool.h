
#ifndef ATB_TOOL_H
#define ATB_TOOL_H

#include <sys/times.h>
#include <sys/types.h>
#include <string.h>
#include <aterm1.h>

/* ToolBus callback functions */
typedef ATerm (*ATBhandler)(int file_desc, ATerm input_term);

/* ToolBus tool functions */
int    ATBinit(int argc, char *argv[], ATerm *stack_bottom);
int    ATBconnect(char *tool, char *host, int port, ATBhandler h);
void   ATBdisconnect(int file_desc);
int    ATBeventloop(void);

ATerm  ATBreadTerm(int file_desc);
int    ATBwriteTerm(int file_desc, ATerm term);
void   ATBpostEvent(int file_desc, ATerm event);

ATbool ATBpeekOne(int file_desc);
int    ATBpeekAny(void);
int    ATBhandleOne(int file_desc);
int    ATBhandleAny(void);

int    ATBgetDescriptors(fd_set *set);

/* Generic signature checker */
ATerm ATBcheckSignature(ATerm signature, char *sigs[], int nrsigs);

#ifndef streq
#  define streq(s,t)	(!(strcmp(s,t)))
#endif

#ifndef MIN
#  define MIN(a,b)	((a)<(b) ? (a) : (b))
#endif

#ifndef MAX
#  define MAX(a,b)	((a)>(b) ? (a) : (b))
#endif

#endif /* ATB_TOOL_H */
