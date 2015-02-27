package ch.usi.dag.disl.processor.generator;

import java.util.Collection;
import java.util.Map;

import ch.usi.dag.disl.snippet.Shadow;
import ch.usi.dag.util.Maps;


public class PIResolver {

    private final Map <ResolverKey, ProcInstance> piStore = Maps.newHashMap ();

    //

    private static class ResolverKey {
        private final Shadow shadow;
        private final int instrPos;


        public ResolverKey (final Shadow shadow, final int instrPos) {
            this.shadow = shadow;
            this.instrPos = instrPos;
        }


        @Override
        public int hashCode () {
            final int prime = 31;

            int result = instrPos;
            result += prime;
            result *= prime;
            result += (shadow == null) ? 0 : shadow.hashCode ();
            return result;
        }


        @Override
        public boolean equals (final Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || getClass () != obj.getClass ()) {
                return false;
            }

            final ResolverKey that = (ResolverKey) obj;
            if (instrPos != that.instrPos) {
                return false;
            }

            if (shadow == null) {
                return that.shadow == null;
            } else {
                return shadow.equals (that.shadow);
            }
        }
    }


    public ProcInstance get (final Shadow shadow, final int instrPos) {
        final ResolverKey key = new ResolverKey (shadow, instrPos);
        return piStore.get (key);
    }


    public void set (
        final Shadow shadow, final int instrPos,
        final ProcInstance processorInstance
    ) {
        final ResolverKey key = new ResolverKey (shadow, instrPos);
        piStore.put (key, processorInstance);
    }


    public Collection <ProcInstance> getAllProcInstances () {
        return piStore.values ();
    }

}
