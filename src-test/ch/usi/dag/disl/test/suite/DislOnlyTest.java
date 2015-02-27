package ch.usi.dag.disl.test.suite;

import java.io.IOException;

import ch.usi.dag.disl.test.utils.ClientServerRunner;
import ch.usi.dag.disl.test.utils.Runner;


public abstract class DislOnlyTest extends BaseTest {

    @Override
    protected Runner _createRunner () {
        return new ClientServerRunner (this.getClass ());
    }


    @Override
    protected final void _checkOutErr (final Runner runner) throws IOException {
        _checkOutErr ((ClientServerRunner) runner);
    }


    protected void _checkOutErr (final ClientServerRunner runner) throws IOException {
        runner.assertClientOut ("client.out.resource");
    }

}
