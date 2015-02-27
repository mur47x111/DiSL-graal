package ch.usi.dag.disl.weaver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;

import ch.usi.dag.disl.snippet.Shadow;
import ch.usi.dag.disl.snippet.Shadow.WeavingRegion;
import ch.usi.dag.disl.snippet.Snippet;
import ch.usi.dag.disl.util.AsmHelper;
import ch.usi.dag.disl.util.AsmHelper.Insns;
import ch.usi.dag.disl.util.FrameHelper;

public class WeavingInfo {

    private ArrayList<Snippet> sortedSnippets;

    private Map<AbstractInsnNode, Frame<BasicValue>> basicFrameMap;
    private Map<AbstractInsnNode, Frame<SourceValue>> sourceFrameMap;

    private Frame<BasicValue> retFrame;

    public WeavingInfo(ClassNode classNode, MethodNode methodNode,
            Map<Snippet, List<Shadow>> snippetMarkings) {

        sortedSnippets = new ArrayList<Snippet>(snippetMarkings.keySet());
        Collections.sort(sortedSnippets);

        InsnList instructions = methodNode.instructions;

        List<LabelNode> tcb_ends = new LinkedList<LabelNode>();

        for (TryCatchBlockNode tcb : methodNode.tryCatchBlocks) {
            tcb_ends.add(tcb.end);
        }

        // initialize weaving start
        for (Snippet snippet : sortedSnippets) {

            for (Shadow shadow : snippetMarkings.get(snippet)) {

                WeavingRegion region = shadow.getWeavingRegion();
                AbstractInsnNode start = region.getStart();
                LabelNode lstart = new LabelNode();
                instructions.insertBefore(start, lstart);
                region.setStart(lstart);
            }
        }

        // first pass: adjust weaving end for one-instruction shadow
        for (Snippet snippet : sortedSnippets) {

            for (Shadow shadow : snippetMarkings.get(snippet)) {

                WeavingRegion region = shadow.getWeavingRegion();

                if (region.getEnds() == null) {

                    List<AbstractInsnNode> ends = new LinkedList<AbstractInsnNode>();

                    for (AbstractInsnNode end : shadow.getRegionEnds()) {

                        if (AsmHelper.isBranch(end)) {
                            end = end.getPrevious();
                        }

                        ends.add(end);
                    }

                    region.setEnds(ends);
                }
            }
        }

        // second pass: calculate weaving location
        for (Snippet snippet : sortedSnippets) {

            for (Shadow shadow : snippetMarkings.get(snippet)) {

                WeavingRegion region = shadow.getWeavingRegion();
                List<AbstractInsnNode> ends = new LinkedList<AbstractInsnNode>();

                for (AbstractInsnNode end : region.getEnds()) {

                    LabelNode lend = new LabelNode();
                    instructions.insert(end, lend);
                    ends.add(lend);
                }

                region.setEnds(ends);

                LabelNode lthrowstart = new LabelNode();
                instructions.insertBefore(region.getAfterThrowStart(),
                        lthrowstart);
                region.setAfterThrowStart(lthrowstart);

                LabelNode lthrowend = new LabelNode();
                instructions.insert(region.getAfterThrowEnd(), lthrowend);
                region.setAfterThrowEnd(lthrowend);
            }
        }

        basicFrameMap = FrameHelper.createBasicMapping(classNode.name,
                methodNode);
        sourceFrameMap = FrameHelper.createSourceMapping(classNode.name,
                methodNode);

        AbstractInsnNode last = Insns.REVERSE.firstRealInsn (instructions.getLast());
        retFrame = basicFrameMap.get(last);
    }

    public ArrayList<Snippet> getSortedSnippets() {
        return sortedSnippets;
    }

    public Frame<BasicValue> getBasicFrame(AbstractInsnNode instr) {
        return basicFrameMap.get(instr);
    }

    public Frame<BasicValue> getRetFrame() {
        return retFrame;
    }

    public Frame<SourceValue> getSourceFrame(AbstractInsnNode instr) {
        return sourceFrameMap.get(instr);
    }

    public boolean stackNotEmpty(AbstractInsnNode loc) {
        return basicFrameMap.get(loc).getStackSize() > 0;
    }

    public InsnList backupStack(AbstractInsnNode loc, int startFrom) {
        return FrameHelper.enter(basicFrameMap.get(loc), startFrom);
    }

    public InsnList restoreStack(AbstractInsnNode loc, int startFrom) {
        return FrameHelper.exit(basicFrameMap.get(loc), startFrom);
    }

    public int getStackHeight(AbstractInsnNode loc) {
        return FrameHelper.getOffset(basicFrameMap.get(loc));
    }

}
