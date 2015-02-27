package ch.usi.dag.disl.util.cfg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;

import ch.usi.dag.disl.exception.DiSLFatalException;

public class BasicBlock implements Iterable<AbstractInsnNode>{
    // index of the basic block, count according to the order in a method
    private int index;

    private AbstractInsnNode entrance;
    private AbstractInsnNode exit;

    private Set<BasicBlock> predecessors;
    private Set<BasicBlock> successors;

    // joins refer to the join point of a new cfg to an existing cfg in the
    // same method. NOTE that an exception handler is regarded as a new cfg
    // but not included in the normal execution cfg
    private Set<BasicBlock> joins;

    public BasicBlock(int index, AbstractInsnNode entrance,
            AbstractInsnNode exit) {
        this.index = index;
        this.entrance = entrance;
        this.exit = exit;

        successors = new HashSet<BasicBlock>();
        predecessors = new HashSet<BasicBlock>();
        joins = new HashSet<BasicBlock>();
    }

    public int getIndex() {
        return index;
    }

    public AbstractInsnNode getEntrance() {
        return entrance;
    }

    public void setExit(AbstractInsnNode exit) {
        this.exit = exit;
    }

    public AbstractInsnNode getExit() {
        return exit;
    }

    public Set<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public Set<BasicBlock> getSuccessors() {
        return successors;
    }

    public Set<BasicBlock> getJoins() {
        return joins;
    }

    class BasicBlockIterator implements Iterator<AbstractInsnNode> {

        private AbstractInsnNode current;

        public BasicBlockIterator() {
            current = entrance;
        }

        @Override
        public boolean hasNext() {
            return current != exit.getNext();
        }

        @Override
        public AbstractInsnNode next() {
            AbstractInsnNode temp = current;
            current = current.getNext();
            return temp;
        }

        @Override
        public void remove() {
            throw new DiSLFatalException("Readonly iterator.");
        }

    }

    @Override
    public Iterator<AbstractInsnNode> iterator() {
        return new BasicBlockIterator();
    }
}
