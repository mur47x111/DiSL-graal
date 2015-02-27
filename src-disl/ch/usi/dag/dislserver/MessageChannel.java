package ch.usi.dag.dislserver;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

final class MessageChannel implements Closeable {

    private final SocketChannel __socket;

    private final ByteBuffer __head = ByteBuffer.allocateDirect (12).order (ByteOrder.BIG_ENDIAN);
    private ByteBuffer __body = ByteBuffer.allocateDirect (128 * 1024);

    private final ByteBuffer [] __sendBuffers = new ByteBuffer [] {
        __head, null, null
    };

    //

    public MessageChannel (final SocketChannel socket) {
        __socket = socket;
    }

    //

    public Message recvMessage () throws IOException {
        //
        // request protocol:
        //
        // java int - request flags
        // java int - control data length (cdl)
        // java int - payload data length (pdl)
        // bytes[cdl] - control data (contains class name)
        // bytes[pdl] - payload data (contains class code)
        //

        __head.rewind ();

        do {
            __socket.read (__head);
        } while (__head.hasRemaining ());

        //

        __head.rewind ();

        final int flags = __head.getInt ();
        final int controlLength = __head.getInt ();
        final int payloadLength = __head.getInt ();

        //

        __ensureBodyCapacity (controlLength + payloadLength);

        __body.rewind ();

        do {
            __socket.read (__body);
        } while (__body.hasRemaining ());

        //

        __body.rewind ();

        final byte [] control = new byte [controlLength];
        __body.get (control);

        final byte [] payload = new byte [payloadLength];
        __body.get (payload);

        return new Message (flags, control, payload);
    }


    public void sendMessage (final Message message) throws IOException {
        //
        // response protocol:
        //
        // java int - response flags
        // java int - control data length (cdl)
        // java int - payload data length (pdl)
        // bytes[cdl] - control data (nothing, or error message)
        // bytes[pdl] - payload data (instrumented class code)
        //

        __head.rewind ();

        __head.putInt (message.flags ());

        final int controlLength = message.control ().length;
        __head.putInt (controlLength);

        final int payloadLength = message.payload ().length;
        __head.putInt (payloadLength);

        //

        __sendBuffers [1] = ByteBuffer.wrap (message.control ());
        __sendBuffers [2] = ByteBuffer.wrap (message.payload ());

        //

        __head.rewind ();

        do {
            __socket.write (__sendBuffers);
        } while (__sendBuffers [2].hasRemaining ());
    }


    private void __ensureBodyCapacity (final int length) {
        if (__body.capacity () < length) {
            __body = ByteBuffer.allocateDirect (__align (length, 12));
        }

        __body.limit (length);
    }

    private static int __align (final int value, final int bits) {
        final int mask = -1 << bits;
        final int fill = (1 << bits) - 1;
        return (value + fill) & mask;
    }


    @Override
    public void close () throws IOException {
        __socket.close ();
    }

}
