#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "common.h"


/**
 * Returns the index of the given value in the given array of values.
 * Returns -1 if the value could not be found among the allowed values.
 */
int
find_value_index (const char * strval, const char * values [], int nvals) {
	for (int i = 0; i < nvals; i++) {
		if (strcasecmp (values [i], strval) == 0) {
			return i;
		}
	}

	return -1;
}

//

/**
 * Reports an error and terminates the program. This function implements the
 * slow path of check_error(). It prints the given error message and exits with
 * an error code indicating a generic error.
 */
void
die_with_error (const char * message) {
	fprintf (stderr, "%s%s\n", ERROR_PREFIX, message);
	exit (ERROR_GENERIC);
}


/**
 * Reports a standard library error and terminates the program. This function
 * implements the slow path of check_std_error(). It prints the given error
 * message along with the error message provided by the standard library and
 * exits with an error code indicating failure in standard library call.
 */
void
die_with_std_error (const char * message, int errnum) {
	char * cause = strerror (errnum);
	fprintf (stderr, "%s%s\ncause: %s", ERROR_PREFIX, message, cause);
	exit (ERROR_STD);
}


#ifdef MINGW

/**
 * Obtains an error message for the given error code.
 * Allocates a new string that has to be released by the caller.
 */
static char *
__get_error_message (const DWORD msg_id) {
	LPVOID msg_buffer = NULL;
	size_t size = FormatMessageA (
		FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
		NULL, msg_id, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPSTR) &msg_buffer, 0, NULL
	);

	if (size != 0 && msg_buffer != NULL) {
		char * message = strdup (msg_buffer);
		LocalFree (msg_buffer);
		return message;

	} else {
		static const char * msg_format = "unknown error (%d)";
		size_t msg_length = strlen (msg_format) + ((sizeof(DWORD) * 8) / 3) + 1;
		char * message = (char *) malloc (msg_length);
		if (message != NULL) {
			snprintf (message, msg_length, msg_format, msg_id);
		}

		return message;
	}
}


/**
 * Reports a windows error and terminates the program. This function
 * implements the slow path of check_win_error(). It prints the given error
 * message along with the error message provided by windows and
 * exits with an error code indicating failure in standard library call.
 */
void
die_with_win_error (const char * message, DWORD errnum) {
	char * cause = __get_error_message (errnum);
	fprintf (stderr, "%s%s\ncause: %s", ERROR_PREFIX, message, cause);
	if (cause != NULL) {
		free (cause);
	}

	exit (ERROR_STD);
}

#endif /* MINGW */
