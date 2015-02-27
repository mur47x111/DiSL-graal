package ch.usi.dag.disl.snippet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.TryCatchBlockNode;

import ch.usi.dag.disl.coderep.Code;
import ch.usi.dag.disl.coderep.StaticContextMethod;
import ch.usi.dag.disl.localvar.LocalVars;
import ch.usi.dag.disl.localvar.SyntheticLocalVar;
import ch.usi.dag.disl.localvar.ThreadLocalVar;


/**
 * Represents an analyzed and partially expanded snippet code template.
 * Instances of {@link SnippetCode} are obtained from
 * {@link SnippetUnprocessedCode} instances as a result of calling the
 * {@link SnippetUnprocessedCode#process(LocalVars) process()} method on them.
 */
public class SnippetCode {
    /**
     * Processed snippet code.
     */
    private final Code __code;

    /**
     * An unmodifiable map of indices of instructions in the snippet code to
     * invoked argument processors.
     */
    private final Map <Integer, ProcInvocation> __apInvocations;

    private final AtomicReference <Set <StaticContextMethod>> __scmCache;

    //

    public SnippetCode (
        final Code code, final Map <Integer, ProcInvocation> apInvocations
    ) {
        __code = code;
        __apInvocations = Collections.unmodifiableMap (apInvocations);
        __scmCache = new AtomicReference <> ();
    }


    private SnippetCode (final SnippetCode that) {
        //
        // We only need to clone the underlying code. The map of argument
        // processor invocations is immutable, and the cache as well, so
        // both can be shared.
        //
        __code = that.__code.clone ();
        __apInvocations = that.__apInvocations;
        __scmCache = that.__scmCache;
    }

    //

    /**
     * @return An unmodifiable map of all argument processor invocations in the
     *         code.
     */
    public Map <Integer, ProcInvocation> getInvokedProcessors () {
        return __apInvocations;
    }


    /**
     * @return An unmodifiable set of static context methods referenced in the
     *         code, including those referenced in argument processors applied
     *         within this snippet.
     */
    public Set <StaticContextMethod> getReferencedSCMs () {
        return __getCachedReferencedSCMs ();
    }


    private Set <StaticContextMethod> __getCachedReferencedSCMs () {
        Set <StaticContextMethod> result = __scmCache.get ();
        if (result == null) {
            //
            // Merge static context methods from all argument processors with
            // the set of static context methods invoked in the snippet.
            //
            result = __apInvocations.values ().stream ()
                .flatMap (pi -> pi.getProcessor ().getReferencedSCMs ().stream ())
                .collect (Collectors.toSet ());

            result.addAll (__code.getReferencedSCMs ());
            result = Collections.unmodifiableSet (result);

            //
            // Multiple threads may have been building the cache, use the
            // result from the thread that made it first.
            //
            if (! __scmCache.compareAndSet (null, result)) {
                result = __scmCache.get ();
            }
        }

        return result;
    }


    /**
     * @return A clone of this {@link SnippetCode} instance.
     */
    @Override
    public SnippetCode clone () {
         return new SnippetCode (this);
    }

    // Delegate the following calls.

    public InsnList getInstructions () {
        return __code.getInstructions ();
    }


    public List <TryCatchBlockNode> getTryCatchBlocks () {
        return __code.getTryCatchBlocks ();
    }


    public Set <SyntheticLocalVar> getReferencedSLVs () {
        return __code.getReferencedSLVs ();
    }


    public Set <ThreadLocalVar> getReferencedTLVs () {
        return __code.getReferencedTLVs ();
    }

    public boolean containsHandledException () {
        return __code.containsHandledException ();
    }

}
