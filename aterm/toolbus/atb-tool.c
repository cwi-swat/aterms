/*

    ATerm -- The ATerm (Annotated Term) library
    Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
                             The  Netherlands.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA

*/

/*{{{  includes */

#include <assert.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <sys/un.h>
#include <sys/param.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include "atb-tool.h"
#include "_aterm.h"

/*}}}  */
/*{{{  defines */

#define TB_HOST			 "-TB_HOST"
#define TB_PORT			 "-TB_PORT"
#define TB_TOOL_ID		 "-TB_TOOL_ID"
#define TB_TOOL_NAME	 "-TB_TOOL_NAME"
#define TB_HANDSHAKE_LEN 512

#define MIN_MSG_SIZE          128
#define MAX_CONNECT_ATTEMPTS 1024
#define INITIAL_BUFFER_SIZE  1024

#define MAX_NR_QUEUES	64
#define MAX_QUEUE_LEN	128

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
  ATbool      verbose;		/* Should info be dumped on stderr           */
} Connection;

typedef struct
{
  AFun afun;					/* afun -1 => slot is empty            */
  ATbool ack_pending;			/* ack of event is still pending       */
  int first;					/* First element in the (cyclic) queue */
  int last;					/* Last element in the (cyclic) queue  */
  ATerm data[MAX_QUEUE_LEN];	/* Actual queue elements               */
} EventQueue;

/*}}}  */
/*{{{  globals */

char atb_tool_id[] = "$Id$";

static char  this_host[MAXHOSTNAMELEN + 1] = "";
static char *default_host = this_host;

static char *default_toolname = NULL;

static int default_port = 8999;
static int default_tid  = -1;

static Connection *connections[FD_SETSIZE] = { NULL };

static AFun symbol_rec_do    = (AFun) NULL;
static AFun symbol_ack_event = (AFun) NULL;
static AFun symbol_baf       = (AFun) NULL;  
static ATerm term_snd_void = NULL;

/* term buffer */
static int buffer_size = 0;
static char *buffer = NULL;

/* Event Queues */
static EventQueue event_queues[MAX_NR_QUEUES];
static int nr_event_queues = 0;

/* Static functions */
static int connect_to_socket(const char *host, int port);
static void resize_buffer(int size);
static int mwrite(int fd, char *buf, int len);
static int mread(int fd, char *buf, int len);
static void handshake(Connection *connection);
static ATerm ATBunpack(ATerm t);

/*}}}  */

/*{{{  int ATBinitialize(int argc, char *argv[]) */

int ATBinitialize(int argc, char *argv[])
{
  ATerm bottom;

  return ATBinit(argc, argv, &bottom);
}

/*}}}  */
/*{{{  int ATBinit(int argc, char *argv[], ATerm *stack_bottom) */

/**
 * Initialize the ToolBus layer.
 *
 */

int
ATBinit(int argc, char *argv[], ATerm *stack_bottom)
{
  int lcv;
  
  for (lcv = 1; lcv < argc; lcv++) {
    if (streq(argv[lcv], "-at-help")) {
      fprintf(stderr, "    %-20s: specify toolbus tool name\n", TB_TOOL_NAME);
      fprintf(stderr, "    %-20s: specify toolbus host\n", TB_HOST);
      fprintf(stderr, "    %-20s: specify toolbus port\n", TB_PORT);
      fprintf(stderr, "    %-20s: specify toolbus tool id\n", TB_TOOL_ID);
    }
  }

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
  ATprotectSymbol(symbol_rec_do);
  term_snd_void = ATparse("snd-void");
  ATprotect(&term_snd_void);
  symbol_ack_event = ATmakeSymbol("rec-ack-event", 1, ATfalse);
  ATprotectSymbol(symbol_ack_event);

  symbol_baf = ATmakeSymbol("_baf-encoded_", 1, ATtrue);

  /* Allocate initial buffer */
  buffer = (char *)malloc(INITIAL_BUFFER_SIZE);
  if(!buffer)
    ATerror("cannot allocate initial term buffer of size %d\n", 
	    INITIAL_BUFFER_SIZE);
  buffer_size = INITIAL_BUFFER_SIZE;
  
  /* Initialize event_queues. */
  for (lcv=0; lcv<MAX_NR_QUEUES; lcv++)
    event_queues[lcv].afun = -1;
  
  /* Get hostname of machine that runs this particular tool */
  return gethostname(this_host, MAXHOSTNAMELEN);
}

/*}}}  */
/*{{{  int ATBconnect(char *tool, char *host, int port, handler) */

/**
	* Create a new ToolBus connection.
	*/

int
ATBconnect(char *tool, char *host, int port, ATBhandler h)
{
  Connection *connection = NULL;
  int fd;

  int tid = port < 0 ? default_tid : -1;
  port = port >= 0 ? port : default_port;

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

  connection->port     = port;
  connection->handler  = h;
  connection->verbose  = ATfalse;
  connection->tid      = tid;
  connection->fd       = fd;
	
  /* Link connection in array */
  connections[fd] = connection;

  /* Perform the ToolBus handshake */
  handshake(connection);

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

/*{{{  void ATBpostEvent(int fd, ATerm event) */

void ATBpostEvent(int fd, ATerm event)
{
  int free_index, i;
  AFun afun;

  if (ATgetType(event) != AT_APPL)
    ATabort("Illegal eventtype (should be appl): %t\n", event);
	
  afun = ATgetAFun((ATermAppl)event);
	
  for(i=0, free_index=-1; i<MAX_NR_QUEUES; i++) {
    if(event_queues[i].afun == afun)
      break;
    if(event_queues[i].afun == -1)
      free_index = i;
  }
	
  if (i >= MAX_NR_QUEUES) {
    if (free_index == -1)
      ATerror("Maximum number of eventqueues exceeded.\n");
    i = free_index;		/* occupy free slot */
    event_queues[i].afun  = afun;
    ATprotectAFun(afun);
    event_queues[i].first = 0;
    event_queues[i].last  = 0;
    memset(event_queues[i].data, sizeof(event_queues[i].data), 0);
    ATprotectArray(event_queues[i].data, MAX_QUEUE_LEN);
    event_queues[i].ack_pending = ATfalse;
    nr_event_queues++;
  }
	
  if (event_queues[i].ack_pending == ATfalse) {
    ATBwriteTerm(fd, ATmake("snd-event(<term>)", event));
    event_queues[i].ack_pending = ATtrue;
  } else {
    if( (event_queues[i].last + 1) % MAX_QUEUE_LEN == event_queues[i].first)
      ATerror("Maximum number of events in queue %y exceeded.\n", afun);

    event_queues[i].data[event_queues[i].last] = event;
    event_queues[i].last = (event_queues[i].last + 1) % MAX_QUEUE_LEN;
  }
}

/*}}}  */
/*{{{  static void handle_ack_event(int fd, AFun afun) */

static void handle_ack_event(int fd, AFun afun)
{
  int i;
	
  for(i=MAX_NR_QUEUES-1; i>=0; i--) {
    if(event_queues[i].afun == afun) {
      ATfprintf(stderr, "found queue: %y\n", afun);
      if (event_queues[i].first != event_queues[i].last) {
	ATerm event = event_queues[i].data[event_queues[i].first];
	ATfprintf(stderr, "reviving queued event: %t\n", event);
	event_queues[i].first =
	  (event_queues[i].first + 1) % MAX_QUEUE_LEN;
	ATBwriteTerm(fd, ATmake("snd-event(<term>)", event));
				/* stil pending */
      } else {
	ATfprintf(stderr, "queue empty.\n");
	event_queues[i].ack_pending = ATfalse;
      }
    }
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
/*{{{  int ATBwriteTerm(int fd, ATerm term) */

/**
 * Send a term to the ToolBus.
 */

int ATBwriteTerm(int fd, ATerm term)
{
  int len, wirelen;

  len = AT_calcTextSize(term)+8;	  /* Add lenspec */
  wirelen = MAX(len, MIN_MSG_SIZE);
  resize_buffer(wirelen+1);               /* Add '\0' character */
  sprintf(buffer, "%-.7d:", len);
  AT_writeToStringBuffer(term, buffer+8);
  if(mwrite(fd, buffer, wirelen) < 0)
    ATerror("ATBwriteTerm: connection with ToolBus lost.\n");
  return 0;
}

/*}}}  */
/*{{{  ATerm  ATBreadTerm(int fd) */

/**
 * Receive a term from the ToolBus.
 */

ATerm  ATBreadTerm(int fd)
{
  int len;
  ATerm t;

  /* Read the first batch */
  if(mread(fd, buffer, MIN_MSG_SIZE) <= 0) 
    ATerror("ATBreadTerm: connection with ToolBus lost.\n");
  
  /* Retrieve the data length */
  if(sscanf(buffer, "%7d:", &len) != 1)
    ATerror("ATBreadTerm: error in lenspec: %s\n", buffer);
  
  /* Make sure the buffer is large enough */
  resize_buffer(len+1);

  if(len > MIN_MSG_SIZE) {
    /* Read the rest of the data */
    if(mread(fd, buffer+MIN_MSG_SIZE, len-MIN_MSG_SIZE) < 0)
      ATerror("ATBreadTerm: connection with ToolBus lost.\n");
  }
  buffer[len] = '\0';
	

  /* Parse the string */
  t = ATparse(buffer+8);
  assert(t);

  t = ATBunpack(t);

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
  ATermAppl appl;
  ATerm result;

  appl = (ATermAppl)ATBreadTerm(fd);

  if(appl == NULL)
    return -1;
	
  result = connections[fd]->handler(fd, (ATerm)appl);

  if(result)
    return ATBwriteTerm(fd, result);
  else if(ATgetSymbol(appl) == symbol_rec_do)
    return ATBwriteTerm(fd, term_snd_void);
  else if(ATgetSymbol(appl) == symbol_ack_event)
    handle_ack_event(fd, ATgetAFun((ATermAppl)ATgetArgument(appl, 0)));

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
  int start, max, cur, count = 0;
	
  FD_ZERO(&set);
  max = ATBgetDescriptors(&set) + 1;

  count = select(max, &set, NULL, NULL, NULL);
  assert(count > 0);


  start = last+1;	
  cur = start;
  do {
    if(connections[cur] && FD_ISSET(cur, &set)) {
      last = cur;
      if(ATBhandleOne(cur) < 0)
	return -1;
      return cur;
    }
    cur = (cur+1) % max;
  } while(cur != start);

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

/*{{{  ATermList ATBcheckSignature(ATerm signature, char *sigs[], int nrsigs) */

/**
	* Check a signature.
	*/

ATerm ATBcheckSignature(ATerm signature, char *sigs[], int nrsigs)
{
  ATermList list = (ATermList)signature;
  ATermList errors = ATempty;
  int i;

  while(!ATisEmpty(list)) {
    ATerm entry = ATgetFirst(list);
    list = ATgetNext(list);

    for(i=0; i<nrsigs; i++) {
      if(ATisEqual(ATparse(sigs[i]), entry))
	break;
    }

    if(i == nrsigs)
      errors = ATinsert(errors, entry);				
  }

  return (ATerm)errors;
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

  sprintf (name, "/var/tmp/%d", port);
  for(attempt=0; attempt<MAX_CONNECT_ATTEMPTS; attempt++) {
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
  ATerror("connect_to_unix_socket: cannot connect to unix socket %s "
	  "after %d attempts, giving up.\n", name, attempt);
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
  ATerror("connect_to_inet_socket: cannot connect after %d attempts, "
	  "giving up.\n", attempt);
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
/*{{{  static void resize_buffer(int size) */

/**
	* Make the term buffer big enough.
	*/

static void resize_buffer(int size)
{
  if(size > buffer_size) {
    buffer = realloc(buffer, size);
    if(buffer == NULL)
      ATerror("resize_buffer: cannot allocate buffer of size %d\n", size);
    buffer_size = size;
  }
}

/*}}}  */

/*{{{  static int mwrite(int fd, char *buf, int len) */

/**
	* Write a buffer to a file descriptor. 
	* Make sure all data has been written.
	*/

static int mwrite(int fd, char *buf, int len)
{
  int cnt = 0, n;

  while(cnt < len) {
    if((n = write(fd, &buf[cnt], len-cnt)) <= 0) {
      if(errno != EINTR)
        return n;
    } else
      cnt += n;
  }
  assert(cnt == len);
  return cnt;
}

/*}}}  */
/*{{{  static int mread(int fd, char *buf, int len) */

/**
	* Read len bytes from fd in buf.
	*/

static int mread(int fd, char *buf, int len)
{
  int cnt = 0, n;

  while(cnt < len){
    if((n = read(fd, &buf[cnt], len - cnt)) <= 0) {
      if(errno != EINTR)
        return n;
    } else
      cnt += n;
  }
  assert(cnt == len);
  return cnt;
}

/*}}}  */

/*{{{  static void handshake(Connection *connection) */

/**
	* Execute the ToolBus handshake protocol.
	*/

static void handshake(Connection *conn)
{
  char buf[TB_HANDSHAKE_LEN];
  char remote_toolname[TB_HANDSHAKE_LEN];
  int  remote_tid;

  sprintf(buf, "%s %s %d", conn->toolname, conn->host, conn->tid);
  if(mwrite(conn->fd, buf, TB_HANDSHAKE_LEN) < 0)
    ATerror("handshake: mwrite failed.\n");

  if(mread(conn->fd, buf, TB_HANDSHAKE_LEN) < 0)
    ATerror("handshake: cannot get tool-id!\n");

  if(sscanf(buf, "%s %d", remote_toolname, &remote_tid) != 2)
    ATerror("handshake: protocol error, illegal tid spec: %s\n", buf);

  if(!streq(remote_toolname, conn->toolname))
    ATerror("handshake: protocol error, wrong toolname %s != %s\n", 
	    remote_toolname, conn->toolname);
	
  if(remote_tid < 0 || (conn->tid >= 0 && remote_tid != conn->tid))
    ATerror("handshake: illegal tid assigned by ToolBus (%d != %d)\n",
	    remote_tid, conn->tid);
}

/*}}}  */

/*{{{  ATerm ATBpack(ATerm t) */

ATerm ATBpack(ATerm t)
{
  int len;
  char *data;
  ATermBlob blob;
  
  data = ATwriteToBinaryString(t, &len);
  blob = ATmakeBlob(len, data);
  
  return (ATerm)ATmakeAppl(symbol_baf, blob); 
}

/*}}}  */
/*{{{  static ATerm ATBunpack(ATerm t) */

static ATerm ATBunpack(ATerm t)
{
  int i;

  switch (ATgetType(t)) {
  case AT_INT:
  case AT_REAL:
  case AT_BLOB:
    return t;

  case AT_PLACEHOLDER:
    {
      ATerm type = ATgetPlaceholder((ATermPlaceholder)t);
      ATerm unpacked_type = ATBunpack(type);
      if (ATisEqual(unpacked_type, type)) {
	return t;
      }
      return (ATerm)ATmakePlaceholder(unpacked_type);
    }

  case AT_APPL:
    {
      ATermAppl appl = (ATermAppl)t;
      AFun fun = ATgetAFun(appl);
      if (fun == symbol_baf) {
	ATerm unpacked_term, arg;
	ATermBlob blob;
	char *data;
	int size;

	arg = ATgetArgument(appl, 0);
	assert(ATgetType(arg) == AT_BLOB);
	blob = (ATermBlob)arg;
	data = ATgetBlobData(blob);
	size = ATgetBlobSize(blob);
	unpacked_term = ATreadFromBinaryString(data, size);

	return unpacked_term;
      } else {
	ATermList unpacked_args = ATempty;

	for (i=ATgetArity(fun)-1; i>=0; i--) {
	  unpacked_args = ATinsert(unpacked_args, ATBunpack(ATgetArgument(appl, i)));
	}

	return (ATerm)ATmakeApplList(fun, unpacked_args);
      }
    }

  case AT_LIST:
    {
      ATermList list = (ATermList)t;
      ATermList unpacked_list = ATempty;

      while (!ATisEmpty(list)) {
	unpacked_list = ATinsert(unpacked_list, ATBunpack(ATgetFirst(list)));
	list = ATgetNext(list);
      }
      
      return (ATerm)ATreverse(list);
    }

  default:
    abort();
  }

  return NULL;

}

/*}}}  */
