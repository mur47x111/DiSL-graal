#ifndef _JVMTIHELPER_H
#define	_JVMTIHELPER_H

#ifndef ERR_PREFIX
#error ERR_PREFIX macro has to be deffined
#endif

#include <jvmti.h>

#include <stdlib.h>

// true/false consts
#define TRUE 1
#define FALSE 0

// error nums
#define ERR 10000
#define ERR_STD 10002
#define ERR_JVMTI 10003

/*
 * Reports error if condition is true
 */
void check_error(int cond, const char *str) {

	if (cond) {

		fprintf(stderr, "%s%s\n", ERR_PREFIX, str);

		exit(ERR);
	}
}

/*
 * Check error routine - reporting on one place
 */
void check_std_error(int retval, int errorval, const char *str) {

	if (retval == errorval) {

		static const int BUFFSIZE = 1024;

		char msgbuf[BUFFSIZE];

		snprintf(msgbuf, BUFFSIZE, "%s%s", ERR_PREFIX, str);

		perror(msgbuf);

		exit(ERR_STD);
	}
}

/*
 * Every JVMTI interface returns an error code, which should be checked
 *   to avoid any cascading errors down the line.
 *   The interface GetErrorName() returns the actual enumeration constant
 *   name, making the error messages much easier to understand.
 */
void check_jvmti_error(jvmtiEnv *jvmti, jvmtiError errnum, const char *str) {

	if (errnum != JVMTI_ERROR_NONE) {
		char *errnum_str;

		errnum_str = NULL;
		(void) (*jvmti)->GetErrorName(jvmti, errnum, &errnum_str);

		fprintf(stderr, "%sJVMTI: %d(%s): %s\n", ERR_PREFIX, errnum,
				(errnum_str == NULL ? "Unknown" : errnum_str),
				(str == NULL ? "" : str));

		exit(ERR_JVMTI);
	}
}

/*
 * Enter a critical section by doing a JVMTI Raw Monitor Enter
 */
void enter_critical_section(jvmtiEnv *jvmti, jrawMonitorID lock_id) {

	jvmtiError error;

	error = (*jvmti)->RawMonitorEnter(jvmti, lock_id);
	check_jvmti_error(jvmti, error, "Cannot enter with raw monitor");
}

/*
 * Exit a critical section by doing a JVMTI Raw Monitor Exit
 */
void exit_critical_section(jvmtiEnv *jvmti, jrawMonitorID lock_id) {

	jvmtiError error;

	error = (*jvmti)->RawMonitorExit(jvmti, lock_id);
	check_jvmti_error(jvmti, error, "Cannot exit with raw monitor");
}

#endif	/* _JVMTIHELPER_H */
