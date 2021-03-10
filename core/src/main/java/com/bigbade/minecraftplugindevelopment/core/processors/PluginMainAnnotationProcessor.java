package com.bigbade.minecraftplugindevelopment.core.processors;

import com.bigbade.minecraftplugindevelopment.core.PluginYMLManager;
import com.bigbade.minecraftplugindevelopment.core.annotations.PluginMain;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes("com.bigbade.minecraftplugindevelopment.core.annotations.PluginMain")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PluginMainAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(annotations.isEmpty()) {
            return false;
        }
        for(Element element : roundEnv.getElementsAnnotatedWith(PluginMain.class)) {
            PluginYMLManager.setMainClass((TypeElement) element);
        }
        return true;
    }
}
