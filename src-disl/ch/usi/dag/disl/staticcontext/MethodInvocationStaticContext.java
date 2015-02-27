package ch.usi.dag.disl.staticcontext;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import ch.usi.dag.disl.marker.BytecodeMarker;


/**
 * Represents a static context for method invocations. Provides information
 * related to a method invocation at a particular call site.
 * <p>
 * <b>Note:</b> This context can only be used with the {@link BytecodeMarker}
 * triggering on method invocation instructions, i.e., INVOKESTATIC,
 * INVOKEVIRTUAL, INVOKESPECIAL, and INVOKEINTERFACE. If you are not sure
 * whether the context can be used, use the {@link #isValid()} method to check
 * if the context is valid.
 *
 * @author Aibek Sarimbekov
 * @author Lubomir Bulej
 */
final class MethodInvocationStaticContext extends AbstractStaticContext {

    public MethodInvocationStaticContext () {
        // invoked by DiSL
    }

    /**
     * @return {@code True} if the context is valid.
     */
    public boolean isValid () {
        return staticContextData.getRegionStart () instanceof MethodInsnNode;
    }


    public String invocationTarget () {
        final MethodInsnNode node = __methodInsnNode ();
        return String.format (
            "%s.%s%s", node.owner, node.name, node.desc
        );
    }

    /**
     * @return The name of the method being invoked.
     */
    public String getName () {
        return __methodInsnNode ().name;
    }


    /**
     * @return The descriptor of the method being invoked.
     */
    public String getDescriptor () {
        return __descriptor ();
    }


    /**
     * @return The return type descriptor of the method being invoked.
     */
    public String getReturnTypeDescriptor () {
        return Type.getReturnType (__descriptor ()).getDescriptor ();
    }


    /**
     * @return The internal name of the invoked method's owner.
     */
    public String getOwnerInternalName () {
        return __methodInsnNode ().owner;
    }

    //

    private String __descriptor () {
        return __methodInsnNode ().desc;
    }

    private MethodInsnNode __methodInsnNode () {
        //
        // This will throw an exception when used in a region that does not
        // start with a method invocation instruction.
        //
        return ((MethodInsnNode) staticContextData.getRegionStart ());
    }

}
