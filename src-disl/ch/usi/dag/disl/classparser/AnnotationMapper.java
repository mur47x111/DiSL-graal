package ch.usi.dag.disl.classparser;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;


final class AnnotationMapper {

    private final Map <
        Class <?>, Map <Predicate <String>, BiConsumer <String, Object>>
    > __consumers = new LinkedHashMap <> ();

    //

    public AnnotationMapper register (
        final Class <?> annotationClass,
        final String nameRegex, final BiConsumer <String, Object> consumer
    ) {
        final Predicate <String> predicate = Pattern.compile (nameRegex).asPredicate ();
        __getConsumers (annotationClass).put (predicate, consumer);
        return this;
    }


    private Map <Predicate <String>, BiConsumer <String, Object>> __getConsumers (
        final Class <?> ac
    ) {
        Map <Predicate <String>, BiConsumer <String, Object>> result = __consumers.get (ac);
        if (result == null) {
            result = new HashMap <> ();
            __consumers.put (ac, result);
        }

        return result;
    }

    private Map <Predicate <String>, BiConsumer <String, Object>> __findConsumers (
        final Class <?> ac
    ) {
        return __consumers.entrySet ().stream ()
            .filter (e -> e.getKey ().isAssignableFrom (ac))
            .findFirst ()
            .orElseThrow (() -> new ParserRuntimeException (
                "unsupported annotation type: %s", ac.getName ()
            )).getValue ();
    }

    public AnnotationMapper processDefaults () {
        __consumers.keySet ().stream ()
            .filter (ac -> ac.isAnnotation ())
            .forEach (ac -> __accept (ac));

        return this;
    }


    private void __accept (final Class <?> ac) {
        final Map <
            Predicate <String>, BiConsumer <String, Object>
        > consumers = __findConsumers (ac);

        Arrays.stream (ac.getDeclaredMethods ()).forEach (m -> {
            final String name = m.getName ();
            __getConsumer (consumers, name).accept (name, m.getDefaultValue ());
        });
    }


    private BiConsumer <String, Object> __getConsumer (
        final Map <Predicate <String>, BiConsumer <String, Object>> consumers,
        final String name
    ) {
        return consumers.entrySet ().stream ()
            .filter (e -> e.getKey ().test (name))
            .findFirst ()
            .orElseThrow (() -> new ParserRuntimeException (
                "no consumer for annotation attribute %s", name
            )).getValue ();
    }


    public AnnotationMapper accept (final MethodNode mn) {
        Arrays.asList (mn.visibleAnnotations, mn.invisibleAnnotations).stream ()
            .filter (l -> l != null)
            .flatMap (l -> l.stream ())
            .forEach (an -> accept (an));

        return this;
    }


    public AnnotationMapper accept (final AnnotationNode an) {
        final Class <?> ac = __resolveClass (Type.getType (an.desc));

        final Map <
            Predicate <String>, BiConsumer <String, Object>
        > consumers = __findConsumers (ac);

        an.accept (new AnnotationVisitor (Opcodes.ASM5) {
            @Override
            public void visit (final String name, final Object value) {
                __getConsumer (consumers, name).accept (name, value);
            }

            @Override
            public void visitEnum (
                final String name, final String desc, final String value
            ) {
                final Object enumValue = __instantiateEnum (desc, value);
                __getConsumer (consumers, name).accept (name, enumValue);
            }

            @Override
            public AnnotationVisitor visitArray (final String name) {
                final BiConsumer <String, Object> consumer = __getConsumer (consumers, name);
                return new ListCollector (name, consumer);
            }
        });

        return this;
    }


    /**
     * Collects individual values into a list and submits the result to the
     * given consumer when the {@link #visitEnd()} method is called.
     * <p>
     * <b>Note:</b>This collector does not currently support nested arrays or
     * annotation values.
     */
    private static class ListCollector extends AnnotationVisitor {
        final List <Object> __values = new ArrayList <> ();

        final String __name;
        final BiConsumer <String, Object> __consumer;

        ListCollector (final String name, final BiConsumer <String, Object> consumer) {
            super (Opcodes.ASM5);

            __name = name;
            __consumer = consumer;
        }

        @Override
        public void visit (final String name, final Object value) {
            __values.add (value);
        }

        @Override
        public void visitEnum (final String name, final String desc, final String value) {
            __values.add (__instantiateEnum (desc, value));
        }

        @Override
        public void visitEnd () {
            __consumer.accept (__name, __values);
        }
    };


    private static Class <?> __resolveClass (final Type type) {
        try {
            return Class.forName (type.getClassName ());

        } catch (final ClassNotFoundException e) {
            throw new ParserRuntimeException (e);
        }
    }


    private static Object __instantiateEnum (
        final String desc, final String value
    ) {
        final String className = Type.getType (desc).getClassName ();

        try {
            final Class <?> enumClass = Class.forName (className);
            final Method valueMethod = enumClass.getMethod ("valueOf", String.class );
            final Object result = valueMethod.invoke (null, value);
            if (result != null) {
                return result;
            }

            // Throw the exception outside this try-catch block.

        } catch (final Exception e) {
            throw new ParserRuntimeException (
                e, "failed to instantiate enum value %s.%s", className, value
            );
        }

        throw new ParserRuntimeException (
            "failed to instantiate enum value %s.%s", className, value
        );
    }

}

