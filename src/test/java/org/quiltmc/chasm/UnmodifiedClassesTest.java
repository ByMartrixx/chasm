package org.quiltmc.chasm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.quiltmc.chasm.asm.ChasmClassVisitor;
import org.quiltmc.chasm.asm.ChasmClassWriter;
import org.quiltmc.chasm.tree.MapNode;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class UnmodifiedClassesTest {
    public static String[] classNames() {
        return new String[] {
                "org.quiltmc.chasm.ExampleClass",
                "org.quiltmc.chasm.ExampleClass$ExampleAnnotation",
                "org.quiltmc.chasm.ExampleClass$ExampleRecord",
        };
    }

    @ParameterizedTest
    @MethodSource("classNames")
    public void checkClassIdentity(String className) throws IOException {
        // Create class reader
        ClassReader reader = new ClassReader(className);

        // Parse class
        ChasmClassVisitor classVisitor = new ChasmClassVisitor();
        reader.accept(classVisitor, 0);
        MapNode mapNode = classVisitor.getClassNode();

        // Write class
        ChasmClassWriter resultWriter = new ChasmClassWriter(mapNode);
        ClassWriter resultClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        resultWriter.accept(resultClassWriter);
        ClassReader resultReader = new ClassReader(resultClassWriter.toByteArray());

        // Write class into string
        StringWriter resultString = new StringWriter();
        TraceClassVisitor resultVisitor = new TraceClassVisitor(new PrintWriter(resultString));
        resultReader.accept(resultVisitor, 0);

        ClassWriter referenceClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        reader.accept(referenceClassWriter, 0);
        ClassReader referenceReader = new ClassReader(referenceClassWriter.toByteArray());

        // Write original class into string
        StringWriter referenceString = new StringWriter();
        TraceClassVisitor referenceVisitor = new TraceClassVisitor(new PrintWriter(referenceString));
        referenceReader.accept(referenceVisitor, 0);

        // Class shouldn't have changed
        Assertions.assertEquals(referenceString.toString(), resultString.toString());
    }
}