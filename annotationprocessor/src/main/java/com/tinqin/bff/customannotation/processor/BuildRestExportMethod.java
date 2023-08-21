package com.tinqin.bff.customannotation.processor;

import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.tinqin.bff.customannotation.exception.ClassBuilderException;
import feign.Param;
import feign.RequestLine;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

public class BuildRestExportMethod {

    public void process(Element method, JDefinedClass jc, String url) {
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
        methodForRestExport
                .annotate(RequestLine.class)
                .param("value", url);

        method.getAnnotationMirrors()
                .stream()
                .filter(this::checkIfAnnotationNameIsValid)
                .forEach(annotation -> addParameters(method, methodForRestExport));
    }

    private boolean checkIfAnnotationNameIsValid(AnnotationMirror annotation) {
        return annotation.getAnnotationType().toString().endsWith("Mapping") ||
                annotation.getAnnotationType().toString().endsWith("RequestLine");
    }

    private void addParameters(Element method, JMethod methodForRestExport) {
        ((ExecutableElement) method).getParameters()
                .forEach(parameter -> {
                    String name = parameter.getSimpleName().toString();

                    String[] tokens = parameter.asType().toString().split("\\s+");
                    Class<?> parameterType;
                    try {
                        parameterType = Class.forName(tokens[tokens.length - 1]);
                    } catch (ClassNotFoundException e) {
                        throw new ClassBuilderException(e.getMessage());
                    }

                    methodForRestExport.param(parameterType, name).annotate(Param.class);
                });
    }
}
