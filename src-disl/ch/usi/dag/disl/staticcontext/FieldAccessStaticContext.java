package ch.usi.dag.disl.staticcontext;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;

import ch.usi.dag.disl.marker.BytecodeMarker;


/**
 * Represents a field access static context. Provides field's name and type
 * descriptor, along with the internal name of the field's owner class.
 * <p>
 * <b>Note:</b> This context can only be used with {@link BytecodeMarker}
 * triggering on field access instructions, i.e., GETFIELD, PUTFIELD, GETSTATIC,
 * and PUTSTATIC. If you are not sure whether the context can be used, use the
 * {@link #isValid()} method to check if the context is valid.
 *
 * @author Aibek Sarimbekov
 * @author Lubomir Bulej
 */
public final class FieldAccessStaticContext extends AbstractStaticContext {

    public FieldAccessStaticContext () {
        // invoked by DiSL
    }


    /**
     * @return {@code True} if the context is valid.
     */
    public boolean isValid () {
        return staticContextData.getRegionStart () instanceof FieldInsnNode;
    }


    /**
     * @return The field's name.
     */
    public String getName () {
        return __getFieldInsnNode ().name;
    }


    /**
     * @return The field's type descriptor.
     */
    public String getDescriptor () {
        return __getFieldInsnNode ().desc;
    }


    /**
     * @return The name of the field's owner class.
     */
    public String getOwnerClassName () {
        return Type.getObjectType (__getFieldInsnNode ().owner).getClassName ();
    }


    /**
     * @return The internal name of the field's owner class.
     */
    public String getOwnerInternalName () {
        return __getFieldInsnNode ().owner;
    }

    //

    private FieldInsnNode __getFieldInsnNode () {
        //
        // This will throw an exception when used in a region that does not
        // start with a field access instruction.
        //
        return (FieldInsnNode) staticContextData.getRegionStart ();
    }

}
