package ch.usi.dag.disl.weaver.pe;

import org.objectweb.asm.tree.analysis.Value;

public class ConstValue implements Value {

    public final static Object NULL = new Object();

    /**
     * The size of this value.
     */
    public final int size;

    public Object cst;

    public ConstValue(int size) {
        this(size, null);
    }

    public ConstValue(int size, Object cst) {
        this.size = size;
        this.cst = cst;
    }

    @Override
    public int getSize() {
        return size;
    }

}
