package ch.usi.dag.disl;

import java.util.List;

import ch.usi.dag.disl.cbloader.ManifestHelper;
import ch.usi.dag.disl.cbloader.ManifestHelper.ManifestInfo;
import ch.usi.dag.disl.exception.ManifestInfoException;
import ch.usi.dag.util.Lists;

class Transformers {

    private final List <Transformer> __transformers;

    //

    private Transformers (final List <Transformer> transformers) {
        // not to be instantiated from outside
        __transformers = transformers;
    }

    public byte [] apply (
        final byte [] originalBytes
    ) throws TransformerException {
        byte [] result = originalBytes;

        for (final Transformer transformer : __transformers) {
            try {
                final byte [] bytes = transformer.transform (result);
                if (bytes != null) {
                    result = bytes;
                }

            } catch (final Exception e) {
                throw new TransformerException (
                    e, "transformation failed in %s", transformer
                );
            }
        }

        return result;
    }

    //

    /**
     * Loads and instantiates {@link Transformer} classes.
     */
    public static Transformers load () throws TransformerInitializationException {
        try {
            return new Transformers (__loadTransformers ());

        } catch (final ManifestInfoException e) {
            throw new TransformerInitializationException (
                e, "failed to load transformers"
            );
        }
    }

    private static List <Transformer> __loadTransformers ()
    throws ManifestInfoException, TransformerInitializationException {
        final List <Transformer> result = Lists.newLinkedList ();

        final ManifestInfo mi = ManifestHelper.getDiSLManifestInfo();
        if (mi != null) {
            final String xfClassName = mi.getDislTransformer ();
            if (xfClassName != null) {
                result.add (__createTransformer (xfClassName));
            }
        }

        return result;
    }

    private static Transformer __createTransformer (
        final String className
    ) throws TransformerInitializationException {
        final Class <?> resolvedClass = __resolveTransformer (className);
        if (Transformer.class.isAssignableFrom (resolvedClass)) {
            return __instantiateTransformer (resolvedClass);
        } else {
            throw new TransformerInitializationException (
                "invalid transformer %s: class does not implement %s",
                className, Transformer.class.getName ()
            );
        }
    }


    private static Class <?> __resolveTransformer (final String className)
    throws TransformerInitializationException {
        try {
            return Class.forName (className);

        } catch (final Exception e) {
            throw new TransformerInitializationException (
                e, "failed to resolve transformer %s", className
            );
        }
    }


    private static Transformer __instantiateTransformer (
        final Class <?> transformerClass
    ) throws TransformerInitializationException {
        try {
            return (Transformer) transformerClass.newInstance ();

        } catch (final Exception e) {
            throw new TransformerInitializationException (
                e, "failed to instantiate transformer %s",
                transformerClass.getName ()
            );
        }
    }

}
