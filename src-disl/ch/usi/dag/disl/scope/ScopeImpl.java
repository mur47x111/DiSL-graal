package ch.usi.dag.disl.scope;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Type;

import ch.usi.dag.disl.exception.ScopeParserException;
import ch.usi.dag.disl.util.Constants;


/**
 * Filters methods based on class name, method name, method parameters and
 * return type. A filter is specified as follows:
 * <ul>
 * <b>[&lt;returnType&gt;] [&lt;className&gt;.]&lt;methodName&gt;
 * [(&lt;paramTypes&gt;)]</b>
 * </ul>
 * To match multiple methods, the individual filter elements (or their parts)
 * can be substituted with the "*" wild card character, which will expand to
 * zero or more non-whitespace characters. The individual filter elements have
 * the following meaning:
 * <p>
 * <dl>
 * <dt>&lt;returnType&gt;
 * <dd>The type of the method's return value, specified either as a fully
 * qualified class name (for reference types) or a primitive type name. If not
 * specified, the filter will match all return types. For example:
 * <p>
 * <dl>
 * <dt>* or nothing
 * <dd>matches all return types
 * <dt>*.String
 * <dd>matches methods returning a String class from any package, e.g.,
 * java.lang.String, or my.package.String.
 * <dt>*String
 * <dd>matches methods returning any class with a name ending with String from
 * any package, e.g., java.lang.String, my.package.String, or
 * my.package.BigString
 * </dl>
 * <dt>&lt;className&gt;
 * <dd>Fully qualified name of the class containing the methods the filter is
 * supposed to match. If not specified, the filter will match all classes. The
 * package part of a class name can be omitted, in which case the filter will
 * match all packages. To match a class without a package name (i.e. in the
 * default package), specify the package name as "[default]". For example:
 * <p>
 * <dl>
 * <dt>* or nothing
 * <dd>matches all classes
 * <dt>TargetClass
 * <dd>matches a class named TargetClass in any package
 * <dt>[default].TargetClass
 * <dd>matches a class named TargetClass only in the default package, i.e., it
 * does not match my.pkg.TargetClass
 * <dt>TargetClass*
 * <dd>matches any class with a name starting with TargetClass in any package,
 * e.g., TargetClassFoo in any package, or TargetClassBar in any package
 * <dt>my.pkg.*Math
 * <dd>matches any class with a name ending with Math in the my.pkg package and
 * all sub packages, e.g., my.pkg.Math, my.pkg.FastMath, my.pkg.new.FastMath, or
 * my.pkg.new.fast.Math
 * </dl>
 * <dt>&lt;methodName&gt;
 * <dd>The name of the method the filter is supposed to match. This filter
 * element is mandatory, therefore to match any method name, the
 * &lt;methodName&gt; element must be replaced with a "*". To match class
 * initializers and class constructors, use their bytecode-internal names, i.e.,
 * "clinit" and "init", respectively. For example:
 * <p>
 * <dl>
 * <dt>*
 * <dd>matches all methods
 * <dt>*init
 * <dd>matches class initializer (clinit), class constructor (init), and any
 * method with a name ending with "init"
 * </dl>
 * <p>
 * <dt>&lt;paramTypes&gt;
 * <dd>A comma-separated list of method parameter types. Each parameter type is
 * specified either as a fully qualified class name or a primitive type name.
 * The filter parameter list can end with "..", which matches all remaining
 * method parameters. If not specified, the filter matches all methods
 * regardless of their parameter types. For example:
 * <p>
 * <dl>
 * <dt>(..)
 * <dd>matches methods with any (including none) parameters, e.g., (), or (int)
 * <dt>(int, int, ..)
 * <dd>matches any method with at least two parameters, and the parameter list
 * starting with two integers, e.g., (int, int), (int, int, double), or (int,
 * int, Object, String)
 * <dt>(java.lang.String, java.lang.String[])
 * <dd>matches a method with exactly two parameters with matching types. The
 * types are matched verbatim, i.e., there is no matching based on subtyping.
 * </dl>
 * </dl>
 * To put it all together, consider the following complete examples:
 * <p>
 * <dl>
 * <dt>my.pkg.TargetClass.main(java.lang.String[])
 * <dd>matches the "main" method in class my.pkg.TargetClass which takes as a
 * parameter an array of Strings. In this case, the return type is not
 * important, because the parameter signature is fully specified.
 * <dt>int *
 * <dd>matches all methods returning an integer value
 * <dt>*(int, int, int)
 * <dd>matches all methods accepting three integer values
 * </dl>
 */
public class ScopeImpl implements Scope {

    private final String PARAM_BEGIN = "(";

    private final String PARAM_END = ")";

    private final String PARAM_DELIM = ",";

    private final String METHOD_DELIM = ".";

    private final String PARAM_MATCH_REST = "..";

    private final String DEFAULT_PKG = "[default]";

    private final String RETURN_DELIM = " ";

    private String classWildCard;

    private String methodWildCard;

    private String returnWildCard;

    private List <String> paramsWildCard;


    private int lastWhitespace (final String str) {
        final int len = str.length ();
        for (int pos = len - 1; pos >= 0; pos--) {
            if (Character.isWhitespace (str.charAt (pos))) {
                return pos;
            }
        }

        return -1;
    }


    private boolean containsWhiteSpace (final String str) {
        final int len = str.length ();
        for (int pos = 0; pos < len; pos++) {
            if (Character.isWhitespace (str.charAt (pos))) {
                return true;
            }
        }

        return false;
    }


    public ScopeImpl (final String scopeExpression) throws ScopeParserException {

        // -- parse the scope into parts - trim whitespace everywhere --

        // parse it from the end
        // its better because you can easier identify return type
        // otherwise you don't know if the first empty space doesn't mean
        // something else

        String restOfExpr = scopeExpression;

        // -- method parameters --
        final int paramBegin = restOfExpr.lastIndexOf (PARAM_BEGIN);
        if (paramBegin != -1) {

            // + 1 - don't include PARAM_BEGIN
            String paramsStr = restOfExpr.substring (paramBegin + 1);
            restOfExpr = restOfExpr.substring (0, paramBegin);

            // remove whitespace
            paramsStr = paramsStr.trim ();

            // PARAM_END check
            if (!paramsStr.endsWith (PARAM_END)) {
                throw new ScopeParserException ("Scope \"" + scopeExpression
                    + "\" should end with \"" + PARAM_END + "\"");
            }

            // remove PARAM_END
            final int paramEnd = paramsStr.lastIndexOf (PARAM_END);
            paramsStr = paramsStr.substring (0, paramEnd);

            paramsWildCard = new LinkedList <String> ();

            // test for emptiness
            if (!paramsStr.trim ().isEmpty ()) {

                // separate params and trim them again
                final String [] params = paramsStr.split (PARAM_DELIM);
                for (String param : Arrays.asList (params)) {

                    param = param.trim ();

                    if (param.isEmpty ()) {
                        throw new ScopeParserException ("Scope \""
                            + scopeExpression
                            + "\" has bad parameter definition");
                    }

                    paramsWildCard.add (param);
                }
            }

            final int pmrIndex = paramsWildCard.indexOf (PARAM_MATCH_REST);

            // if the index is valid, the first occurrence of PARAM_MATCH_REST
            // should be at the end of the parameters
            if (pmrIndex != -1 && pmrIndex != paramsWildCard.size () - 1) {
                throw new ScopeParserException ("Scope \""
                    + scopeExpression
                    + "\" should have \"" + PARAM_MATCH_REST + "\""
                    + " only as last parameter");
            }
        }

        // see ScopeTest bugs for reference
        // split returnExpr first cause
        // after parsing parameters and trimming split around white space
        // this should work
        String returnExpr = "";
        if (restOfExpr.split (RETURN_DELIM).length == 2) {
            returnExpr = restOfExpr.split (RETURN_DELIM, 2) [0];
            restOfExpr = restOfExpr.split (RETURN_DELIM, 2) [1];
        }

        // -- method name --
        final int methodDelim = restOfExpr.lastIndexOf (METHOD_DELIM);
        if (methodDelim != -1) {
            // + 1 - don't include METHOD_DELIM
            methodWildCard = restOfExpr.substring (methodDelim + 1);
            restOfExpr = restOfExpr.substring (0, methodDelim);
        } else {
            methodWildCard = restOfExpr;
            restOfExpr = null;
        }

        // remove whitespace
        methodWildCard = methodWildCard.trim ();

        if (methodWildCard.isEmpty ()) {
            throw new ScopeParserException ("Scope \"" + scopeExpression
                + "\" should have defined method at least as \"*\"");
        }

        // -- full class name --
        if (restOfExpr != null) {
            // remove whitespace
            restOfExpr = restOfExpr.trim ();

            if (!restOfExpr.isEmpty ()) {

                final int classDelim = lastWhitespace (restOfExpr);
                if (classDelim != -1) {
                    // + 1 - don't include whitespace
                    classWildCard = restOfExpr.substring (classDelim + 1);
                    restOfExpr = restOfExpr.substring (0, classDelim);
                } else {
                    classWildCard = restOfExpr;
                    restOfExpr = null;
                }

                // if there is no package specified - allow any
                if (classWildCard.indexOf (Constants.PACKAGE_STD_DELIM) == -1
                    && !classWildCard.startsWith (WildCard.WILDCARD_STR)) {
                    classWildCard = WildCard.WILDCARD_STR +
                        Constants.PACKAGE_STD_DELIM + classWildCard;
                }
            }
        }

        // -- method return type --

        restOfExpr = returnExpr;
        // remove whitespace for next parsing
        if (restOfExpr != null) {
            restOfExpr = restOfExpr.trim ();
            if (!restOfExpr.isEmpty ()) {
                // no whitespace in restOfExpr
                if (containsWhiteSpace (restOfExpr)) {
                    throw new ScopeParserException ("Cannot parse scope \""
                        + scopeExpression + "\"");
                }

                returnWildCard = restOfExpr;
            }
        }
    }


    @Override
    public boolean matches (String className, final String methodName,
        final String methodDesc) {
        // write(className, methodName, methodDesc);

        // -- match class (with package) --

        // replace delimiters for matching
        className = className.replace (
            Constants.PACKAGE_INTERN_DELIM, Constants.PACKAGE_STD_DELIM);

        // if className has default package (nothing), add our default package
        // reasons:
        // 1) we can restrict scope on default package by putting our default
        // package into scope
        // 2) default package would not be matched if no package was specified
        // in the scope (because of substitution made)
        if (className.indexOf (Constants.PACKAGE_STD_DELIM) == -1) {
            className = DEFAULT_PKG + Constants.PACKAGE_STD_DELIM + className;
        }

        if (classWildCard != null
            && !WildCard.match (className, classWildCard)) {
            return false;
        }

        // -- match method name --

        if (methodWildCard != null
            && !WildCard.match (methodName, methodWildCard)) {
            return false;
        }

        // -- match parameters --

        if (paramsWildCard != null) {

            // get parameters and match one by one
            final Type [] parameters = Type.getArgumentTypes (methodDesc);

            // get last param
            String lastParamWC = null;
            if (!paramsWildCard.isEmpty ()) {
                lastParamWC = paramsWildCard.get (paramsWildCard.size () - 1);
            }

            // if the last param is not PARAM_MATCH_REST then test for equal
            // size
            if (!PARAM_MATCH_REST.equals (lastParamWC) &&
                parameters.length != paramsWildCard.size ()) {
                return false;
            }

            // not enough parameters
            if (PARAM_MATCH_REST.equals (lastParamWC) &&
                parameters.length < paramsWildCard.size () - 1) {
                return false;
            }

            for (int i = 0; i < parameters.length; ++i) {

                final String paramWC = paramsWildCard.get (i);

                // if there is PARAM_MATCH_REST then stop
                // works even if there is no additional parameter
                if (paramWC.equals (PARAM_MATCH_REST)) {
                    break;
                }

                final String typeName = parameters [i].getClassName ();

                if (!WildCard.match (typeName, paramWC)) {
                    return false;
                }
            }
        }

        // -- match return type --

        if (returnWildCard != null) {
            final Type returnType = Type.getReturnType (methodDesc);
            final String typeName = returnType.getClassName ();

            if (!WildCard.match (typeName, returnWildCard)) {
                return false;
            }
        }

        return true;
    }


    @Override
    public String toString () {
        final StringBuilder params = new StringBuilder ();
        if (paramsWildCard != null) {
            params.append ("(");

            String delim = "";
            for (final String param : paramsWildCard) {
                params.append (delim);
                params.append (param);
                delim = ", ";
            }

            params.append (")");
        }

        return String.format ("r=%s c=%s m=%s p=%s",
            returnWildCard, classWildCard, methodWildCard, params.toString ()
        );
    }
}
