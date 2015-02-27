#ifndef _JVMTIUTIL_H_
#define _JVMTIUTIL_H_

#include <jvmti.h>

#include "common.h"


void die_with_jvmti_error (jvmtiEnv * jvmti, jvmtiError error, const char * message);

unsigned char * jvmti_alloc_copy (jvmtiEnv * jvmti, const void * src, size_t size);

bool jvmti_redefine_class (
	jvmtiEnv * jvmti, JNIEnv * jni,
	const char * name, const jvmtiClassDefinition * definition
);

bool jvmti_get_system_property_bool (
	jvmtiEnv * jvmti, const char * name, bool dflval
);

char * jvmti_get_system_property_string (
	jvmtiEnv * jvmti, const char * name, const char * dflval
);


/**
 * Checks whether a JVMTI invocation returned an error. Every JVMTI interface
 * returns an error code, which should be checked to avoid any cascading errors
 * down the line.
 */
inline static void
check_jvmti_error(jvmtiEnv *jvmti, jvmtiError errnum, const char * message) {
	if (errnum != JVMTI_ERROR_NONE) {
		die_with_jvmti_error (jvmti, errnum, message);
	}
}


/**
 * Enters a critical section protected by a JVMTI Raw Monitor.
 */
inline static void
enter_critical_section (jvmtiEnv *jvmti, jrawMonitorID lock_id) {
	jvmtiError error = (*jvmti)->RawMonitorEnter(jvmti, lock_id);
	check_jvmti_error (jvmti, error, "failed to enter critical section");
}


/**
 * Leaves a critical section protected by a JVMTI Raw Monitor.
 */
inline static void
exit_critical_section (jvmtiEnv *jvmti, jrawMonitorID lock_id) {
	jvmtiError error = (*jvmti)->RawMonitorExit(jvmti, lock_id);
	check_jvmti_error (jvmti, error, "failed to exit critical section");
}

#endif /* _JVMTIUTIL_H_ */
