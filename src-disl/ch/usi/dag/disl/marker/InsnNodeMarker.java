package ch.usi.dag.disl.marker;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.exception.MarkerException;
import ch.usi.dag.disl.util.AsmHelper.Insns;

/**
 * Marks bytecode instructions depending on the ASM class type.
 * <p>
 * <b>Note:</b> This class is work in progress.
 */
public class InsnNodeMarker extends AbstractInsnMarker {

    protected Set<Class<? extends AbstractInsnNode>> classes;

    public InsnNodeMarker(Parameter param)
            throws MarkerException {

        classes = new HashSet<Class<? extends AbstractInsnNode>>();

        // translate all instructions to opcodes
        for (String className : param.getMultipleValues()) {

            try {

                Class<?> clazz = Class.forName(className);
                classes.add(clazz.asSubclass(AbstractInsnNode.class));
            } catch (ClassNotFoundException e) {

                throw new MarkerException("Instruction Node Class \""
                        + className + "\" cannot be found.");
            } catch (ClassCastException e) {

                throw new MarkerException("Class \"" + className
                        + "\" is not an instruction node class.");
            }
        }

        if (classes.isEmpty()) {
            throw new MarkerException(
                    "Instruction node class should be passed as a parameter.");
        }
    }

    @Override
    public List<AbstractInsnNode> markInstruction(MethodNode methodNode) {

        List<AbstractInsnNode> seleted = new LinkedList<AbstractInsnNode>();

        for (AbstractInsnNode instr : Insns.selectAll (methodNode.instructions)) {

            for (Class<? extends AbstractInsnNode> clazz : classes) {

                if (clazz.isInstance(instr)) {
                    seleted.add(instr);
                }
            }
        }

        return seleted;
    }

}
