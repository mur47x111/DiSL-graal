package ch.usi.dag.disl;

import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.CodeSizeEvaluator;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import ch.usi.dag.disl.dynamicbypass.BypassCheck;
import ch.usi.dag.disl.exception.DiSLFatalException;
import ch.usi.dag.disl.util.AsmHelper;


abstract class CodeMerger {

    private static final Type BPC_CLASS = Type.getType (BypassCheck.class);

    private static final String BPC_METHOD = "executeUninstrumented";

    private static final Type BPC_DESC = Type.getMethodType ("()Z");

    private static final int ALLOWED_SIZE = 64 * 1024; // 64KB limit


    // NOTE: the instCN ClassNode will be modified in the process
    // NOTE: abstract/native methods should not be included in changedMethods list
    public static void mergeOriginalCode (
        final Set <String> changedMethods, final ClassNode origCN,
        final ClassNode instCN
    ) {
        if (changedMethods == null) {
            throw new DiSLFatalException ("Set of changed methods cannot be null");
        }

        //
        // Select instrumented methods and merge into their code the original
        // (un-instrumented) method to be executed when the bypass is active.
        // Duplicate the original method code to preserve it for the case
        // where the resulting method is too long.
        //
        instCN.methods.parallelStream ().unordered ()
            .filter (instMN -> changedMethods.contains (instMN.name + instMN.desc))
            .forEach (instMN -> {
                final MethodNode cloneMN = AsmHelper.cloneMethod (
                    __findMethodNode (origCN, instMN.name, instMN.desc)
                );

                __createBypassCheck (
                    instMN.instructions, instMN.tryCatchBlocks,
                    cloneMN.instructions, cloneMN.tryCatchBlocks
                );
            });
    }


    private static void __createBypassCheck (
        final InsnList instCode, final List <TryCatchBlockNode> instTcbs,
        final InsnList origCode, final List <TryCatchBlockNode> origTcbs
    ) {
        // The bypass check code has the following layout:
        //
        //     if (!BypassCheck.executeUninstrumented ()) {
        //         <instrumented code>
        //     } else {
        //         <original code>
        //     }
        //
        final MethodInsnNode checkNode = AsmHelper.invokeStatic (
            BPC_CLASS, BPC_METHOD, BPC_DESC
        );
        instCode.insert (checkNode);

        final LabelNode origLabel = new LabelNode ();
        instCode.insert (checkNode, new JumpInsnNode (Opcodes.IFNE, origLabel));

        instCode.add (origLabel);
        instCode.add (origCode);
        instTcbs.addAll (origTcbs);
    }


    // NOTE: the origCN and instCN nodes will be destroyed in the process
    // NOTE: abstract or native methods should not be included in the
    // changedMethods list
    public static ClassNode fixupLongMethods (
        final boolean splitLongMethods, final ClassNode origCN,
        final ClassNode instCN
    ) {
        //
        // Choose a fix-up strategy and process all over-size methods in the
        // instrumented class.
        //
        final IntConsumer fixupStrategy = splitLongMethods ?
            i -> __splitLongMethod (i, instCN, origCN) :
            i -> __revertToOriginal (i, instCN, origCN);

        IntStream.range (0, instCN.methods.size ()).parallel ().unordered ()
            .filter (i -> __methodSize (instCN.methods.get (i)) > ALLOWED_SIZE)
            .forEach (i -> fixupStrategy.accept (i));

        return instCN;
    }


    private static void __splitLongMethod (
        final int methodIndex, final ClassNode instCN, final ClassNode origCN
    ) {
        // TODO jb ! add splitting for to long methods
        // - ignore clinit - output warning
        // - output warning if splitted is to large and ignore

        // check the code size of the instrumented method
        // add if to the original method that jumps to the renamed instrumented
        // method
        // add original method to the instrumented code
        // rename instrumented method
    }


    private static void __revertToOriginal (
        final int instIndex, final ClassNode instCN, final ClassNode origCN
    ) {
        //
        // Replace the instrumented method with the original method,
        // and print a warning about it.
        //
        final MethodNode instMN = instCN.methods.get (instIndex);
        final MethodNode origMN = __findMethodNode (origCN, instMN.name, instMN.desc);
        instCN.methods.set (instIndex, origMN);

        System.err.printf (
            "warning: method %s.%s%s not instrumented, because its size "+
            "(%d) exceeds the maximal allowed method size (%d)\n",
            AsmHelper.className (instCN), instMN.name, instMN.desc,
            __methodSize (instMN), ALLOWED_SIZE
        );
    }


    private static int __methodSize (final MethodNode method) {
        final CodeSizeEvaluator cse = new CodeSizeEvaluator (null);
        method.accept (cse);
        return cse.getMaxSize ();
    }


    private static MethodNode __findMethodNode (
        final ClassNode cn, final String name, final String desc
    ) {
        return cn.methods.parallelStream ().unordered ()
            .filter (m -> m.name.equals (name) && m.desc.equals (desc))
            .findAny ().orElseThrow (() -> new RuntimeException (
                "Code merger fatal error: method for merge not found"
            ));
    }

}
