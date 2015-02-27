#include <jvmti.h>

#ifndef _DISLAGENT_H
#define	_DISLAGENT_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *jvm, char *options, void *reserved);

#ifdef __cplusplus
}
#endif

#endif	/* _DISLAGENT_H */
