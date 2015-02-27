package ch.usi.dag.disl.localvar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public final class LocalVars {

    private final Map <String, SyntheticLocalVar> __syntheticLocals;

    private final Map <String, ThreadLocalVar> __threadLocals;

    //

    public LocalVars () {
        __syntheticLocals = new HashMap <String, SyntheticLocalVar> ();
        __threadLocals = new HashMap <String, ThreadLocalVar> ();
    }

    //

    public void put (final SyntheticLocalVar slv) {
        __syntheticLocals.put (slv.getID (), slv);
    }

    public Map <String, SyntheticLocalVar> getSyntheticLocals () {
        return Collections.unmodifiableMap (__syntheticLocals);
    }


    public void put (final ThreadLocalVar tlv) {
        __threadLocals.put (tlv.getID (), tlv);
    }

    public Map <String, ThreadLocalVar> getThreadLocals () {
        return Collections.unmodifiableMap (__threadLocals);
    }

    //

    public void putAll (final LocalVars other) {
        __syntheticLocals.putAll (other.__syntheticLocals);
        __threadLocals.putAll (other.__threadLocals);
    }

}
