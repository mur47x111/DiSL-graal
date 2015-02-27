package ch.usi.dag.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Utility class providing generic static factory methods for creating instances
 * of {@link Map} collection types. The factory methods rely on type inference
 * to determine the type parameters needed for constructing a specific instance.
 *
 * @author Lubomir Bulej
 */
public class Maps {

    private Maps () {
        // pure static class - not to be instantiated
    }


    /* ***********************************************************************
     * HashMap
     * ***********************************************************************/

    /**
     * Creates a new instance of a generic {@link HashMap}.
     *
     * @see HashMap#HashMap()
     *
     * @param <K>
     *      the type of the key
     * @param <V>
     *      the type of the value
     * @return
     *      new instance of HashMap <K, V>
     */
    public static <K, V> HashMap <K, V> newHashMap () {
        return new HashMap <K, V> ();
    }


    /**
     * Creates a new instance of a generic {@link HashMap} using
     * mappings provided by source {@link Map}.
     *
     * @see HashMap#HashMap(Map)
     *
     * @param <K>
     *      the type of the key
     * @param <V>
     *      the type of the value
     * @return
     *      new instance of HashMap <K, V>
     */
    public static <K, V> HashMap <K, V> newHashMap (final Map <K, V> source) {
        return new HashMap <K, V> (source);
    }


    /* ***********************************************************************
     * LinkedHashMap
     * ***********************************************************************/

    /**
     * Creates a new instance of a generic {@link LinkedHashMap}.
     *
     * @see LinkedHashMap#LinkedHashMap()
     *
     * @param <K>
     *      the type of the key
     * @param <V>
     *      the type of the value
     * @return
     *      new instance of LinkedHashMap <K, V>
     */
    public static <K, V> LinkedHashMap <K, V> newLinkedHashMap () {
        return new LinkedHashMap <K, V> ();
    }


    /* ***********************************************************************
     * ConcurrentHashMap
     * ***********************************************************************/

    /**
     * Creates a new instance of a generic {@link ConcurrentHashMap}.
     *
     * @see ConcurrentHashMap#ConcurrentHashMap()
     *
     * @param <K>
     *      the type of the key
     * @param <V>
     *      the type of the value
     * @return
     *      new instance of ConcurrentHashMap <K, V>
     */
    public static <K, V> ConcurrentHashMap <K, V> newConcurrentHashMap () {
        return new ConcurrentHashMap <K, V> ();
    }

}
