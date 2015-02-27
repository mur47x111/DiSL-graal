package ch.usi.dag.dislserver;

final class IntervalTimer <E extends Enum <E>> {

    private final long [] __stamps;
    private final long [] __intervals;

    private long __start;

    //

    IntervalTimer (final Class <E> tsClass) {
        final int count = tsClass.getEnumConstants ().length;

        __stamps = new long [count];
        __intervals = new long [count];
    }

    void reset () {
        __start = System.nanoTime ();
    }

    void mark (final E ts) {
        __stamps [ts.ordinal ()] = System.nanoTime ();
    }


    long [] intervals () {
        final int count = __stamps.length;

        long start = __start;
        for (int i = 0; i < count; i++) {
            final long stamp = __stamps [i];
            __intervals [i] = stamp - start;
            start = stamp;
        }

        return __intervals;
    }

}
