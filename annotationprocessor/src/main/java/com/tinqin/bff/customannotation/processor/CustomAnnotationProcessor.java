package com.tinqin.bff.customannotation.processor;

import com.google.auto.service.AutoService;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JCodeModelException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.writer.JCMWriter;
import com.tinqin.bff.customannotation.exception.ClassBuilderException;
import com.tinqin.bff.customannotation.exception.InvalidRequestLineException;
import com.tinqin.bff.customannotation.exception.RequestMappingMethodNotFound;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes(
        "com.tinqin.bff.customannotation.annotation.GenerateRestExport"
)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class CustomAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.getRootElements().isEmpty()) {
            return true;
        }

        JCodeModel codeModel = new JCodeModel();
        JDefinedClass jc = createInterface(codeModel);

        annotations.forEach(
                ann -> roundEnv.getElementsAnnotatedWith(ann)
                        .stream()
                        .filter(this::checkIfElementIsInController)
                        .forEach(method -> {
                                    Element clazz = method.getEnclosingElement();
                                    RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                                    String requestMappingPath = requestMapping != null ? requestMapping.value()[0] : "";
                                    constructMethod(method, jc, requestMappingPath);
                                }
                        ));

        try {
            JCMWriter writer = new JCMWriter(codeModel);
            writer.build(new File("restexport/src/main/java/"));
        } catch (IOException e) {
            throw new ClassBuilderException(e.getMessage());
        }

        return true;
    }

    private static JDefinedClass createInterface(JCodeModel codeModel) {
        JPackage jp = codeModel._package("com.tinqin.bff.restexport");
        JDefinedClass jc;

        try {
            jc = jp._class(JMod.PUBLIC, "BffRestClient", EClassType.INTERFACE);
        } catch (JCodeModelException e) {
            throw new ClassBuilderException(e.getMessage());
        }

        jc.annotate(Headers.class)
                .paramArray("value", "Content-Type: application/json");
        return jc;
    }

    private boolean checkIfElementIsInController(Element element) {
        return element.getEnclosingElement().getAnnotation(RestController.class) != null
                || element.getEnclosingElement().getAnnotation(Controller.class) != null;
    }

    private void constructMethod(
            Element method,
            JDefinedClass jc,
            String requestMappingPath
    ) {
        StringBuilder sb = new StringBuilder();

        Class<?> returnType;
        try {
            returnType = Class.forName(((DeclaredType) ((ExecutableElement) method)
                    .getReturnType())
                    .getTypeArguments()
                    .stream()
                    .findFirst()
                    .get()
                    .toString());
        } catch (ClassNotFoundException e) {
            throw new ClassBuilderException(e.getMessage());
        }

        String methodName = method.getSimpleName().toString();
        JMethod methodForRestExport = jc.method(JMod.NONE, returnType, methodName);

        method.getAnnotationMirrors()
                .stream()
                .filter(this::checkIfAnnotationNameIsValid)
                .forEach(annotation -> {
                            appendAnnotationValue(sb, annotation, requestMappingPath, method);
                            addRequestLineAnnotationAndParametersToMethod(method, methodForRestExport, sb);
                        }
                );
    }

    private boolean checkIfAnnotationNameIsValid(AnnotationMirror annotation) {
        return annotation.getAnnotationType().toString().endsWith("Mapping") ||
                annotation.getAnnotationType().toString().endsWith("RequestLine");
    }

    private void appendAnnotationValue(
            StringBuilder sb,
            AnnotationMirror annotation,
            String requestMappingPath,
            Element method
    ) {
        String annotationName = annotation.getAnnotationType().asElement().getSimpleName().toString();
        switch (annotationName) {
            case "RequestLine" -> sb.append(getValueFromRequestLine(method, requestMappingPath));
            case "RequestMapping" -> sb.append(getValueFromRequestMapping(method, requestMappingPath));
            default -> sb.append(getValueFromSpecificMapping(annotation, requestMappingPath));
        }
    }

    private String getValueFromRequestLine(Element method, String requestMappingPath) {
        RequestLine annotation = method.getAnnotation(RequestLine.class);
        String annotationValue = annotation.value();
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

    private String getValueFromRequestMapping(Element method, String requestMappingPath) {
        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
        String[] annotationValue = annotation.value();
        String specificMappingPath = annotation.value().length == 0 ? "" : annotationValue[0];

        RequestMethod[] annotationMethod = annotation.method();
        if (annotationMethod.length == 0) {
            throw new RequestMappingMethodNotFound();
        }
        RequestMethod requestType = annotationMethod[0];

        return buildStringBuilder(requestType.toString(), requestMappingPath, specificMappingPath);
    }

    private String getValueFromSpecificMapping(AnnotationMirror annotation, String requestMappingPath) {
        String specificMappingPath = "";

        if (!annotation.getElementValues().values().isEmpty()) {
            String value = annotation
                    .getElementValues()
                    .values()
                    .toArray()[0]
                    .toString();

            specificMappingPath = value.substring(2, value.length() - 2);
        }

        String requestType = annotation
                .getAnnotationType()
                .asElement()
                .getSimpleName()
                .toString()
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

    private void addRequestLineAnnotationAndParametersToMethod(
            Element method,
            JMethod methodForRestExport,
            StringBuilder sb
    ) {
        ((ExecutableElement) method).getParameters()
                .forEach(parameter -> {
                    String name = parameter.getSimpleName().toString();

                    Class<?> parameterType;

                    String[] tokens = parameter.asType().toString().split("\\s+");
                    try {
                        parameterType = Class.forName(tokens[tokens.length - 1]);
                    } catch (ClassNotFoundException e) {
                        throw new ClassBuilderException(e.getMessage());
                    }


                    methodForRestExport.param(parameterType, name).annotate(Param.class);

                    if (parameter.getAnnotation(RequestParam.class) != null) {
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
