package de.eternalwings.bukkit.invisiblenms;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import de.eternalwings.bukkit.invisiblenms.annotations.CopyAs;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

        final String targetName = this.getTargetName(decl);

        final JCMethodDecl methodCopy = this.copier.copy(decl, new TreeCopyContext(sourceCompilationUnit,
                this.targetCompilationUnit, totalOffset));
        methodCopy.name = methodCopy.name.table.fromString(targetName);
        methodCopy.mods = this.treeMaker.at(originalDeclarationPosition).Modifiers(Flags.PUBLIC);

        final Map<CharSequence, JCFieldAccess> imports = CompilationUnitExt.getImports(sourceCompilationUnit);


        methodCopy.accept(new TreeTranslator() {

            @Override
            public void visitApply(JCMethodInvocation tree) {
                if (tree.getMethodSelect() instanceof JCFieldAccess) {
                    if (shouldReplaceWithSuperCall((JCFieldAccess) tree.getMethodSelect())) {
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

    private String getTargetName(JCMethodDecl decl) {
        final Optional<JCAnnotation> nameAnnotationOpt = decl.mods.annotations.stream().filter(jcAnnotation -> {
            return jcAnnotation.type.tsym.getQualifiedName().contentEquals(CopyAs.class.getTypeName());
        }).findAny();

        if(nameAnnotationOpt.isPresent()) {
            final JCAnnotation nameAnnotation = nameAnnotationOpt.get();
            final Optional<JCExpression> newNameValue = nameAnnotation.args.stream()
                    .filter(arg -> arg instanceof JCAssign)
                    .map(arg -> (JCAssign) arg)
                    .filter(argAssign -> argAssign.lhs instanceof JCIdent && ((JCIdent) argAssign.lhs).name.contentEquals("value"))
                    .map(argAssign -> argAssign.rhs)
                    .findAny();

            if(newNameValue.isPresent()) {
                final JCExpression nameExpression = newNameValue.get();
                if(nameExpression instanceof JCLiteral) {
                    return ((JCLiteral) nameExpression).getValue().toString();
                }
            }
        }
        return decl.name.toString();
    }

    private boolean shouldReplaceWithSuperCall(JCFieldAccess selectedMethod) {
        final Name methodNameIdentifier = selectedMethod.getIdentifier();
        if (methodNameIdentifier.contentEquals(SUPER_CALL_METHOD_NAME)) {
            final JCExpression methodObjectExpression = selectedMethod.getExpression();

            if (methodObjectExpression instanceof JCIdent) {
                return ((JCIdent) methodObjectExpression).name.contentEquals(Super.class.getSimpleName());
            } else if (methodObjectExpression instanceof JCFieldAccess) {
                final String fullyQualifiedAccessName = getFullyQualifiedAccessName(methodObjectExpression);
                return Objects.equals(fullyQualifiedAccessName, Super.class.getName());
            }
        }
        return false;
    }

    private JCMethodInvocation createSuperCall(JCMethodInvocation tree, JCMethodDecl decl) {
        final JCIdent ident = this.treeMaker.at(((JCFieldAccess) tree.meth).selected.pos).Ident(decl.name.table.fromString(SUPER_KEYWORD));
        final JCFieldAccess superMethod = this.treeMaker.at(tree.meth.pos).Select(ident, decl.name);
        return this.treeMaker.at(tree.pos).Apply(List.nil(), superMethod, tree.args);
    }

    private static String getFullyQualifiedAccessName(JCTree tree) {
        if (tree instanceof JCFieldAccess) {
            final JCFieldAccess fieldAccess = (JCFieldAccess) tree;
            return getFullyQualifiedAccessName(fieldAccess.selected) + "." + fieldAccess.name.toString();
        } else if (tree instanceof JCIdent) {
            return ((JCIdent) tree).name.toString();
        } else {
            throw new IllegalStateException();
        }
    }
}
