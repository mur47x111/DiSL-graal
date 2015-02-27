package ch.usi.dag.disl.test.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.usi.dag.disl.exception.ScopeParserException;
import ch.usi.dag.disl.scope.Scope;
import ch.usi.dag.disl.scope.ScopeImpl;

public class ScopeTest {

    // smoke tests

    @Test
    public void testSimple()
            throws ScopeParserException {
        Scope s = new ScopeImpl("my.pkg.TargetClass.main()");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "main", "()V"));
    }

    @Test
    public void testComplete()
            throws ScopeParserException {
        Scope s = new ScopeImpl("java.lang.String my.pkg.TargetClass.main(java.lang.String[])");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "main", "([Ljava.lang.String;)Ljava.lang.String;"));
    }

    // method tests

    @Test
    public void testMethodWildCard()
            throws ScopeParserException {
        Scope s = new ScopeImpl("java.lang.String my.pkg.TargetClass.*main(java.lang.String[])");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "blablablamain", "([Ljava.lang.String;)Ljava.lang.String;"));
    }

    @Test
    public void testMethodAllWildCard()
            throws ScopeParserException {
        Scope s = new ScopeImpl("my.pkg.TargetClass.*");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "clinit", "()V"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "init", "()V"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method_init", "()V"));
    }

    @Test
    public void testMethodInitWildCard()
            throws ScopeParserException {
        Scope s = new ScopeImpl("my.pkg.TargetClass.*init");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "clinit", "()V"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "init", "()V"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method_init", "()V"));
    }

    // return tests

    @Test
    public void testReturnAllWildCard()
            throws ScopeParserException {
        Scope s = new ScopeImpl("* my.pkg.TargetClass.method");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()V"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()I"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()Ljava.lang.String;"));
    }

    @Test
    public void testReturnStartSepStringWildCard()
            throws ScopeParserException {
        Scope s = new ScopeImpl("*.String my.pkg.TargetClass.method");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()Ljava.lang.String;"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()Lmy.package.String;"));
    }

    @Test
    public void testReturnStartNoSepStringWildCard()
            throws ScopeParserException {
        Scope s = new ScopeImpl("*String my.pkg.TargetClass.method");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()Ljava.lang.String;"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()Lmy.package.String;"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()Lmy.package.BigString;"));
    }

    // classname tests

    /**
     * FIXED
     * 
     * input:
     * java.lang.String main()
     * 
     * result:
     * r=null c=java.lang.String m=main p=()
     * 
     * correct:
     * r=java.lang.String c=null m=main p=()
     */
    @Test
    public void testMissingClassName()
            throws ScopeParserException {
        Scope s = new ScopeImpl("java.lang.String main()");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "main", "()Ljava.lang.String;"));
    }

    /**
     * FIXED
     * 
     * input:
     * java.*.String main()
     * 
     * result:
     * r=null c=java.* m=String main p=()
     * 
     * correct:
     * r=java.*.String c=null m=main p=()
     */
    @Test
    public void testMissingClassName2()
            throws ScopeParserException {
        Scope s = new ScopeImpl("java.*.String main()");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "main", "()Ljava.lang.String;"));
    }

    @Test
    public void testClassAllPackages()
            throws ScopeParserException {
        Scope s = new ScopeImpl("TargetClass.method");
        assertTrue(s.toString(), s.matches("TargetClass", "method", "()V"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()V"));
    }

    @Test
    public void testClassDefaultPackage()
            throws ScopeParserException {
        Scope s = new ScopeImpl("[default].TargetClass.method");
        assertTrue(s.toString(), s.matches("TargetClass", "method", "()V"));
        assertFalse(s.toString(), s.matches("my/pkg/TargetClass", "method", "()V"));
    }

    @Test
    public void testClassWildCard()
            throws ScopeParserException {
        Scope s = new ScopeImpl("my.pkg.*TargetClass.method");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()V"));
        assertTrue(s.toString(), s.matches("my/pkg/pkg/TargetClass", "method", "()V"));
        assertTrue(s.toString(), s.matches("my/pkg/AnotherTargetClass", "method", "()V"));
        assertTrue(s.toString(), s.matches("my/pkg/pkg/AnotherTargetClass", "method", "()V"));
    }

    // parameter tests

    @Test
    public void testParameterAllRandom()
            throws ScopeParserException {
        Scope s = new ScopeImpl("my.pkg.TargetClass.method(..)");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()V"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "(I)V"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "([I)V"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "([Ljava.lang.String;[I[I[I)V"));
    }

    @Test
    public void testParameterNone()
            throws ScopeParserException {
        Scope s = new ScopeImpl("my.pkg.TargetClass.method()");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()V"));
        assertFalse(s.toString(), s.matches("my/pkg/TargetClass", "method", "(I)V"));
        assertFalse(s.toString(), s.matches("my/pkg/TargetClass", "method", "([I)V"));
        assertFalse(s.toString(), s.matches("my/pkg/TargetClass", "method", "([Ljava.lang.String;[I[I[I)V"));
    }

    /**
     * FIXED
     * 
     * details:
     * (int, int, int, ..) should not match (I)V
     */
    @Test
    public void testParameterEndRandom()
            throws ScopeParserException {
        Scope s = new ScopeImpl("my.pkg.TargetClass.method(int, int, int, ..)");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "(III)V"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "(IIII)V"));
        assertFalse(s.toString(), s.matches("my/pkg/TargetClass", "method", "(I)V"));
    }

    // complete tests

    @Test
    public void testCompleteAllReturnPattern()
            throws ScopeParserException {
        Scope s = new ScopeImpl("int *");
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "()I"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "(I)I"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "(Ljava.lang.String)I"));
        assertTrue(s.toString(), s.matches("TargetClass", "method", "()I"));
    }

    @Test
    public void testCompleteAllAcceptPattern()
            throws ScopeParserException {
        Scope s = new ScopeImpl("*(int, int, int)");
        assertTrue(s.toString(), s.matches("TargetClass", "method", "(III)I"));
        assertTrue(s.toString(), s.matches("my/pkg/TargetClass", "method", "(III)V"));
        assertFalse(s.toString(), s.matches("my/pkg/TargetClass", "method", "(II)I"));
        assertFalse(s.toString(), s.matches("my/pkg/TargetClass", "method", "(IIII)I"));
        assertFalse(s.toString(), s.matches("my/pkg/TargetClass", "method", "(Ljava.lang.String;)I"));
    }

    @Test(expected=ScopeParserException.class)
    public void cannotCreateEmptyScope() throws ScopeParserException {
        final Scope s = new ScopeImpl ("");
    }
}
