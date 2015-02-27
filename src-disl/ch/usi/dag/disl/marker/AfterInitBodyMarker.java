package ch.usi.dag.disl.marker;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.snippet.Shadow.WeavingRegion;
import ch.usi.dag.disl.util.AsmHelper;
import ch.usi.dag.disl.util.AsmHelper.Insns;
import ch.usi.dag.disl.util.Constants;


/**
 * Marks a method body. This marker can be safely used with constructors.
 * <p>
 * For normal methods, the {@link Before} snippets will be inserted before the
 * first instruction of a method. However, for constructors, they will be
 * inserted before the first instruction following a call to the superclass
 * constructor.
 */
// TODO LB: I believe this should be the default BodyMarker
// TODO LB: Consequently, current BodyMarker should be RawBodyMarker
public class AfterInitBodyMarker extends AbstractMarker {

    @Override
    public List <MarkedRegion> mark (final MethodNode method) {
        final MarkedRegion region = new MarkedRegion (
            __findBodyStart (method)
        );

        //
        // Add all RETURN instructions as marked-region ends.
        //
        for (final AbstractInsnNode insn : Insns.selectAll (method.instructions)) {
            if (AsmHelper.isReturn (insn)) {
                region.addEnd (insn);
            }
        }

        final WeavingRegion wr = region.computeDefaultWeavingRegion (method);
        wr.setAfterThrowEnd (method.instructions.getLast ());
        region.setWeavingRegion (wr);

        //

        final List <MarkedRegion> result = new LinkedList <MarkedRegion> ();
        result.add (region);
        return result;
    }


    //
    // Finds the first instruction of a method body. For normal methods, this is
    // the first instruction of a method, but for constructor, this is the first
    // instruction after a call to the superclass constructor.
    //
    private static AbstractInsnNode __findBodyStart (final MethodNode method) {
        //
        // Fast path for non-constructor methods.
        //
        if (!Constants.isConstructorName (method.name)) {
            return method.instructions.getFirst ();
        }

        //
        // ASM calls AdviceAdapter.onMethodEnter() at the beginning of a method
        // or after a call to the superclass constructor. Use a simple adapter
        // to detect the end of superclass initialization code.
        //
        final AtomicBoolean superInitialized = new AtomicBoolean (false);
        final AdviceAdapter adapter = new AdviceAdapter (
            Opcodes.ASM4, new MethodVisitor (Opcodes.ASM4) { /* empty */ },
            method.access, method.name, method.desc
        ) {
            @Override
            public void onMethodEnter () {
                superInitialized.set (true);
            }
        };

        //
        // Initialize the adapter and feed it with the method's instructions
        // until the superclass is considered initialized. The next instruction
        // after a call to the superclass constructor is the first instruction
        // of the constructor body.
        //
        adapter.visitCode ();
        for (final AbstractInsnNode node : AsmHelper.Insns.selectAll (method.instructions)) {
            node.accept (adapter);

            if (superInitialized.get ()) {
                return node.getNext ();
            }
        }

        //
        // If we get here, we are in the Object constructor (there is no
        // superclass) and ASM does not appear to call onMethodEnter() for
        // that constructor. We just return the first instruction.
        //
        return method.instructions.getFirst ();
    }

}
