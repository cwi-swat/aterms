
#ifndef ATB_TOOL_H
#define ATB_TOOL_H

#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <aterm1.h>

/* ToolBus callback functions */
typedef ATerm (*ATBhandler)(int file_desc, ATerm input_term);
typedef ATerm (*ATBchecker)(int file_desc, ATerm signature);

/* ToolBus tool functions */
int    ATBinit(int argc, char *argv[]);
int    ATBconnect(char *tool, char *host, int port, ATBhandler h, ATBchecker c);
void   ATBdisconnect(int file_desc);
int    ATBeventloop(void);

int    ATBsend(int file_desc, ATerm term);
ATerm  ATBreceive(int file_desc);

ATbool ATBpeekOne(int file_desc);
ATbool ATBpeekAny(void);
int    ATBhandleOne(int file_desc);
int    ATBhandleAny(void);

int    ATBgetDescriptors(fd_set *set);

#endif /* ATB_TOOL_H */
