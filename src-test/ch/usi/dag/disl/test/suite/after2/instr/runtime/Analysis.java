package ch.usi.dag.disl.test.suite.after2.instr.runtime;

import java.util.Stack;

public class Analysis {
    public static void onExit(Stack<Integer> stack, int methodUID) {
        int topOfTheStack = stack.pop();
        if (topOfTheStack != methodUID) {
        	System.err.println("After method: " + methodUID);
        	int size = stack.size();
        	System.err.println("Stack[" + size + "]: " + topOfTheStack);
        	for(int i = size - 1; i >= 0; i--) {
        		System.err.println("Stack[" + i + "]: " + stack.elementAt(i));
        	}
        	System.exit(1);
    	}
    }
}
