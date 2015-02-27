package ch.usi.dag.disl.test.suite.dispatch.junit;

import java.io.IOException;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ch.usi.dag.disl.test.suite.ShadowVmTest;
import ch.usi.dag.disl.test.utils.ClientServerEvaluationRunner;


@RunWith (JUnit4.class)
public class DispatchTest extends ShadowVmTest {

    @Override
    protected void _checkOutErr (
        final ClientServerEvaluationRunner runner
    ) throws IOException {
        runner.assertShadowOut ("evaluation.out.resource");
    }

}
