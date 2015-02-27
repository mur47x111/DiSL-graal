#ifndef _BUFFER_H
#define	_BUFFER_H

#include <stdlib.h>
#include <string.h>

#include "jvmtihelper.h"

// initial buffer size
static const size_t INIT_BUFF_SIZE = 512;

typedef struct {
	unsigned char * buff;
	size_t occupied;
	size_t capacity;
} buffer;

// ******************* Buffer routines *******************

void buffer_alloc(buffer * b) {

	b->buff = (unsigned char *) malloc(INIT_BUFF_SIZE);
	b->capacity = INIT_BUFF_SIZE;
	b->occupied = 0;
}

void buffer_free(buffer * b) {

	free(b->buff);
	b->buff = NULL;
	b->capacity = 0;
	b->occupied = 0;
}

void buffer_fill(buffer * b, const void * data, size_t data_length) {

	// not enough free space - extend buffer
	if(b->capacity - b->occupied < data_length) {

		unsigned char * old_buff = b->buff;

		// alloc as much as needed to be able to insert data
		size_t new_capacity = 2 * b->capacity;
		while(new_capacity - b->occupied < data_length) {
			new_capacity *= 2;
		}

		b->buff = (unsigned char *) malloc(new_capacity);
		b->capacity = new_capacity;

		memcpy(b->buff, old_buff, b->occupied);

		free(old_buff);
	}

	memcpy(b->buff + b->occupied, data, data_length);
	b->occupied += data_length;
}

// the space has to be already filled with data - no extensions
void buffer_fill_at_pos(buffer * b, size_t pos, const void * data,
		size_t data_length) {

	// space is not filled already - error
	if(b->occupied < pos + data_length) {
		check_error(TRUE, "Filling buffer at non-occupied position.");
	}

	memcpy(b->buff + pos, data, data_length);
}

void buffer_read(buffer * b, size_t pos, void * data, size_t data_length) {
	memcpy(data, b->buff + pos, data_length);
}

size_t buffer_filled(buffer * b) {
	return b->occupied;
}

void buffer_clean(buffer * b) {
	b->occupied = 0;
}

#endif	/* _BUFFER_H */
