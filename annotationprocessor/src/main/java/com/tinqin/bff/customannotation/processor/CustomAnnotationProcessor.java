package com.tinqin.bff.customannotation.processor;

import com.google.auto.service.AutoService;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JCodeModelException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.writer.JCMWriter;
import com.tinqin.bff.customannotation.exception.ClassBuilderException;
import feign.Headers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.File;
import java.io.IOException;
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

        GetUrlFromAnnotation getUrlFromAnnotation = new GetUrlFromAnnotation();
        BuildRestExportMethod buildRestExportMethod = new BuildRestExportMethod();

        annotations.forEach(
                ann -> roundEnv.getElementsAnnotatedWith(ann)
                        .stream()
                        .filter(this::checkIfElementIsInController)
                        .forEach(method -> {
                                    String url = getUrlFromAnnotation.process(method);
                                    buildRestExportMethod.process(method, jc, url);
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

    private JDefinedClass createInterface(JCodeModel codeModel) {
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
}
