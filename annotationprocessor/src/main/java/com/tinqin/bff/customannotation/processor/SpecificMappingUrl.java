package com.tinqin.bff.customannotation.processor;

import javax.lang.model.element.AnnotationMirror;

public class SpecificMappingUrl {

    public String process(AnnotationMirror annotation, String requestMappingPath) {
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

        return requestType +
                " " +
                requestMappingPath +
                specificMappingPath;
    }
}
