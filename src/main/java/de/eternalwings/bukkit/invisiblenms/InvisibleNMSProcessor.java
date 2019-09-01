package de.eternalwings.bukkit.invisiblenms;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Name;
import de.eternalwings.bukkit.invisiblenms.annotations.CopyAs;
import de.eternalwings.bukkit.invisiblenms.annotations.CopyDefaults;
import de.eternalwings.bukkit.invisiblenms.annotations.DontCopy;
import de.eternalwings.bukkit.invisiblenms.annotations.Mixin;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("de.eternalwings.bukkit.invisiblenms.annotations.CopyDefaults")
public class InvisibleNMSProcessor extends AbstractProcessor {

    private Trees trees;
    private Elements elements;
    private TreeMaker treeMaker;

    private static boolean annotationIsType(JCAnnotation annotation, Class<?> annotationType) {
        return annotation.type.tsym.getQualifiedName().contentEquals(annotationType.getTypeName());
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.trees = Trees.instance(processingEnv);
        this.elements = processingEnv.getElementUtils();
        this.treeMaker = TreeMaker.instance(((JavacProcessingEnvironment) processingEnv).getContext());
        this.treeMaker.at(0);
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        roundEnvironment.getRootElements().forEach(element -> {

            final JCCompilationUnit sourceCompilationUnit = (JCCompilationUnit) this.trees.getPath(element).getCompilationUnit();
            final JCTree sourceTree = (JCTree) this.trees.getTree(element);

            sourceTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCClassDecl tree) {

                    final MethodInserter methodInserter = new MethodInserter(tree, sourceCompilationUnit, treeMaker);

                    if (tree.mods.annotations.stream().anyMatch(annotation -> annotationIsType(annotation, CopyDefaults.class))) {
                        final List<Name> mixinTypes = getImplementedMixinTypeNames(tree);

                        treeMaker.at(tree.pos);
                        mixinTypes.forEach(typeName -> copyMethodsFrom(tree, methodInserter, typeName));

                        super.visitClassDef(tree);
                    }

                }
            });
        });
        return false;
    }

    private void copyMethodsFrom(JCClassDecl tree, MethodInserter methodInserter, Name typeName) {
        final TypeElement mixinType = elements.getTypeElement(typeName);
        final ClassTree mixinTree = trees.getTree(mixinType);
        final JCCompilationUnit mixinCompilationUnit =
                (JCCompilationUnit) trees.getPath(mixinType).getCompilationUnit();
        final List<JCMethodDecl> defaultMethods = getMethodsToCopy(mixinTree);

        defaultMethods.forEach(method -> {
            final String targetName = getTargetName(method);
            final boolean hasCustomOverwrite = tree.defs.stream()
                    .filter(def -> def instanceof JCMethodDecl)
                    .map(def -> (JCMethodDecl) def)
                    .filter(def -> def.name.contentEquals(targetName))
                    .anyMatch(def -> haveEqualParameterTypes(def, method));

            if (!hasCustomOverwrite) {
                methodInserter.addMethod(method, targetName, mixinCompilationUnit);
            }
        });
    }

    private boolean haveEqualParameterTypes(JCMethodDecl first, JCMethodDecl second) {
        if (first.params.size() != second.params.size()) {
            return false;
        }

        for (int i = 0; i < first.params.size(); i++) {
            final JCVariableDecl firstParam = first.params.get(i);
            final JCVariableDecl secondParam = second.params.get(i);
            if (!firstParam.vartype.type.tsym.getQualifiedName().contentEquals(secondParam.vartype.type.tsym.getQualifiedName())) {
                return false;
            }
        }

        return true;
    }

    private List<Name> getImplementedMixinTypeNames(JCClassDecl tree) {
        return tree.implementing.stream()
                .map(implement -> {
                    if(implement instanceof JCTypeApply) {
                        return ((JCTypeApply) implement).clazz;
                    } else {
                        return implement;
                    }
                })
                .filter(expr -> expr instanceof JCIdent)
                .map(expr -> (JCIdent) expr)
                .filter(this::isMixinType)
                .map(ident -> ident.type.tsym.getQualifiedName())
                .collect(Collectors.toList());
    }

    private List<JCMethodDecl> getMethodsToCopy(ClassTree mixinTree) {
        return mixinTree.getMembers().stream()
                .filter(member -> member instanceof JCMethodDecl)
                .map(member -> (JCMethodDecl) member)
                .filter(decl ->
                        decl.getModifiers().getFlags().contains(Modifier.DEFAULT) &&
                                decl.getModifiers().getAnnotations().stream()
                                        .noneMatch(annotation -> annotationIsType(annotation, DontCopy.class)))
                .collect(Collectors.toList());
    }

    private boolean isMixinType(JCIdent ident) {
        final TypeElement typeElement = elements.getTypeElement(ident.type.tsym.getQualifiedName());
        final List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
        return annotations.stream().anyMatch(annotation -> ((ClassSymbol) annotation.getAnnotationType().asElement()).getQualifiedName().contentEquals(Mixin.class.getTypeName()));
    }

    private String getTargetName(JCMethodDecl decl) {
        final Optional<JCAnnotation> nameAnnotationOpt = decl.mods.annotations.stream().filter(jcAnnotation -> {
            return jcAnnotation.type.tsym.getQualifiedName().contentEquals(CopyAs.class.getTypeName());
        }).findAny();

        if (nameAnnotationOpt.isPresent()) {
            final JCAnnotation nameAnnotation = nameAnnotationOpt.get();
            final Predicate<JCAssign> isValueAnnotationProperty = argAssign -> argAssign.lhs instanceof JCIdent && ((JCIdent) argAssign.lhs).name.contentEquals("value");
            final Optional<JCExpression> newNameValue = nameAnnotation.args.stream()
                    .filter(arg -> arg instanceof JCAssign)
                    .map(arg -> (JCAssign) arg)
                    .filter(isValueAnnotationProperty)
                    .map(argAssign -> argAssign.rhs)
                    .findAny();

            if (newNameValue.isPresent()) {
                final JCExpression nameExpression = newNameValue.get();
                if (nameExpression instanceof JCLiteral) {
                    return ((JCLiteral) nameExpression).getValue().toString();
                }
            }
        }
        return decl.name.toString();
    }
}
