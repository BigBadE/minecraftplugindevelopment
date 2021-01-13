package software.bigbade.minecraftplugindevelopment.processors;

import software.bigbade.minecraftplugindevelopment.annotations.SpigotPermission;
import software.bigbade.minecraftplugindevelopment.manager.PermissionManager;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes("software.bigbade.minecraftplugindevelopment.annotations.SpigotPermission")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SpigotPermissionAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(annotations.isEmpty()) {
            return false;
        }

        annotations.stream()
                .flatMap(element -> roundEnv.getElementsAnnotatedWith(element).stream())
                .map(element -> element.getAnnotation(SpigotPermission.class))
                .forEach(PermissionManager.INSTANCE::addPermission);
        return true;
    }
}
