package ch.usi.dag.util;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.concurrent.TimeUnit;

import ch.usi.dag.util.function.Predicate;


/**
 *
 * @author Lubomir Bulej
 */
public final class Duration {

    private final long __amount;
    private final TimeUnit __unit;

    //

    private Duration (final long amount, final TimeUnit unit) {
        __amount = amount;
        __unit = unit;
    }

    //

    /**
     * Converts this {@link Duration} to the specified time unit.
     *
     * @param unit
     *        the time unit to convert this {@link Duration} to.
     * @return the amount of the given time units representing this
     *         {@link Duration}.
     */
    public long to (final TimeUnit unit) {
        return unit.convert (__amount, __unit);
    }

    //

    /**
     * @see TimeUnit#timedWait(Object, long)
     */
    public void wait (final Object object) throws InterruptedException {
        __unit.timedWait (object, __amount);
    }

    /**
     * @see TimeUnit#timedJoin(Thread, long)
     */
    public void join (final Thread thread) throws InterruptedException {
        __unit.timedJoin (thread, __amount);
    }

    //

    /**
     * @see TimeUnit#sleep(long)
     */
    public void sleep () throws InterruptedException {
        __unit.sleep (__amount);
    }


    /**
     * Delays the execution of the current thread for this {@link Duration}. If
     * interrupted during the sleep, this method resumes the sleep and does not
     * return until this {@link Duration} has elapsed. The current thread's
     * {@code interrupted} status is not cleared.
     */
    public void sleepUninterruptibly () {
        long remainingNanos = this.to (NANOSECONDS);
        final long timeoutEndNanos = System.nanoTime () + remainingNanos;

        while (remainingNanos > 0) {
            try {
                NANOSECONDS.sleep (remainingNanos);
                remainingNanos = timeoutEndNanos - System.nanoTime ();

            } catch (final InterruptedException ie) {
                // keep sleeping until this duration elapsed
            }
        }
    }


    /**
     * Suspends the execution of the current thread until either a condition
     * becomes true or a timeout expires. The condition is a {@link Predicate}
     * to which the target object is passed as an argument. The condition is
     * evaluated before waiting on the object and after every wakeup, with the
     * target's lock held. The timeout is determined by this {@link Duration}.
     *
     * @param target
     *        the object to synchronize on, passed to the {@code condition}
     *        {@link Predicate}.
     * @param condition
     *        the {@link Predicate} used to evaluate the condition.
     * @return the value of the condition at exit, i.e., {@code true} if the
     *         condition has been met, or {@code false} if the timeout expired.
     * @throws InterruptedException
     *         if a thread has been interrupted while waiting.
     */
    public <E> boolean await (
        final E target, final Predicate <E> condition
    ) throws InterruptedException {
        synchronized (target) {
            if (! __await (target, condition, true)) {
                throw new InterruptedException ();
            }

            return condition.test (target);
        }
    }


    /**
     * Suspends the execution of the current thread until either a condition
     * becomes true or a timeout expires. The condition is a {@link Predicate}
     * to which the target object is passed as an argument. The condition is
     * evaluated before waiting on the object and after every wakeup, with the
     * target's lock held. The timeout is determined by this {@link Duration}.
     * <p>
     * In contrast to the {@link #await(Object, Predicate) await} method, if a
     * thread is interrupted while waiting on the target object, this method
     * keeps waiting until the timeout fully expires.
     *
     * @param target
     *        the object to synchronize on, passed to the {@code condition}
     *        {@link Predicate}.
     * @param condition
     *        the {@link Predicate} used to evaluate the condition.
     * @return the value of the condition at exit, i.e., {@code true} if the
     *         condition has been met, or {@code false} if the timeout expired.
     */
    public <E> boolean awaitUninterruptibly (
        final E object, final Predicate <E> condition
    ) {
        synchronized (object) {
            __await (object, condition, false);
            return condition.test (object);
        }
    }


    private <E> boolean __await (
        final E object, final Predicate <E> condition,
        final boolean isInterruptible
    ) {
        long remainingNanos = this.to (NANOSECONDS);
        final long timeoutEndNanos = System.nanoTime () + remainingNanos;

        while (!condition.test (object) && remainingNanos > 0) {
            try {
                NANOSECONDS.timedWait (object, remainingNanos);
                remainingNanos = timeoutEndNanos - System.nanoTime ();

            } catch (final InterruptedException ie) {
                if (isInterruptible) {
                    return false;
                }
            }
        }

        return true;
    }

    //

    /**
     * Creates a {@link Duration} instance representing a given amount of given
     * time units.
     *
     * @param amount
     *      the amount of time units, must be positive
     * @param unit
     *      the time unit representing the granularity of the duration
     * @return
     */
    public static Duration of (final long amount, final TimeUnit unit) {
        if (amount < 0) {
            throw new IllegalArgumentException ("amount must be non-negative");
        }

        return new Duration (amount, unit);
    }

}
