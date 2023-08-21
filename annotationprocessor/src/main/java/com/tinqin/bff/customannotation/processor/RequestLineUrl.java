package com.tinqin.bff.customannotation.processor;

import com.tinqin.bff.customannotation.exception.InvalidRequestLineException;
import com.tinqin.bff.customannotation.exception.UnsupportedRequestTypeException;
import feign.RequestLine;

import javax.lang.model.element.Element;
import java.util.List;

public class RequestLineUrl {

    public String process(Element method, String requestMappingPath) {
        RequestLine annotation = method.getAnnotation(RequestLine.class);
        String annotationValue = annotation.value();
        if (annotationValue.isBlank()) {
            throw new InvalidRequestLineException();
        }

        String[] infoFromRequestLineValue = annotationValue.split("\\s+");
        List<String> possibleRequestMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE");
        String requestType = infoFromRequestLineValue[0].toUpperCase();
        if (possibleRequestMethods.stream().noneMatch(r -> r.equals(requestType))) {
            throw new UnsupportedRequestTypeException();
        }
        String specificMappingPath = infoFromRequestLineValue.length == 2
                ? infoFromRequestLineValue[1] : "";

        return requestType +
                " " +
                requestMappingPath +
                specificMappingPath;
    }
}
