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
import java.util.Set;
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
                        mixinTypes.forEach(typeName -> {
                            final TypeElement mixinType = elements.getTypeElement(typeName);
                            final ClassTree mixinTree = trees.getTree(mixinType);
                            final JCCompilationUnit mixinCompilationUnit =
                                    (JCCompilationUnit) trees.getPath(mixinType).getCompilationUnit();
                            final List<JCMethodDecl> defaultMethods = getMethodsToCopy(mixinTree);

                            defaultMethods.forEach(method -> {
                                methodInserter.addMethod(method, mixinCompilationUnit);
                            });
                        });

                        super.visitClassDef(tree);
                    }

                }
            });
        });
        return false;
    }

    private List<Name> getImplementedMixinTypeNames(JCClassDecl tree) {
        return tree.implementing.stream()
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
}
