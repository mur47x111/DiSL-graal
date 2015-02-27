#ifndef _MESSAGETYPE_H
#define	_MESSAGETYPE_H

// Messages Types
//  - should be in sync with java server

// closing connection
static const jbyte MSG_CLOSE = 0;
// sending analysis
static const jbyte MSG_ANALYZE = 1;
// sending object free
static const jbyte MSG_OBJ_FREE = 2;
// sending new class
static const jbyte MSG_NEW_CLASS = 3;
// sending class info
static const jbyte MSG_CLASS_INFO = 4;
// sending string info
static const jbyte MSG_STRING_INFO = 5;
// sending registration for analysis method
static const jbyte MSG_REG_ANALYSIS = 6;
// sending thread info
static const jbyte MSG_THREAD_INFO = 7;
// sending thread end message
static const jbyte MSG_THREAD_END = 8;

#endif	/* _MESSAGETYPE_H */
