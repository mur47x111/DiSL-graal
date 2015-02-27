package ch.usi.dag.disl.classparser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import ch.usi.dag.disl.DiSL.CodeOption;
import ch.usi.dag.disl.annotation.ArgumentProcessor;
import ch.usi.dag.disl.cbloader.ClassByteLoader;
import ch.usi.dag.disl.exception.DiSLInitializationException;
import ch.usi.dag.disl.exception.ParserException;
import ch.usi.dag.disl.exception.ProcessorException;
import ch.usi.dag.disl.exception.ReflectionException;
import ch.usi.dag.disl.exception.StaticContextGenException;
import ch.usi.dag.disl.localvar.LocalVars;
import ch.usi.dag.disl.processor.ArgProcessor;
import ch.usi.dag.disl.snippet.Snippet;
import ch.usi.dag.disl.util.ClassNodeHelper;


/**
 * Parser for DiSL classes containing either snippets or method argument
 * processors.
 */
public class DislClasses {

    private final SnippetParser __snippetParser;

    //

    private DislClasses (final SnippetParser snippetParser) {
        // not to be instantiated from outside
        __snippetParser = snippetParser;
    }

    //

    public static DislClasses load (
        final Set <CodeOption> options
    ) throws DiSLInitializationException, ParserException,
    StaticContextGenException, ReflectionException, ProcessorException {
        final List <InputStream> classStreams = ClassByteLoader.loadDiSLClasses ();
        if (classStreams == null) {
            throw new DiSLInitializationException (
                "Cannot load DiSL classes. Please set the property "+
                ClassByteLoader.PROP_DISL_CLASSES +
                " or supply jar with DiSL classes and proper manifest"
            );
        }

        //

        final SnippetParser sp = new SnippetParser ();
        final ArgProcessorParser app = new ArgProcessorParser ();

        for (final InputStream is : classStreams) {
            //
            // Get an ASM tree representation of the DiSL class first, then
            // parse it as a snippet or an argument processor depending on the
            // annotations associated with the class.
            //
            final ClassNode classNode = __createClassNode (is);
            if (__isArgumentProcessor (classNode)) {
                app.parse (classNode);
            } else {
                sp.parse (classNode);
            }
        }

        //
        // Collect all local variables and initialize the argument processor
        // and snippets.
        //
        final LocalVars localVars = __collectLocals (sp, app);

        // TODO LB: Move the loop to the ArgProcessorParser class
        for (final ArgProcessor processor : app.getProcessors ().values()) {
            processor.init (localVars);
        }

        // TODO LB: Consider whether we need to create the argument processor
        // invocation map now -- we basically discard the argument processors
        // and keep an invocation map keyed to instruction indices! :-(

        // TODO LB: Move the loop to the SnippetParser class
        for (final Snippet snippet : sp.getSnippets ()) {
            snippet.init (localVars, app.getProcessors (), options);
        }

        return new DislClasses (sp);
    }


    private static ClassNode __createClassNode (final InputStream is)
    throws ParserException {
        //
        // Parse input stream into a class node. Include debug information so
        // that we can report line numbers in case of problems in DiSL classes.
        // Re-throw any exceptions as DiSL exceptions.
        //
        try {
            return ClassNodeHelper.SNIPPET.unmarshal (is);

        } catch (final IOException ioe) {
            throw new ParserException (ioe);
        }
    }


    private static boolean __isArgumentProcessor (final ClassNode classNode) {
        //
        // An argument processor must have an @ArgumentProcessor annotation
        // associated with the class. DiSL instrumentation classes may have
        // an @Instrumentation annotation. DiSL classes without annotations
        // are by default considered to be instrumentation classes.
        //
        if (classNode.invisibleAnnotations != null) {
            final Type apType = Type.getType (ArgumentProcessor.class);

            for (final AnnotationNode annotation : classNode.invisibleAnnotations) {
                final Type annotationType = Type.getType (annotation.desc);
                if (apType.equals (annotationType)) {
                    return true;
                }
            }
        }

        // default: not an argument processor
        return false;
    }

    //

    private static LocalVars __collectLocals (
        final SnippetParser sp, final ArgProcessorParser app
    ) {
        //
        // Merge all local variables from snippets and argument processors.
        //
        final LocalVars result = new LocalVars ();
        result.putAll (sp.getAllLocalVars ());
        result.putAll (app.getAllLocalVars ());
        return result;
    }


    public List <Snippet> getSnippets () {
        return __snippetParser.getSnippets ();
    }

}
