#ifndef _CONNPOOL_H_
#define _CONNPOOL_H_

#include <sys/types.h>

#ifdef MINGW
#include <winsock2.h>
#include <ws2tcpip.h>
#else
#include <sys/socket.h>
#include <netdb.h>
#endif

#include "list.h"
#include "connection.h"


/**
 * Callback function type for events on the connection pool.
 */
typedef void (* connection_hook_fn) (struct connection * connection);


struct connection_pool {
	int connections_count;
	struct list free_connections;
	struct list busy_connections;

	struct addrinfo * endpoint;
	connection_hook_fn after_open_hook;
	connection_hook_fn before_close_hook;
};


void connection_pool_init (struct connection_pool * cp, struct addrinfo * endpoint);
void connection_pool_close (struct connection_pool * cp);

void connection_pool_set_after_open_hook (struct connection_pool * cp, connection_hook_fn after_open_fn);
void connection_pool_set_before_close_hook (struct connection_pool * cp, connection_hook_fn before_close_fn);

struct connection * connection_pool_get_connection (struct connection_pool * cp);
void connection_pool_put_connection (struct connection_pool * cp, struct connection * connection);

#endif /* _CONNPOOL_H_ */
