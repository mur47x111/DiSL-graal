#ifndef _NETWORK_H_
#define _NETWORK_H_

#include "connection.h"


void network_init (const char * host_name, const char * port_number);
void network_fini ();

struct connection * network_acquire_connection ();
void network_release_connection (struct connection * connection);

#endif /* _NETWORK_H_ */
