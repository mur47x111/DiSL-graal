#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <jvmti.h>

#include "common.h"
#include "jvmtiutil.h"

#ifndef ERROR_PREFIX
#error ERROR_PREFIX macro has to be defined
#endif

#ifndef ERROR_JVMTI
#error ERROR_JVMTI macro has to be defined
#endif


/**
 * Allocates JVM memory for the given buffer and copies the buffer
 * into JVM memory.
 */
unsigned char *
jvmti_alloc_copy (jvmtiEnv * jvmti, const void * src, size_t size) {
	assert (jvmti != NULL);
	assert (src != NULL);

	unsigned char * jvm_dst;
	jvmtiError error = (*jvmti)->Allocate (jvmti, (jlong) size, &jvm_dst);
	check_jvmti_error (jvmti, error, "failed create a JVM copy of a buffer");

	memcpy (jvm_dst, src, size);
	return jvm_dst;
}


/**
 * Redefines a class given name and (partial) class definition.
 * The class to be redefined is first looked up using JNI to
 * complete the class definition information. Returns true
 * if the redefinition was complete, false if the class could
 * not be found.
 */
bool
jvmti_redefine_class (
	jvmtiEnv * jvmti, JNIEnv * jni,
	const char * class_name, const jvmtiClassDefinition * class_def
) {
	assert (jvmti != NULL);
	assert (jni != NULL);
	assert (class_name != NULL);
	assert (class_def != NULL);

	jclass class = (* jni)->FindClass (jni, class_name);
	if (class == NULL) {
		return false;
	}

	//

	jvmtiClassDefinition new_classdef = {
		.klass = class,
		.class_byte_count = class_def->class_byte_count,
		.class_bytes = jvmti_alloc_copy (
			jvmti, class_def->class_bytes, class_def->class_byte_count
		),
	};

	jvmtiError error = (*jvmti)->RedefineClasses (jvmti, 1, &new_classdef);
	check_jvmti_error (jvmti, error, "failed to redefine class");

	return true;
}



static char *
__get_system_property (jvmtiEnv * jvmti, const char * name) {
	//
	// If the requested property does not exist, GetSystemProperty() will
	// return JVMTI_ERROR_NOT_AVAILABLE and will not modify the value pointer.
	// The other error that could occur is JVMTI_ERROR_NULL_POINTER, but we
	// assert that could not happen.
	//
	char * value = NULL;
	(*jvmti)->GetSystemProperty (jvmti, name, &value);

	if (value == NULL) {
		return NULL;
	}

	//

	char * result = strdup (value);
	check_error (result == NULL, "failed to duplicate system property value");

	jvmtiError error = (*jvmti)->Deallocate (jvmti, (unsigned char *) value);
	check_jvmti_error (jvmti, error, "failed to deallocate system property value");

	return result;
}


static bool
__parse_bool (const char * strval) {
	static const char * trues [] = { "true", "yes", "on", "1" };
	return find_value_index (strval, trues, sizeof_array (trues)) >= 0;
}


/**
 * Returns the boolean value of a system property, or the default
 * value if it not defined.
 */
bool
jvmti_get_system_property_bool (
	jvmtiEnv * jvmti, const char * name, bool dflval
) {
	assert (jvmti != NULL);
	assert (name != NULL);

	char * strval = __get_system_property (jvmti, name);
	if (strval != NULL) {
		bool result = __parse_bool (strval);
		free (strval);

		return result;

	} else {
		return dflval;
	}
}


/**
 * Returns the string value of a system property, or the default
 * value if the property is not defined. The memory for the returned
 * value is always allocated (even for the default value) and the 
 * caller is responsible for releasing it.
 */
char *
jvmti_get_system_property_string (
	jvmtiEnv * jvmti, const char * name, const char * dflval
) {
	assert (jvmti != NULL);
	assert (name != NULL);

	char * strval = __get_system_property (jvmti, name);
	if (strval != NULL) {
		return strval;

	} else if (dflval != NULL) {
		//
		// Duplicate the default value so that the caller always "owns"
		// the returned value and can release it using free().
		//
		char * result = strdup (dflval);
		check_error (result == NULL, "failed to duplicate default value");
		return result;

	} else {
		return NULL;
	}
}


/**
 * Reports a JVMTI error and terminates the program. This function implements
 * the slow path of check_jvmti_error() and prints the given error message
 * along with a JVMTI error name obtained using the GetErrorName() JVMTI
 * interface.
 */
void
die_with_jvmti_error (jvmtiEnv *jvmti, jvmtiError errnum, const char *str) {
	char * errnum_str = NULL;
	(void) (*jvmti)->GetErrorName (jvmti, errnum, &errnum_str);

	fprintf (
		stderr, "%sJVMTI: %d (%s): %s\n",
		ERROR_PREFIX, errnum,
		(errnum_str == NULL ? "Unknown" : errnum_str),
		(str == NULL ? "" : str)
	);

	exit (ERROR_JVMTI);
}
