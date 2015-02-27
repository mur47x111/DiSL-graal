#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <jni.h>
#include <jvmti.h>

#include "common.h"
#include "jvmtiutil.h"

#include "dislagent.h"
#include "connection.h"

#include "network.h"
#include "msgchannel.h"
#include "bytecode.h"
#include "codeflags.h"


// ****************************************************************************
// AGENT CONFIG
// ****************************************************************************

#define DISL_SERVER_HOST "disl.server.host"
#define DISL_SERVER_HOST_DEFAULT "localhost"

#define DISL_SERVER_PORT "disl.server.port"
#define DISL_SERVER_PORT_DEFAULT "11217"

#define DISL_BYPASS "disl.bypass"
#define DISL_BYPASS_DEFAULT "dynamic"

#define DISL_SPLIT_METHODS "disl.splitmethods"
#define DISL_SPLIT_METHODS_DEFAULT true

#define DISL_CATCH_EXCEPTIONS "disl.excepthandler"
#define DISL_CATCH_EXCEPTIONS_DEFAULT false

#define DISL_DEBUG "debug"
#define DISL_DEBUG_DEFAULT false


/**
 * The instrumentation bypass mode.
 * NOTE: The ordering of the modes is important!
 */
enum bypass_mode {
	/**
	 * The original method code will not be preserved. Consequently, the
	 * instrumented version of a method will be always executed, even when
	 * invoked inside a DiSL snippet.
	 */
	BYPASS_MODE_DISABLED = 0,

	/**
	 * The original method code will be preserved, and will be always
	 * executed instead of the instrumented version during JVM bootstrap.
	 * After bootstrap, only the instrumented version of a method will
	 * be executed.
	 */
	BYPASS_MODE_BOOTSTRAP = 1,

	/**
	 * The original method code will be preserved, and executed whenever
	 * invoked from inside a DiSL snippet.
	 */
	BYPASS_MODE_DYNAMIC = 2
};


/**
 * Flags representing code options, derived from the values generated from Java.
 */
enum code_flags {
	CF_CREATE_BYPASS = ch_usi_dag_disl_DiSL_CodeOption_Flag_CREATE_BYPASS,
	CF_DYNAMIC_BYPASS = ch_usi_dag_disl_DiSL_CodeOption_Flag_DYNAMIC_BYPASS,
	CF_SPLIT_METHODS = ch_usi_dag_disl_DiSL_CodeOption_Flag_SPLIT_METHODS,
	CF_CATCH_EXCEPTIONS = ch_usi_dag_disl_DiSL_CodeOption_Flag_CATCH_EXCEPTIONS,
};


struct config {
	char * server_host;
	char * server_port;

	enum bypass_mode bypass_mode;
	bool split_methods;
	bool catch_exceptions;

	bool debug;
};


/**
 * Agent configuration.
 */
static struct config agent_config;


/**
 * Code option flags that control the instrumentation.
 */
static volatile jint agent_code_flags;


/**
 * Prints a debug message to stdout, if runtime agent debugging is enabled.
 *
 * @format	format string for printf
 * @args	arguments associated with the format string
 *
 */
#define rdprintf(args...) \
	if (agent_config.debug) { \
		fprintf (stdout, "agent: "); \
		fprintf (stdout, args); \
	}


// ****************************************************************************
// CLASS FILE LOAD
// ****************************************************************************

static jint
__calc_code_flags (struct config * config, bool jvm_is_booting) {
	jint result = 0;

	//
	// If bypass is desired, always create bypass code while the JVM is
	// bootstrapping. If dynamic bypass is desired, create bypass code as
	// well as code to control it dynamically.
	//
	if (config->bypass_mode > BYPASS_MODE_DISABLED) {
		result |= jvm_is_booting ? CF_CREATE_BYPASS : 0;
		if (config->bypass_mode > BYPASS_MODE_BOOTSTRAP) {
			result |= (CF_CREATE_BYPASS | CF_DYNAMIC_BYPASS);
		}
	}

	result |= config->split_methods ? CF_SPLIT_METHODS : 0;
	result |= config->catch_exceptions ? CF_CATCH_EXCEPTIONS : 0;

	return result;
}


/**
 * Sends the given class to the remote server for instrumentation. If the
 * server modified the class, provided class definition structure is updated
 * and the function returns TRUE. Otherwise, the structure is left unmodified
 * and FALSE is returned.
 *
 * NOTE: The class_name parameter may be NULL -- this is often the case for
 * anonymous classes.
 */
static bool
__instrument_class (
	jint request_flags, const char * class_name,
	jvmtiClassDefinition * class_def
) {
	//
	// Put the class data into a request message, acquire a connection and
	// send the it to the server. Receive the response and release the
	// connection again.
	//
	struct message request = {
		.message_flags = request_flags,
		.control_size = (class_name != NULL) ? strlen (class_name) : 0,
		.classcode_size = class_def->class_byte_count,
		.control = (unsigned char *) class_name,
		.classcode = class_def->class_bytes,
	};

	//

	struct connection * conn = network_acquire_connection ();
	message_send (conn, &request);

	struct message response;
	message_recv (conn, &response);
	network_release_connection (conn);

	//
	// Check if error occurred on the server.
	// The control field of the response contains the error message.
	//
	if (response.control_size > 0) {
		fprintf (
			stderr,
			"%sinstrumentation server error:\n%s\n",
			ERROR_PREFIX, response.control
		);

		exit (ERROR_SERVER);
	}

	//
	// Update the class definition and signal that the class has been
	// modified if non-empty class code has been returned. Otherwise,
	// signal that the class has not been modified.
	//
	if (response.classcode_size > 0) {
		class_def->class_byte_count = response.classcode_size;
		class_def->class_bytes = response.classcode;
		return true;

	} else {
		return false;
	}
}


static void JNICALL
jvmti_callback_class_file_load (
	jvmtiEnv * jvmti, JNIEnv * jni,
	jclass class_being_redefined, jobject loader,
	const char * class_name, jobject protection_domain,
	jint class_byte_count, const unsigned char * class_bytes,
	jint * new_class_byte_count, unsigned char ** new_class_bytes
) {
	assert (jvmti != NULL);

	rdprintf (
		"%s loaded, %ld bytes at %p\n",
		(class_name != NULL) ? class_name : "<unknown class>",
		(long) class_byte_count, class_bytes
	);

	//
	// Avoid instrumenting the bypass check class.
	//
	if (class_name != NULL && (strcmp (class_name, BPC_CLASS_NAME) == 0)) {
		rdprintf ("skipping bypass check class (%s)\n", class_name);
		return;
	}


	//
	// Instrument the class and if changed by the server, provide the
	// code to the JVM in its own memory.
	//
	jvmtiClassDefinition class_def = {
		.class_byte_count = class_byte_count,
		.class_bytes = class_bytes,
	};

	bool class_changed = __instrument_class (
		agent_code_flags, class_name, &class_def
	);

	if (class_changed) {
		unsigned char * jvm_class_bytes = jvmti_alloc_copy (
			jvmti, class_def.class_bytes, class_def.class_byte_count
		);

		free ((void *) class_def.class_bytes);

		*new_class_byte_count = class_def.class_byte_count;
		*new_class_bytes = jvm_class_bytes;

		rdprintf (
			"class redefined, %ld bytes at %p\n",
			(long) class_def.class_byte_count, jvm_class_bytes
		);
	} else {
		rdprintf ("class not modified\n");
	}
}


// ****************************************************************************
// JVMTI EVENT: VM INIT
// ****************************************************************************

static void JNICALL
jvmti_callback_vm_init (jvmtiEnv * jvmti, JNIEnv * jni, jthread thread) {
	rdprintf ("the VM has been initialized\n");

	//
	// Update code flags to reflect that the VM has stopped booting.
	//
	agent_code_flags = __calc_code_flags (&agent_config, false);

	//
	// Redefine the bypass check class. If dynamic bypass is required, use
	// a class that honors the dynamic bypass state for the current thread.
	// Otherwise use a class that disables bypassing instrumented code.
	//
	jvmtiClassDefinition * bpc_classdef;
	if (agent_config.bypass_mode == BYPASS_MODE_DYNAMIC) {
		rdprintf ("redefining BypassCheck for dynamic bypass\n");
		bpc_classdef = &dynamic_BypassCheck_classdef;
	} else {
		rdprintf ("redefining BypassCheck to disable bypass\n");
		bpc_classdef = &never_BypassCheck_classdef;
	}

	jvmti_redefine_class (jvmti, jni, BPC_CLASS_NAME, bpc_classdef);
}


// ****************************************************************************
// JVMTI EVENT: VM DEATH
// ****************************************************************************

static void JNICALL
jvmti_callback_vm_death (jvmtiEnv * jvmti, JNIEnv * jni) {
	rdprintf ("the VM is shutting down, closing connections\n");

	//
	// Just close all the connections.
	//
	network_fini ();
}


// ****************************************************************************
// AGENT ENTRY POINT: ON LOAD
// ****************************************************************************

static void
__configure_from_properties (jvmtiEnv * jvmti, struct config * config) {
	//
	// Get bypass mode configuration
	//
	char * bypass = jvmti_get_system_property_string (
		jvmti, DISL_BYPASS, DISL_BYPASS_DEFAULT
	);

	static const char * values [] = { "disabled", "bootstrap", "dynamic" };
	int bypass_index = find_value_index (bypass, values, sizeof_array (values));
	check_error (bypass_index < 0, "invalid bypass mode, check " DISL_BYPASS);

	config->bypass_mode = bypass_index;
	free (bypass);

	rdprintf ("bypass mode: %s\n", values [bypass_index]);

	//
	// Get boolean values from system properties
	//
	config->split_methods = jvmti_get_system_property_bool (
		jvmti, DISL_SPLIT_METHODS, DISL_SPLIT_METHODS_DEFAULT
	);

	config->catch_exceptions = jvmti_get_system_property_bool (
		jvmti, DISL_CATCH_EXCEPTIONS, DISL_CATCH_EXCEPTIONS_DEFAULT
	);

	config->debug = jvmti_get_system_property_bool (
		jvmti, DISL_DEBUG, DISL_DEBUG_DEFAULT
	);
}


static void
__configure_from_options (const char * options, struct config * config) {
	//
	// Assign default host name and port and bail out
	// if there are no agent options.
	//
	if (options == NULL) {
		config->server_host = strdup (DISL_SERVER_HOST_DEFAULT);
		config->server_port = strdup (DISL_SERVER_PORT_DEFAULT);
		return;
	}

	//
	// Parse the host name and port of the remote server.
	// Look for port specification first, then take the prefix
	// before ':' as the host name.
	//
	char * host_start = strdup (options);
	char * port_start = strchr (host_start, ':');
	if (port_start != NULL) {
		//
		// Split the option string at the port delimiter (':')
		// using an end-of-string character ('\0') and copy
		// the port.
		//
		port_start [0] = '\0';
		port_start++;

		config->server_port = strdup (port_start);
	}

	config->server_host = strdup (host_start);
}


static jvmtiEnv *
__get_jvmti (JavaVM * jvm) {
	jvmtiEnv * jvmti = NULL;

	jint result = (*jvm)->GetEnv (jvm, (void **) &jvmti, JVMTI_VERSION_1_0);
	if (result != JNI_OK || jvmti == NULL) {
		//
		// The VM was unable to provide the requested version of the
		// JVMTI interface. This is a fatal error for the agent.
		//
		fprintf (
			stderr,
			"%sFailed to obtain JVMTI interface Version 1 (0x%x)\n"
			"JVM GetEnv() returned %ld - is your Java runtime "
			"version 1.5 or newer?\n",
			ERROR_PREFIX, JVMTI_VERSION_1, (long) result
		);

		exit (ERROR_JVMTI);
	}

	return jvmti;
}


#ifdef WHOLE
#define VISIBLE __attribute__((externally_visible))
#else
#define VISIBLE
#endif


JNIEXPORT jint JNICALL VISIBLE
Agent_OnLoad (JavaVM * jvm, char * options, void * reserved) {
	jvmtiEnv * jvmti = __get_jvmti (jvm);

	// add capabilities
	jvmtiCapabilities caps = {
		.can_redefine_classes = 1,
		.can_generate_all_class_hook_events = 1,
	};

	jvmtiError error = (*jvmti)->AddCapabilities (jvmti, &caps);
	check_jvmti_error (jvmti, error, "failed to add capabilities");


	// configure agent and init connections
	__configure_from_options (options, &agent_config);
	__configure_from_properties (jvmti, &agent_config);

	agent_code_flags = __calc_code_flags (&agent_config, true);
	network_init (agent_config.server_host, agent_config.server_port);


	// register callbacks
	jvmtiEventCallbacks callbacks = {
		.VMInit = &jvmti_callback_vm_init,
		.VMDeath = &jvmti_callback_vm_death,
		.ClassFileLoadHook = &jvmti_callback_class_file_load,
	};

	error = (*jvmti)->SetEventCallbacks (jvmti, &callbacks, (jint) sizeof (callbacks));
	check_jvmti_error (jvmti, error, "failed to register event callbacks");


	// enable event notification
	error = (*jvmti)->SetEventNotificationMode (jvmti, JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, NULL);
	check_jvmti_error (jvmti, error, "failed to enable VM INIT event");

	error = (*jvmti)->SetEventNotificationMode (jvmti, JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH, NULL);
	check_jvmti_error (jvmti, error, "failed to enable VM DEATH event");

	error = (*jvmti)->SetEventNotificationMode (jvmti, JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, NULL);
	check_jvmti_error (jvmti, error, "failed to enable CLASS FILE LOAD event");

	return 0;
}
