package ch.usi.dag.disl.testtools.agent;

import java.lang.instrument.Instrumentation;

public class Agent {

	// INSTRUCTIONS: run ant agent
	// under Eclipse create runner for desired target class
	// add these jvm parameters (example)
	// -javaagent:build/eclipse-agent.jar
	// -Ddisl.classes=bin/ch/usi/dag/disl/test/suite/bodymarker/instr/DiSLClass.class

    /**
     * Premain method is a method called when the agent is loaded. As the name
     * says, the method is called before the application main method.
     *
     * @param agentArguments
     * @param instrumentation
     */
    public static void premain (final String agentArguments,
        final Instrumentation instrumentation) {

        instrumentation.addTransformer(new Transformer());
    }

}
