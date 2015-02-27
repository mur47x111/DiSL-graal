package ch.usi.dag.disl.weaver;

import java.util.Collections;
import java.util.Comparator;

import org.objectweb.asm.commons.TryCatchBlockSorter;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import ch.usi.dag.disl.exception.DiSLFatalException;
import ch.usi.dag.disl.util.AsmHelper;
import ch.usi.dag.disl.util.AsmHelper.Insns;

public class AdvancedSorter extends TryCatchBlockSorter {

    public AdvancedSorter(MethodNode method) {
        super(null, method.access, method.name, method.desc, method.signature,
                null);

        this.instructions = method.instructions;
        this.tryCatchBlocks = method.tryCatchBlocks;
    }

    public void validate() {

        TryCatchBlockNode[] tcbs = new TryCatchBlockNode[tryCatchBlocks.size()];
        tcbs = tryCatchBlocks.toArray(tcbs);

        for (int i = 0; i < tcbs.length; i++) {
            int istart = instructions.indexOf(Insns.FORWARD.firstRealInsn (tcbs[i].start));
            int iend = instructions.indexOf(tcbs[i].end);

            for (int j = i; j < tcbs.length; j++) {
                int jstart = instructions.indexOf(Insns.FORWARD.firstRealInsn (tcbs[j].start));
                int jend = instructions.indexOf(tcbs[j].end);

                if ((
                        AsmHelper.offsetBefore(instructions, istart, jstart) &&
                        AsmHelper.offsetBefore(instructions, jstart, iend) &&
                        AsmHelper.offsetBefore(instructions, iend, jend)
                    ) || (
                        AsmHelper.offsetBefore(instructions, jstart, istart) &&
                        AsmHelper.offsetBefore(instructions, istart, jend) &&
                        AsmHelper.offsetBefore(instructions, jend, iend)
                )) {
                    throw new DiSLFatalException ("Overlapping exception handler.");
                }
            }
        }
    }

    public void visitEnd() {
        // Compares TryCatchBlockNodes by the length of their "try" block.
        Comparator<TryCatchBlockNode> comp = new Comparator<TryCatchBlockNode>() {

            public int compare(TryCatchBlockNode t1, TryCatchBlockNode t2) {
                int len1 = blockLength(t1);
                int len2 = blockLength(t2);
                return len1 - len2;
            }

            private int blockLength(TryCatchBlockNode block) {
                int startidx = instructions.indexOf(Insns.FORWARD.firstRealInsn (block.start));
                int endidx = instructions.indexOf(block.end);
                return endidx - startidx;
            }
        };

        Collections.sort(tryCatchBlocks, comp);
    }

    public static void sort(MethodNode method) {
        AdvancedSorter sorter = new AdvancedSorter(method);
        sorter.visitEnd();
        sorter.validate();
    }
}
