#ifndef _CONNECTION_H_
#define _CONNECTION_H_

#ifdef MINGW
#include <winsock2.h>
#include <ws2tcpip.h>
#else
#include <sys/uio.h>
#include <sys/socket.h>
#include <netdb.h>
#endif

#include <sys/types.h>

#include "list.h"

struct connection {
	/** File descriptor of the connection socket. */
	int sockfd;

	/** Link in the connection pool list. */
	struct list cp_link;

#ifdef DEBUG
	/** Number of bytes sent over the connection. */
	uint64_t sent_bytes;

	/** Number of bytes received over the connection. */
	uint64_t recv_bytes;
#endif /* DEBUG */

};

struct connection * connection_open (struct addrinfo * addr);
void connection_close (struct connection * connection);

ssize_t connection_send (struct connection * connection, const void * buf, const ssize_t len);
ssize_t connection_recv (struct connection * connection, void * buf, const ssize_t len);

#ifndef MINGW
ssize_t connection_send_iov (struct connection * connection, struct iovec * iovs, int iov_count);
ssize_t connection_recv_iov (struct connection * connection, struct iovec * iovs, int iov_count);
#endif

#endif /* _CONNECTION_H_ */
