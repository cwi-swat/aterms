
/*{{{  includes */

#include <assert.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/param.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include "atb-tool.h"
#include "_aterm.h"

/*}}}  */
/*{{{  defines */

#define streq(s,t)	(!(strcmp((s),(t))))

#define TB_HOST			"-TB_HOST"
#define TB_PORT			"-TB_PORT"
#define TB_TOOL_ID		"-TB_TOOL_ID"
#define TB_TOOL_NAME	"-TB_TOOL_NAME"

#define MAX_CONNECT_ATTEMPTS 1024

/*}}}  */
/*{{{  types */

typedef struct
{
	int         tid;			/* Tool id (assigned by ToolBus)             */
	int         fd;				/* Filedescriptor of ToolBus connection      */
    FILE       *stream;         /* The stream associated with fd             */
	int         port;			/* Well-known ToolBus port (in/out)          */
	char       *toolname;		/* Tool name (uniquely identifies interface) */
	char       *host;			/* ToolBus host                              */
	ATBhandler  handler;		/* Function that handles incoming terms      */
	ATBchecker  checker;		/* Function that checks the signature        */
	ATbool      verbose;		/* Should info be dumped on stderr           */
} Connection;

/*}}}  */
/*{{{  globals */

char atb_tool_id[] = "$Id$";

static char  this_host[MAXHOSTNAMELEN + 1] = "";
static char *default_host = this_host;

static char *default_toolname = NULL;

static int default_port = 8999;
static int default_tid  = -1;

static Connection *connections[FD_SETSIZE] = { NULL };

static Symbol symbol_rec_do = NULL;
static ATermAppl term_snd_void = NULL;

/* Static functions */
static int connect_to_socket(const char *host, int port);

/*}}}  */

/*{{{  int ATBinit(int argc, char *argv[], ATerm *stack_bottom) */

/**
 * Initialize the ToolBus layer.
 *
 * PRE: Underlying ATerm layer has been initialized.
 *
 */

int
ATBinit(int argc, char *argv[], ATerm *stack_bottom)
{
	int lcv;

	ATinit(argc, argv, stack_bottom);

	/* Parse commandline arguments, set up default values */
	for (lcv = 1; lcv < argc; lcv++)
	{
		if (streq(argv[lcv], TB_TOOL_NAME))
			default_toolname = argv[++lcv];
		else if (streq(argv[lcv], TB_HOST))
			default_host = argv[++lcv];
		else if (streq(argv[lcv], TB_PORT))
			default_port = atoi(argv[++lcv]);
		else if (streq(argv[lcv], TB_TOOL_ID))
			default_tid = atoi(argv[++lcv]);
	}

	/* Build some constants */
	symbol_rec_do = ATmakeSymbol("rec-do", 1, ATfalse);
	term_snd_void = (ATermAppl)ATparse("snd-void");
	ATprotectSymbol(symbol_rec_do);
	ATprotect((ATerm *)&term_snd_void);

	/* Get hostname of machine that runs this particular tool */
	return gethostname(this_host, MAXHOSTNAMELEN);
}

/*}}}  */
/*{{{  int ATBconnect(char *tool, char *host, int port, handler, checker) */

/**
	* Create a new ToolBus connection.
	*/

int
ATBconnect(char *tool, char *host, int port, ATBhandler h, ATBchecker c)
{
	Connection *connection = NULL;
	int fd;
	
	/* Make new connection */
	fd = connect_to_socket(host, port);

	assert(fd >= 0 && fd <= FD_SETSIZE);

	/* There cannot be another connection with this descriptor */
	if (connections[fd] != NULL)
		ATerror("ATBconnect: descriptor %d already in use.\n", fd);

	/* Allocate new connection */
	connection = (Connection *) malloc(sizeof(Connection));
	if (connection == NULL)
		ATerror("ATBconnect: no memory for new connection %d.\n", fd);

	connection->stream = fdopen(fd, "w");
	if(connection->stream == NULL)
	  ATerror("couldn't open stream for connection %d\n", fd);

	/* Initialize connection */
	connection->toolname = strdup(tool ? tool : default_toolname);
	if (connection->toolname == NULL)
		ATerror("ATBconnect: no memory for toolname.\n");

	connection->host = strdup(host ? host : default_host);
	if (connection->host == NULL)
		ATerror("ATBconnect: no memory for host.\n");

	connection->port     = port > 0 ? port : default_port;
	connection->handler  = h;
	connection->checker  = c;
	connection->verbose  = ATfalse;
	connection->tid      = default_tid;
	connection->fd       = fd;
	
	/* Link connection in array */
	connections[fd] = connection;

	return fd;
}

/*}}}  */
/*{{{  void ATBdisconnect(int fd) */

/**
	* Terminate a ToolBus connection
	*/

void
ATBdisconnect(int fd)
{
	/* Abort on illegal filedescriptors */
	assert(fd >= 0 && fd < FD_SETSIZE);

	/* Close the actual filedescriptor */
	fclose(connections[fd]->stream);

	/* If there was a connection-structure associated with this fd,
	 * then clean it up.
	 */
	if (connections[fd] != NULL)
	{
		if (connections[fd]->toolname)
			free(connections[fd]->toolname);
		if (connections[fd]->host)
			free(connections[fd]->host);

		free(connections[fd]);
		connections[fd] = NULL;
	}
}

/*}}}  */
/*{{{  int ATBeventloop(void) */

/**
	* Wait for terms coming from the ToolBus and dispatch them to 
	* the appropriate handlers.
	*/

int ATBeventloop(void)
{
	int fd;
	while(ATtrue) {
		fd = ATBhandleAny();
		if(fd < 0)
			return -1;
	}
}

/*}}}  */
/*{{{  int ATBsend(int fd, ATerm term) */

/**
	* Send a term to the ToolBus.
	*/

int ATBsend(int fd, ATerm term)
{
  int result, len = AT_calcTextSize(term);
  result = ATfprintf(connections[fd]->stream, "%-.7d:%t", len, term);
  fflush(connections[fd]->stream);
  return result;
}

/*}}}  */
/*{{{  ATerm  ATBreceive(int fd) */

/**
	* Receive a term from the ToolBus.
	*/

ATerm  ATBreceive(int fd)
{
  int len;
  ATerm t;

  if(fscanf(connections[fd]->stream, "%7d:", &len) != 1)
	ATerror("ATBreceive: error in lenspec.\n");
  t = ATreadFromTextFile(connections[fd]->stream);
  assert(AT_calcTextSize(t) == len); /* Very expensive! */
  return t;
}

/*}}}  */
/*{{{  ATbool ATBpeekOne(int fd) */

/**
	* Check if there is any input waiting on a speficic connection.
	*/

ATbool ATBpeekOne(int fd)
{
    fd_set set;
	int count = 0;
	struct timeval t;

	FD_ZERO(&set);
	FD_SET(connections[fd]->fd, &set);
	t.tv_sec = 0;
	t.tv_usec = 0;
	
	count = select(FD_SETSIZE, &set, NULL, NULL, &t);
	if(count)
	  return ATtrue;
	else
	  return ATfalse;   
}

/*}}}  */
/*{{{  int ATBpeekAny(void) */

/**
	* Check if there is input pending on any ToolBus connection.
	*/

int ATBpeekAny(void)
{
    fd_set set;
	static int last = -1;
	int max, cur, count = 0;
	struct timeval t;

	t.tv_sec = 0;
	t.tv_usec = 0;
	
	FD_ZERO(&set);
	max = ATBgetDescriptors(&set);
	
	count = select(max+1, &set, NULL, NULL, &t);
	if(count <= 0)
	  return -1;

	cur = last+1;
	while(cur != last) {
	  if(connections[cur] && FD_ISSET(connections[cur]->fd, &set)) {
		last = cur;
		return cur;
	  }
	  cur = (cur+1) % max;
	}
	return -1;
}

/*}}}  */
/*{{{  int ATBhandleOne(int fd) */

/**
	* Handle a single term from the ToolBus.
	*/

int ATBhandleOne(int fd)
{
	ATermAppl appl, result;
	ATbool recdo = ATfalse;

	appl = (ATermAppl)ATBreceive(fd);
	if(ATgetSymbol(appl) == symbol_rec_do)
	  recdo = ATtrue;

	result = connections[fd]->handler(fd, appl);
	
	if(result)
	  return ATBsend(fd, (ATerm)result);
	else if(recdo)
	  return ATBsend(fd, (ATerm)term_snd_void);

	return 0;
}

/*}}}  */
/*{{{  int ATBhandleAny(void) */

/**
	* Handle a single term coming from any ToolBus connection.
	*/

int ATBhandleAny(void)
{
    fd_set set;
	static int last = -1;
	int max, cur, count = 0;
	
	FD_ZERO(&set);
	max = ATBgetDescriptors(&set);
	
	count = select(max+1, &set, NULL, NULL, NULL);
	assert(count > 0);

	cur = last+1;
	while(cur != last) {
	  if(connections[cur] && FD_ISSET(connections[cur]->fd, &set)) {
		last = cur;
		return ATBhandleOne(cur);
	  }
	  cur = (cur+1) % max;
	}
	ATerror("ATBhandleAny: bottom\n");
	return -1;
}

/*}}}  */
/*{{{  int ATBgetDescriptors(fd_set *set) */

/**
	* Retrieve a set of descriptors for all connections.
	*/

int ATBgetDescriptors(fd_set *set)
{
    int lcv, max = -1;

	for(lcv=0; lcv<FD_SETSIZE; lcv++) {
	  if(connections[lcv] != NULL) {
		max = lcv;
		assert(connections[lcv]->fd == lcv);
		FD_SET(lcv, set);
	  }
	}
	return max;
}

/*}}}  */

/*{{{  static int connect_unix_socket(int port) */

/**
	* Connect to a AF_UNIX type socket.
	*/

static int connect_to_unix_socket(int port)
{
  int sock;
  char name[128];
  struct sockaddr_un usin;
  int attempt = 0;

  for(attempt=0; attempt<MAX_CONNECT_ATTEMPTS; attempt++) {
    sprintf (name, "/usr/tmp/%d", port);
    if((sock = socket(AF_UNIX,SOCK_STREAM,0)) < 0)
			ATerror("cannot open socket\n");

    /* Initialize the socket address to the server's address. */
    memset((char *) &usin, 0, sizeof(usin));
    usin.sun_family = AF_UNIX;
    strcpy (usin.sun_path, name);

    /* Connect to the server. */
    if(connect(sock, (struct sockaddr *) &usin,sizeof(usin)) < 0) {
      close(sock);
    } else {
      /* Connection established */
      /*chmod(name, 0777);*/
			setsockopt(sock, IPPROTO_TCP, TCP_NODELAY, (char *)&sock, sizeof sock);
      return sock;
    }
  }
  ATerror("cannot connect after %d attempts, giving up.\n", attempt);
  return -1;
}

/*}}}  */
/*{{{  static int connect_to_inet_socket(const char *host, int port) */

/**
	* Connect to a AF_INET type socket.
	*/

static int connect_to_inet_socket(const char *host, int port)
{
  int sock;
  struct sockaddr_in isin;
  struct hostent *hp;
  int attempt = 0;

  for(attempt=0; attempt<MAX_CONNECT_ATTEMPTS; attempt++) {
    if((sock = socket(AF_INET,SOCK_STREAM,0)) < 0)
			ATerror("cannot open socket\n");

    /* Initialize the socket address to the server's address. */
    memset((char *) &isin, 0, sizeof(isin));
    isin.sin_family = AF_INET;

    /* to get host address */
    hp = gethostbyname(host);
    if(hp == NULL)
			ATerror("cannot get hostname\n");

    memcpy (&(isin.sin_addr.s_addr), hp->h_addr, hp->h_length);
    isin.sin_port = htons(port);

    /* Connect to the server. */
    if(connect(sock, (struct sockaddr *)&isin, sizeof(isin)) < 0){
      close(sock);
    } else {
      setsockopt(sock, IPPROTO_TCP, TCP_NODELAY, (char *)&sock, sizeof(sock));
      return sock;
    }
  }
	ATerror("cannot connect after %d attempts, giving up.\n", attempt);
	return -1;
}

/*}}}  */
/*{{{  static int connect_to_socket (const char *host, int port) */

/**
	* Connect to a well-known ToolBus socket.
	*/

static int connect_to_socket (const char *host, int port)
{
	if(!host || streq(host, this_host))
    return connect_to_unix_socket(port);
  else
    return connect_to_inet_socket(host, port);
}

/*}}}  */

