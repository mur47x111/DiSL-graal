package ch.usi.dag.disl.test.suite.dispatch.instr;

import org.objectweb.asm.tree.AbstractInsnNode;

import ch.usi.dag.disl.staticcontext.AbstractStaticContext;

public class CodeLengthSC extends AbstractStaticContext {

	public int methodSize() {
		return staticContextData.getMethodNode().instructions.size();
	}
	
	public int codeSize() {
		
		AbstractInsnNode ain = staticContextData.getRegionStart();
		
		int size = 0;
		
		// count the size until the first end
		while(ain != null && ain != staticContextData.getRegionEnds().get(0)) {
			++size;
			ain = ain.getNext();
		}
		
		if(ain == null) {
			size = 0;
		}
		
		return size;
	}
}
