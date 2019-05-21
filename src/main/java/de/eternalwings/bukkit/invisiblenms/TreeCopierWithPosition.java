package de.eternalwings.bukkit.invisiblenms;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;

public class TreeCopierWithPosition extends TreeCopier<TreeCopyContext> {

    public TreeCopierWithPosition(TreeMaker treeMaker) {
        super(treeMaker);
    }

    @Override
    public <T extends JCTree> T copy(T t, TreeCopyContext context) {
        if (t == null) {
            return null;
        }

        if (context == null) {
            throw new IllegalArgumentException("Context parameter cannot be null");
        }

        final T copiedTree = super.copy(t, context);
        if (copiedTree.getStartPosition() != -1) {
            copiedTree.setPos(this.calculateNewPosition(copiedTree.getStartPosition(), context));
            final int originalPosition = context.getEndPositionInSource(t);
            final int newPosition = this.calculateNewPosition(originalPosition, context);
            context.setEndPositionInTarget(copiedTree, newPosition);
        }
        return copiedTree;
    }

    private int calculateNewPosition(int oldPosition, TreeCopyContext context) {
        return oldPosition + context.getOffset();
    }
}
