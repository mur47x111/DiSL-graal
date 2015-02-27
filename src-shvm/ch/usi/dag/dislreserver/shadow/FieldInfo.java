package ch.usi.dag.dislreserver.shadow;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;

public class FieldInfo {

    // TODO ! is this implementation of methods really working ??

    private FieldNode fieldNode;
    private int modifiers;
    private String name;
    private String type;

    public FieldInfo(FieldNode fieldNode) {

        this.fieldNode = fieldNode;
        name = fieldNode.name;
        type = fieldNode.desc;
        modifiers = fieldNode.access;
    }

    public FieldNode getFieldNode() {
        return fieldNode;
    }

    public String getName() {
        return name;
    }

    public int getModifiers() {
        return modifiers;
    }

    public String getType() {
        return type;
    }

    public boolean isPublic() {
        return (modifiers & Opcodes.ACC_PUBLIC) != 0;
    }
}
