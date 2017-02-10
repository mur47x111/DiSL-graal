package ch.usi.dag.disl.test.suite.after2.instr;

import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.annotation.ThreadLocal;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.staticcontext.uid.RandomMethodUid;
import ch.usi.dag.disl.test.suite.after2.instr.runtime.Analysis;

import java.util.Stack;

public class DiSLClass {
    @ThreadLocal
    static Stack<Integer> stackTL;

    @Before(marker = BodyMarker.class, order = 0, scope = "*.*", guard = NotInitNorClinit.class)
    public static void onMethodEntryObjectArgs(final RandomMethodUid id) {
        Stack<Integer> thisStack;
        if((thisStack = stackTL) == null) {
            thisStack = (stackTL = new Stack<Integer>());
        }
        thisStack.push(id.get());
    }

    @After(marker = BodyMarker.class, order = 0, scope = "*.*", guard = NotInitNorClinit.class)
    public static void onMethodExit(final RandomMethodUid id) {
        Analysis.onExit(stackTL, id.get());
    }
}
