#ifndef _BLOCKINGQUEUE_H
#define	_BLOCKINGQUEUE_H

#include <string.h>

#include <pthread.h>

#include <jvmti.h>
#include <jni.h>

#include "jvmtihelper.h"

typedef struct {

	// array of elements
	char * qarray;
	size_t qa_size;
	size_t qa_element_size;

	size_t first;
	size_t occupied;

	pthread_mutex_t mutex;
	pthread_cond_t cond;

	jvmtiEnv * jvmti;

} blocking_queue;

// ** Monitor helper functions **

static void _bq_monitor_enter(blocking_queue * bq) {

	pthread_mutex_lock(&(bq->mutex));
}

static void _bq_monitor_exit(blocking_queue * bq) {

	pthread_mutex_unlock(&(bq->mutex));
}

static void _bq_monitor_wait(blocking_queue * bq) {

	pthread_cond_wait(&(bq->cond), &(bq->mutex));
}

static void _bq_monitor_notify_all(blocking_queue * bq) {

	pthread_cond_broadcast(&(bq->cond));
}

// ** Blocking queue functions **

void bq_create(jvmtiEnv *jvmti, blocking_queue * bq, size_t queue_capacity,
		size_t queue_element_size) {

	check_std_error((bq == NULL), TRUE, "Invalid blocking queue structure");

	bq->qarray = malloc(queue_capacity * queue_element_size);
	bq->qa_size = queue_capacity;
	bq->qa_element_size = queue_element_size;
	bq->first = 0;
	bq->occupied = 0;

	// create lock and cond
	int pci = pthread_cond_init(&(bq->cond), NULL);
	check_std_error((pci != 0), TRUE, "Cannot create pthread condition");

	int pmi = pthread_mutex_init(&(bq->mutex), NULL);
	check_std_error((pmi != 0), TRUE, "Cannot create pthread mutex");

	bq->jvmti = jvmti;
}

void bq_term (blocking_queue * bq) {

	_bq_monitor_enter(bq); {

		// delete array
		free(bq->qarray);
		bq->qarray = NULL;

	}
	_bq_monitor_exit(bq);

	// destroy lock and cond
	pthread_mutex_destroy(&(bq->mutex));
	pthread_cond_destroy(&(bq->cond));
}

void bq_push(blocking_queue * bq, void * data) {

	_bq_monitor_enter(bq); {

		// wait for some empty space
		while(bq->occupied == bq->qa_size) {
			_bq_monitor_wait(bq);
		}

		// add data
		size_t last = (bq->first + bq->occupied) % bq->qa_size;
		size_t last_pos = last * bq->qa_element_size;
		memcpy(&((bq->qarray)[last_pos]), data, bq->qa_element_size);
		++(bq->occupied);

		// notify
		_bq_monitor_notify_all(bq);

	}
	_bq_monitor_exit(bq);
}

void bq_pop(blocking_queue * bq, void * empty) {

	_bq_monitor_enter(bq); {

		// wait for some item
		while(bq->occupied == 0) {
			_bq_monitor_wait(bq);
		}

		// get the data
		size_t first_pos = bq->first * bq->qa_element_size;
		memcpy(empty, &((bq->qarray)[first_pos]), bq->qa_element_size);
		// insert 0 - better problem detection
		memset(&((bq->qarray)[first_pos]), 0, bq->qa_element_size);
		bq->first = (bq->first + 1) % bq->qa_size;
		--(bq->occupied);

		// notify
		_bq_monitor_notify_all(bq);

	}
	_bq_monitor_exit(bq);
}

size_t bq_length(blocking_queue * bq) {

	size_t length = 0;

	_bq_monitor_enter(bq); {

		length = bq->occupied;
	}
	_bq_monitor_exit(bq);

	return length;
}

#endif	/* _BLOCKINGQUEUE_H */
