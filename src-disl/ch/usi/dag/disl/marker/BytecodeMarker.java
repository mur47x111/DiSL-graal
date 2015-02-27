package ch.usi.dag.disl.marker;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.exception.MarkerException;
import ch.usi.dag.disl.util.AsmHelper.Insns;
import ch.usi.dag.disl.util.AsmOpcodes;


/**
 * Marks one bytecode instruction.
 * <p>
 * Sets the start before a bytecode instruction and the end after a bytecode
 * instruction. If the bytecode instruction is (conditional) jump the end is
 * also inserted before the instruction (preserves before-after semantics).
 */
public class BytecodeMarker extends AbstractDWRMarker {

    protected static final String INSTR_DELIM = ",";

    protected Set <Integer> searchedInstrNums = new HashSet <Integer> ();


    public BytecodeMarker (final Parameter param) throws MarkerException {

        // set delim for instruction list
        param.setMultipleValDelim (INSTR_DELIM);

        // translate all instructions to opcodes
        for (final String instr : param.getMultipleValues ()) {
            try {
                final AsmOpcodes opcode = AsmOpcodes.valueOf (
                    instr.trim ().toUpperCase ()
                );

                searchedInstrNums.add (opcode.getNumber ());
            } catch (final IllegalArgumentException e) {
                throw new MarkerException (
                    "Instruction \""+ instr +"\" cannot be found. "+
                    "See the "+ AsmOpcodes.class.getName () +" enum for "+
                    "the list of valid instructions."
                );
            }
        }

        if (searchedInstrNums.isEmpty ()) {
            throw new MarkerException ("Bytecode marker cannot operate without" +
                " selected instructions. Pass instruction list using" +
                " \"param\" annotation attribute.");
        }
    }


    @Override
    public List <MarkedRegion> markWithDefaultWeavingReg (final MethodNode method) {
        final List <MarkedRegion> regions = new LinkedList <MarkedRegion> ();
        for (final AbstractInsnNode insn : Insns.selectAll (method.instructions)) {
            if (searchedInstrNums.contains (insn.getOpcode ())) {
                regions.add (new MarkedRegion (insn, insn));
            }
        }

        return regions;
    }
}
