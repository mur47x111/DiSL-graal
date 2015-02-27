package ch.usi.dag.util;

import java.lang.reflect.Array;
import java.util.Collection;


/**
 * Utility class providing static methods for common assertions on objects from
 * the Java runtime, i.e. the java.* packages. These methods are intended for
 * use at internal interface boundaries to document and check contract
 * assumptions, but not enforce them. For enforcement, use methods from the
 * {@link Require} class.
 *
 * @author Lubomir Bulej
 */
public final class Assert {

    /**
     * Represents the state of runtime assertions.
     */
    public static final boolean ENABLED = __assertionsEnabled ();

    private static boolean __assertionsEnabled () {
        //
        // Determine the state of runtime assertions. Try to force an
        // assertion error and if it succeeds, we know that assertions are
        // enabled.
        //
        try {
            assert false;
            return false;

        } catch (final AssertionError ae) {
            return true;
        }
    }

    //

    private Assert () {
        // pure static class - not to be instantiated
    }


    /* ***********************************************************************
     * Integers
     * ***********************************************************************/

    public static void valueNotNegative (final long value, final String name) {
        assert value >= 0 : name +" is negative ("+ value +")";
    }


    public static void valueIsPositive (final long value, final String name) {
        assert value > 0 : name +" is not positive ("+ value +")";
    }


    /* ***********************************************************************
     * Generic Objects
     * ***********************************************************************/

    public static void objectNotNull (final Object object, final String name) {
        assert object != null : name + " is null";
    }


    public static void objectIsArray (final Object object, final String name) {
        Assert.objectNotNull (object, name);
        assert object.getClass ().isArray () : name +" is not an array";
    }


    /* ***********************************************************************
     * Arrays
     * ***********************************************************************/

    public static void arrayNotEmpty (final int [] array, final String name) {
        Assert.objectNotNull (array, name);
        __arrayNotEmpty (array.length, name);
    }


    public static void arrayNotEmpty (final Object array, final String name) {
        Assert.objectIsArray (array, name);
        __arrayNotEmpty (Array.getLength (array), name);

    }

    private static void __arrayNotEmpty (final int length, final String name) {
        assert length > 0 : name +" array is empty";
    }


    /* ***********************************************************************
     * Collections
     * ***********************************************************************/

    public static void collectionNotEmpty (
        final Collection <?> collection, final String name
    ) {
        Assert.objectNotNull (collection, name);
        assert collection.size () > 0 : name +" collection is empty";
    }


    /* ***********************************************************************
     * Strings
     * ***********************************************************************/

    public static void stringNotEmpty (final String string, final String name) {
        Assert.objectNotNull (string, name);
        assert !string.isEmpty() : name + " is empty";
    }


    /* ***********************************************************************
     * Locations
     * ***********************************************************************/

    public static void unreachable () {
        throw new AssertionError ("unreachable code reached");
    }


    public static void unreachable (final String message) {
        throw new AssertionError (message);
    }

}
