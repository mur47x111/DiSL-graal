package ch.usi.dag.disl.test.suite.afterinit1.app;

import ch.usi.dag.disl.marker.AfterInitBodyMarker;


/**
 * Tests that the {@link AfterInitBodyMarker} correctly marks method body
 * starting after a call to the superclass constructor. The {@link Before}
 * snippet should be placed before the constructor body, but after the call to
 * the superclass constructor, while the {@link After} snippet should be placed
 * after the constructor body.
 */
public class TargetClass {

    public TargetClass () {
        System.out.println ("TargetClass constructor begin");
        System.out.println ("TargetClass constructor end");
    }


    static class TargetSubclass extends TargetClass {

        public TargetSubclass () {
            System.out.println ("TargetSubclass constructor begin");
            System.out.println ("TargetSubclass constructor end");
        }


        public void method () {
            System.out.println ("TargetSubclass method");
        }
    }

    protected final String _thisClassName () {
        return this.getClass ().getSimpleName ();
    }


    public static void main (final String [] args) {
        final TargetSubclass t = new TargetSubclass ();
        t.method ();
    }
}
