package ch.usi.dag.dislreserver.shadow;

public class NetReferenceHelper {
    // ************* special bit mask handling methods **********

    // NOTE names of the methods are unusual for reason
    // you can find almost identical methods in agent

    // should be in sync with net_reference functions on the client

    // format of net reference looks like this
    // HIGHEST (1 bit spec, 23 bits class id, 40 bits object id)
    // bit field not used because there is no guarantee of alignment

    private static final short OBJECT_ID_POS = 0;
    private static final short CLASS_ID_POS = 40;
    private static final short SPEC_POS = 63;
    private static final short CBIT_POS = 62;

    private static final long OBJECT_ID_MASK = 0xFFFFFFFFFFL;
    private static final long CLASS_ID_MASK = 0x3FFFFFL;
    private static final long SPEC_MASK = 0x1L;
    private static final long CBIT_MASK = 0x1L;

    // get bits from "from" with pattern "bit_mask" lowest bit starting on
    // position
    // "low_start" (from 0)
    private static long get_bits(long from, long bit_mask, short low_start) {

        // shift it
        long bits_shifted = from >> low_start;

        // mask it
        return bits_shifted & bit_mask;
    }

    public static long get_object_id(long net_ref) {

        return get_bits(net_ref, OBJECT_ID_MASK, OBJECT_ID_POS);
    }

    public static int get_class_id(long net_ref) {

        return (int) get_bits(net_ref, CLASS_ID_MASK, CLASS_ID_POS);
    }

    public static short get_spec(long net_ref) {

        return (short) get_bits(net_ref, SPEC_MASK, SPEC_POS);
    }

    public static boolean isClassInstance(long net_ref) {

        return get_bits(net_ref, CBIT_MASK, CBIT_POS) != 0;
    }

}
