package de.eternalwings.bukkit.invisiblenms;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class BasicTest {

    private static final JavaFileObject TEST_BASIC_CLASS_FILE = JavaFileObjects.forResource("TestBasic.java");
    private static final JavaFileObject TEST_MIXIN_CLASS_FILE = JavaFileObjects.forResource("TestMixin.java");

    @Test
    void testPreprocessor() {
        final Compilation result = javac() //
                                           .withProcessors(new InvisibleNMSProcessor()) //
                                           .compile(TEST_BASIC_CLASS_FILE, TEST_MIXIN_CLASS_FILE);
        assertThat(result).succeeded();
    }

}
