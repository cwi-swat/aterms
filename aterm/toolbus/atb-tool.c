
/* Includes */
#include <assert.h>
#include "atb-tool.h"

/* Defines */
#define streq(s,t)	(!(strcmp((s),(t))))

#define TB_HOST			"-TB_HOST"
#define TB_PORT			"-TB_PORT"
#define TB_TOOL_ID		"-TB_TOOL_ID"
#define TB_TOOL_NAME	"-TB_TOOL_NAME"

/* Types */
typedef struct
{
	int         tid;			/* Tool id (assigned by ToolBus)             */
	int         fd;				/* Filedescriptor of ToolBus connection      */
	int         port;			/* Well-known ToolBus port (in/out)          */
	char       *toolname;		/* Tool name (uniquely identifies interface) */
	char       *host;			/* ToolBus host                              */
	ATBhandler  handler;		/* Function that handles incoming terms      */
	ATBchecker  checker;		/* Function that checks the signature        */
	ATbool      verbose;		/* Should info be dumped on stderr           */
} Connection;

/* Globals */
static char atb_tool_id[] = "$Id$";

static char  this_host[MAXHOSTNAMELEN + 1] = "";
static char *default_host = this_host;

static char *default_toolname = NULL;

static int default_port = 8999;
static int default_tid  = -1;

static Connection *connections[FD_SETSIZE] = { NULL };

/* Static functions */
static int connect_to_socket(const char *host, int port);

/**
 * Initialize the ToolBus layer.
 *
 * PRE: Underlying ATerm layer has been initialized.
 *
 */

int
ATBinit(int argc, char *argv[])
{
	int lcv;

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

	/* Get hostname of machine that runs this particular tool */
	return gethostname(this_host, MAXHOSTNAMELEN);
}

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

void
ATBdisconnect(int fd)
{
	/* Abort on illegal filedescriptors */
	assert(fd >= 0 && fd < FD_SETSIZE);

	/* Close the actual filedescriptor */
	close(fd);

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

int    ATBeventloop(void)
{
	/* Not yet implemented. */
}


int    ATBsend(int fd, ATerm term)
{
	/* Not yet implemented. */
}

ATerm  ATBreceive(int fd)
{
	/* Not yet implemented. */
}


ATbool ATBpeekOne(int fd)
{
	/* Not yet implemented. */
}

ATbool ATBpeekAny(void)
{
	/* Not yet implemented. */
}

int    ATBhandleOne(int fd)
{
	/* Not yet implemented. */
}

int    ATBhandleAny(void)
{
	/* Not yet implemented. */
}


int    ATBgetDescriptors(fd_set *set)
{
	/* Not yet implemented. */
}

