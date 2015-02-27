package ch.usi.dag.dislreserver.msg.analyze;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.DiSLREServerFatalException;
import ch.usi.dag.dislreserver.remoteanalysis.RemoteAnalysis;

public final class AnalysisResolver {
    private static final String METHOD_DELIM = ".";

    private static final Map <Short, AnalysisMethodHolder>
        methodMap = new HashMap <Short, AnalysisMethodHolder> ();

    private static final Map <String, RemoteAnalysis>
        analysisMap = new HashMap <String, RemoteAnalysis> ();

    // for fast set access - contains all values from analysisMap
    private static final Set <RemoteAnalysis>
        analysisSet = new HashSet <RemoteAnalysis> ();

    //

    public static final class AnalysisMethodHolder {
        private final RemoteAnalysis analysisInstance;
        private final Method analysisMethod;

        public AnalysisMethodHolder(
            final RemoteAnalysis analysisInstance, final Method analysisMethod
        ) {
            this.analysisInstance = analysisInstance;
            this.analysisMethod = analysisMethod;
        }

        public RemoteAnalysis getAnalysisInstance() {
            return analysisInstance;
        }

        public Method getAnalysisMethod() {
            return analysisMethod;
        }
    }

    //

    private static AnalysisMethodHolder resolveMethod (String methodStr
    ) throws DiSLREServerException {
        try {
            int classNameEnd = methodStr.lastIndexOf (METHOD_DELIM);

            // without METHOD_DELIM
            String className = methodStr.substring (0, classNameEnd);
            String methodName = methodStr.substring (classNameEnd + 1);

            // resolve analysis instance
            RemoteAnalysis raInst = analysisMap.get (className);
            if (raInst == null) {
                // resolve class
                Class <?> raClass = Class.forName (className);

                // create instance
                raInst = (RemoteAnalysis) raClass.newInstance ();

                analysisMap.put (className, raInst);
                analysisSet.add (raInst);
            }

            // resolve analysis method
            final Method raMethod = __getAnalysisMethod (raInst, methodName);

            return new AnalysisMethodHolder(raInst, raMethod);
        }

        catch (ClassNotFoundException e) {
            throw new DiSLREServerException(e);
        } catch (InstantiationException e) {
            throw new DiSLREServerException(e);
        } catch (IllegalAccessException e) {
            throw new DiSLREServerException(e);
        }
    }

    private static Method __getAnalysisMethod (
        final RemoteAnalysis analysis, final String methodName
    ) throws DiSLREServerException {
        final Class <?> analysisClass = analysis.getClass ();

        final List <Method> methods = new ArrayList <Method> ();
        for (final Method analysisMethod : analysisClass.getMethods ()) {
            if (analysisMethod.getName ().equals (methodName)) {
                methods.add (analysisMethod);
            }
        }

        //
        // Throw an exception if there are multiple methods
        //
        final int methodCount = methods.size ();
        if (methodCount == 1) {
            return methods.get (0);

        } else if (methodCount > 1) {
            throw new DiSLREServerException (String.format (
                "Multiple methods matching \"%s\" found in %s",
                methodName, analysisClass.getName ()
            ));
        } else {
            throw new DiSLREServerException (String.format (
                "No method matching \"%s\" found in %s",
                methodName, analysisClass.getName ()
            ));
        }
    }


    static AnalysisMethodHolder getMethod (final short methodId)
    throws DiSLREServerException {
        AnalysisMethodHolder result = methodMap.get (methodId);
        if (result == null) {
            throw new DiSLREServerFatalException ("Unknown method id: "+ methodId);
        }

        return result;
    }


    public static void registerMethodId (
        final short methodId, String methodString
    ) throws DiSLREServerException {
        methodMap.put(methodId, resolveMethod(methodString));
    }


    public static Set <RemoteAnalysis> getAllAnalyses () {
        return analysisSet;
    }
}
