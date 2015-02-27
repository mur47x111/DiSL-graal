package ch.usi.dag.dislreserver.remoteanalysis;

import ch.usi.dag.dislreserver.shadow.ShadowObject;

/**
 * Each analysis evaluated remotely have to implement this interface.
 *
 * The method arguments can be only basic types, String, Object and Class.
 * See additional restrictions below.
 *
 * There is a special handling for class literals. If the class literal should
 * be transmitted for evaluation, then the analysis method has to contain
 * int argument after Class argument representing class id.
 *
 * On the server, the Class argument will contain InvalidClass.class and the
 * class id argument will hold proper id value.
 *
 * On the client, the int argument should not be transmitted (should be ignored).
 * Valid only in the case, that transmission is done manually.
 *
 * Object argument on the client will contain NetReference instnace.
 */
public abstract class RemoteAnalysis {

    public abstract void atExit();

    public abstract void objectFree(ShadowObject netRef);
}
