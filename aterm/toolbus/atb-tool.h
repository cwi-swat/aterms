
#ifndef ATB_TOOL_H
#define ATB_TOOL_H

#include <aterm2.h>
#include <sys/times.h>
#include <sys/types.h>

/* ToolBus callback functions */
typedef ATermAppl (*ATBhandler)(int file_desc, ATermAppl input_term);
typedef ATermAppl (*ATBchecker)(int file_desc, ATermAppl signature);

/* ToolBus tool functions */
int    ATBinit(int argc, char *argv[], ATerm *stack_bottom);
int    ATBconnect(char *tool, char *host, int port, ATBhandler h, ATBchecker c);
void   ATBdisconnect(int file_desc);
int    ATBeventloop(void);

int    ATBsend(int file_desc, ATerm term);
ATerm  ATBreceive(int file_desc);

ATbool ATBpeekOne(int file_desc);
int    ATBpeekAny(void);
int    ATBhandleOne(int file_desc);
int    ATBhandleAny(void);

int    ATBgetDescriptors(fd_set *set);

#endif /* ATB_TOOL_H */
