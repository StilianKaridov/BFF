package com.tinqin.bff.customannotation.processor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

public class GetUrlFromAnnotation {

    public String process(Element method) {
        StringBuilder sb = new StringBuilder();

        Element clazz = method.getEnclosingElement();
        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
        String requestMappingPath = requestMapping != null ? requestMapping.value()[0] : "";

        method.getAnnotationMirrors()
                .stream()
                .filter(this::checkIfAnnotationNameIsValid)
                .forEach(annotation -> {
                    sb.append(appendAnnotationValue(annotation, requestMappingPath, method));
                });

        addQueryParameterToUrlIfPresent((ExecutableElement) method, sb);

        return sb.toString();
    }

    private void addQueryParameterToUrlIfPresent(ExecutableElement method, StringBuilder sb) {
        method.getParameters()
                .stream()
                .filter(p -> p.getAnnotation(RequestParam.class) != null)
                .forEach(parameter -> {
                    String name = parameter.getSimpleName().toString();
                    if (sb.indexOf("?") == -1) {
                        sb.append("?");
                    } else {
                        sb.append("&");
                    }
                    sb.append(name).append("={").append(name).append("}");
                });
    }

    private boolean checkIfAnnotationNameIsValid(AnnotationMirror annotation) {
        return annotation.getAnnotationType().toString().endsWith("Mapping") ||
                annotation.getAnnotationType().toString().endsWith("RequestLine");
    }

    private String appendAnnotationValue(
            AnnotationMirror annotation,
            String requestMappingPath,
            Element method
    ) {
        String annotationName = annotation.getAnnotationType().asElement().getSimpleName().toString();
        switch (annotationName) {
            case "RequestLine" -> {
                RequestLineUrl requestLineUrl = new RequestLineUrl();
                return requestLineUrl.process(method, requestMappingPath);
            }
            case "RequestMapping" -> {
                RequestMappingUrl requestMappingUrl = new RequestMappingUrl();
                return requestMappingUrl.process(method, requestMappingPath);
            }
            default -> {
                SpecificMappingUrl specificMappingUrl = new SpecificMappingUrl();
                return specificMappingUrl.process(annotation, requestMappingPath);
            }
        }
    }
}
