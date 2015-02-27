package ch.usi.dag.disl.marker;

import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.exception.MarkerException;
import ch.usi.dag.disl.snippet.Shadow;
import ch.usi.dag.disl.snippet.Snippet;

/**
 * <p>
 * Basic interface that every marker has to implement. Marker should return list
 * of shadows that are marks for particular method.
 *
 * <p>
 * There is a list of already prepared markers that select various regions in
 * scoped methods.
 * <ul>
 * <li>
 * {@link ch.usi.dag.disl.marker.AfterInitBodyMarker
 * AfterInitBodyMarker}</li>
 * <li>
 * {@link ch.usi.dag.disl.marker.BasicBlockMarker
 * BasicBlockMarker}</li>
 * <li>
 * {@link ch.usi.dag.disl.marker.BodyMarker
 * BodyMarker}</li>
 * <li>
 * {@link ch.usi.dag.disl.marker.BytecodeMarker
 * BytcodeMarker}</li>
 * <li>
 * {@link ch.usi.dag.disl.marker.EmptyMarker
 * EmptyMarker}</li>
 * <li>
 * {@link ch.usi.dag.disl.marker.ExceptionHandlerMarker
 * ExceptionHandlerMarker}</li>
 * <li>
 * {@link ch.usi.dag.disl.marker.InsnNodeMarker
 * InsnNodeMarker - experimental}</li>
 * <li>
 * {@link ch.usi.dag.disl.marker.NewObjMarker
 * NewObjMarker - experimental}</li>
 * <li>
 * {@link ch.usi.dag.disl.marker.PreciseBasicBlockMarker
 * PreciseBasicBlockMarker}</li>
 * <li>
 * {@link ch.usi.dag.disl.marker.StrictBytecodeMarker
 * StrictBytecodeMarker}</li>
 * <li>
 * {@link ch.usi.dag.disl.marker.TryClauseMarker
 * TryClauseMarker}</li>
 * </ul>
 *
 * <p>
 * It's also possible to implement and use custom markers. This interface might
 * be implemented directly or following abstract markers might be used.
 * <ul>
 * <li>
 * {@link ch.usi.dag.disl.marker.AbstractMarker
 * AbstractMarker}</li>
 * <li>
 * {@link ch.usi.dag.disl.marker.AbstractDWRMarker
 * AbstractDWRMarker}</li>
 * <li>
 * {@link ch.usi.dag.disl.marker.AbstractInsnMarker
 * AbstractInsnMarker}</li>
 * </ul>
 */
public interface Marker {

    /**
     * <p>
     * Returns shadows for the marked method.
     *
     * @param classNode
     *            represents class being marked
     * @param methodNode
     *            represents method being marked
     * @param snippet
     *            snippet defining the marker
     * @return
     *         list of shadows for marked method
     */
    List<Shadow> mark(ClassNode classNode, MethodNode methodNode,
            Snippet snippet) throws MarkerException;
}
