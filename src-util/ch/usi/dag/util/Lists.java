package ch.usi.dag.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Utility class providing generic static factory methods for creating instances
 * of {@link List} collection types. The factory methods rely on type inference
 * to determine the type parameters needed for constructing a specific instance.
 *
 * @author Lubomir Bulej
 */
public final class Lists {

    private Lists () {
        // pure static class - not to be instantiated
    }


    /* ***********************************************************************
     * ArrayList
     * ***********************************************************************/

    /**
     * Creates a new instance of a generic {@link ArrayList}.
     *
     * @param <E>
     *      element type, inferred from the result type
     * @return
     *      new {@link ArrayList} instance
     */
    public static <E> ArrayList <E> newArrayList () {
        return new ArrayList <E> ();
    }


    /**
     * Creates a new instance of a generic {@link ArrayList} with a given
     * initial capacity.
     *
     * @param <E>
     *      element type, inferred from the result type
     * @param initialCapacity
     *      the initial capacity of the list
     * @return
     *      new {@link ArrayList} instance
     * @throws IllegalArgumentException
     *      if the specified initial capacity is negative
     */
    public static <E> ArrayList <E> newArrayList (final int initialCapacity) {
        return new ArrayList <E> (initialCapacity);
    }


    /**
     * Creates a new instance of a generic {@link ArrayList} and fills it
     * {@code elements} from the given array.
     *
     * @param <E>
     *      element type, inferred from the result type
     * @param elements
     *      the elements to put into the list
     * @return
     *      new {@link ArrayList} instance
     */
    public static <E> ArrayList <E> newArrayList (final E ... elements) {
        Assert.objectNotNull (elements, "elements");

        //
        return __addAllToList (elements, new ArrayList <E> (elements.length));
    }


    /**
     * Creates a new instance of a generic {@link ArrayList} and fills it with
     * {@code elements} from the given {@link Collection}.
     *
     * @param <E>
     *      element type, inferred from the result type
     * @param elements
     *      the elements to put into the list
     * @return
     *      new {@link ArrayList} instance of appropriate type
     */
    public static <E> ArrayList <E> newArrayList (
        final Collection <? extends E> elements
    ) {
        Assert.objectNotNull (elements, "elements");

        //

        return new ArrayList <E> (elements);
    }


    /**
     * Creates a new instance of a generic {@link ArrayList} and fills it with
     * {@code elements} from the given {@link Iterable}.
     *
     * @param <E>
     *      element type, inferred from the result type
     * @param elements
     *      the elements to put into the list
     * @return
     *      new {@link ArrayList} instance
     */
    public static <E> ArrayList <E> newArrayList (
        final Iterable <? extends E> elements
    ) {
        Assert.objectNotNull (elements, "elements");

        //

        final ArrayList <E> result = new ArrayList <E> ();
        for (final E item : elements) {
            result.add (item);
        }

        return result;
    }


    /**
     * Creates a new instance of a generic {@link ArrayList} and fills it with
     * elements from the given collections.
     *
     * @param <E>
     *      element type, inferred from the result type
     * @param collections
     *      the collections of elements to put into the list
     * @return
     *      new {@link ArrayList} instance
     */
    public static <E> List <E> newArrayList (
        final Collection <? extends E> ... collections
    ) {
        Assert.objectNotNull (collections, "collections");

        //

        final int collectionCount = collections.length;
        if (collectionCount > 0) {
            //
            // Aggregate collection sizes to determine the size of the
            // resulting ArrayList to avoid reallocations, then gather
            // all elements from the collections.
            //
            int elementCount = 0;
            for (final Collection <? extends E> collection : collections) {
                elementCount += collection.size ();
            }

            final ArrayList <E> result = new ArrayList <E> (elementCount);
            for (final Collection <? extends E> collection : collections) {
                result.addAll (collection);
            }

            return result;

        } else {
            return Collections.emptyList ();
        }
    }

    /* ***********************************************************************
     * LinkedList
     * ***********************************************************************/

    /**
     * Creates a new instance of a generic {@link LinkedList}.
     *
     * @param <E>
     *      element type, inferred from the result type
     * @return
     *      new {@link LinkedList} instance
     */
    public static <E> LinkedList <E> newLinkedList () {
        return new LinkedList <E> ();
    }


    /**
     * Creates a new instance of a generic {@link LinkedList} and fills it with
     * elements from the given array.
     *
     * @param <E>
     *      element type, inferred from the result type
     * @param elements
     *      the array of elements to put into the list, may not be {@code null}
     * @return
     *      new {@link LinkedList} instance
     * @throws NullPointerException
     *      if the specified array is {@code null}
     */
    public static <E> LinkedList <E> newLinkedList (final E ... elements) {
        Assert.objectNotNull (elements, "elements");

        //
        return __addAllToList (elements, new LinkedList <E> ());
    }


    /**
     * Creates a new instance of a generic {@link LinkedList} and fills it with
     * elements from the given collection.
     *
     * @param <E>
     *      element type, inferred from the result type
     * @param elements
     *      the collection of elements to put into the list, may not be
     *      {@code null}
     * @return
     *      new {@link LinkedList} instance of appropriate type
     * @throws NullPointerException
     *      if the specified collection is {@code null}
     */
    public static <E> LinkedList <E> newLinkedList (
        final Collection <? extends E> elements
    ) {
        Assert.objectNotNull (elements, "elements");

        //

        return new LinkedList <E> (elements);
    }

    //

    private static <E, L extends List <E>> L __addAllToList (
        final E [] elements, final L result
    ) {
        for (final E element : elements) {
            result.add (element);
        }

        return result;
    }

}
