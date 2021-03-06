package com.github.gfx.android.orma.processor;

import com.github.gfx.android.orma.annotation.Table;
import com.github.gfx.android.orma.annotation.VirtualTable;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        "com.github.gfx.android.orma.annotation.*",
})
public class OrmaProcessor extends AbstractProcessor {

    public static final String TAG = OrmaProcessor.class.getSimpleName();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.size() == 0) {
            return true;
        }
        try {
            DatabaseWriter databaseWriter = new DatabaseWriter(processingEnv);

            buildTableSchemas(roundEnv)
                    .peek(schema -> writeCodeForEachModel(schema, new SchemaWriter(schema, processingEnv)))
                    .peek(schema -> writeCodeForEachModel(schema, new RelationWriter(schema, processingEnv)))
                    .peek(schema -> writeCodeForEachModel(schema, new UpdaterWriter(schema, processingEnv)))
                    .peek(schema -> writeCodeForEachModel(schema, new DeleterWriter(schema, processingEnv)))
                    .forEach(databaseWriter::add);

            buildVirtualTableSchemas(roundEnv)
                    .peek(schema -> {
                        throw new ProcessingException("@VirtualTable is not yet implemented.", schema.getElement());
                    });

            if (databaseWriter.isRequired()) {
                writeToFiler(null,
                        JavaFile.builder(databaseWriter.getPackageName(),
                                databaseWriter.buildTypeSpec())
                                .build());
            }

        } catch (ProcessingException e) {
            error(e.getMessage(), e.element);
            throw e;
        }

        return false;
    }

    public Stream<SchemaDefinition> buildTableSchemas(RoundEnvironment roundEnv) {
        SchemaValidator validator = new SchemaValidator(processingEnv);
        return roundEnv
                .getElementsAnnotatedWith(Table.class)
                .stream()
                .map(element -> new SchemaDefinition(validator.validate(element)));
    }

    public Stream<SchemaDefinition> buildVirtualTableSchemas(RoundEnvironment roundEnv) {
        SchemaValidator validator = new SchemaValidator(processingEnv);
        return roundEnv
                .getElementsAnnotatedWith(VirtualTable.class)
                .stream()
                .map(element -> new SchemaDefinition(validator.validate(element)));
    }

    public void writeCodeForEachModel(SchemaDefinition schema, BaseWriter writer) {
        writeToFiler(schema.getElement(),
                JavaFile.builder(schema.getPackageName(), writer.buildTypeSpec())
                        .build());
    }

    public void writeToFiler(Element element, JavaFile javaFile) {
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            error("Failed to write " + javaFile.typeSpec.name + ": " + e, element);
        }
    }

    void note(CharSequence message, Element element) {
        printMessage(Diagnostic.Kind.NOTE, message, element);
    }

    void error(CharSequence message, Element element) {
        printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    void printMessage(Diagnostic.Kind kind, CharSequence message, Element element) {
        processingEnv.getMessager().printMessage(kind, "[" + TAG + "] " + message, element);
    }
}
