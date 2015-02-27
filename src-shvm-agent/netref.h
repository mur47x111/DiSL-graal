#ifndef _NETREF_H
#define	_NETREF_H

#include <stdint.h>

#include <jvmti.h>
#include <jni.h>

#include "jvmtihelper.h"

#include "buffer.h"
#include "buffpack.h"
#include "messagetype.h"

// first available object id
static volatile jlong avail_object_id;

// first available class id
static volatile jint avail_class_id;

// ******************* Net reference get/set routines *******************

// should be in sync with NetReference on the server

// format of net reference looks like this (from HIGHEST)
// 1 bit data trans., 1 bit class instance, 23 bits class id, 40 bits object id
// bit field not used because there is no guarantee of alignment

// TODO rename SPEC
// SPEC flag is used to indicate if some additional data for this object where
// transfered to the server

static const uint8_t OBJECT_ID_POS = 0;
static const uint8_t CLASS_ID_POS = 40;
static const uint8_t CLASS_INSTANCE_POS = 62;
static const uint8_t SPEC_POS = 63;

static const uint64_t OBJECT_ID_MASK = 0xFFFFFFFFFFL;
static const uint64_t CLASS_ID_MASK = 0x3FFFFF;
static const uint64_t CLASS_INSTANCE_MASK = 0x1;
static const uint64_t SPEC_MASK = 0x1;

// get bits from "from" with pattern "bit_mask" lowest bit starting on position
// "low_start" (from 0)
static inline uint64_t get_bits(uint64_t from, uint64_t bit_mask,
		uint8_t low_start) {

	// shift it
	uint64_t bits_shifted = from >> low_start;

	// mask it
	return bits_shifted & bit_mask;
}

// set bits "bits" to "to" with pattern "bit_mask" lowest bit starting on
// position "low_start" (from 0)
static inline void set_bits(uint64_t * to, uint64_t bits,
		uint64_t bit_mask, uint8_t low_start) {

	// mask it
	uint64_t bits_len = bits & bit_mask;
	// move it to position
	uint64_t bits_pos = bits_len << low_start;
	// set
	*to |= bits_pos;
}

inline jlong net_ref_get_object_id(jlong net_ref) {

	return get_bits(net_ref, OBJECT_ID_MASK, OBJECT_ID_POS);
}

inline jint net_ref_get_class_id(jlong net_ref) {

	return get_bits(net_ref, CLASS_ID_MASK, CLASS_ID_POS);
}

inline unsigned char net_ref_get_spec(jlong net_ref) {

	return get_bits(net_ref, SPEC_MASK, SPEC_POS);
}

inline unsigned char net_ref_get_class_instance_bit(jlong net_ref) {

	return get_bits(net_ref, CLASS_INSTANCE_MASK, CLASS_INSTANCE_POS);
}

inline void net_ref_set_object_id(jlong * net_ref, jlong object_id) {

	set_bits((uint64_t *)net_ref, object_id, OBJECT_ID_MASK, OBJECT_ID_POS);
}

inline void net_ref_set_class_id(jlong * net_ref, jint class_id) {

	set_bits((uint64_t *)net_ref, class_id, CLASS_ID_MASK, CLASS_ID_POS);
}

inline void net_ref_set_class_instance(jlong * net_ref, unsigned char cibit) {

	set_bits((uint64_t *)net_ref, cibit, CLASS_INSTANCE_MASK, CLASS_INSTANCE_POS);
}

inline void net_ref_set_spec(jlong * net_ref, unsigned char spec) {

	set_bits((uint64_t *)net_ref, spec, SPEC_MASK, SPEC_POS);
}

// ******************* Net reference routines *******************

// TODO comment

// only retrieves object tag data
jlong get_tag(jvmtiEnv * jvmti_env, jobject obj) {

	jlong net_ref;

	jvmtiError error = (*jvmti_env)->GetTag(jvmti_env, obj, &net_ref);
	check_jvmti_error(jvmti_env, error, "Cannot get object tag");

	return net_ref;
}

// forward declaration
jlong get_net_reference(JNIEnv * jni_env, jvmtiEnv * jvmti_env,
		buffer * new_obj_buff, jobject obj);

// !!! returned local reference should be freed
static jclass _get_class_for_object(JNIEnv * jni_env, jobject obj) {

	return (*jni_env)->GetObjectClass(jni_env, obj);
}

static int _object_is_class(jvmtiEnv * jvmti_env,  jobject obj) {

	// TODO isn't there better way?

	jvmtiError error =
			(*jvmti_env)->GetClassSignature(jvmti_env, obj, NULL, NULL);

	if(error != JVMTI_ERROR_NONE) {
		// object is not class
		return FALSE;
	}

	return TRUE;
}

// does not increment any counter - just sets the values
static jlong _set_net_reference(jvmtiEnv * jvmti_env, jobject obj,
		jlong object_id, jint class_id, unsigned char spec, unsigned char cbit) {

	jlong net_ref = 0;

	net_ref_set_object_id(&net_ref, object_id);
	net_ref_set_class_id(&net_ref, class_id);
	net_ref_set_spec(&net_ref, spec);
	net_ref_set_class_instance(&net_ref, cbit);

	jvmtiError error = (*jvmti_env)->SetTag(jvmti_env, obj, net_ref);
	check_jvmti_error(jvmti_env, error, "Cannot set object tag");

	return net_ref;
}

static void _pack_class_info(buffer * buff, jlong class_net_ref,
		char * class_sig, char * class_gen, jlong class_loader_net_ref,
		jlong super_class_net_ref) {

	// class gen can be NULL, we have to handle it
	if(class_gen == NULL) {
		class_gen = ""; // pack empty string
	}

	// pack class info message

	// msg id
	pack_byte(buff, MSG_CLASS_INFO);
	// class id
	pack_long(buff, class_net_ref);
	// class signature
	pack_string_utf8(buff, class_sig, strlen(class_sig));
	// class generic string
	pack_string_utf8(buff, class_gen, strlen(class_gen));
	// class loader id
	pack_long(buff, class_loader_net_ref);
	// super class id
	pack_long(buff, super_class_net_ref);

}

static jlong _set_net_reference_for_class(JNIEnv * jni_env,
		jvmtiEnv * jvmti_env, buffer * buff, jclass klass) {

	// manage references
	// http://docs.oracle.com/javase/6/docs/platform/jvmti/jvmti.html#refs
	static const jint ADD_REFS = 16;
	jint res = (*jni_env)->PushLocalFrame(jni_env, ADD_REFS);
	check_error(res != 0, "Cannot allocate more references");

	// *** set net reference for class ***

	// assign new net reference - set spec to 1 (binding send over network)
	jlong net_ref = _set_net_reference(jvmti_env, klass,
			avail_object_id, avail_class_id, 1, 1);

	// increment object id counter
	++avail_object_id;

	// increment class id counter
	++avail_class_id;

	// *** pack class info into buffer ***

	jvmtiError error;

	// resolve descriptor + generic
	char * class_sig;
	char * class_gen;
	error = (*jvmti_env)->GetClassSignature(jvmti_env, klass, &class_sig,
			&class_gen);
	check_jvmti_error(jvmti_env, error, "Cannot get class signature");

	// resolve class loader...
	jobject class_loader;
	error = (*jvmti_env)->GetClassLoader(jvmti_env, klass, &class_loader);
	check_jvmti_error(jvmti_env, error, "Cannot get class loader");
	// ... + class loader id
	jlong class_loader_net_ref =
			get_net_reference(jni_env, jvmti_env, buff, class_loader);

	// resolve super class...
	jclass super_class = (*jni_env)->GetSuperclass(jni_env, klass);
	// ... + super class id
	jlong super_class_net_ref =
			get_net_reference(jni_env, jvmti_env, buff, super_class);

	// pack class info into buffer
	_pack_class_info(buff, net_ref, class_sig, class_gen, class_loader_net_ref,
			super_class_net_ref);

	// deallocate memory
	error = (*jvmti_env)->Deallocate(jvmti_env, (unsigned char *)class_sig);
	check_jvmti_error(jvmti_env, error, "Cannot deallocate memory");
	error = (*jvmti_env)->Deallocate(jvmti_env, (unsigned char *)class_gen);
	check_jvmti_error(jvmti_env, error, "Cannot deallocate memory");

	// manage references - see function top
	(*jni_env)->PopLocalFrame(jni_env, NULL);

	return net_ref;
}

static jint _get_class_id_for_class(JNIEnv * jni_env, jvmtiEnv * jvmti_env,
		buffer * buff, jclass klass) {

	jlong class_net_ref = get_tag(jvmti_env, klass);

	if(class_net_ref == 0) {
		class_net_ref =
				_set_net_reference_for_class(jni_env, jvmti_env, buff, klass);
	}

	return net_ref_get_class_id(class_net_ref);
}

static jint _get_class_id_for_object(JNIEnv * jni_env, jvmtiEnv * jvmti_env,
		buffer * buff, jobject obj) {

	// get class of this object
	jclass klass = _get_class_for_object(jni_env, obj);

	// get class id of this class
	jint class_id = _get_class_id_for_class(jni_env, jvmti_env, buff, klass);

	// free local reference
	(*jni_env)->DeleteLocalRef(jni_env, klass);

	return class_id;
}

static jlong _set_net_reference_for_object(JNIEnv * jni_env,
		jvmtiEnv * jvmti_env, buffer * buff, jobject obj) {

	// resolve class id
	jint class_id = _get_class_id_for_object(jni_env, jvmti_env, buff, obj);

	// assign new net reference
	jlong net_ref =
			_set_net_reference(jvmti_env, obj, avail_object_id, class_id, 0, 0);

	// increment object id counter
	++avail_object_id;

	return net_ref;
}

// retrieves net_reference - performs tagging if necessary
// can be used for any object - even classes
// !!! invocation of this method should be protected by lock until the reference
// is queued for sending
jlong get_net_reference(JNIEnv * jni_env, jvmtiEnv * jvmti_env,
		buffer * new_obj_buff, jobject obj) {

	if(obj == NULL) { // net reference for NULL is 0
		return 0;
	}

	// access object tag
	jlong net_ref = get_tag(jvmti_env, obj);

	// set net reference
	if(net_ref == 0) {

		// decide setting method
		if(_object_is_class(jvmti_env, obj)) {
			// we have class object
			net_ref = _set_net_reference_for_class(jni_env, jvmti_env,
					new_obj_buff, obj);
		}
		else {
			// we have non-class object
			net_ref = _set_net_reference_for_object(jni_env, jvmti_env,
					new_obj_buff, obj);
		}
	}

	return net_ref;
}

// !!! invocation of this method should be protected by lock until the reference
// is queued for sending
void update_net_reference(jvmtiEnv * jvmti_env, jobject obj, jlong net_ref) {

	jvmtiError error = (*jvmti_env)->SetTag(jvmti_env, obj, net_ref);
	check_jvmti_error(jvmti_env, error, "Cannot set object tag");
}

#endif	/* _NETREF_H */
