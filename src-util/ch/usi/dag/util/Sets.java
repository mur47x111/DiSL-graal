package ch.usi.dag.util;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;


/**
 * Utility class providing generic static factory methods for creating instances
 * of {@link Set} collection types. The factory methods rely on type inference
 * to determine the type parameters needed for constructing a specific instance.
 *
 * @author Lubomir Bulej
 */
public final class Sets {

    private Sets () {
        // pure static class - not to be instantiated
    }


    /* ***********************************************************************
     * HashSet
     * ***********************************************************************/

    /**
     * Creates a new instance of a generic {@link HashSet}.
     *
     * @param <E>
     *      the type of the set element
     * @return
     *      new {@link HashSet} instance of appropriate type
     */
    public static <E> HashSet <E> newHashSet () {
        return new HashSet <E> ();
    }


    /**
     * Creates a new instance of a generic {@link HashSet} and fills it
     * {@code elements} from the given array.
     *
     * @param <E>
     *      element type, inferred from the result type
     * @param elements
     *      the elements to put into the set
     * @return
     *      new {@link HashSet} instance containing the given elements
     */
    public static <E> HashSet <E> newHashSet (final E ... elements) {
        Assert.objectNotNull (elements, "elements");

        //
        return __addAllToSet (elements, new HashSet <E> (elements.length));
    }


    /* ***********************************************************************
     * LinkedHashSet
     * ***********************************************************************/

    /**
     * Creates a new instance of a generic {@link LinkedHashSet}.
     *
     * @param <E>
     *      the type of the set element
     * @return
     *      new {@link LinkedHashSet} instance of appropriate type
     */
    public static <E> LinkedHashSet <E> newLinkedHashSet () {
        return new LinkedHashSet <E> ();
    }


    /* ***********************************************************************
     * TreeSet
     * ***********************************************************************/

    /**
     * Creates a new instance of a generic {@link TreeSet}.
     *
     * @param <E>
     *      the type of the set element
     * @return
     *      new {@link TreeSet} instance of appropriate type
     */
    public static <E> TreeSet <E> newTreeSet () {
        return new TreeSet <E> ();
    }

    //

    private static <E, L extends Set <E>> L __addAllToSet (
        final E [] elements, final L result
    ) {
        for (final E element : elements) {
            result.add (element);
        }

        return result;
    }

}
