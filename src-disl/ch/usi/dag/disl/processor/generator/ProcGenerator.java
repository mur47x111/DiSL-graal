package ch.usi.dag.disl.processor.generator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import ch.usi.dag.disl.exception.DiSLFatalException;
import ch.usi.dag.disl.exception.ProcessorException;
import ch.usi.dag.disl.guard.GuardHelper;
import ch.usi.dag.disl.processor.ArgProcessor;
import ch.usi.dag.disl.processor.ArgProcessorKind;
import ch.usi.dag.disl.processor.ArgProcessorMethod;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorMode;
import ch.usi.dag.disl.snippet.ProcInvocation;
import ch.usi.dag.disl.snippet.Shadow;
import ch.usi.dag.disl.snippet.Snippet;

public class ProcGenerator {

    Map<ArgProcessor, ProcInstance> insideMethodPIs = new HashMap<ArgProcessor, ProcInstance>();

    public PIResolver compute(final Map<Snippet, List<Shadow>> snippetMarkings)
            throws ProcessorException {

        final PIResolver piResolver = new PIResolver();

        // for each snippet
        for (final Snippet snippet : snippetMarkings.keySet()) {

            final Map<Integer, ProcInvocation> invokedProcs = snippet.getCode()
                    .getInvokedProcessors();

            for (final Shadow shadow : snippetMarkings.get(snippet)) {

                // for each processor defined in snippet
                for (final Integer instrPos : invokedProcs.keySet()) {

                    final ProcInvocation prcInv = invokedProcs.get(instrPos);

                    ProcInstance prcInst = null;

                    // handle apply typegetInvokedProcessors
                    switch (prcInv.getProcApplyType()) {

                    case METHOD_ARGS: {
                        prcInst = computeInsideMethod(shadow, prcInv);
                        break;
                    }

                    case CALLSITE_ARGS: {
                        prcInst = computeBeforeInvocation(shadow, prcInv);
                        break;
                    }

                    default:
                        throw new DiSLFatalException(
                                "Proc computation not defined");
                    }

                    if(prcInst != null) {
                        // add result to processor instance resolver
                        piResolver.set(shadow, instrPos, prcInst);
                    }
                }
            }
        }

        return piResolver;
    }

    private ProcInstance computeInsideMethod(final Shadow shadow,
            final ProcInvocation prcInv) {

        // all instances of inside method processor will be the same
        // if we have one, we can use it multiple times

        ProcInstance procInst = insideMethodPIs.get(prcInv.getProcessor());

        if (procInst == null) {
            procInst = createProcInstance(ArgumentProcessorMode.METHOD_ARGS,
                    shadow.getMethodNode().desc, shadow, prcInv);
        }

        return procInst;
    }

    private ProcInstance computeBeforeInvocation(final Shadow shadow,
            final ProcInvocation prcInv) throws ProcessorException {

        // NOTE: ProcUnprocessedCode checks that CALLSITE_ARGS is
        // used only with BytecodeMarker

        // because it is BytecodeMarker, it should have only one end
        if(shadow.getRegionEnds().size() > 1) {
            throw new DiSLFatalException(
                    "Expected only one end in marked region");
        }

        // get instruction from the method code
        // the method invocation is the instruction marked as end
        final AbstractInsnNode instr = shadow.getRegionEnds().get(0);

        final String fullMethodName = shadow.getClassNode().name + "."
                + shadow.getMethodNode().name;

        // check - method invocation
        if (!(instr instanceof MethodInsnNode)) {
            throw new ProcessorException("ArgumentProcessor "
                    + prcInv.getProcessor().getName()
                    + " is not applied before method invocation in method "
                    + fullMethodName);
        }

        final MethodInsnNode methodInvocation = (MethodInsnNode) instr;

        return createProcInstance(ArgumentProcessorMode.CALLSITE_ARGS,
                methodInvocation.desc, shadow, prcInv);
    }

    private ProcInstance createProcInstance(final ArgumentProcessorMode procApplyType,
            final String methodDesc, final Shadow shadow, final ProcInvocation prcInv) {

        final List<ProcMethodInstance> procMethodInstances =
            new LinkedList<ProcMethodInstance>();

        // get argument types
        final Type[] argTypeArray = Type.getArgumentTypes(methodDesc);

        // create processor method instances for each argument if applicable
        for (int i = 0; i < argTypeArray.length; ++i) {
            final List <ProcMethodInstance> pmis = createMethodInstances (
                i, argTypeArray [i], argTypeArray.length,
                prcInv.getProcessor (), shadow, prcInv
            );

            procMethodInstances.addAll(pmis);
        }

        if(procMethodInstances.isEmpty()) {
            return null;
        }

        // create new processor instance
        return new ProcInstance(procApplyType, procMethodInstances);
    }


    private List <ProcMethodInstance> createMethodInstances (
        final int argIndex, final Type argType, final int argsCount,
        final ArgProcessor processor, final Shadow shadow,
        final ProcInvocation procInv
    ) {
        final ArgProcessorKind argProcType = ArgProcessorKind.valueOf (argType);
        final List <ProcMethodInstance> result = new LinkedList <> ();

        // traverse all methods and find the proper ones
        for (final ArgProcessorMethod method : processor.getMethods ()) {
            // check argument type
            if (method.handlesType (argProcType)) {
                final ProcMethodInstance pmi = new ProcMethodInstance (
                    argIndex, argType, argsCount, argProcType, method.getCode ()
                );

                // check guard
                if (GuardHelper.guardApplicable (method.getGuard (), shadow, pmi)) {
                    result.add (pmi);
                }
            }
        }

        return result;
    }

}
