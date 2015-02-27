package ch.usi.dag.disl.processor.generator;

import org.objectweb.asm.Type;

import ch.usi.dag.disl.coderep.Code;
import ch.usi.dag.disl.processor.ArgProcessorKind;


public class ProcMethodInstance {

    private final int argIndex;
    private final Type argType;
    private final int argsCount;

    private final ArgProcessorKind kind;
    private final Code code;

    //

    public ProcMethodInstance (
        final int argIndex, final Type argType,
        final int argsCount, final ArgProcessorKind kind,
        final Code code
    ) {
        this.argIndex = argIndex;
        this.argType = argType;
        this.argsCount = argsCount;

        this.kind = kind;
        this.code = code;
    }


    public int getArgIndex () {
        return argIndex;
    }


    public Type getArgType () {
        return argType;
    }


    public int getArgsCount () {
        return argsCount;
    }


    public ArgProcessorKind getKind () {
        return kind;
    }

    // Note: Code is NOT cloned for each ProcMethodInstance.
    // If the weaver does not rely on this, we can reuse processor instances
    // which can save us some computation
    public Code getCode () {
        return code;
    }

}
