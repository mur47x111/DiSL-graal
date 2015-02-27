package ch.usi.dag.disl.staticcontext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;

import ch.usi.dag.disl.snippet.Shadow;
import ch.usi.dag.disl.util.Insn;
import ch.usi.dag.disl.util.cfg.CtrlFlowGraph;

/**
 * <b>NOTE: This class is work in progress</b>
 * <br>
 * <br>
 * Provides static context information about instrumented basic block.
 */
public class BasicBlockStaticContext extends AbstractStaticContext {

    private Map<String, CtrlFlowGraph> cache = new HashMap<String, CtrlFlowGraph>();
    protected CtrlFlowGraph customData;

    public void staticContextData (final Shadow shadow) {
        super.staticContextData (shadow);

        String key = staticContextData.getClassNode().name
                + staticContextData.getMethodNode().name
                + staticContextData.getMethodNode().desc;

        customData = cache.get(key);
        if (customData == null) {
            customData = produceCustomData();
            cache.put(key, customData);
        }
    }

    /**
     * Returns total number of basic blocks in a method.
     */
    public int getTotBBs() {
        return customData.getNodes().size();
    }

    /**
     * Returns the size of the instrumented basic block.
     */
    public int getBBSize() {
        //
        // If the start instruction is also an end instruction,
        // then the size of the basic block is 1 instruction.
        //
        int count = 1;
        final List <AbstractInsnNode> ends = staticContextData.getRegionEnds ();

        for (
            AbstractInsnNode insn = staticContextData.getRegionStart ();
            !ends.contains (insn);
            insn = insn.getNext ()
        ) {
            count += Insn.isVirtual (insn) ? 0 : 1;
        }

        return count;
    }

    /**
     * Returns index of the instrumented basic block.
     */
    public int getBBindex() {
        return customData.getIndex(staticContextData.getRegionStart());
    }

    protected CtrlFlowGraph produceCustomData() {
        return new CtrlFlowGraph(staticContextData.getMethodNode());
    }
}
