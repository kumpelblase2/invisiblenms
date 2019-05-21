package de.eternalwings.bukkit.invisiblenms;

import com.google.testing.compile.Compilation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.tools.JavaFileObject;

public class TestUtil {
    public static void writeClassFiles(Compilation compilationResult) {
        for(final JavaFileObject generatedFile : compilationResult.generatedFiles()) {
            try(InputStream classReader = generatedFile.openInputStream()) {
                writeClassFile(classReader, generatedFile.getName().split("/")[2]);
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void writeClassFile(InputStream classReader, String className) throws IOException {
        try(final OutputStream writer = new FileOutputStream(new File(className))) {
            byte[] buffer = new byte[256];
            int read;
            while((read = classReader.read(buffer)) >= 0) {
                writer.write(buffer, 0, read);
            }
        }
    }
}
