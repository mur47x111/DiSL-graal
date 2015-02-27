package ch.usi.dag.dislreserver.shadow;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import ch.usi.dag.dislreserver.DiSLREServerFatalException;

public class ShadowObjectTable {

    private static final int INITIAL_TABLE_SIZE = 10_000_000;
    
    private static ConcurrentHashMap<Long, ShadowObject> 
        shadowObjects = new ConcurrentHashMap<Long, ShadowObject>(INITIAL_TABLE_SIZE);

    //

    public static void register(ShadowObject newObj, boolean debug) {

        if (newObj == null) {
            throw new DiSLREServerFatalException(
                    "Attempting to register a null as a shadow object");
        }

        long objID = newObj.getId();
        ShadowObject exist = shadowObjects.putIfAbsent(objID, newObj);

        if (exist != null) {

            if (newObj.getId() == exist.getId()) {
                if (debug) {
                    System.out.println("Re-register a shadow object.");
                }

                if (newObj.equals(exist)) {
                    return;
                }

                if (newObj instanceof ShadowString) {

                    if (exist instanceof ShadowString) {

                        ShadowString existShadowString = (ShadowString) exist;
                        ShadowString newShadowString = (ShadowString) newObj;

                        if (existShadowString.toString() == null) {
                            existShadowString.setValue(newShadowString
                                    .toString());
                            return;
                        }
                    }
                } else if (newObj instanceof ShadowThread) {

                    if (exist instanceof ShadowThread) {

                        ShadowThread existShadowThread = (ShadowThread) exist;
                        ShadowThread newShadowThread = (ShadowThread) newObj;

                        if (existShadowThread.getName() == null) {
                            existShadowThread
                                    .setName(newShadowThread.getName());
                            existShadowThread.setDaemon(newShadowThread
                                    .isDaemon());
                            return;
                        }
                    }
                }
            }

            throw new DiSLREServerFatalException("Duplicated net reference");
        }
    }

    private static boolean isAssignableFromThread(ShadowClass klass) {

        while (! "java.lang.Object".equals(klass.getName())) {

            if ("java.lang.Thread".equals(klass.getName())) {
                return true;
            }

            klass = klass.getSuperclass();
        }

        return false;
    }

    public static ShadowObject get(long net_ref) {

        long objID = NetReferenceHelper.get_object_id(net_ref);

        if (objID == 0) {
            // reserved ID for null
            return null;
        }

        ShadowObject retVal = shadowObjects.get(objID);

        if (retVal != null) {
            return retVal;
        }

        if (NetReferenceHelper.isClassInstance(objID)) {
            throw new DiSLREServerFatalException("Unknown class instance");
        } else {
            // Only common shadow object will be generated here
            ShadowClass klass = ShadowClassTable.get(NetReferenceHelper
                    .get_class_id(net_ref));
            ShadowObject tmp = null;

            if ("java.lang.String".equals(klass.getName())) {
                tmp = new ShadowString(net_ref, null, klass);
            } else if (isAssignableFromThread(klass)) {
                tmp = new ShadowThread(net_ref, null, false, klass);
            } else {
                tmp = new ShadowObject(net_ref, klass);
            }

            if ((retVal = shadowObjects.putIfAbsent(objID, tmp)) == null) {
                retVal = tmp;
            }

            return retVal;
        }
    }

    public static void freeShadowObject(ShadowObject obj) {
        shadowObjects.remove(obj.getId());
        ShadowClassTable.freeShadowObject(obj);
    }

    //TODO: find a more elegant way to allow users to traverse the shadow object table
    public static Iterator<Entry<Long, ShadowObject>> getIterator() {
        return shadowObjects.entrySet().iterator();
    }

    public static Iterable <ShadowObject> objects () {
        return new Iterable <ShadowObject>() {
            @Override
            public Iterator <ShadowObject> iterator () {
                return shadowObjects.values ().iterator ();
            }
        };
    }
}
