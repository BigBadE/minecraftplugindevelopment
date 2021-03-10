package com.bigbade.minecraftplugindevelopment.core;

import com.bigbade.minecraftplugindevelopment.core.annotations.PluginMain;
import com.bigbade.minecraftplugindevelopment.core.yml.YmlContainer;
import com.bigbade.minecraftplugindevelopment.core.yml.YmlValue;
import lombok.Getter;

import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.Writer;

@Getter
public final class PluginYMLManager {
    private static final YmlContainer outputContainer = new YmlContainer(null);
    private static PluginMain mainAnnotation = null;
    @Getter
    private static TypeElement mainClass = null;

    private PluginYMLManager() { }

    public static void setMainClass(TypeElement mainClass) {
        if(PluginYMLManager.mainClass != null) {
            throw new IllegalStateException("more than one main class detected!");
        }
        PluginYMLManager.mainAnnotation = mainClass.getAnnotation(PluginMain.class);
        PluginYMLManager.mainClass = mainClass;
        addValue("name", mainAnnotation.name());
        addValue("version", mainAnnotation.version());
        addValue("main", mainClass.getQualifiedName());
        if(mainAnnotation.authors().length != 0) {
            addValue("authors", mainAnnotation.authors());
        } else {
            addValue("author", mainAnnotation.author());
        }
        addValue("description", mainAnnotation.description());
        addValue("api-version", mainAnnotation.apiVersion());
        addValue("load", mainAnnotation.load().name());
        addValue("website", mainAnnotation.website());
        addValue("depend", mainAnnotation.depend());
        addValue("prefix", mainAnnotation.prefix());
        addValue("softdepend", mainAnnotation.softDepend());
        addValue("loadbefore", mainAnnotation.loadBefore());
    }

    private static void addValue(String key, Object value) {
        String string = value.toString();
        if(!string.isEmpty()) {
            outputContainer.addValue(new YmlValue(key, string));
        }
    }

    private static void addValue(String key, String[] value) {
        if(value.length != 0) {
            StringBuilder output = new StringBuilder("{ ");
            for(String found : value) {
                output.append(found).append(", ");
            }
            outputContainer.addValue(new YmlValue(key, output.substring(0, output.length()-2) + " }"));
        }
    }

    public static void write(FileObject resource) {
        //TODO permissions and commands
        try(Writer writer = resource.openWriter()) {
            writer.write(outputContainer.getYMLString().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
