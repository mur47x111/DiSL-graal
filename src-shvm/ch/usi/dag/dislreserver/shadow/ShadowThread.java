package ch.usi.dag.dislreserver.shadow;

import java.util.Formattable;
import java.util.Formatter;

// TODO ShadowTrhead should better handle if String data are not send
//     over network - throw a runtime exception ??
public class ShadowThread extends ShadowObject implements Formattable {

    private String  name;
    private boolean isDaemon;

    public ShadowThread(long net_ref, String name, boolean isDaemon,
            ShadowClass klass) {
        super(net_ref, klass);

        this.name = name;
        this.isDaemon = isDaemon;
    }

    // TODO warn user that it will return null when the ShadowThread is not yet
    // sent.
    public String getName() {
        return name;
    }

    // TODO warn user that it will return false when the ShadowThread is not yet
    // sent.
    public boolean isDaemon() {
        return isDaemon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDaemon(boolean isDaemon) {
        this.isDaemon = isDaemon;
    }


    @Override
    public boolean equals (final Object object) {
        //
        // TODO LB: Why do we need to check thread name or other fields?
        //
        if (super.equals (object)) {
            if (object instanceof ShadowThread) {
                final ShadowThread that = (ShadowThread) object;
                if (this.name != null && this.name.equals (that.name)) {
                    return this.isDaemon == that.isDaemon;
                }
            }
        }

        return false;
    }


    @Override
    public int hashCode() {
        //
        // TODO LB: Check ShadowThread.hashCode() -- it's needed.
        //
        // If two shadow threads are considered equal by the above equals()
        // method, then they certainly have the same hash code, because it
        // is derived from objectId (which in turn is derived from object tag,
        // a.k.a. net reference) that is ensured to be equal by the call to
        // super.equals().
        //
        // If they are not equal, nobody cares about the hash code.
        //
        return super.hashCode ();
    }

    //

    @Override
    public void formatTo (
        final Formatter formatter,
        final int flags, final int width, final int precision
    ) { 
        super.formatTo (formatter, flags, width, precision);
        formatter.format (" <%s>", (name != null) ? name : "unknown");
    }

}
