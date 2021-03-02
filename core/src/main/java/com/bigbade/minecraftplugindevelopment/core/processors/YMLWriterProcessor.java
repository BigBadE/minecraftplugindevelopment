package com.bigbade.minecraftplugindevelopment.core.processors;

import com.bigbade.minecraftplugindevelopment.core.PluginYMLManager;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class YMLWriterProcessor extends AbstractProcessor {
    private boolean written = false;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("Writing");
        if(!written) {
            try {
                FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
                        "plugin.yml");
                PluginYMLManager.write(resource);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Error making plugin.yml");
                e.printStackTrace();
            }
            written = true;
        }
        return false;
    }
}
