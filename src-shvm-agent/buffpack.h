#ifndef _BUFFPACK_H
#define	_BUFFPACK_H

#if defined (__APPLE__) && defined (__MACH__)

#include <machine/endian.h>

#if BYTE_ORDER == BIG_ENDIAN
#define htobe64(x) (x)
#else // BYTE_ORDER != BIG_ENDIAN
#define htobe64(x) __DARWIN_OSSwapInt64((x))
#endif

#else // !(__APPLE__ && __MACH__)
#include <endian.h>
#endif

// Disabled check to make it compile under OS X with clang/LLVM.
//#ifndef __STDC_IEC_559__
//#error "Requires IEEE 754 floating point!"
//#endif

#include "jvmtihelper.h"

#include "buffer.h"

// interpret bytes differently
union float_jint {

	float f;
	jint i;
};

// interpret bytes differently
union double_jlong {

	double d;
	jlong l;
};

void pack_boolean(buffer * buff, jboolean to_send) {
	buffer_fill(buff, &to_send, sizeof(jboolean));
}

void pack_byte(buffer * buff, jbyte to_send) {
	buffer_fill(buff, &to_send, sizeof(jbyte));
}

void pack_char(buffer * buff, jchar to_send) {
	jchar nts = htons(to_send);
	buffer_fill(buff, &nts, sizeof(jchar));
}

void pack_short(buffer * buff, jshort to_send) {
	jshort nts = htons(to_send);
	buffer_fill(buff, &nts, sizeof(jshort));
}

void pack_int(buffer * buff, jint to_send) {
	jint nts = htonl(to_send);
	buffer_fill(buff, &nts, sizeof(jint));
}

void pack_long(buffer * buff, jlong to_send) {
	jlong nts = htobe64(to_send);
	buffer_fill(buff, &nts, sizeof(jlong));
}

void pack_float(buffer * buff, jfloat to_send) {
	// macro ensures that the formating of the float is correct
	// so make "int" from it and send it
	union float_jint convert;
	convert.f = to_send;
	pack_int(buff, convert.i);
}

void pack_double(buffer * buff, jdouble to_send) {
	// macro ensures that the formating of the double is correct
	// so make "long" from it and send it
	union double_jlong convert;
	convert.d = to_send;
	pack_long(buff, convert.l);
}

void pack_string_utf8(buffer * buff, const void * string_utf8,
		uint16_t size_in_bytes) {

	// send length first
	uint16_t nsize = htons(size_in_bytes);
	buffer_fill(buff, &nsize, sizeof(uint16_t));

	buffer_fill(buff, string_utf8, size_in_bytes);
}

void pack_bytes(buffer * buff, const void * data, jint size) {
	buffer_fill(buff, data, size);
}

#endif	/* _BUFFPACK_H */
