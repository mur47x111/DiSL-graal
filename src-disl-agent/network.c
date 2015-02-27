#include <assert.h>

#include <sys/types.h>


#ifdef MINGW

#include <winsock2.h>
#include <ws2tcpip.h>

#ifndef AI_NUMERICSERV
#define AI_NUMERICSERV 0x00000008
#endif

#else /* !MINGW */

#include <sys/socket.h>
#include <netdb.h>

#endif /* !MINGW */


#include "common.h"
#include "threads.h"
#include "connpool.h"
#include "connection.h"
#include "msgchannel.h"


/**
 * Address info for the DiSL server.
 */
static struct addrinfo * disl_addrinfo;

/**
 * Pool of connections to DiSL server.
 */
static struct connection_pool disl_connections;

/**
 * Mutex to protect the manipulation with the connection pool.
 */
static mutex_t disl_connections_mutex;


static void
__connection_close_hook (struct connection * conn) {
	//
	// Send an empty message to the server before closing the connection
	// to indicate end of processing for that connection.
	//
	struct message shutdown = {
		.message_flags = 0,
		.control_size = 0, .classcode_size = 0,
		.control = NULL, .classcode = NULL
	};

	message_send (conn, &shutdown);
}


/**
 * Initializes the address info, the pool of connections to the remote
 * instrumentation server, and a mutex guarding the pool.
 */
void
network_init (const char * host_name, const char * port_number) {
	assert (host_name != NULL);
	assert (port_number != NULL);

	//

	mutex_init (&disl_connections_mutex);

	struct addrinfo hints;
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_NUMERICSERV | AI_CANONNAME;
	hints.ai_protocol = 0;

	int gai_result = getaddrinfo (host_name, port_number, &hints, &disl_addrinfo);
	check_error (gai_result != 0, gai_strerror (gai_result));

	connection_pool_init (&disl_connections, disl_addrinfo);
	connection_pool_set_before_close_hook (&disl_connections, __connection_close_hook);
}


/**
 * Closes all connections in the connection pool, releases the address
 * info, and destroys the mutex guarding the pool.
 */
void
network_fini () {
	//
	// Shut down and close all connections to the server. This has to run
	// under lock, because the connection pool will manipulate the list of
	// connections.
	//
	mutex_lock (&disl_connections_mutex);
	{
		connection_pool_close (&disl_connections);
	}
	mutex_unlock (&disl_connections_mutex);

	freeaddrinfo (disl_addrinfo);
	disl_addrinfo = NULL;

	mutex_destroy (&disl_connections_mutex);
}


struct connection *
network_acquire_connection () {
	dlprintf ("acquiring connection ... ");

	//
	// The connection pool must be protected by a lock so that multiple threads
	// acquiring a connection do not corrupt the internal state of the pool.
	//
	struct connection * connection;
	mutex_lock (&disl_connections_mutex);
	{
		connection = connection_pool_get_connection (&disl_connections);
	}
	mutex_unlock (&disl_connections_mutex);

	//

	dprintf ("done\n");
	return connection;
}


/**
 * Returns the connection to the pool of available connections.
 */
void
network_release_connection (struct connection * connection) {
	assert (connection != NULL);
	dlprintf ("releasing connection ... ");

	//
	// The connection pool must be protected by a lock so that multiple threads
	// releasing a connection do not corrupt the internal state of the pool.
	//
	mutex_lock (&disl_connections_mutex);
	{
		connection_pool_put_connection (&disl_connections, connection);
	}
	mutex_unlock (&disl_connections_mutex);

	//

	dprintf ("done\n");
}
