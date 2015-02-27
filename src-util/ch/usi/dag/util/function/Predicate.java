package ch.usi.dag.util.function;

/**
 * Represents a functor which determines whether a given object has a certain
 * (implementation specific) property.
 * <p>
 * A {@link Predicate} functor is typically used to filter elements from an
 * iterable container.
 */
public interface Predicate <E> {
    /**
     * Determines whether the predicate holds for the given element.
     *
     * @param element
     *        element to check, may be {@code null}
     * @return {@code true} if the element is acceptable by the filter,
     *         {@code false} otherwise
     */
    boolean test (E element);
}
