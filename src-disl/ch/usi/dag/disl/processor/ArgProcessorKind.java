package ch.usi.dag.disl.processor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.exception.DiSLFatalException;


public enum ArgProcessorKind {

    BOOLEAN (Type.BOOLEAN_TYPE),

    BYTE (Type.BYTE_TYPE) {
        @Override
        public EnumSet <ArgProcessorKind> secondaryTypes () {
            return EnumSet.of (ArgProcessorKind.BOOLEAN);
        }
    },

    CHAR (Type.CHAR_TYPE),
    DOUBLE (Type.DOUBLE_TYPE),
    FLOAT (Type.FLOAT_TYPE),

    INT (Type.INT_TYPE) {
        @Override
        public Set <ArgProcessorKind> secondaryTypes () {
            return EnumSet.of (BOOLEAN, BYTE, SHORT);
        }
    },

    LONG (Type.LONG_TYPE),

    SHORT (Type.SHORT_TYPE) {
        @Override
        public Set <ArgProcessorKind> secondaryTypes () {
            return EnumSet.of (BOOLEAN, BYTE);
        }
    },

    OBJECT (Type.getType (Object.class));

    private final Type __primaryType;

    //

    private ArgProcessorKind (final Type primaryType) {
        __primaryType = primaryType;
    }


    public Type primaryType () {
        return __primaryType;
    }

    public Set <ArgProcessorKind> secondaryTypes () {
        return Collections.emptySet ();
    }

    public static ArgProcessorKind valueOf (final Type type) {
        if (type == null) {
            throw new DiSLFatalException ("conversion from <null> not defined");
        }

        //
        // Try to find a primitive type match first.
        //
        for (final ArgProcessorKind kind : values ()) {
            if (kind.__primaryType.equals (type)) {
                return kind;
            }
        }

        //
        // Handle objects and arrays based on the sort.
        //
        final int sort = type.getSort ();
        if (sort == Type.OBJECT || sort == Type.ARRAY) {
            return OBJECT;
        }

        throw new DiSLFatalException (
            "conversion from %s not defined", type.getClassName ()
        );
    }


    public static ArgProcessorKind forMethod (final MethodNode method) {
        final Type [] argTypes = Type.getArgumentTypes (method.desc);
        if (argTypes.length > 0) {
            return valueOf (argTypes [0]);
        } else {
            return null;
        }
    }

}
