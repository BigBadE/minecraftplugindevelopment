package com.bigbade.minecraftplugindevelopment.core.processors;

import com.bigbade.minecraftplugindevelopment.api.factories.INodeFactory;
import com.bigbade.minecraftplugindevelopment.api.nodes.IClassNode;
import com.bigbade.minecraftplugindevelopment.core.annotations.Test;
import com.bigbade.minecraftplugindevelopment.javac.factories.JavacNodeFactory;
import com.bigbade.processorcodeapi.NodeFactoryCreator;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.Modifier;
import java.util.Set;

@SupportedAnnotationTypes("com.bigbade.minecraftplugindevelopment.core.annotations.Test")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TestAnnotationProcessor extends AbstractProcessor {
    private INodeFactory factory;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        factory = NodeFactoryCreator.getNodeFactory(processingEnv);
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        for(Element element : roundEnv.getElementsAnnotatedWith(Test.class)) {
            //IClassNode node = factory.getClassNode((TypeElement) element);
            //node.getMethodBuilder("testMethod").setModifier(Modifier.PUBLIC | Modifier.STATIC).build();
        }
        return true;
    }
}
