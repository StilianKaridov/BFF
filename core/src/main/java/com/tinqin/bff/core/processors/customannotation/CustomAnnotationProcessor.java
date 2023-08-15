package com.tinqin.bff.core.processors.customannotation;

import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.tinqin.bff.core.annotations.GenerateRestExport;
import com.tinqin.bff.core.exception.ClassBuilderException;
import com.tinqin.bff.core.exception.ClassCreationException;
import com.tinqin.bff.core.exception.InvalidRequestLineException;
import com.tinqin.bff.core.exception.RequestMappingMethodNotFound;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

@Component
public class CustomAnnotationProcessor {

    private static final String CONTROLLERS_DIRECTORY_PATH = "rest/src/main/java/com/tinqin/bff/rest/controller";
    private static final String SPECIFIC_CONTROLLER_PATH = "com.tinqin.bff.rest.controller.";
    private static final String REST_EXPORT_PATH = "restexport/src/main/java";

    @SneakyThrows
    @PostConstruct
    public void init() {
        JCodeModel codeModel = new JCodeModel();
        JDefinedClass jc = createInterface(codeModel);

        File controllersDirectory = new File(CONTROLLERS_DIRECTORY_PATH);
        File[] files = controllersDirectory.listFiles();
        if (files != null) {
            createClassObjectIfFileIsController(files, jc);

            try {
                codeModel.build(new File(REST_EXPORT_PATH));
            } catch (IOException e) {
                throw new ClassBuilderException(e.getMessage());
            }
        }
    }

    private void createClassObjectIfFileIsController(File[] files, JDefinedClass jc) {
        Arrays.stream(files)
                .filter(this::checkIfFileIsController)
                .forEach(file -> {
                    String className = file.getName().replace(".java", "");
                    Class<?> clazz = getClassObjectFromClassName(SPECIFIC_CONTROLLER_PATH + className);

                    RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                    String requestMappingPath = requestMapping != null ? requestMapping.value()[0] : "";

                    createMethodIfAnnotationPresent(clazz, jc, requestMappingPath);
                });
    }

    @SneakyThrows
    private static JDefinedClass createInterface(JCodeModel codeModel) {
        JPackage jp = codeModel._package("com.tinqin.bff.restexport");
        JDefinedClass jc;
        try {
            jc = jp._class(JMod.PUBLIC, "BffRestClient", EClassType.INTERFACE);
        } catch (JClassAlreadyExistsException e) {
            throw new ClassCreationException(e.getMessage());
        }
        jc.annotate(Headers.class)
                .paramArray("value", "Content-Type: application/json");
        return jc;
    }

    private boolean checkIfFileIsController(File file) {
        String className = file.getName().replace(".java", "");
        Class<?> clazz = getClassObjectFromClassName(SPECIFIC_CONTROLLER_PATH + className);
        boolean isFileAController = clazz.isAnnotationPresent(RestController.class) || clazz.isAnnotationPresent(Controller.class);

        return file.isFile() && isFileAController;
    }

    @SneakyThrows
    private Class<?> getClassObjectFromClassName(String className) {
        return Class.forName(className);
    }

    private void createMethodIfAnnotationPresent(
            Class<?> clazz,
            JDefinedClass jc,
            String requestMappingPath
    ) {
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(GenerateRestExport.class))
                .forEach(method -> {
                    constructMethod(method, jc, requestMappingPath);
                });
    }

    private void constructMethod(
            Method method,
            JDefinedClass jc,
            String requestMappingPath
    ) {
        StringBuilder sb = new StringBuilder();
        Class<?> methodReturnType = (Class<?>) ((ParameterizedType) method
                .getGenericReturnType())
                .getActualTypeArguments()[0];

        String methodName = method.getName();
        JMethod methodForRestExport = jc.method(JMod.NONE, methodReturnType, methodName);

        Arrays.stream(method.getDeclaredAnnotations())
                .filter(this::checkIfAnnotationNameIsValid)
                .forEach(annotation -> {
                            appendAnnotationValue(sb, annotation, requestMappingPath);
                            addRequestLineAnnotationAndParametersToMethod(method, methodForRestExport, sb);
                        }
                );
    }

    private boolean checkIfAnnotationNameIsValid(Annotation annotation) {
        return annotation.annotationType().getSimpleName().endsWith("Mapping") ||
                annotation.annotationType().getSimpleName().endsWith("RequestLine");
    }

    private void appendAnnotationValue(
            StringBuilder sb,
            Annotation annotation,
            String requestMappingPath
    ) {
        String annotationName = annotation.annotationType().getSimpleName();
        switch (annotationName) {
            case "RequestLine" -> sb.append(getValueFromRequestLine(annotation, requestMappingPath));
            case "RequestMapping" -> sb.append(getValueFromRequestMapping(annotation, requestMappingPath));
            default -> sb.append(getValueFromSpecificMapping(annotation, requestMappingPath));
        }
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

    private String buildStringBuilder(
            String requestType,
            String requestMappingPath,
            String specificMappingPath
    ) {
        return new StringBuilder()
                .append(requestType)
                .append(" ")
                .append(requestMappingPath)
                .append(specificMappingPath)
                .toString();
    }

    @SneakyThrows
    private String[] performMethodInvoke(Annotation annotation, String method) {
        return (String[]) annotation
                .annotationType()
                .getMethod(method)
                .invoke(annotation);
    }

    private void addRequestLineAnnotationAndParametersToMethod(
            Method method,
            JMethod methodForRestExport,
            StringBuilder sb
    ) {
        Arrays.stream(method.getParameters())
                .forEach(parameter -> {
                    Class<?> type = parameter.getType();
                    String name = parameter.getName();
                    methodForRestExport.param(type, name).annotate(Param.class);

                    if (parameter.isAnnotationPresent(RequestParam.class)) {
                        if (sb.indexOf("?") == -1) {
                            sb.append("?");
                        } else {
                            sb.append("&");
                        }
                        sb.append(name).append("={").append(name).append("}");
                    }
                });

        methodForRestExport
                .annotate(RequestLine.class)
                .param("value", sb.toString());
    }
}
