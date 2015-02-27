package ch.usi.dag.disl.coderep;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.classparser.ContextKind;


/**
 * Captures the set of context kinds and static context types referenced by
 * parameters of a particular method. This class is <b>immutable</b>.
 */
final class ContextUsage {

    /** An unmodifiable set of used contexts. */
    private final Set <ContextKind> __contextKinds;

    /** An unmodifiable set of referenced static context types. */
    private final Set <Type> __contextTypes;

    //

    private ContextUsage (
        final Set <ContextKind> usedContexts, final Set <Type> staticContexts
    ) {
        __contextKinds = Collections.unmodifiableSet (usedContexts);
        __contextTypes = Collections.unmodifiableSet (staticContexts);
    }

    //

    /**
     * @return An unmodifiable set of used context kinds.
     */
    public Set <ContextKind> usedContextKinds () {
        return __contextKinds;
    }

    /**
     * @return An unmodifiable set of used static context types.
     */
    public Set <Type> staticContextTypes () {
        return __contextTypes;
    }

    //

    public static ContextUsage forMethod (final MethodNode method) {
        //
        // Collect the kinds of contexts appearing in the arguments as well as
        // the types of static contexts.
        //
        final EnumSet <ContextKind> usedContexts = EnumSet.noneOf (ContextKind.class);
        final Set <Type> staticContextTypes = new HashSet <Type> ();

        for (final Type argType : Type.getArgumentTypes (method.desc)) {
            final ContextKind contextKind = ContextKind.forType (argType);
            if (contextKind != null) {
                usedContexts.add (contextKind);
                if (contextKind == ContextKind.STATIC) {
                    staticContextTypes.add (argType);
                }
            }
        }

        return new ContextUsage (usedContexts, staticContextTypes);
    }

}
