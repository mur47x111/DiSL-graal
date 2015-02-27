package ch.usi.dag.disl.test.suite;

import java.io.IOException;

import ch.usi.dag.disl.test.utils.ClientServerEvaluationRunner;
import ch.usi.dag.disl.test.utils.Runner;


public abstract class ShadowVmTest extends BaseTest {

    @Override
    protected Runner _createRunner () {
        return new ClientServerEvaluationRunner (this.getClass ());
    }


    @Override
    protected final void _checkOutErr (final Runner runner) throws IOException {
        _checkOutErr ((ClientServerEvaluationRunner) runner);
    }


    protected void _checkOutErr (
        final ClientServerEvaluationRunner runner
    ) throws IOException {
        runner.assertClientOut ("client.out.resource");
        runner.assertShadowOut ("evaluation.out.resource");
    }

}
