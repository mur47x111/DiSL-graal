package ch.usi.dag.disl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.classparser.DislClasses;
import ch.usi.dag.disl.exception.DiSLException;
import ch.usi.dag.disl.exception.DiSLIOException;
import ch.usi.dag.disl.exception.DiSLInMethodException;
import ch.usi.dag.disl.exclusion.ExclusionSet;
import ch.usi.dag.disl.guard.GuardHelper;
import ch.usi.dag.disl.localvar.SyntheticLocalVar;
import ch.usi.dag.disl.localvar.ThreadLocalVar;
import ch.usi.dag.disl.processor.generator.PIResolver;
import ch.usi.dag.disl.processor.generator.ProcGenerator;
import ch.usi.dag.disl.processor.generator.ProcInstance;
import ch.usi.dag.disl.processor.generator.ProcMethodInstance;
import ch.usi.dag.disl.scope.Scope;
import ch.usi.dag.disl.snippet.Shadow;
import ch.usi.dag.disl.snippet.Snippet;
import ch.usi.dag.disl.staticcontext.generator.SCGenerator;
import ch.usi.dag.disl.util.ClassNodeHelper;
import ch.usi.dag.disl.util.Logging;
import ch.usi.dag.disl.weaver.Weaver;
import ch.usi.dag.util.logging.Logger;


/**
 * Provides an entry-point and a simple interface to the DiSL instrumentation
 * framework. This interface is primarily used by the DiSL instrumentation
 * server, but is generally intended for any instrumentation tool wishing to use
 * DiSL.
 */
public final class DiSL {

    private final Logger __log = Logging.getPackageInstance ();

    private final boolean debug = Boolean.getBoolean ("debug");

    //

    private final Set <CodeOption> __codeOptions;

    private final Transformers __transformers;

    private final Set <Scope> __excludedScopes;

    private final DislClasses __dislClasses;


    /**
     * Initializes a DiSL instance by loading transformers, exclusion lists, and
     * DiSL classes.
     * <p>
     * <b>Note:</b> This constructor is deprecated and will be removed in later
     * releases. Use the {@link #init()} static factory method to obtain a
     * {@link DiSL} instance.
     *
     * @param useDynamicBypass
     *        determines whether to generate bypass code and whether to control
     *        the bypass dynamically.
     */
    @Deprecated
    public DiSL (final boolean useDynamicBypass) throws DiSLException {
        __codeOptions = __codeOptionsFrom (System.getProperties ());

        // Add the necessary bypass options for backwards compatibility.
        if (useDynamicBypass) {
            __codeOptions.add (CodeOption.CREATE_BYPASS);
            __codeOptions.add (CodeOption.DYNAMIC_BYPASS);
        }

        __transformers = Transformers.load ();
        __excludedScopes = ExclusionSet.prepare();
        __dislClasses = DislClasses.load (__codeOptions);
    }


    /**
     * Initializes a DiSL instance.
     */
    private DiSL (
        final Set <CodeOption> codeOptions, final Transformers transformers,
        final Set <Scope> excludedScopes, final DislClasses dislClasses
    ) {
        __codeOptions = codeOptions;
        __transformers = transformers;
        __excludedScopes = excludedScopes;
        __dislClasses = dislClasses;
    }


    /**
     * Loads transformers, exclusion lists, and DiSL classes with snippets, and
     * creates an instance of the {@link DiSL} class.
     *
     * @return A {@link DiSL} instance.
     * @throws DiSLException
     *         if the initialization failed.
     */
    public static DiSL init () throws DiSLException {
        return init (System.getProperties ());
    }


    /**
     * Loads transformers, exclusion lists, and DiSL classes with snippets, and
     * creates an instance of the {@link DiSL} class.
     *
     * @param properties
     *        the properties to use in place of system properties, may not be
     *        {@code null}
     * @return A {@link DiSL} instance.
     * @throws DiSLException
     *         if the initialization failed.
     */
    private static DiSL init (final Properties properties) throws DiSLException {
        final Set <CodeOption> codeOptions = __codeOptionsFrom (
            Objects.requireNonNull (properties)
        );

        final Transformers transformers = Transformers.load ();
        final Set <Scope> excludedScopes = ExclusionSet.prepare();
        final DislClasses dislClasses = DislClasses.load (codeOptions);

        // TODO put checker here
        // like After should catch normal and abnormal execution
        // but if you are using After (AfterThrowing) with BasicBlockMarker
        // or InstructionMarker that doesn't throw exception, then it is
        // probably something, you don't want - so just warn the user
        // also it can warn about unknown opcodes if you let user to
        // specify this for InstructionMarker

        return new DiSL (codeOptions, transformers, excludedScopes, dislClasses);
    }


    /**
     * Derives code options from global properties. This is a transitional
     * compatibility method for the transition to per-request code options.
     */
    private static Set <CodeOption> __codeOptionsFrom (
        final Properties properties
    ) {
        final Set <CodeOption> result = EnumSet.noneOf (CodeOption.class);

        final boolean useExceptHandler = !__getBoolean ("disl.noexcepthandler", properties);
        if (useExceptHandler) {
            result.add (CodeOption.CATCH_EXCEPTIONS);
        }

        final boolean disableBypass = __getBoolean ("disl.disablebypass", properties);
        if (!disableBypass) {
            result.add (CodeOption.CREATE_BYPASS);
            result.add (CodeOption.DYNAMIC_BYPASS);
        }

        final boolean splitLongMethods = __getBoolean ("disl.splitmethods", properties);
        if (splitLongMethods) {
            result.add (CodeOption.SPLIT_METHODS);
        }

        final boolean graalsupport = __getBoolean ("disl.graalsupport", properties);
        if (graalsupport) {
            result.add (CodeOption.GRAAL_SUPPORT);
        }

        return result;
    }

    private static boolean __getBoolean (
        final String name, final Properties properties
    ) {
        return Boolean.parseBoolean (properties.getProperty(name));
    }


    /**
     * Instruments a method in a class. <b>Note:</b> This method changes the
     * {@code classNode} arugment.
     *
     * @param classNode
     *        class that will be instrumented
     * @param methodNode
     *        method in the classNode argument, that will be instrumented
     * @return {@code true} if the methods was changed, {@code false} otherwise.
     */
    private boolean instrumentMethod (
        final ClassNode classNode, final MethodNode methodNode
    ) throws DiSLException {

        // skip abstract methods
        if ((methodNode.access & Opcodes.ACC_ABSTRACT) != 0) {
            return false;
        }

        // skip native methods
        if ((methodNode.access & Opcodes.ACC_NATIVE) != 0) {
            return false;
        }

        final String className = classNode.name;
        final String methodName = methodNode.name;
        final String methodDesc = methodNode.desc;

        // evaluate exclusions
        // TODO LB: Add support for inclusion
        for (final Scope exclScope : __excludedScopes) {
            if (exclScope.matches (className, methodName, methodDesc)) {
                __log.debug ("excluding method: %s.%s(%s)",
                    className, methodName, methodDesc);
                return false;
            }
        }

        // *** match snippet scope ***

        final List <Snippet> matchedSnippets = new LinkedList <Snippet> ();
        for (final Snippet snippet : __dislClasses.getSnippets ()) {
            if (snippet.getScope ().matches (className, methodName, methodDesc)) {
                matchedSnippets.add (snippet);
            }
        }

        // if there is nothing to instrument -> quit
        // just to be faster out
        if (matchedSnippets.isEmpty ()) {
            __log.debug ("skipping unaffected method: %s.%s(%s)",
                className, methodName, methodDesc);
            return false;
        }

        // *** create shadows ***

        // shadows mapped to snippets - for weaving
        final Map<Snippet, List<Shadow>> snippetMarkings = new HashMap <> ();

        for (final Snippet snippet : matchedSnippets) {
            __log.trace ("\tsnippet: %s.%s()",
                snippet.getOriginClassName (), snippet.getOriginMethodName ());

            // marking
            final List <Shadow> shadows = snippet.getMarker ().mark (
                classNode, methodNode, snippet
            );

            // select shadows according to snippet guard
            final List <Shadow> selectedShadows = selectShadowsWithGuard (
                snippet.getGuard (), shadows
            );

            __log.trace ("\tselected shadows: %d", selectedShadows.size ());

            // add to map
            if (!selectedShadows.isEmpty ()) {
                snippetMarkings.put (snippet, selectedShadows);
            }
        }

        // *** compute static info ***

        __log.trace ("calculating static information for method: %s.%s(%s)",
            className, methodName, methodDesc);

        // prepares SCGenerator class (computes static context)
        final SCGenerator staticInfo = SCGenerator.computeStaticInfo (snippetMarkings);

        // *** used synthetic local vars in snippets ***

        __log.trace ("finding synthetic locals used by method: %s.%s(%s)",
            className, methodName, methodDesc);

        // weaver needs list of synthetic locals that are actively used in
        // selected (matched) snippets

        final Set <SyntheticLocalVar> usedSLVs = new HashSet <SyntheticLocalVar> ();
        for (final Snippet snippet : snippetMarkings.keySet ()) {
            usedSLVs.addAll (snippet.getCode ().getReferencedSLVs ());
        }

        // *** prepare processors ***

        __log.trace ("preparing argument processors for method: %s.%s(%s)",
            className, methodName, methodDesc);

        final PIResolver piResolver = new ProcGenerator ().compute (snippetMarkings);

        // *** used synthetic local vars in processors ***

        // include SLVs from processor methods into usedSLV
        for (final ProcInstance pi : piResolver.getAllProcInstances ()) {
            for (final ProcMethodInstance pmi : pi.getMethods ()) {
                usedSLVs.addAll (pmi.getCode ().getReferencedSLVs ());
            }
        }

        // *** weaving ***

        if (snippetMarkings.size () > 0) {
            __log.debug ("found %d snippet marking(s), weaving method: %s.%s(%s)",
                snippetMarkings.size (), className, methodName, methodDesc);
            Weaver.instrument (
                classNode, methodNode, snippetMarkings,
                new LinkedList <SyntheticLocalVar> (usedSLVs),
                staticInfo, piResolver
            );

            return true;

        } else {
            __log.debug ("found %d snippet marking(s), skipping method: %s.%s(%s)",
                snippetMarkings.size (), className, methodName, methodDesc);

            return false;
        }
    }


    /**
     * Selects only shadows passing the given guard.
     *
     * @param guard
     *        the guard to use for filtering the {@link Shadow} instances.
     * @param shadows
     *        the list of {@link Shadow} instances to filter.
     * @return A list of {@link Shadow} instances passing the guard.
     */
    private List <Shadow> selectShadowsWithGuard (
        final Method guard, final List <Shadow> shadows
    ) {
        if (guard == null) {
            return shadows;
        }

        return shadows.stream ()
            // potentially .parallel(), needs thread-safe static context
            .filter (shadow -> GuardHelper.guardApplicable (guard, shadow))
            .collect (Collectors.toList ());
    }


    /**
     * Data holder for an instrumented class
     */
    private static class InstrumentedClass {
        final ClassNode classNode;
        final Set <String> changedMethods;


        public InstrumentedClass (
            final ClassNode classNode, final Set <String> changedMethods
        ) {
            this.classNode = classNode;
            this.changedMethods = changedMethods;
        }
    }


    /**
     * Instruments class node.
     *
     * Note: This method is thread safe. Parameter classNode is changed during
     * the invocation.
     *
     * @param classNode
     *            class node to instrument
     * @return instrumented class
     */
    private InstrumentedClass instrumentClass (
        ClassNode classNode
    ) throws DiSLException {
        // NOTE that class can be changed without changing any method
        // - adding thread local fields
        boolean classChanged = false;

        // track changed methods for code merging
        final Set <String> changedMethods = new HashSet <String> ();

        // instrument all methods in a class
        for (final MethodNode methodNode : classNode.methods) {
            boolean methodChanged = false;

            // intercept all exceptions and add a method name
            try {
                __log.trace ("processing method: %s.%s(%s)",
                    classNode.name, methodNode.name, methodNode.desc);
                methodChanged = instrumentMethod (classNode, methodNode);

            } catch (final DiSLException e) {
                throw new DiSLInMethodException (
                    classNode.name + "." + methodNode.name, e);
            }

            // add method to the set of changed methods
            if (methodChanged) {
                changedMethods.add (methodNode.name + methodNode.desc);
                classChanged = true;
            }
        }

        // instrument thread local fields
        if (Type.getInternalName (Thread.class).equals (classNode.name)) {
            final Set <ThreadLocalVar> insertTLVs = new HashSet <ThreadLocalVar> ();

            // dynamic bypass
            if (__codeOptions.contains (CodeOption.DYNAMIC_BYPASS)) {
                // prepare dynamic bypass thread local variable
                final ThreadLocalVar tlv = new ThreadLocalVar (
                    null, "bypass", Type.getType (boolean.class), false
                );

                tlv.setDefaultValue (0);
                insertTLVs.add (tlv);
            }

            // get all thread locals in snippets
            for (final Snippet snippet : __dislClasses.getSnippets ()) {
                insertTLVs.addAll (snippet.getCode ().getReferencedTLVs ());
            }

            if (!insertTLVs.isEmpty ()) {
                // instrument fields
                final ClassNode cnWithFields = new ClassNode (Opcodes.ASM4);
                classNode.accept (new TLVInserter (cnWithFields, insertTLVs));

                // replace original code with instrumented one
                classNode = cnWithFields;
                classChanged = true;
            }
        }

        // we have changed some methods
        return classChanged ?
            new InstrumentedClass (classNode, changedMethods) :
            null;
    }


    /**
     * Instruments the given class, provided as an array of bytes representing
     * the contents of its class file.
     *
     * @param originalBytes
     *        the class to instrument as an array of bytes
     * @return An array of bytes representing the instrumented class, or
     *         {@code null} if the class has not been instrumented.
     */
    // TODO ! current static context interface does not allow to have nice
    // synchronization - it should be redesigned such as the staticContextData
    // also invokes the required method and returns result - if this method
    // (and static context class itself) will be synchronized, it should work
    public synchronized byte [] instrument (
        final byte [] originalBytes
    ) throws DiSLException {
        if (debug) {
            // keep the currently processed class around in case of errors
            __dumpBytesToFile (originalBytes, "err.class");
        }

        final byte [] transformedBytes = __transformers.apply (originalBytes);
        final ClassNode inputCN = ClassNodeHelper.FULL.unmarshal (transformedBytes);

        //
        // Instrument the class. If the class is modified neither by DiSL,
        // nor by any of the transformers, bail out early and return NULL
        // to indicate that the class has not been modified in any way.
        //
        final InstrumentedClass instResult = instrumentClass (inputCN);
        if (instResult == null && transformedBytes == originalBytes) {
            return null;
        }

        // TODO LB: Try to avoid unmarshaling the class again (duplicate it).
        final ClassNode origCN = ClassNodeHelper.FULL.unmarshal (transformedBytes);

        //
        // If creating bypass code is requested, merge the original method code
        // with the instrumented method code and create code to switch between
        // the two versions based on the result of a bypass check.
        //
        final ClassNode instCN = instResult.classNode;
        if (__codeOptions.contains (CodeOption.CREATE_BYPASS)) {
            CodeMerger.mergeOriginalCode (
                instResult.changedMethods, origCN, instCN
            );
        }


        //
        // Fix-up methods that have become too long due to instrumentation.
        // To split out the instrumented version of the method, we will need
        // to preserve the instrumented version in the previous step.
        //
        // XXX LB: This will not help long methods produced by the transformers.
        //
        CodeMerger.fixupLongMethods (
            __codeOptions.contains (CodeOption.SPLIT_METHODS), origCN, instCN
        );

        return ClassNodeHelper.marshal (instCN);
    }


    private void __dumpBytesToFile (
        final byte [] classBytes, final String fileName
    ) throws DiSLIOException {
        try {
            final FileOutputStream fos = new FileOutputStream (fileName);
            try {
                fos.write (classBytes);
            } finally {
                fos.close ();
            }
        } catch (final IOException ioe) {
            throw new DiSLIOException (ioe);
        }
    }


    /**
     * Termination handler - should be invoked by the instrumentation framework.
     */
    public void terminate () {
        // currently empty
    }


    //

    /**
     * Options for code transformations performed by DiSL.
     */
    public enum CodeOption {

        /**
         * Create a copy of the original method code and check whether to
         * execute the instrumented or the uninstrumented version of the code
         * upon method entry.
         */
        CREATE_BYPASS (Flag.CREATE_BYPASS),

        /**
         * Insert code for dynamic bypass control. Enable bypass when entering
         * instrumentation code and disable it when leaving it.
         */
        DYNAMIC_BYPASS (Flag.DYNAMIC_BYPASS),

        /**
         * Split methods exceeding the limit imposed by the class file format.
         */
        SPLIT_METHODS (Flag.SPLIT_METHODS),

        /**
         * Wrap snippets in exception handlers to catch exceptions. This is
         * mainly useful for debugging instrumentation code, because the
         * handlers terminate the program execution.
         */
        CATCH_EXCEPTIONS (Flag.CATCH_EXCEPTIONS),

        /**
         * auto instrument invocation to instrumentationBegin/instrumentationEnd
         */
        GRAAL_SUPPORT (Flag.GRAAL_SUPPORT);

        /**
         * Flags corresponding to individual code options. The flags are
         * used when communicating with DiSL agent.
         */
        public interface Flag {
            static final int CREATE_BYPASS = 1 << 0;
            static final int DYNAMIC_BYPASS = 1 << 1;
            static final int SPLIT_METHODS = 1 << 2;
            static final int CATCH_EXCEPTIONS = 1 << 3;
            static final int GRAAL_SUPPORT = 1 << 4;
        }

        //

        private final int __flag;

        private CodeOption (final int flag) {
            __flag = flag;
        }

        //

        /**
         * Creates a set of code options from an array of options.
         */
        public static Set <CodeOption> setOf (final CodeOption... options) {
            final EnumSet <CodeOption> result = EnumSet.noneOf (CodeOption.class);
            for (final CodeOption option : options) {
                result.add (option);
            }

            return result;
        }


        /**
         * Creates a set of code options from flags in an integer.
         */
        public static Set <CodeOption> setOf (final int flags) {
            final EnumSet <CodeOption> result = EnumSet.noneOf (CodeOption.class);
            for (final CodeOption option : CodeOption.values ()) {
                if ((flags & option.__flag) != 0) {
                    result.add (option);
                }
            }

            return result;
        }
    }

}
