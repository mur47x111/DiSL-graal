package ch.usi.dag.dislserver;



final class Message {

    private static final byte [] __EMPTY_ARRAY__ = new byte [0];

    //

    private final int __flags;

    private final byte [] __control;

    private final byte [] __payload;

    //

    public Message (
        final int flags, final byte [] control, final byte [] payload
    ) {
        __flags = flags;
        __control = control;
        __payload = payload;
    }

    //

    public int flags () {
        return __flags;
    }


    public byte [] control () {
        return __control;
    }


    public byte [] payload () {
        return __payload;
    }

    //

    public boolean isShutdown () {
        return (__control.length == 0) && (__payload.length == 0);
    }

    //

    /**
     * Creates a message containing a modified class bytecode.
     *
     * @param bytecode
     *      the bytecode of the modified class.
     */
    public static Message createClassModifiedResponse (final byte [] bytecode) {
        //
        // The flags are all reset, the control part of the network message
        // is empty, and the payload contains the modified class bytecode.
        //
        return new Message (0, __EMPTY_ARRAY__, bytecode);
    }


    /**
     * Creates a message indicating that a class was not modified.
     */
    public static Message createNoOperationResponse () {
        //
        // The flags are all reset, and both the control part and the
        // payload parts of the network message are empty.
        //
        return new Message (0, __EMPTY_ARRAY__, __EMPTY_ARRAY__);
    }

    /**
     * Creates a message indicating a server-side error.
     */
    public static Message createErrorResponse (final String error) {
        //
        // The flags are all set, the control part of the network message
        // contains the error message, and the payload is empty.
        //
        return new Message (-1, error.getBytes (), __EMPTY_ARRAY__);
    }

}
