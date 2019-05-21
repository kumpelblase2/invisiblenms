package de.eternalwings.bukkit.invisiblenms;

import com.sun.tools.javac.tree.JCTree;

public class TreeCopyContext {

    private final JCTree.JCCompilationUnit sourceCompilationUnit;
    private final JCTree.JCCompilationUnit targetCompilationUnit;
    private final int offset;

    public TreeCopyContext(JCTree.JCCompilationUnit sourceCompilationUnit, JCTree.JCCompilationUnit targetCompilationUnit, int offset) {
        this.sourceCompilationUnit = sourceCompilationUnit;
        this.targetCompilationUnit = targetCompilationUnit;
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public int getEndPositionInSource(JCTree tree) {
        return this.sourceCompilationUnit.endPositions.getEndPos(tree);
    }

    public void setEndPositionInTarget(JCTree tree, int position) {
        this.targetCompilationUnit.endPositions.storeEnd(tree, position);
    }
}
