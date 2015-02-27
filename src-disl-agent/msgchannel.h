#ifndef _MSGCHANNEL_H_
#define _MSGCHANNEL_H_

#include <jni.h>

#include "connection.h"


struct message {
	jint message_flags;
	jint control_size;
	jint classcode_size;
	const uint8_t * control;
	const uint8_t * classcode;
};


ssize_t message_send (struct connection * conn, struct message * msg);
void message_recv (struct connection * conn, struct message * msg);

#endif /* _MSGCHANNEL_H_ */
