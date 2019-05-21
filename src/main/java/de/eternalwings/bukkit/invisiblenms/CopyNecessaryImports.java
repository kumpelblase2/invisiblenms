package de.eternalwings.bukkit.invisiblenms;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;

import java.util.Map;

public class CopyNecessaryImports extends TreeScanner {

    private final TreeMaker treeMaker;
    private final Map<CharSequence, JCTree.JCFieldAccess> sourceImports;
    private final Map<CharSequence, JCTree.JCFieldAccess> targetImports;
    private final JCTree.JCCompilationUnit targetCompilationUnit;

    public CopyNecessaryImports(TreeMaker treeMaker, Map<CharSequence, JCTree.JCFieldAccess> sourceImports, Map<CharSequence, JCTree.JCFieldAccess> targetImports, JCTree.JCCompilationUnit targetCompilationUnit) {
        this.treeMaker = treeMaker.at(-1);
        this.sourceImports = sourceImports;
        this.targetImports = targetImports;
        this.targetCompilationUnit = targetCompilationUnit;
    }

    @Override
    public void visitIdent(JCTree.JCIdent jcIdent) {
        if (sourceImports.containsKey(jcIdent.name) && !targetImports.containsKey(jcIdent.name)) {
            final JCTree.JCFieldAccess importTree = sourceImports.get(jcIdent.name);
            targetImports.put(jcIdent.name, importTree);
            final JCTree.JCImport newImport = treeMaker.Import(importTree, false);
            targetCompilationUnit.defs = targetCompilationUnit.defs.prepend(newImport);
        }
        super.visitIdent(jcIdent);
    }
}
