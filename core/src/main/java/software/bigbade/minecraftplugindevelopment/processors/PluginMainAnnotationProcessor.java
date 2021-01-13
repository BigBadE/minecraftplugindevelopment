package software.bigbade.minecraftplugindevelopment.processors;

import com.sun.tools.javac.tree.JCTree;
import software.bigbade.minecraftplugindevelopment.annotations.PluginMain;
import software.bigbade.minecraftplugindevelopment.annotations.SpigotPermission;
import software.bigbade.minecraftplugindevelopment.annotations.command.MinecraftCommand;
import software.bigbade.minecraftplugindevelopment.manager.PermissionManager;
import software.bigbade.minecraftplugindevelopment.manager.PluginYMLManager;
import software.bigbade.minecraftplugindevelopment.utils.JavacUtils;
import software.bigbade.minecraftplugindevelopment.utils.javac.TreeWrapper;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@SupportedAnnotationTypes("software.bigbade.minecraftplugindevelopment.annotations.PluginMain")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PluginMainAnnotationProcessor extends AbstractProcessor {
    private Messager messager;
    private TreeWrapper wrapper;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        wrapper = new TreeWrapper(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            try {
                FileObject resource = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
                        "plugin.yml");
                writePluginYML(resource);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Error creating plugin.yml");
                e.printStackTrace();
            }
            return false;
        }
        if (annotations.isEmpty()) {
            if(PluginYMLManager.INSTANCE.getMain() == null) {
                messager.printMessage(Diagnostic.Kind.WARNING, "No PluginMain annotation found!");
            }
            return false;
        }
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotations.iterator().next());
        if (elements.size() != 1) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                    "More than one PluginMain annotation detected! Using first one found");
        }

        Element element = elements.iterator().next();
        PluginYMLManager.INSTANCE.setMain(element.getAnnotation(PluginMain.class),
                ((TypeElement) element).getQualifiedName().toString());
        return true;
    }

    //A super simplistic approach, but I don't want to deal with SnakeYML.
    public void writePluginYML(FileObject resource) {
        StringBuilder ymlBuilder = new StringBuilder();
        writePluginMain(ymlBuilder);
        writeCommands(ymlBuilder);
        writePermissions(ymlBuilder);
        try (OutputStreamWriter writer = new OutputStreamWriter(resource.openOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(ymlBuilder.toString());
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR,  "Error writing plugin.yml");
            e.printStackTrace();
        }
    }

    private static void writeCommands(StringBuilder builder) {
        if(!PluginYMLManager.INSTANCE.getCommands().isEmpty()) {
            builder.append("commands:\n");
        }
        for(MinecraftCommand command : PluginYMLManager.INSTANCE.getCommands()) {
            builder.append("  ").append(command.name()).append(":\n");
            addValue(command.description(), "description", builder, 2);
            addValue(command.aliases(), "aliases", builder, 2);
            addValue(command.permission().permission(), "permission", builder, 2);
            //TODO Localize this to English at least
            addValue(command.permissionError(), "permission-message", builder, 2);
            addValue(command.usage(), "usage", builder, 2);
        }
    }

    private static void writePermissions(StringBuilder builder) {
        if(!PermissionManager.INSTANCE.getPermissions().isEmpty()) {
            builder.append("permissions:\n");
        }
        for(SpigotPermission permission : PermissionManager.INSTANCE.getPermissions()) {
            builder.append("  ").append(permission.permission()).append(":\n");
            addValue(permission.description(), "description", builder, 2);
            addValue(permission.defaultValue(), "default", builder, 2);
            if(permission.children().length != 0) {
                builder.append("    children:\n");
                for(int i = 0; i < permission.children().length; i++) {
                    builder.append("      ").append(permission.children()[i]).append(": ")
                            .append(permission.childrenInheritance().length < i
                                    ? "true" : permission.childrenInheritance()[i])
                            .append("\n");
                }
            }
        }
    }

    private static void writePluginMain(StringBuilder builder) {
        PluginMain main = PluginYMLManager.INSTANCE.getMain();
        builder.append("name: ").append(main.name()).append("\n")
                .append("version: \"").append(main.version()).append("\"\n")
                .append("main: ").append(PluginYMLManager.INSTANCE.getMainClassPath())
                .append("\n");
        addValue(main.author(), "author", builder, 0);
        addValue(main.authors(), "authors", builder, 0);
        addValue(main.description(), "description", builder, 0);
        addValue(main.apiVersion(), "apiVersion", builder, 0);
        addValue(main.load(), "load", builder, 0);
        addValue(main.website(), "website", builder, 0);
        addValue(main.depend(), "depend", builder, 0);
        addValue(main.prefix(), "prefix", builder, 0);
        addValue(main.softDepend(), "softDepend", builder, 0);
        addValue(main.loadBefore(), "loadBefore", builder, 0);
    }

    private static void addValue(Object value, String name, StringBuilder builder, int tabs) {
        if(value instanceof String && value.toString().isEmpty()
                || value instanceof Object[] && ((Object[]) value).length == 0) {
            return;
        }
        for (int i = 0; i < tabs; i++) {
            builder.append("  ");
        }
        builder.append(name).append(": ");
        if(value instanceof String) {
            builder.append(value).append("\n");
        } else if(value instanceof String[]) {
            builder.append("[ ").append(String.join(", ", (String[]) value)).append(" ]\n");
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + value.getClass() + " (" + value.toString() + ")");
        }
    }
}
