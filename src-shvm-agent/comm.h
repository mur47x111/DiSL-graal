#ifndef _COMM_H
#define	_COMM_H

#include <sys/types.h>
#include <sys/socket.h>

#include "jvmtihelper.h"

// sends data over network
void send_data(int sockfd, const void * data, int data_len) {

	int sent = 0;

	while (sent != data_len) {

		int res = send(sockfd, ((unsigned char *)data) + sent,
				(data_len - sent), 0);
		check_std_error(res, -1, "Error while sending data to server");
		sent += res;
	}
}

// receives data from network
void rcv_data(int sockfd, void * data, int data_len) {

	int received = 0;

	while (received != data_len) {

		int res = recv(sockfd, ((unsigned char *)data) + received,
				(data_len - received), 0);
		check_std_error(res, -1, "Error while receiving data from server");

		received += res;
	}
}

#endif	/* _COMM_H */
