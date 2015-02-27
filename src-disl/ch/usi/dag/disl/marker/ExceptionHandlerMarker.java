package ch.usi.dag.disl.marker;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import ch.usi.dag.disl.util.AsmHelper.Insns;
import ch.usi.dag.disl.util.cfg.CtrlFlowGraph;

/**
 * Marks an exception handler.
 * <p>
 * Sets the start at the beginning of an exception handler and the end at the
 * end of an exception handler.
 */
public class ExceptionHandlerMarker extends AbstractDWRMarker {

    @Override
    public List<MarkedRegion> markWithDefaultWeavingReg(MethodNode method) {

        List<MarkedRegion> regions = new LinkedList<MarkedRegion>();

        CtrlFlowGraph cfg = new CtrlFlowGraph(method);

        cfg.visit(method.instructions.getFirst());

        for (TryCatchBlockNode tcb : method.tryCatchBlocks) {

            List<AbstractInsnNode> exits = cfg.visit(tcb.handler);
            regions.add(new MarkedRegion(
                Insns.FORWARD.firstRealInsn (tcb.handler), exits
            ));
        }

        return regions;
    }

}
