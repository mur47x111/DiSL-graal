package ch.usi.dag.disl.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import ch.usi.dag.disl.exception.DiSLFatalException;


public abstract class AsmHelper {

    public static boolean offsetBefore(final InsnList ilst, final int from, final int to) {
        if (from >= to) {
            return false;
        }

        for (int i = from; i < to; i++) {
            if (ilst.get(i).getOpcode() != -1) {
                return true;
            }
        }

        return false;
    }


    public static AbstractInsnNode loadConst (final Object value) {
        if (value instanceof Boolean) {
            return new InsnNode (
                ((Boolean) value) ? Opcodes.ICONST_1 : Opcodes.ICONST_0
            );

        } else if (
            value instanceof Byte ||
            value instanceof Short ||
            value instanceof Integer
        ) {
            final int intValue = ((Number) value).intValue ();

            if (-1 <= intValue && intValue <= 5) {
                // The opcodes from ICONST_M1 to ICONST_5 are consecutive.
                return new InsnNode (Opcodes.ICONST_0 + intValue);
            } else if (Byte.MIN_VALUE <= intValue && intValue <= Byte.MAX_VALUE) {
                return new IntInsnNode (Opcodes.BIPUSH, intValue);
            } else if (Short.MIN_VALUE <= intValue && intValue <= Short.MAX_VALUE) {
                return new IntInsnNode (Opcodes.SIPUSH, intValue);
            } else {
                // Force use of LDC with an Integer argument.
                return new LdcInsnNode (Integer.valueOf (intValue));
            }

        } else if (value instanceof Long) {
            final long longValue = ((Long) value).longValue ();

            if (longValue == 0) {
                return new InsnNode (Opcodes.LCONST_0);
            } else if (longValue == 1) {
                return new InsnNode (Opcodes.LCONST_1);
            }

            // default to LDC

        } else if (value instanceof Float) {
            final float floatValue = ((Float) value).floatValue ();

            if (floatValue == 0) {
                return new InsnNode (Opcodes.FCONST_0);
            } else if (floatValue == 1) {
                return new InsnNode (Opcodes.FCONST_1);
            } else if (floatValue == 2) {
                return new InsnNode (Opcodes.FCONST_2);
            }

            // default to LDC

        } else if (value instanceof Double) {
            final double doubleValue = ((Double) value).doubleValue ();

            if (doubleValue == 0) {
                return new InsnNode (Opcodes.DCONST_0);
            } else if (doubleValue == 1) {
                return new InsnNode (Opcodes.DCONST_1);
            }

            // default to LDC
        }

        return new LdcInsnNode (value);
    }


    public static String getStringConstOperand (final AbstractInsnNode insn) {
        if (Insn.LDC.matches (insn)) {
            final LdcInsnNode ldcNode = (LdcInsnNode) insn;
            if (ldcNode.cst instanceof String) {
                return (String) ldcNode.cst;
            }
        }

        // Not a String literal load instruction.
        return null;
    }


    public static <T extends Enum <T>> T getEnumConstOperand (
        final Class <T> enumType, final AbstractInsnNode insn
    ) {
        if (enumType.isEnum () && Insn.GETSTATIC.matches (insn)) {
            final FieldInsnNode fieldInsn = (FieldInsnNode) insn;

            final String enumTypeName = Type.getInternalName (enumType);
            if (enumTypeName.equals (fieldInsn.owner)) {
                final String expectedName = fieldInsn.name;
                for (final T value : enumType.getEnumConstants ()) {
                    if (value.name ().equals (expectedName)) {
                        return value;
                    }
                }
            }
        }

        // Not an Enum<T> const operand.
        return null;
    }


    public static Integer getIntConstOperand (final AbstractInsnNode insn) {
        final int opcode = insn.getOpcode();
        if (Opcodes.ICONST_M1 <= opcode && opcode <= Opcodes.ICONST_5) {
            // The opcodes from ICONST_M1 to ICONST_5 are consecutive.
            return opcode - Opcodes.ICONST_0;

        } else if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) {
            return ((IntInsnNode) insn).operand;

        } else if (opcode == Opcodes.LDC) {
            final LdcInsnNode ldc = (LdcInsnNode) insn;
            if (ldc.cst instanceof Integer) {
                return (Integer) ldc.cst;
            }
        }

        // Not an integer literal load instruction.
        return null;
    }


    @SuppressWarnings ("serial")
    public static final Map <String, Type> PRIMITIVE_TYPES = new HashMap <String, Type> () {
        {
            put (Boolean.class, Type.BOOLEAN_TYPE);
            put (Byte.class, Type.BYTE_TYPE);
            put (Character.class, Type.CHAR_TYPE);
            put (Short.class, Type.SHORT_TYPE);
            put (Integer.class, Type.INT_TYPE);
            put (Float.class, Type.FLOAT_TYPE);
            put (Long.class, Type.LONG_TYPE);
            put (Double.class, Type.DOUBLE_TYPE);
        }

        void put (final Class <?> typeClass, final Type type) {
            put (Type.getInternalName (typeClass), type);
        }
    };


    /**
     * Returns a {@link Type} instance corresponding to a class literal if the
     * given instruction loads a type constant on the stack, otherwise returns
     * {@code null}.
     *
     * @param insn the instruction to check
     * @return {@link Type} corresponding to the loaded type constant, or
     *         {@code null}.
     */
    public static Type getTypeConstOperand (final AbstractInsnNode insn) {
        //
        // Class literals are loaded on the stack in two ways:
        //
        // Literals for common classes are loaded using the LDC instruction,
        // referencing a constant pool item.
        //
        // Literals for primitive types are loaded by accessing the static
        // TYPE field in the corresponding boxing class for the type. The
        // field is a specialized Class<> instance, so to determine the type,
        // we need to look at the owner.
        //
        if (Insn.LDC.matches (insn)) {
            final LdcInsnNode ldcNode = (LdcInsnNode) insn;
            if (ldcNode.cst instanceof Type) {
                return (Type) ldcNode.cst;
            }

        } else if (Insn.GETSTATIC.matches (insn)) {
            //
            // When accessing the static TYPE field in a class boxing a
            // primitive type, the result is the primitive type class literal.
            //
            final FieldInsnNode fieldNode = (FieldInsnNode) insn;
            if ("TYPE".equals (fieldNode.name)) {
                return PRIMITIVE_TYPES.get (fieldNode.owner);
            }
        }

        // Not a class literal load.
        return null;
    }


    public static VarInsnNode loadThis () {
        return loadObjectVar (0);
    }


    public static VarInsnNode loadObjectVar (final int slot) {
        return loadVar (Type.getType (Object.class), slot);
    }


    public static VarInsnNode loadVar (final Type type, final int slot) {
        return new VarInsnNode (type.getOpcode (Opcodes.ILOAD), slot);
    }


    public static VarInsnNode storeObjectVar (final int slot) {
        return storeVar (Type.getType (Object.class), slot);
    }


    public static VarInsnNode storeVar (final Type type, final int slot) {
        return new VarInsnNode (type.getOpcode (Opcodes.ISTORE), slot);
    }


    public static InsnNode loadNull () {
        return loadDefault (Type.getType (Object.class));
    }


    public static InsnNode loadDefault (final Type type) {
        switch (type.getSort ()) {
        case Type.BOOLEAN:
        case Type.BYTE:
        case Type.CHAR:
        case Type.INT:
        case Type.SHORT:
            return new InsnNode (Opcodes.ICONST_0);
        case Type.LONG:
            return new InsnNode (Opcodes.LCONST_0);
        case Type.FLOAT:
            return new InsnNode (Opcodes.FCONST_0);
        case Type.DOUBLE:
            return new InsnNode (Opcodes.DCONST_0);
        case Type.OBJECT:
            // XXX LB: consider putting Type.ARRAY here as well
            return new InsnNode (Opcodes.ACONST_NULL);
        default:
            throw new DiSLFatalException (
                "No default value for type: "+ type.getDescriptor ()
            );
        }
    }

    public static TypeInsnNode checkCast (final Type type) {
        return new TypeInsnNode (Opcodes.CHECKCAST, type.getDescriptor ());
    }

    //

    public static FieldInsnNode getField (
        final Type owner, final String name, final Type desc
    ) {
        return getField (owner.getInternalName (), name, desc.getDescriptor ());
    }

    public static FieldInsnNode getField (
        final Type ownerType, final String name, final String desc
    ) {
        return getField (ownerType.getInternalName (), name, desc);
    }


    public static FieldInsnNode getField (
        final String owner, final String name, final String desc
    ) {
        return new FieldInsnNode (Opcodes.GETFIELD, owner, name, desc);
    }


    public static FieldInsnNode putField (
        final Type ownerType, final String name, final String desc
    ) {
        return putField (ownerType.getInternalName (), name, desc);
    }


    public static FieldInsnNode putField (
        final String owner, final String name, final String desc
    ) {
        return new FieldInsnNode (Opcodes.PUTFIELD, owner, name, desc);
    }

    //

    public static FieldInsnNode getStatic (final Field field) {
        return getStatic (
            Type.getInternalName (field.getDeclaringClass ()),
            field.getName (), Type.getDescriptor (field.getType ())
        );
    }

    public static FieldInsnNode getStatic (
        final Type owner, final String name, final Type desc
    ) {
        return getStatic (owner.getInternalName (), name, desc.getDescriptor ());
    }

    public static FieldInsnNode getStatic (
        final String owner, final String name, final String desc
    ) {
        return new FieldInsnNode (Opcodes.GETSTATIC, owner, name, desc);
    }

    //

    public static FieldInsnNode putStatic (
        final String owner, final String name, final String desc
    ) {
        return new FieldInsnNode (Opcodes.PUTSTATIC, owner, name, desc);
    }


    public static JumpInsnNode jumpTo (final LabelNode target) {
        return new JumpInsnNode (Opcodes.GOTO, target);
    }

    //

    public static MethodInsnNode invokeStatic (final Method method) {
        return invokeStatic (
            Type.getInternalName (method.getDeclaringClass ()),
            method.getName (), Type.getMethodDescriptor (method)
        );
    }

    public static MethodInsnNode invokeStatic (
        final Type ownerType, final String methodName, final Type methodType
    ){
        return invokeStatic (
            ownerType.getInternalName (), methodName, methodType.getDescriptor ()
        );
    }

    private static MethodInsnNode invokeStatic (
        final String ownerName, final String methodName, final String methodDesc
    ) {
        return new MethodInsnNode (
            Opcodes.INVOKESTATIC, ownerName, methodName, methodDesc, false
        );
    }

    //

    public static MethodInsnNode invokeVirtual (final Method method) {
        return invokeVirtual (
            Type.getInternalName (method.getDeclaringClass ()),
            method.getName (), Type.getMethodDescriptor (method)
        );
    }

    public static MethodInsnNode invokeVirtual (
        final Type ownerType, final String methodName, final Type methodType
    ) {
        return invokeVirtual (
            ownerType.getInternalName (), methodName, methodType.getDescriptor ()
        );
    }


    private static MethodInsnNode invokeVirtual (
        final String ownerName, final String methodName, final String methodDesc
    ) {
        return new MethodInsnNode (
            Opcodes.INVOKEVIRTUAL, ownerName, methodName, methodDesc, false
        );
    }

    //

    public static int getParameterSlot (
        final MethodNode method, final int paramIndex
    ) {
        final Type [] paramTypes = Type.getArgumentTypes (method.desc);
        if (paramIndex >= paramTypes.length) {
            throw new DiSLFatalException ("parameter index out of bounds");
        }

        final boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
        int slot = isStatic ? 0 : 1;

        for (int i = 0; i < paramIndex; i++) {
            // add number of occupied slots
            slot += paramTypes [i].getSize ();
        }

        return slot;
    }


    /**
     * Returns the number of local slots occupied by parameters of the given
     * method.
     */
    public static int getParameterSlotCount (final MethodNode method) {
        final Type [] types = Type.getArgumentTypes (method.desc);
        final boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;

        int result = isStatic ? 0 : 1;
        for (final Type type : types) {
            result += type.getSize ();
        }

        return result;
    }

    //

    /**
     * Clones a method node, including all code, try-catch blocks, and
     * annotations. This is actually faster than cloning just the code and the
     * try-catch blocks by hand.
     *
     * @param method
     *        {@link MethodNode} to clone
     * @return a new instance of {@link MethodNode}
     */
    public static MethodNode cloneMethod (final MethodNode method) {
        final MethodNode result = new MethodNode (
            Opcodes.ASM5, method.access, method.name, method.desc,
            method.signature, method.exceptions.toArray (
                new String [method.exceptions.size ()]
            )
        );

        method.accept (result);
        return result;
    }


    public static final class ClonedCode {
        private final InsnList __instructions;
        private final List <TryCatchBlockNode> __tryCatchBlocks;


        public ClonedCode (
            final InsnList instructions,
            final List <TryCatchBlockNode> tryCatchBlocks
        ) {
            __instructions = instructions;
            __tryCatchBlocks = tryCatchBlocks;
        }

        //

        public InsnList getInstructions () {
            return __instructions;
        }

        public List <TryCatchBlockNode> getTryCatchBlocks () {
            return __tryCatchBlocks;
        }

        //

        public static ClonedCode create (
            final InsnList instructions,
            final List <TryCatchBlockNode> tryCatchBlocks
        ) {
            final Map <LabelNode, LabelNode> replacementLabels =
                __createReplacementLabelMap (instructions);

            return new ClonedCode (
                __cloneInstructions (instructions, replacementLabels),
                __cloneTryCatchBlocks (tryCatchBlocks, replacementLabels)
            );
        }
    }


    /**
     * Returns a clone of the given instruction list.
     *
     * @param insnList
     *     the instruction list to clone
     *
     * @return
     *     a clone of the instruction list
     */
    public static InsnList cloneInstructions (final InsnList insnList) {
        //
        // To clone an instruction list, we have to clone the labels and
        // use the cloned labels to clone the individual instructions.
        //
        return __cloneInstructions (insnList, __createReplacementLabelMap (insnList));
    }


    private static Map <LabelNode, LabelNode> __createReplacementLabelMap (
        final InsnList insnList
    ) {
        //
        // Clone all the labels and key them to the original label.
        // LB: Consider using an instruction filter.
        //
        final Map <LabelNode, LabelNode> result = new HashMap <LabelNode, LabelNode> ();
        for (final AbstractInsnNode insn : Insns.selectAll (insnList)) {
            if (insn instanceof LabelNode) {
                final LabelNode clone = new LabelNode (new Label ());
                final LabelNode original = (LabelNode) insn;
                result.put (original, clone);
            }
        }

        return result;
    }


    private static InsnList __cloneInstructions (
        final InsnList insnList,
        final Map <LabelNode, LabelNode> replacementLabels
    ) {
        //
        // Clone individual instructions using the clone label map.
        // LB: This is typical map-reduce
        //
        final InsnList result = new InsnList ();
        for (final AbstractInsnNode insn : Insns.selectAll (insnList)) {
            result.add (insn.clone (replacementLabels));
        }

        return result;
    }


    private static List <TryCatchBlockNode> __cloneTryCatchBlocks (
        final List <TryCatchBlockNode> tryCatchBlocks,
        final Map <LabelNode, LabelNode> replacementLabels
    ) {
        final List <TryCatchBlockNode> result = new LinkedList <TryCatchBlockNode> ();
        for (final TryCatchBlockNode tcb : tryCatchBlocks) {
            final TryCatchBlockNode tcbClone = new TryCatchBlockNode (
                replacementLabels.get (tcb.start),
                replacementLabels.get (tcb.end),
                replacementLabels.get (tcb.handler),
                tcb.type
            );
            result.add (tcbClone);
        }

        return result;
    }

    //

    /**
     * Provides utility methods for instructions in an instruction list.
     */
    public enum Insns {
        REVERSE {
            @Override
            protected AbstractInsnNode _next (final AbstractInsnNode insn) {
                return insn.getPrevious ();
            }
        },

        FORWARD {
            @Override
            protected AbstractInsnNode _next (final AbstractInsnNode insn) {
                return insn.getNext ();
            }
        };

        /**
         * Traverses the instruction list, starting at the given instruction,
         * and returns the first real (non-virtual) instruction encountered, or
         * {@code null} if none was found before reaching the end of the
         * instruction list. If the starting instruction itself is a real
         * instruction, it is returned and no iteration is performed.
         *
         * @param start
         *        the starting instruction
         * @return The first real (non-virtual) instruction in the suffix of the
         *         instruction list starting at the given instruction (including
         *         the starting instruction), or {@code null} if there is no
         *         such instruction.
         */
        public AbstractInsnNode firstRealInsn (final AbstractInsnNode start) {
            AbstractInsnNode insn = start;
            while (insn != null && Insn.isVirtual (insn)) {
                insn = _next (insn);
            }

            return insn;
        }


        /**
         * Traverses the instruction list, starting at the given instruction,
         * and returns the next real (non-virtual) instruction encountered, or
         * {@code null} if none was found before reaching the end of the
         * instruction list.
         *
         * @param start
         *        the starting instruction
         * @return The first non-virtual instruction following the given
         *         instruction, or {@code null} if there is no such instruction.
         */
        public AbstractInsnNode nextRealInsn (final AbstractInsnNode start) {
            AbstractInsnNode insn = start;
            while (insn != null) {
                insn = _next (insn);
                if (insn != null && !Insn.isVirtual (insn)) {
                    return insn;
                }
            }

            // not found
            return null;
        }

        protected abstract AbstractInsnNode _next (final AbstractInsnNode insn);

        //

        /**
         * Wraps an {@link InsnList} as an {@link Iterable} that provides all
         * instruction nodes (both real and virtual).
         *
         * @param list
         *        the list of instruction nodes to wrap
         * @return An {@link Iterable} instance for the given instruction list.
         */
        public static Iterable <AbstractInsnNode> selectAll (final InsnList list) {
            return new Iterable <AbstractInsnNode> () {
                @Override
                public Iterator <AbstractInsnNode> iterator () {
                    return list.iterator ();
                }
            };
        }


        /**
         * Wraps an {@link InsnList} as an {@link Iterable} that provides only
         * the real instruction nodes (and skips the virtual nodes).
         *
         * @param list
         *        the list of instruction nodes to wrap
         * @return An {@link Iterable} instance for the given instruction list.
         */
        public static Iterable <AbstractInsnNode> selectReal (final InsnList list) {
            return new Iterable <AbstractInsnNode> () {
                @Override
                public Iterator <AbstractInsnNode> iterator () {
                    return __onlyRealInsnsIterator (list);
                }
            };
        }

        private static Iterator <AbstractInsnNode> __onlyRealInsnsIterator (final InsnList list) {
            return new Iterator <AbstractInsnNode>() {
                private final ListIterator <AbstractInsnNode> __source = list.iterator ();
                private AbstractInsnNode __next;


                @Override
                public void remove () {
                    throw new UnsupportedOperationException ();
                }


                @Override
                public AbstractInsnNode next () {
                    if (!hasNext ()) {
                        throw new NoSuchElementException ();
                    }

                    final AbstractInsnNode result = __next;
                    __next = null;

                    return result;
                }


                @Override
                public boolean hasNext () {
                    //
                    // If there is no next value ready, try to get one and if there
                    // still is not any, report no more elements to the caller.
                    //
                    if (__next == null) {
                        __next = __getNext ();
                    }

                    return (__next != null);
                }

                private AbstractInsnNode __getNext () {
                    final Iterator <AbstractInsnNode> source = __source;

                    //
                    // Iterate over the source until we encounter an acceptable element.
                    // If we reach the end without encountering any, just return null.
                    //
                    while (source.hasNext ()) {
                        final AbstractInsnNode node = source.next ();
                        if (!Insn.isVirtual (node)) {
                            return node;
                        }
                    }

                    return null;
                }
            };
        }


        /**
         * Wraps an {@link InsnList} into a {@link List} instance to enable
         * stream processing.
         *
         * @param insns
         *        the {@link InsnList} to wrap
         * @return an instance of {@link List} wrapping the given
         *         {@link InsnList}
         */
        public static List <AbstractInsnNode> asList (final InsnList insns) {
            return new AbstractList <AbstractInsnNode> () {

                @Override
                public int size () {
                    return insns.size ();
                }


                //
                // Each AbstractInsnNode instance is considered unique
                // and cannot be part of multiple lists. Therefore if an
                // instruction appears in a list, it is its first as well
                // the last occurrence.
                //

                @Override
                public boolean contains (final Object obj) {
                    if (obj instanceof AbstractInsnNode) {
                        return insns.contains ((AbstractInsnNode) obj);
                    } else {
                        return false;
                    }
                }


                @Override
                public int indexOf (final Object obj) {
                    return __indexOf (insns, obj);
                }


                @Override
                public int lastIndexOf (final Object obj) {
                    return __indexOf (insns, obj);
                }


                private int __indexOf (final InsnList insns, final Object obj) {
                    if (obj instanceof AbstractInsnNode) {
                        return insns.indexOf ((AbstractInsnNode) obj);
                    } else {
                        return -1;
                    }
                }

                //

                @Override
                public AbstractInsnNode [] toArray () {
                    return insns.toArray ();
                }

                @Override
                public <T> T [] toArray (final T [] output) {
                    //
                    // The following cast may fail, if the target type
                    // is not a super-type of AbstractInsnNode [].
                    //
                    @SuppressWarnings ("unchecked")
                    final T [] nodes = (T []) insns.toArray ();

                    if (output.length < nodes.length) {
                        return nodes;
                    } else {
                        System.arraycopy (nodes, 0, output, 0, nodes.length);
                        if (output.length > nodes.length) {
                            Arrays.fill (output, nodes.length, output.length, null);
                        }

                        return output;
                    }
                }

                //

                @Override
                public boolean add (final AbstractInsnNode insn) {
                    __ensureValidInsn (insn);
                    insns.add (insn);
                    return true;
                }


                @Override
                public void add (final int index, final AbstractInsnNode insn) {
                    __ensureValidInsn (insn);
                    insns.insertBefore (insns.get (index), insn);
                }


                @Override
                public boolean addAll (
                    final int index, final Collection <? extends AbstractInsnNode> c
                ) {
                    final int sizeBefore = insns.size ();
                    final AbstractInsnNode oldInsn = insns.get (index);

                    for (final AbstractInsnNode insn : c) {
                        __ensureValidInsn (insn);
                        insns.insertBefore (oldInsn, insn);
                    }

                    return sizeBefore != insns.size ();
                }


                private void __ensureValidInsn (final AbstractInsnNode insn) {
                    //
                    // Make sure the instruction is not null and does not
                    // belong to any other instruction list.
                    //
                    Objects.requireNonNull (insn, "insn cannot be <null>");
                    __ensureFreeStandingInsn (insn);
                }


                private void __ensureFreeStandingInsn (final AbstractInsnNode insn) {
                    if (insn.getPrevious () != null || insn.getNext () != null) {
                        throw new IllegalArgumentException ("cannot add instruction already belonging to a list");
                    }
                }

                //

                @Override
                public boolean remove (final Object obj) {
                    if (obj instanceof AbstractInsnNode) {
                        //
                        // Make sure the instruction belongs to this list.
                        //
                        final AbstractInsnNode insn = (AbstractInsnNode) obj;
                        if (insns.contains (insn)) {
                            insns.remove (insn);
                            return true;

                        } else {
                            throw new IllegalArgumentException ("cannot remove instruction not belonging to the list");
                        }
                    }

                    return false;
                }


                @Override
                public AbstractInsnNode remove (final int index) {
                    final AbstractInsnNode oldInsn = insns.get (index);
                    insns.remove (oldInsn);
                    return oldInsn;
                }


                @Override
                public void clear () {
                    insns.clear ();
                }

                //

                @Override
                public AbstractInsnNode get (final int index) {
                    return insns.get (index);
                }


                @Override
                public AbstractInsnNode set (final int index, final AbstractInsnNode insn) {
                    __ensureValidInsn (insn);

                    final AbstractInsnNode oldInsn = insns.get (index);
                    insns.set (oldInsn, insn);
                    return oldInsn;
                }

                //

                @Override
                public Iterator <AbstractInsnNode> iterator () {
                    return listIterator ();
                }

                @Override
                public ListIterator <AbstractInsnNode> listIterator (final int index) {
                    return insns.iterator (index);
                }
            };
        }

    }

    //
    // LB: Some of the following tests should be moved to the Insn enum.
    //

    public static boolean isReferenceType (final Type type) {
        return type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY;
    }


    public static boolean isStaticFieldAccess (final AbstractInsnNode node) {
        return isStaticFieldAccess (node.getOpcode ());
    }

    public static boolean isStaticFieldAccess (final int opcode) {
        return opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC;
    }


    public static boolean isReturn (final int opcode) {
        return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
    }

    public static boolean isReturn (final AbstractInsnNode insn) {
        final int opcode = insn.getOpcode ();
        return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
    }


    public static boolean isBranch (final AbstractInsnNode insn) {
        final int opcode = insn.getOpcode();
        return insn instanceof JumpInsnNode
                || insn instanceof LookupSwitchInsnNode
                || insn instanceof TableSwitchInsnNode
                || opcode == Opcodes.ATHROW || opcode == Opcodes.RET
                || (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN);
    }


    public static boolean isConditionalBranch (final AbstractInsnNode instruction) {
        final int opcode = instruction.getOpcode();
        return (instruction instanceof JumpInsnNode && opcode != Opcodes.GOTO);
    }


    public static boolean mightThrowException (final AbstractInsnNode instruction) {
        switch (instruction.getOpcode()) {

        // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.BALOAD:
        case Opcodes.DALOAD:
        case Opcodes.FALOAD:
        case Opcodes.IALOAD:
        case Opcodes.LALOAD:
        case Opcodes.BASTORE:
        case Opcodes.CASTORE:
        case Opcodes.DASTORE:
        case Opcodes.FASTORE:
        case Opcodes.IASTORE:
        case Opcodes.LASTORE:
        case Opcodes.AALOAD:
        case Opcodes.CALOAD:
        case Opcodes.SALOAD:
        case Opcodes.SASTORE:
            // NullPointerException, ArrayIndexOutOfBoundsException,
            // ArrayStoreException
        case Opcodes.AASTORE:
            // NullPointerException
        case Opcodes.ARRAYLENGTH:
        case Opcodes.ATHROW:
        case Opcodes.GETFIELD:
        case Opcodes.PUTFIELD:
            // NullPointerException, StackOverflowError
        case Opcodes.INVOKEINTERFACE:
        case Opcodes.INVOKESPECIAL:
        case Opcodes.INVOKEVIRTUAL:
            // StackOverflowError
        case Opcodes.INVOKESTATIC:
            // NegativeArraySizeException
        case Opcodes.ANEWARRAY:
            // NegativeArraySizeException, OutOfMemoryError
        case Opcodes.NEWARRAY:
        case Opcodes.MULTIANEWARRAY:
            // OutOfMemoryError, InstantiationError
        case Opcodes.NEW:
            // OutOfMemoryError
        case Opcodes.LDC:
            // ClassCastException
        case Opcodes.CHECKCAST:
            // ArithmeticException
        case Opcodes.IDIV:
        case Opcodes.IREM:
        case Opcodes.LDIV:
        case Opcodes.LREM:
            // New instruction in JDK7
        case Opcodes.INVOKEDYNAMIC:
            return true;

        default:
            return false;
        }
    }


    /**
     * Returns instruction that will call the method to box the instruction
     * residing on the stack
     *
     * @param valueType type to be boxed
     */
    public static MethodInsnNode boxValueOnStack (final Type valueType) {
        switch (valueType.getSort ()) {
        case Type.BOOLEAN:
            return __constructValueOf (Boolean.class, boolean.class);
        case Type.BYTE:
            return __constructValueOf (Byte.class, byte.class);
        case Type.CHAR:
            return __constructValueOf (Character.class, char.class);
        case Type.DOUBLE:
            return __constructValueOf (Double.class, double.class);
        case Type.FLOAT:
            return __constructValueOf (Float.class, float.class);
        case Type.INT:
            return __constructValueOf (Integer.class, int.class);
        case Type.LONG:
            return __constructValueOf (Long.class, long.class);
        case Type.SHORT:
            return __constructValueOf (Short.class, short.class);

        default:
            throw new DiSLFatalException (
                "Impossible to box type: "+ valueType.getDescriptor ()
            );
        }
    }

    private static MethodInsnNode __constructValueOf (
        final Class <?> boxClass, final Class <?> primitiveClass
    ) {
        final Type boxType = Type.getType (boxClass);
        final Type primitiveType = Type.getType (primitiveClass);
        final Type methodType  = Type.getMethodType (boxType, primitiveType);

        return invokeStatic (boxType, "valueOf", methodType);
    }

    //

    /**
     * Determines the line number corresponding to the given instructions and
     * returns it embedded in a string with the given format. Returns empty
     * string if the line number could not be determined.
     *
     * @param format
     *     format of the resulting string
     * @param insn
     *     instruction to determine the line number for
     * @return
     *     string containing the line number corresponding to the instruction,
     *     or empty string if the line number could not be determined
     */
    public static final String formatLineNo (
        final String format, final AbstractInsnNode insn
    ) {
        final int lineNo = getLineNo (insn);
        if (lineNo > 0) {
            return String.format (format, lineNo);
        } else {
            return "";
        }
    }


    /**
     * Determines the line number corresponding to the given instruction.
     * Returns -1 if the line number could not be determined.
     *
     * @param insn
     *     instruction to determine the line number for
     * @return
     *     line number corresponding to the instruction, or -1 if the line
     *     number could not be determined
     */
    public static int getLineNo (final AbstractInsnNode insn) {
        //
        // Starting at the given instruction, traverse the instructions
        // backwards and find the closest LineNumberInstance.
        //
        for (AbstractInsnNode node = insn; node != null; node = node.getPrevious ()) {
            if (node instanceof LineNumberNode) {
                return ((LineNumberNode) node).line;
            }
        }

        return -1;
    }

    //

    /**
     * @return Canonical class name for the given {@link ClassNode}.
     */
    public static String className (final ClassNode classNode) {
        return internalToStdName (classNode.name);
    }


    /**
     * @return Canonical class name for the given internal class name.
     */
    public static String internalToStdName (final String internalName) {
        return internalName.replace (
            Constants.PACKAGE_INTERN_DELIM, Constants.PACKAGE_STD_DELIM
        );
    }

}
