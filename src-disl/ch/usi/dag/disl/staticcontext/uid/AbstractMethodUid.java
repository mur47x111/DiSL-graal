package ch.usi.dag.disl.staticcontext.uid;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.util.Constants;


public abstract class AbstractMethodUid extends AbstractUniqueId {

    private static AbstractUniqueId __instance__;

    //

    // constructor for static context
    public AbstractMethodUid () {
        super ();
    }

    // constructor for singleton
    protected AbstractMethodUid (
        final AbstractIdCalculator idCalc, final String outputFileName
    ) {
        super (idCalc, outputFileName);
    }

    //

    @Override
    protected final String idFor () {
        final ClassNode classNode = staticContextData.getClassNode ();
        final MethodNode methodNode = staticContextData.getMethodNode ();

        return
            classNode.name + Constants.CLASS_DELIM +
            methodNode.name +"("+ methodNode.desc +")";
    }

    @Override
    protected final synchronized AbstractUniqueId getSingleton () {
        if (__instance__ == null) {
            __instance__ = _getInstance ();
        }

        return __instance__;
    }

    //

    protected abstract AbstractUniqueId _getInstance ();

}
