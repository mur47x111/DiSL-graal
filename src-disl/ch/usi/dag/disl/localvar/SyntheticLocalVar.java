package ch.usi.dag.disl.localvar;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;

import ch.usi.dag.disl.annotation.SyntheticLocal;


public class SyntheticLocalVar extends AbstractLocalVar {

    private SyntheticLocal.Initialize initialize;

    private InsnList initCode;

    //

    public SyntheticLocalVar (
        String className, String fieldName, Type type,
        SyntheticLocal.Initialize initialize
    ) {
        super (className, fieldName, type);
        this.initialize = initialize;
    }


    public SyntheticLocal.Initialize getInitialize () {
        return initialize;
    }


    public InsnList getInitCode () {
        return initCode;
    }

    public boolean hasInitCode () {
        return initCode != null;
    }

    public void setInitCode (final InsnList initCode) {
        this.initCode = initCode;
    }
}
