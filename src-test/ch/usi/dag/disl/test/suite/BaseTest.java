package ch.usi.dag.disl.test.suite;

import java.io.IOException;

import org.junit.After;
import org.junit.Test;

import ch.usi.dag.disl.test.utils.Runner;


public abstract class BaseTest {

    private final Runner __runner = _createRunner ();

    protected abstract Runner _createRunner ();


    //

    @Test
    public void test () throws Exception {
        __runner.start ();
        __runner.waitFor ();

        //

        __runner.assertIsFinished ();
        if (Boolean.getBoolean ("test.verbose")) {
            __runner.destroyIfRunningAndDumpOutputs ();
        }

        __runner.assertIsSuccessful ();

        //

        _checkOutErr (__runner);
        __runner.assertRestOutErrEmpty ();
    }


    protected abstract void _checkOutErr (Runner runner) throws IOException;


    //

    @After
    public void cleanup () {
        __runner.destroy ();
    }

}
