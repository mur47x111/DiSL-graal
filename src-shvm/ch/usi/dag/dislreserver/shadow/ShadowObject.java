package ch.usi.dag.dislreserver.shadow;

import java.util.Formattable;
import java.util.Formatter;

public class ShadowObject implements Formattable {

    final private long netRef;
    final private long shadowId;
    final private ShadowClass shadowClass;

    private Object shadowState;

    //


    ShadowObject (final long netReference, final ShadowClass shadowClass) {
        this.netRef = netReference;
        this.shadowId = NetReferenceHelper.get_object_id (netReference);
        this.shadowClass = shadowClass;
        this.shadowState = null;
    }

    //

    public long getNetRef () {
        return netRef;
    }

    public long getId () {
        return shadowId;
    }

    public ShadowClass getShadowClass() {

        if (shadowClass != null) {
            return shadowClass;
        } else {

            if (equals(ShadowClassTable.BOOTSTRAP_CLASSLOADER)) {
                throw new NullPointerException();
            }

            return ShadowClassTable.JAVA_LANG_CLASS;
        }
    }

    public synchronized Object getState () {
        return shadowState;
    }


    public synchronized <T> T getState (final Class <T> type) {
        return type.cast (shadowState);
    }


    public synchronized void setState (final Object shadowState) {
        this.shadowState = shadowState;
    }

    public synchronized Object setStateIfAbsent(Object shadowState) {

        Object retVal = this.shadowState;

        if (retVal == null) {
            this.shadowState = shadowState;
        }

        return retVal;
    }

    // only object id considered
    // TODO consider also the class ID
    @Override
    public int hashCode() {
        return 31 + (int) (shadowId ^ (shadowId >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ShadowObject) {
            final ShadowObject that = (ShadowObject) obj;
            return this.netRef == that.netRef;
        }

        return false;
    }

    //

    @Override
    public void formatTo (
        final Formatter formatter,
        final int flags, final int width, final int precision
    ) {
        formatter.format ("%s@%x", (shadowClass != null) ? 
            shadowClass.getName () : "<missing>", shadowId
        );
    }

}
