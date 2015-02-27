package ch.usi.dag.disl.marker;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.util.AsmHelper.Insns;
import ch.usi.dag.disl.util.Constants;

/**
 * Marks object creation.
 * <p>
 * Sets the start before new instruction and the end after the constructor
 * invocation.
 * <p>
 * <b>Note:</b> This class is work in progress.
 */
public class NewObjMarker extends AbstractDWRMarker {

    // NOTE: does not work for arrays

    @Override
    public List<MarkedRegion> markWithDefaultWeavingReg(MethodNode method) {

        List<MarkedRegion> regions = new LinkedList<MarkedRegion>();
        int invokedNews = 0;

        // find invocation of constructor after new instruction
        for (AbstractInsnNode instruction : Insns.selectAll (method.instructions)) {

            // track new instruction
            if (instruction.getOpcode() == Opcodes.NEW) {

                ++invokedNews;
            }

            // if it is invoke special and there are new pending
            if (instruction.getOpcode() == Opcodes.INVOKESPECIAL
                    && invokedNews > 0) {

                MethodInsnNode min = (MethodInsnNode) instruction;

                if (Constants.isConstructorName (min.name)) {
                    regions.add(new MarkedRegion(instruction, instruction));
                }
            }
        }

        return regions;
    }

}
