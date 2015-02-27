package ch.usi.dag.disl.test.suite.after2.instr;

import ch.usi.dag.disl.annotation.GuardMethod;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class NotInitNorClinit {
    
	@GuardMethod
    public static boolean isApplicable(MethodStaticContext msc) {
        return (msc.thisMethodName().endsWith("init>")) ? false : true;
    }
}
