package ch.usi.dag.disl.staticcontext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import ch.usi.dag.disl.util.cfg.BasicBlock;
import ch.usi.dag.disl.util.cfg.CtrlFlowGraph;

/**
 * <b>NOTE: This class is work in progress</b>
 * <br>
 * <br>
 * Provides static context information about instrumented instruction.
 */
public class LoopStaticContext extends BasicBlockStaticContext {

	private Map<BasicBlock, Set<BasicBlock>> dominatormapping;

	@Override
	protected CtrlFlowGraph produceCustomData() {

		MethodNode method = staticContextData.getMethodNode();
		CtrlFlowGraph cfg = CtrlFlowGraph.build(method);

		dominatormapping = new HashMap<BasicBlock, Set<BasicBlock>>();

		Set<BasicBlock> entries = new HashSet<BasicBlock>();
		entries.add(cfg.getBB(method.instructions.getFirst()));

		for (TryCatchBlockNode tcb : method.tryCatchBlocks) {
			entries.add(cfg.getBB(tcb.handler));
		}

		for (BasicBlock bb : cfg.getNodes()) {

			Set<BasicBlock> dominators = new HashSet<BasicBlock>();

			if (entries.contains(bb)) {
				dominators.add(bb);
			} else {
				dominators.addAll(cfg.getNodes());
			}

			dominatormapping.put(bb, dominators);
		}

		// whether the dominators of any basic block is changed
		boolean changed;

		// loop until no more changes
		do {
			changed = false;

			for (BasicBlock bb : cfg.getNodes()) {

				if (entries.contains(bb)) {
					continue;
				}

				Set<BasicBlock> dominators = dominatormapping.get(bb);
				dominators.remove(bb);

				// update the dominators of current basic block,
				// contains only the dominators of its predecessors
				for (BasicBlock predecessor : bb.getPredecessors()) {

					if (dominators.retainAll(dominatormapping.get(predecessor))) {
						changed = true;
					}
				}

				dominators.add(bb);
			}
		} while (changed);

		return cfg;
	}

	/**
	 * Returns true if the instrumented instruction is start of a loop.
	 */
	public boolean isFirstOfLoop() {

		BasicBlock entry = customData.getBB(staticContextData
				.getRegionStart());

		for (BasicBlock bb : entry.getPredecessors()) {
			if (dominatormapping.get(bb).contains(entry)) {
				return true;
			}
		}

		return false;
	}

}
