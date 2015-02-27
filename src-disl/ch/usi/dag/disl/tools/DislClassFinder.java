package ch.usi.dag.disl.tools;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.annotation.AfterThrowing;
import ch.usi.dag.disl.annotation.ArgumentProcessor;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.annotation.Guarded;
import ch.usi.dag.disl.annotation.ProcessAlso;
import ch.usi.dag.disl.annotation.SyntheticLocal;
import ch.usi.dag.util.Sets;


/**
 * Identifies DiSL classes based on the use of DiSL annotations. Only classes
 * related to snippet expansion are considered DiSL classes, i.e., classes
 * containing DiSL snippets and argument processor classes. Guards are not
 * considered DiSL classes.
 *
 * @author Lubomir Bulej
 */
public final class DislClassFinder implements Processor {

    //
    // Configuration. Initialized by the init() method.
    //

    private Messager __messager;
    private PrintStream __output;
    private String __separator;
    private boolean __forceEol;

    //
    // State kept between processing rounds.
    //

    private String __effectiveSeparator;
    private final Set <String> __dislClasses = Sets.newHashSet ();

    //

    private static final String __OPTION_OUTPUT__ = "disl.classfinder.output";
    private static final String __OPTION_SEPARATOR__ = "disl.classfinder.separator";
    private static final String __OPTION_FORCE_EOL__ = "disl.classfinder.force-eol";

    private static final Set <String> __options__ = Collections.unmodifiableSet (
        Sets.newHashSet (__OPTION_OUTPUT__, __OPTION_SEPARATOR__)
    );

    @Override
    public Set <String> getSupportedOptions () {
        return __options__;
    }


    private static final Class <?> [] __annotations__ = new Class <?> [] {
        Before.class, After.class, AfterReturning.class, AfterThrowing.class,
        ArgumentProcessor.class, Guarded.class, ProcessAlso.class,
        SyntheticLocal.class, ThreadLocal.class
    };

    @Override
    public Set <String> getSupportedAnnotationTypes () {

        final Set <String> result = Sets.newLinkedHashSet ();
        for (final Class <?> annotationClass : __annotations__) {
            result.add (annotationClass.getName ());
        }

        return result;
    }

    @Override
    public SourceVersion getSupportedSourceVersion () {
        return SourceVersion.latestSupported ();
    }

    @Override
    public void init (final ProcessingEnvironment env) {
        __messager = env.getMessager ();

        //
        // Configure the class finder output. If no output is specified,
        // the processor will not do any work. If "-" is specified as output,
        // the system output will be used.
        //
        final String fileName = env.getOptions ().get (__OPTION_OUTPUT__);
        if (fileName != null && !fileName.isEmpty ()) {
            if ("-".equals (fileName)) {
                __output = System.out;

            } else {
                try {
                    __output = new PrintStream (fileName);

                } catch (final FileNotFoundException e) {
                    __messager.printMessage (Kind.WARNING, String.format (
                        "failed to create %s: %s", fileName, e.getMessage ()
                    ));
                }
            }
        }

        //
        // Configure class name separator. Use a new line by default.
        // The effective separator starts as an empty string and after the
        // first class printed, it becomes the configured separator.
        //
        final String separator = env.getOptions ().get (__OPTION_SEPARATOR__);
        __separator = (separator != null) ? separator : "\n";
        __effectiveSeparator = "";

        //
        // Configure whether to force end-of-line at the end of file.
        // By default, EOL is not forced.
        //
        final String forceEol = env.getOptions ().get (__OPTION_FORCE_EOL__);
        __forceEol = (forceEol != null) ? Boolean.parseBoolean (forceEol) : false;
    }


    @Override
    public boolean process (
        final Set <? extends TypeElement> annotations,
        final RoundEnvironment env
    ) {
        //
        // If no file name is set, ignore the annotations.
        //
        if (__output == null) {
            return false;
        }

        //
        // The annotations may be processed in multiple rounds. Collect
        // DiSL classes in each round and only report those not seen before.
        //
        final Set <String> newClasses = Sets.newHashSet ();

        for (final TypeElement te : annotations) {
            for (final Element e : env.getElementsAnnotatedWith (te)) {
                final String className = __getEnclosingClassName (e);
                if (className == null) {
                    __messager.printMessage (Kind.WARNING, String.format (
                        "could not get enclosing class name for %s (%s)",
                        e, e.getKind ()
                    ));

                } else if (__dislClasses.add (className)) {
                    newClasses.add (className);
                }
            }
        }

        for (final String dislClass : newClasses) {
            __output.print (__effectiveSeparator);
            __output.print (dislClass);

            __effectiveSeparator = __separator;
        }

        //
        // At the end of processing, force a new line in the output file
        // (if required) and close the output (unless it is system output).
        //
        if (env.processingOver ()) {
            if (__forceEol && __dislClasses.size () > 0) {
                __output.println ();
            }

            __output.flush ();
            if (__output != System.out) {
                __output.close ();
            }
        }

        return true;
    }


    private String __getEnclosingClassName (final Element element) {
        //
        // Our annotations apply to classes, methods, and fields.
        // For methods and fields, find the enclosing class and then
        // return the binary qualified name of the resulting class.
        // Return null if the enclosing class could not be found.
        //
        final ElementKind kind = element.getKind ();
        if (kind.isClass ()) {
            return __getBinaryClassName (element);

        } else if (kind == ElementKind.METHOD || kind == ElementKind.FIELD) {
            final Element parent = element.getEnclosingElement ();
            if (parent.getKind ().isClass ()) {
                return __getBinaryClassName (parent);
            }
        }

        //
        // Could not find enclosing class.
        //
        return null;
    }


    private String __getBinaryClassName (final Element element) {
        //
        // If the parent element is package, we can just use the class name.
        // Otherwise we have to traverse the enclosing classes to build the
        // binary name.
        //
        if (element.getEnclosingElement ().getKind () == ElementKind.PACKAGE) {
            return element.toString ();

        } else {
            return __buildBinaryClassName (element);
        }
    }


    private String __buildBinaryClassName (final Element element) {
        //
        // Replace '.' before inner class names with '$'.
        // For details on class binary names, see
        //
        // http://docs.oracle.com/javase/specs/jls/se8/html/jls-13.html#jls-13.1
        //
        final StringBuilder name = new StringBuilder (element.toString ());

        Element current = element;
        int lastPosition = name.length ();

        TRAVERSAL: while (true) {
            final Element parent = current.getEnclosingElement ();
            if (!parent.getKind ().isClass ()) {
                break TRAVERSAL;
            }

            lastPosition = __lastIndexOf (name, '.', lastPosition);
            name.setCharAt (lastPosition,  '$');
            current = parent;
        }

        return name.toString ();
    }


    private int __lastIndexOf (
        final StringBuilder sb, final char chr, final int startPosition
    ) {
        int position = Math.min (startPosition, sb.length () - 1);
        while (position >= 0 && sb.charAt (position) != chr) {
            position--;
        }

        return position;
    }


    @Override
    public Iterable <? extends Completion> getCompletions (
        final Element element, final AnnotationMirror annotation,
        final ExecutableElement member, final String userText
    ) {
        return Collections.emptyList ();
    }

}
