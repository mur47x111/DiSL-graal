package ch.usi.dag.dislserver;

final class CounterSet <E extends Enum <E>> {

    private final long [] __counters;

    //

    CounterSet (final Class <E> type) {
        __counters = new long [type.getEnumConstants ().length];
    }

    //

    long get (final E ts) {
        return __counters [ts.ordinal ()];
    }


    void update (final IntervalTimer <E> timer) {
        __update (__counters, timer.intervals ());
    }


    synchronized void update (final CounterSet <E> other) {
        __update (__counters, other.__counters);
    }


    private static void __update (
        final long [] __dst, final long [] __src
    ) {
        final int count = __dst.length;
        for (int i = 0; i < count; i++) {
            __dst [i] += __src [i];
        }
    }
}
