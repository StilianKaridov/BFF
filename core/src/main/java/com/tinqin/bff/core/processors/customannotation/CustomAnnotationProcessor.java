package com.tinqin.bff.core.processors.customannotation;

import com.tinqin.bff.core.annotations.RequestInfoToTextFile;
import com.tinqin.bff.core.exception.InvalidRequestLineException;
import com.tinqin.bff.core.exception.RequestMappingMethodNotFound;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

@Component
public class CustomAnnotationProcessor {

    private static final String CONTROLLERS_DIRECTORY_PATH = "rest/src/main/java/com/tinqin/bff/rest/controller";
    private static final String SPECIFIC_CONTROLLER_PATH = "com.tinqin.bff.rest.controller.";
    private static final String FILE_WITH_ANNOTATED_METHODS_PATH = CONTROLLERS_DIRECTORY_PATH + "/requestInfo.txt";

    @PostConstruct
    public void init() {
        StringBuilder sb = new StringBuilder();

        File controllersDirectory = new File(CONTROLLERS_DIRECTORY_PATH);
        File[] files = controllersDirectory.listFiles();
        if (files != null) {
            Arrays.stream(files)
                    .filter(this::checkIfFileIsValid)
                    .forEach(file -> {
                        String className = file.getName().replace(".java", "");
                        Class<?> clazz = getClassObjectFromClassName(SPECIFIC_CONTROLLER_PATH + className);

                        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                        String requestMappingPath = requestMapping != null ? requestMapping.value()[0] : "";

                        Arrays.stream(clazz.getDeclaredMethods())
                                .filter(method -> method.isAnnotationPresent(RequestInfoToTextFile.class))
                                .forEach(method -> Arrays.stream(method.getDeclaredAnnotations())
                                        .filter(this::checkIfAnnotationNameIsValid)
                                        .forEach(annotation ->
                                                this.appendAnnotationValue(sb, annotation, requestMappingPath)
                                        )
                                );
                    });
            writeToFile(sb);
        }
    }

    @SneakyThrows
    private String getValueFromRequestMapping(Annotation annotation, String requestMappingPath) {
        String[] annotationValues = performMethodInvoke(annotation, "value");
        String specificMappingPath = annotationValues.length != 0 ? annotationValues[0] : "";

        RequestMethod[] annotationMethod = (RequestMethod[]) annotation
                .annotationType()
                .getMethod("method")
                .invoke(annotation);

        if (annotationMethod.length == 0) {
            throw new RequestMappingMethodNotFound();
        }
        RequestMethod requestType = annotationMethod[0];
        return buildStringBuilder(requestType.toString(), requestMappingPath, specificMappingPath);
    }

    @SneakyThrows
    private String getValueFromSpecificMapping(Annotation annotation, String requestMappingPath) {
        String[] annotationValues = performMethodInvoke(annotation, "value");
        String specificMappingPath = annotationValues.length != 0 ? annotationValues[0] : "";
        String requestType = annotation
                .annotationType()
                .getSimpleName()
                .replace("Mapping", "")
                .toUpperCase();
        return buildStringBuilder(requestType, requestMappingPath, specificMappingPath);
    }

    @SneakyThrows
    private String getValueFromRequestLine(Annotation annotation, String requestMappingPath) {
        String annotationValue = (String) annotation
                .annotationType()
                .getMethod("value")
                .invoke(annotation);
        if (annotationValue.isBlank()) {
            throw new InvalidRequestLineException();
        }

        String[] infoFromRequestLineValue = annotationValue.split("\\s+");
        List<String> possibleRequestMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE");
        String requestType = infoFromRequestLineValue[0].toUpperCase();
        if (possibleRequestMethods.stream().noneMatch(r -> r.equals(requestType))) {
            throw new UnsupportedOperationException("Unsupported request type.");
        }
        String specificMappingPath = infoFromRequestLineValue.length == 2
                ? infoFromRequestLineValue[1] : "";

        return buildStringBuilder(requestType, requestMappingPath, specificMappingPath);
    }

    @SneakyThrows
    private Class<?> getClassObjectFromClassName(String className) {
        return Class.forName(className);
    }

    @SneakyThrows
    private void writeToFile(StringBuilder sb) {
        File allAnnotatedMethods = new File(FILE_WITH_ANNOTATED_METHODS_PATH);
        FileWriter fileWriter = new FileWriter(allAnnotatedMethods);
        fileWriter.write(sb.toString().trim());
        fileWriter.close();
    }

    @SneakyThrows
    private String[] performMethodInvoke(Annotation annotation, String method) {
        return (String[]) annotation
                .annotationType()
                .getMethod(method)
                .invoke(annotation);
    }

    private String buildStringBuilder(String requestType, String requestMappingPath, String specificMappingPath) {
        return new StringBuilder()
                .append(requestType)
                .append(" ")
                .append(requestMappingPath)
                .append(specificMappingPath)
                .append("\n")
                .toString();
    }

    private void appendAnnotationValue(StringBuilder sb, Annotation annotation, String requestMappingPath) {
        String annotationName = annotation.annotationType().getSimpleName();
        switch (annotationName) {
            case "RequestLine" -> sb.append(getValueFromRequestLine(annotation, requestMappingPath));
            case "RequestMapping" -> sb.append(getValueFromRequestMapping(annotation, requestMappingPath));
            default -> sb.append(getValueFromSpecificMapping(annotation, requestMappingPath));
        }
    }

    private boolean checkIfAnnotationNameIsValid(Annotation annotation) {
        return annotation.annotationType().getSimpleName().endsWith("Mapping") ||
                annotation.annotationType().getSimpleName().endsWith("RequestLine");
    }

    private boolean checkIfFileIsValid(File file) {
        return file.isFile() && file.getName().endsWith("Controller.java");
    }
}
