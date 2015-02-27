package ch.usi.dag.disl.test.suite.gettarget.instr;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import ch.usi.dag.disl.staticcontext.AbstractStaticContext;

public class GetTargetAnalysis extends AbstractStaticContext {

	public boolean isCalleeStatic() {

		AbstractInsnNode instr = staticContextData.getRegionStart();

		return instr.getOpcode() == Opcodes.INVOKESTATIC;
	}

	public int calleeParCount() {

		AbstractInsnNode instr = staticContextData.getRegionStart();

		if (!(instr instanceof MethodInsnNode)) {
			return 0;
		}

		return Type.getArgumentTypes(((MethodInsnNode) instr).desc).length;
	}
}
