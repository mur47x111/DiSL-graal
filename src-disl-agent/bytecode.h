#ifndef _BYTECODE_H_
#define _BYTECODE_H_

#include <jvmti.h>


/**
 * Name of the class used for bypass checks.
 */
#define BPC_CLASS_NAME "ch/usi/dag/disl/dynamicbypass/BypassCheck"
#define DBP_CLASS_NAME "ch/usi/dag/disl/dynamicbypass/DynamicBypass"


/**
 * Externs for various implementations of the BypassCheck class.
 */
extern jvmtiClassDefinition always_BypassCheck_classdef;
extern jvmtiClassDefinition dynamic_BypassCheck_classdef;
extern jvmtiClassDefinition dynamic_DynamicBypass_classdef;
extern jvmtiClassDefinition never_BypassCheck_classdef;

#endif /* _BYTECODE_H_ */
