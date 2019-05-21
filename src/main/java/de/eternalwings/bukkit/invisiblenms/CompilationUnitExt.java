package de.eternalwings.bukkit.invisiblenms;

import com.sun.tools.javac.tree.JCTree;

import java.util.Map;
import java.util.stream.Collectors;

public final class CompilationUnitExt {
    private CompilationUnitExt() {
    }

    public static Map<CharSequence, JCTree.JCFieldAccess> getImports(JCTree.JCCompilationUnit compilationUnit) {
        return compilationUnit.defs.stream()
                .filter(tree -> tree instanceof JCTree.JCImport)
                .map(tree -> (JCTree.JCFieldAccess) ((JCTree.JCImport) tree).qualid)
                .collect(Collectors.toMap(importDecl -> importDecl.name, importDecl -> importDecl));
    }
}
