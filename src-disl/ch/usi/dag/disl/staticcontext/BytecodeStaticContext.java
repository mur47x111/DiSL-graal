package ch.usi.dag.disl.staticcontext;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * <b>NOTE: This class is work in progress</b>
 * <br>
 * <br>
 * Provides static context information about instrumented bytecode.
 */
public class BytecodeStaticContext extends AbstractStaticContext {

	/**
	 * Returns (ASM) integer number of the instrumented bytecode.
	 */
	public int getBytecodeNumber() {

		return staticContextData.getRegionStart().getOpcode();
	}

	public int getBCI() {
	    return staticContextData.getRegionStart().getOffset ();
	}

	public String bci() {
        final StringBuilder builder = new StringBuilder();

        final ClassNode classNode = staticContextData.getClassNode();
        final MethodNode methodNode = staticContextData.getMethodNode();
        final AbstractInsnNode instruction = staticContextData.getRegionStart();

        builder.append(classNode.name.replace('/', '.'));
        builder.append('.');
        builder.append(methodNode.name);
        builder.append(methodNode.desc);
        builder.append('@');
        builder.append(instruction.getOffset());

        return builder.toString();
    }


}
