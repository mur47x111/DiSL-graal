#define _POSIX_C_SOURCE 200908L

#include <assert.h>
#include <stdlib.h>
#include <unistd.h>

#ifdef MINGW

#include <winsock2.h>
#define setsockopt(a,b,c,d,e) setsockopt(a,b,c,(const void*)(d),e)

#else

#include <sys/uio.h>
#include <sys/socket.h>
#include <netinet/tcp.h>
#include <netdb.h>

#endif

#include <sys/types.h>

#include "common.h"
#include "connection.h"


static void
__connection_init (struct connection * connection, const int sockfd) {
	connection->sockfd = sockfd;
	list_init (&connection->cp_link);

#ifdef DEBUG
	connection->sent_bytes = 0;
	connection->recv_bytes = 0;
#endif
}


/**
 * Opens a new connection to the given remote address and
 * returns a new connection structure.
 */
struct connection *
connection_open (struct addrinfo * addr) {
	//
	// Create a stream socket to the given address and connect to the server.
	// Upon connection, disable the Nagle algorithm to avoid delays on the
	// sender side and create a wrapper object for the connection.
	//
	int sockfd = socket(addr->ai_family, SOCK_STREAM, 0);
	check_std_error (sockfd < 0, "failed to create socket");

	int connect_result = connect(sockfd, addr->ai_addr, addr->ai_addrlen);
	check_std_error (connect_result < 0, "failed to connect to server");

	int tcp_nodelay = 1;
	int sso_result = setsockopt (
		sockfd, IPPROTO_TCP, TCP_NODELAY,
		&tcp_nodelay, sizeof (tcp_nodelay)
	);
	check_std_error (sso_result < 0, "failed to enable TCP_NODELAY");

	//

	struct connection * connection =
		(struct connection *) malloc (sizeof (struct connection));
	check_error (connection == NULL, "failed to allocate connection structure");

	__connection_init (connection, sockfd);
	return connection;
}


/**
 * Closes the connection and destroys the connection structure.
 */
void
connection_close (struct connection * connection) {
	assert (connection != NULL);

	dprintf (
		"socket %d: sent bytes %" PRIu64 ", recv bytes %" PRIu64 "\n",
		connection->sockfd, connection->sent_bytes, connection->recv_bytes
	);

	close (connection->sockfd);
	free (connection);
}

//

typedef ssize_t (* xfer_fn) (int sockfd, void * buf, size_t len, int flags);

inline static ssize_t
__socket_xfer (xfer_fn xfer, const int sockfd, const void * buf, const ssize_t len) {
	unsigned char * buf_tail = (unsigned char *) buf;
	size_t remaining = len;

	while (remaining > 0) {
		ssize_t xferred = xfer (sockfd, buf_tail, remaining, 0);
		if (xferred < 0) {
			return -remaining;
		}

		remaining -= xferred;
		buf_tail += xferred;
	}

	return len;
}


/**
 * Sends data into the given connection. Does not return until all provided
 * data has been sent. Returns the number of bytes sent.
 */
ssize_t
connection_send (struct connection * connection, const void * buf, const ssize_t len) {
	assert (connection != NULL);
	assert (buf != NULL);
	assert (len >= 0);

	ssize_t sent = __socket_xfer ((xfer_fn) send, connection->sockfd, buf, len);
	check_std_error (sent < 0, "error sending data to server");

#ifdef DEBUG
	connection->sent_bytes += sent;
#endif

	return sent;
}


/**
 * Receives a predefined amount of data from the given connection. Does not return
 * until all requested data has been received. Returns the number of bytes received.
 */
ssize_t
connection_recv (struct connection * connection, void * buf, const ssize_t len) {
	assert (connection != NULL);
	assert (buf != NULL);
	assert (len >= 0);

	ssize_t received = __socket_xfer ((xfer_fn) recv, connection->sockfd, buf, len);
	check_std_error (received < 0, "error receiving data from server");

#ifdef DEBUG
	connection->recv_bytes += received;
#endif

	return received;
}

//

#ifndef MINGW

typedef ssize_t (* xfer_iov_fn) (int sockfd, struct iovec * iovs, int iov_count);

inline static ssize_t
__socket_xfer_iov (
	xfer_iov_fn xfer_iov, const int sockfd, struct iovec * iovs, const int iov_count
) {
	ssize_t total = 0;

	int iov_index = 0;
	while (iov_index < iov_count) {
		//
		// Transfer as much of the remaining data as possible.
		// Return if an error occurred.
		//
		ssize_t res = xfer_iov (sockfd, iovs + iov_index, iov_count - iov_index);
		if (res >= 0) {
			size_t count = res;

			//
			// Remove bytes transferred from the beginning of the
			// chunk of data held in the IO vectors. If there is
			// data remaining in a vector, adjust its base and
			// length, and resume transfer.
			//
			total += count;
			while (iov_index < iov_count && count >= iovs [iov_index].iov_len) {
				count -= iovs [iov_index].iov_len;
				iov_index++;
			}

			if (count != 0) {
				iovs [iov_index].iov_base += count;
				iovs [iov_index].iov_len -= count;
			}
		} else {
			return res;
		}
	}

	return total;
}


/**
 * Sends vectored data into the given connection. Does not return until all
 * data have been sent. Returns the number of bytes sent.
 */
ssize_t
connection_send_iov (struct connection * connection, struct iovec * iovs, int iov_count) {
	assert (connection != NULL);
	assert (iovs != NULL);
	assert (iov_count >= 0);

	ssize_t sent = __socket_xfer_iov ((xfer_iov_fn) writev, connection->sockfd, iovs, iov_count);
	check_std_error (sent < 0, "error sending data to server");

#ifdef DEBUG
	connection->sent_bytes += sent;
#endif

	return sent;
}


/**
 * Receives vectored data from the given connection. Does not return until
 * all requested data have been received. Returns the number of bytes received.
 */
ssize_t
connection_recv_iov (struct connection * connection, struct iovec * iovs, int iov_count) {
	assert (connection != NULL);
	assert (iovs != NULL);
	assert (iov_count >= 0);

	ssize_t received = __socket_xfer_iov ((xfer_iov_fn) readv, connection->sockfd, iovs, iov_count);
	check_std_error (received < 0, "error receiving data from server");

#ifdef DEBUG
	connection->recv_bytes += received;
#endif

	return received;
}

#endif /* !MINGW */
