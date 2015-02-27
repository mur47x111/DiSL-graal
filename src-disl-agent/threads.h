#ifndef _THREADS_H_
#define _THREADS_H_


#include "common.h"


#ifdef MINGW

#include <windows.h>

#define thread_t HANDLE
#define mutex_t HANDLE

//

static inline void
thread_create (thread_t * thread, void * (* thread_fn) (void *), void * data) {
	*thread = CreateThread (NULL, 0, (LPTHREAD_START_ROUTINE) thread_fn, data, 0, NULL);
	check_win_error (*thread == NULL, "failed to create thread");
}


static inline void
mutex_init (mutex_t * mutex) {
	*mutex = CreateMutex (NULL, FALSE, NULL);
	check_win_error (*mutex == NULL, "failed to create mutex");
}


static inline void
mutex_lock (mutex_t * mutex) {
	DWORD result = WaitForSingleObject (*mutex, INFINITE);
	check_win_error (result != WAIT_OBJECT_0, "failed to lock mutex");
}


static inline void
mutex_unlock (mutex_t * mutex) {
	BOOL released = ReleaseMutex (*mutex);
	check_win_error (!released, "failed to release mutex");
}


static inline void
mutex_destroy (mutex_t * mutex) {
	BOOL closed = CloseHandle (*mutex);
	check_win_error (!closed, "failed to destroy mutex");
}


#else

#include <pthread.h>

#define thread_t pthread_t
#define mutex_t pthread_mutex_t

//

static inline void
thread_create (thread_t * thread, void * (* thread_fn) (void *), void * data) {
	int result = pthread_create (thread, NULL, thread_fn, data);
	check_std_error (result != 0, "failed to create thread");
}


static inline void
thread_join (thread_t * thread) {
	int result = pthread_join (*thread, NULL);
	check_std_error (result != 0, "failed to join thread");
}



static inline void
mutex_init (mutex_t * mutex) {
	int result = pthread_mutex_init (mutex, NULL);
	check_std_error (result != 0, "failed to initialize mutex");
}


static inline void
mutex_lock (mutex_t * mutex) {
	int result = pthread_mutex_lock (mutex);
	check_std_error (result != 0, "failed to lock mutex");
}


static inline void
mutex_unlock (mutex_t * mutex) {
	int result = pthread_mutex_unlock (mutex);
	check_std_error (result != 0, "failed to release mutex");
}


static inline void
mutex_destroy (mutex_t * mutex) {
	int result = pthread_mutex_destroy (mutex);
	check_std_error (result != 0, "failed to destroy mutex");
}

#endif /* !MINGW */

#endif /* _THREADS_H_ */
