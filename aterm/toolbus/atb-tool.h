
#ifndef ATB_TOOL_H
#define ATB_TOOL_H

#include <aterm2.h>
#include <sys/times.h>
#include <sys/types.h>

/* ToolBus callback functions */
typedef ATerm (*ATBhandler)(int file_desc, ATerm input_term);
typedef ATerm (*ATBchecker)(int file_desc, ATerm signature);

/* ToolBus tool functions */
int    ATBinit(int argc, char *argv[], ATerm *stack_bottom);
int    ATBconnect(char *tool, char *host, int port, ATBhandler h, ATBchecker c);
void   ATBdisconnect(int file_desc);
int    ATBeventloop(void);

int    ATBwriteTerm(int file_desc, ATerm term);
ATerm  ATBreadTerm(int file_desc);

ATbool ATBpeekOne(int file_desc);
int    ATBpeekAny(void);
int    ATBhandleOne(int file_desc);
int    ATBhandleAny(void);

int    ATBgetDescriptors(fd_set *set);

/* Generic signature checker */
ATerm  ATBcheckSignature(ATerm signature, char *sigs[], int nrsigs);

#endif /* ATB_TOOL_H */
