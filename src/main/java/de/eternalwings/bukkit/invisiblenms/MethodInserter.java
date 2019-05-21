package de.eternalwings.bukkit.invisiblenms;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

import java.util.Map;

public class MethodInserter {

    private static final String SUPER_KEYWORD = "super";
    private static final String SUPER_CALL_METHOD_NAME = "call";
    private final JCClassDecl targetTree;
    private final JCCompilationUnit targetCompilationUnit;
    private final TreeMaker treeMaker;
    private final TreeCopier<TreeCopyContext> copier;
    private final Map<CharSequence, JCFieldAccess> targetImports;

    public MethodInserter(JCClassDecl targetTree, JCCompilationUnit targetCompilationUnit, TreeMaker treeMaker) {
        this.targetTree = targetTree;
        this.targetCompilationUnit = targetCompilationUnit;
        this.treeMaker = treeMaker;
        this.copier = new TreeCopierWithPosition(this.treeMaker);
        this.targetImports = CompilationUnitExt.getImports(this.targetCompilationUnit);
    }

    public void addMethod(JCMethodDecl decl, JCCompilationUnit sourceCompilationUnit) {
        final int originalDeclarationPosition = decl.mods.pos;
        final EndPosTable endPosTable = this.targetCompilationUnit.endPositions;
        final int targetDeclarationPosition = this.targetTree.getEndPosition(endPosTable);
        final int totalOffset = targetDeclarationPosition - originalDeclarationPosition;

        final JCMethodDecl methodCopy = this.copier.copy(decl, new TreeCopyContext(sourceCompilationUnit,
                this.targetCompilationUnit, totalOffset));
        methodCopy.mods = this.treeMaker.at(originalDeclarationPosition).Modifiers(Flags.PUBLIC);

        final Map<CharSequence, JCFieldAccess> imports = CompilationUnitExt.getImports(sourceCompilationUnit);


        methodCopy.accept(new TreeTranslator() {

            @Override
            public void visitApply(JCMethodInvocation tree) {
                if (tree.getMethodSelect() instanceof JCFieldAccess) {
                    final JCFieldAccess selectedMethod = (JCFieldAccess) tree.getMethodSelect();
                    final Name methodNameIdentifier = selectedMethod.getIdentifier();
                    final JCExpression methodObjectExpression = selectedMethod.getExpression();
                    if (methodNameIdentifier.contentEquals(SUPER_CALL_METHOD_NAME)
                            && methodObjectExpression instanceof JCIdent
                            && ((JCIdent) methodObjectExpression).name.contentEquals(Super.class.getSimpleName())) {
                        // TODO the above fails with FQ calls to Super.call()
                        tree = createSuperCall(tree, decl);
                    }
                }
                super.visitApply(tree);
            }

        });

        methodCopy.accept(new CopyNecessaryImports(this.treeMaker, imports, this.targetImports, this.targetCompilationUnit));
        this.targetTree.defs = this.targetTree.defs.append(methodCopy);
        endPosTable.storeEnd(this.targetTree, methodCopy.getEndPosition(endPosTable) + 1);
    }

    private JCMethodInvocation createSuperCall(JCMethodInvocation tree, JCMethodDecl decl) {
        final JCIdent ident = this.treeMaker.at(((JCFieldAccess) tree.meth).selected.pos).Ident(decl.name.table.fromString(SUPER_KEYWORD));
        final JCFieldAccess superMethod = this.treeMaker.at(tree.meth.pos).Select(ident, decl.name);
        return this.treeMaker.at(tree.pos).Apply(List.nil(), superMethod, tree.args);
    }
}
