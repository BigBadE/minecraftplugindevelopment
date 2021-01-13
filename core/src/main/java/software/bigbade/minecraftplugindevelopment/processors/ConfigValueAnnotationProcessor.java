package software.bigbade.minecraftplugindevelopment.processors;

import software.bigbade.minecraftplugindevelopment.annotations.ConfigValue;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;
import software.bigbade.minecraftplugindevelopment.api.ICallableStatement;
import software.bigbade.minecraftplugindevelopment.utils.JavacUtils;
import software.bigbade.minecraftplugindevelopment.utils.javac.AssignExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.CallExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.ExecutedExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.GetVariable;
import software.bigbade.minecraftplugindevelopment.utils.javac.IdentifyExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.LiteralValue;
import software.bigbade.minecraftplugindevelopment.utils.javac.MethodWrapper;
import software.bigbade.minecraftplugindevelopment.utils.javac.TreeWrapper;
import software.bigbade.minecraftplugindevelopment.utils.javac.VariableStatement;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("software.bigbade.minecraftplugindevelopment.annotations.ConfigValue")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ConfigValueAnnotationProcessor extends AbstractProcessor {
    private static final String FIELD_NAME = "config";
    private static final String CONFIG_CLASS = "org.bukkit.configuration.ConfigurationSection";

    private TreeWrapper wrapper;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        wrapper = new TreeWrapper(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<Element> added = new HashSet<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(ConfigValue.class)) {
            if (!added.contains(element.getEnclosingElement())) {
                added.add(element);
                addConfigConstructors(element.getEnclosingElement());
            }

            List<String> field = JavacUtils.getField(element.getEnclosingElement(), CONFIG_CLASS);
            if (!field.isEmpty()) {
                System.out.println("Found fields " + String.join(", ", field));
                if (field.contains(FIELD_NAME)) {
                    setFieldSetter(new IdentifyExpression(FIELD_NAME), element);
                } else if (field.contains("configuration")) {
                    setFieldSetter(new IdentifyExpression("configuration"), element);
                }
                setFieldSetter(new IdentifyExpression(field.get(0)), element);
                return true;
            }
            boolean hasConfigMethod = JavacUtils.findMethodWithSuperclasses(element.getEnclosingElement(),
                    "getConfig", "org.bukkit.configuration.file.FileConfiguration").isPresent();
            if (hasConfigMethod) {
                setFieldSetter(new CallExpression(new GetVariable(element.getEnclosingElement(), "getConfig"),
                        Collections.emptyList()), element);
                return true;
            }

            setFieldSetter(new IdentifyExpression(FIELD_NAME), element);
        }
        return true;
    }

    private void setFieldSetter(ICallableExpression<?> configExpression, Element element) {
        ConfigValue configValue = element.getAnnotation(ConfigValue.class);
        List<ICallableExpression<?>> args = new ArrayList<>();
        args.add(new LiteralValue(configValue.path()));
        boolean primitive = JavacUtils.isPrimitive(wrapper, element);
        if (!primitive) {
            args.add(new GetVariable(JavacUtils.getFieldType(element), "class"));
        }
        ICallableExpression<?> fieldInit = JavacUtils.getFieldInit(element);
        if (fieldInit.call(wrapper) != null) {
            args.add(fieldInit);
        }
        if (primitive) {
            String primitiveType = JavacUtils.getPrimitiveType(wrapper, element);
            primitiveType = primitiveType.substring(0, 1).toUpperCase() + primitiveType.substring(1);
            JavacUtils.setFieldInit(wrapper, element,
                    new CallExpression(new GetVariable(configExpression, "get" + primitiveType), args));
        } else {
            JavacUtils.setFieldInit(wrapper, element,
                    new CallExpression(new GetVariable(configExpression, "getObject"), args));
        }
    }

    private void addConfigConstructors(Element element) {
        ICallableExpression<?> configType = new IdentifyExpression("ConfigurationSection");
        for (Element testing : element.getEnclosedElements()) {
            if (testing instanceof ExecutableElement
                    && testing.getSimpleName().contentEquals("<init>")
                    && testing.getModifiers().contains(Modifier.PUBLIC)) {
                MethodWrapper constructorWrapper = JavacUtils.getWrapper(wrapper, (ExecutableElement) testing);
                JavacUtils.setPrivate(wrapper, testing);
                List<ICallableStatement<?>> params = new ArrayList<>();
                StringBuilder name = new StringBuilder(FIELD_NAME);
                boolean found = false;
                for (int i = 0; i < constructorWrapper.getTotalParams(); i++) {
                    if (constructorWrapper.getParamName(i).equals(name.toString())) {
                        name.append("-").append(i);
                    }
                    if (constructorWrapper.getParamType(i).equals(configType)) {
                        found = true;
                    }
                    params.add(new VariableStatement(0, constructorWrapper.getParamName(i), constructorWrapper.getParamType(i)));
                }
                if (!found) {
                    params.add(new VariableStatement(0, name.toString(), configType));
                    JavacUtils.importClass(wrapper, element, CONFIG_CLASS);
                }
                JavacUtils.addMethodToClass(wrapper, element, "<init>", 0L,
                        params, Arrays.asList(new ExecutedExpression(
                                        new IdentifyExpression("super"), Collections.emptyList()),
                                new ExecutedExpression(new AssignExpression(
                                        new GetVariable("this", FIELD_NAME),
                                        new IdentifyExpression(FIELD_NAME)), Collections.emptyList())));
            }
        }
        JavacUtils.addFieldToClass(wrapper, element, 2L, FIELD_NAME, configType);
    }
}
