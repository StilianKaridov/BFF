package com.tinqin.bff.customannotation.processor;

import com.tinqin.bff.customannotation.exception.RequestMappingMethodNotFound;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Element;

public class RequestMappingUrl {

    public String process(Element method, String requestMappingPath) {
        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
        String[] annotationValue = annotation.value();
        String specificMappingPath = annotation.value().length == 0 ? "" : annotationValue[0];

        RequestMethod[] annotationMethod = annotation.method();
        if (annotationMethod.length == 0) {
            throw new RequestMappingMethodNotFound();
        }
        RequestMethod requestType = annotationMethod[0];

        return requestType +
                " " +
                requestMappingPath +
                specificMappingPath;
    }
}
