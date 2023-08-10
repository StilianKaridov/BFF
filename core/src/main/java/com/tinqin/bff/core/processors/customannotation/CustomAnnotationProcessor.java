package com.tinqin.bff.core.processors.customannotation;

import com.tinqin.bff.core.annotations.RequestInfoToTextFile;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@Component
public class CustomAnnotationProcessor {

    private static final String CONTROLLERS_DIRECTORY_PATH = "rest/src/main/java/com/tinqin/bff/rest/controller";
    private static final String SPECIFIC_CONTROLLER_PATH = "com.tinqin.bff.rest.controller.";
    private static final String FILE_WITH_ANNOTATED_METHODS_PATH = CONTROLLERS_DIRECTORY_PATH + "/requestInfo.txt";

    @PostConstruct
    public void process() throws IOException {
        StringBuilder sb = new StringBuilder();

        File controllersDirectory = new File(CONTROLLERS_DIRECTORY_PATH);
        File[] files = controllersDirectory.listFiles();
        if (files != null) {
            Arrays.stream(files)
                    .filter(file -> file.isFile() && file.getName().contains("Controller"))
                    .forEach(file -> {
                        String className = file.getName().replace(".java", "");

                        Class<?> clazz;
                        try {
                            clazz = Class.forName(SPECIFIC_CONTROLLER_PATH + className);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }

                        String requestMappingPath = clazz.getAnnotation(RequestMapping.class).value()[0];

                        Arrays.stream(clazz.getDeclaredMethods())
                                .filter(method -> method.isAnnotationPresent(RequestInfoToTextFile.class))
                                .forEach(method -> Arrays.stream(method.getDeclaredAnnotations())
                                        .filter(annotation -> annotation.toString().contains("Mapping"))
                                        .forEach(annotation -> {
                                            String[] annotationValues;
                                            try {
                                                annotationValues = (String[]) annotation
                                                        .annotationType()
                                                        .getMethod("value")
                                                        .invoke(annotation);
                                            } catch (IllegalAccessException |
                                                     InvocationTargetException |
                                                     NoSuchMethodException e) {
                                                throw new RuntimeException(e);
                                            }
                                            String specificMappingPath = annotationValues.length != 0
                                                    ? annotationValues[0] : "";

                                            String requestType = annotation
                                                    .annotationType()
                                                    .getSimpleName()
                                                    .replace("Mapping", "")
                                                    .toUpperCase();

                                            sb.append(requestType)
                                                    .append(" ")
                                                    .append(requestMappingPath)
                                                    .append(specificMappingPath)
                                                    .append("\n");
                                        }));
                    });
            File allAnnotatedMethods = new File(FILE_WITH_ANNOTATED_METHODS_PATH);
            FileWriter fileWriter = new FileWriter(allAnnotatedMethods);
            fileWriter.write(sb.toString().trim());
            fileWriter.close();
        }
    }
}
