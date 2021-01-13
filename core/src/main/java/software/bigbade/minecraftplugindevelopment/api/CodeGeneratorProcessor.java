package software.bigbade.minecraftplugindevelopment.api;

import software.bigbade.minecraftplugindevelopment.annotations.PluginMain;
import software.bigbade.minecraftplugindevelopment.utils.JavacUtils;
import software.bigbade.minecraftplugindevelopment.utils.javac.CallExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.ExecutedExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.GetVariable;
import software.bigbade.minecraftplugindevelopment.utils.javac.IdentifyExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.MergeExpressions;
import software.bigbade.minecraftplugindevelopment.utils.javac.MethodWrapper;
import software.bigbade.minecraftplugindevelopment.utils.javac.TreeWrapper;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class CodeGeneratorProcessor<T extends Annotation> extends AbstractProcessor {
    protected Messager messager;
    protected TreeWrapper wrapper;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        wrapper = new TreeWrapper(processingEnv);
        messager = processingEnv.getMessager();
    }

    public abstract ICallableStatement<?> getRegisterStatement(T annotation, List<ICallableExpression<?>> params, Element method);

    public abstract String getRegisterMethod();

    //Modifiers: first bit = public, second = private, third = protected, fourth = static
    protected void register(RoundEnvironment roundEnv, Class<T> annotationClass) {
        List<ICallableStatement<?>> registered = new ArrayList<>();
        Element classElement = roundEnv.getElementsAnnotatedWith(PluginMain.class).iterator().next();

        for (Element registering : roundEnv.getElementsAnnotatedWith(annotationClass)) {
            setupRegisterBlock(registered, annotationClass, registering, classElement);
        }

        Element mainElement = roundEnv.getElementsAnnotatedWith(annotationClass).iterator().next();
        MethodWrapper registerMethod = JavacUtils.findMethod(wrapper, mainElement, getRegisterMethod(), null).orElseGet(() -> {
            MethodWrapper createdMethod = JavacUtils.addMethodToClass(wrapper, classElement, getRegisterMethod(), 2L,
                    Collections.emptyList(), registered);

            MethodWrapper onEnableMethod = JavacUtils.findMethod(wrapper, classElement, "onEnable", null).orElseThrow(() ->
                    new IllegalStateException("Could not find onEnable method!"));
            ICallableStatement<?> statement = new ExecutedExpression(new GetVariable(classElement, getRegisterMethod()), Collections.emptyList());
            onEnableMethod.setBody(new MergeExpressions(onEnableMethod.getStatements(), statement), wrapper);
            return createdMethod;
        });

        registerMethod.setBody(new MergeExpressions(registered), wrapper);
    }

    private void setupRegisterBlock(List<ICallableStatement<?>> registered, Class<T> annotationClass, Element registering, Element mainClass) {
        T element = registering.getAnnotation(annotationClass);
        List<ICallableExpression<?>> arguments = new ArrayList<>();

        ExecutableElement constructor = null;
        //Try to get constructor params from local variables
        for (Element testing : registering.getEnclosedElements()) {
            if (testing instanceof ExecutableElement && testing.getSimpleName().contentEquals("<init>")
                    && testing.getModifiers().contains(Modifier.PUBLIC)) {
                constructor = (ExecutableElement) testing;
                break;
            }
        }

        if (constructor == null) {
            throw new IllegalStateException("Command has no public constructor!");
        }

        MethodWrapper methodWrapper = JavacUtils.getWrapper(wrapper, constructor);

        for (int i = 0; i < methodWrapper.getTotalParams(); i++) {
            String param = methodWrapper.getParamName(i);
            String paramGetter = "get" + param.substring(0, 1).toUpperCase() + param.substring(1);
            ICallableExpression<?> type = methodWrapper.getParamType(i);
            String strType = JavacUtils.getType(type.call(wrapper));

            List<String> field = JavacUtils.getField(mainClass, strType);
            if (!field.isEmpty()) {
                if(field.contains(param)) {
                    arguments.add(new IdentifyExpression(param));
                } else {
                    arguments.add(new IdentifyExpression(field.get(0)));
                }
                continue;
            }
            boolean hasConfigMethod = JavacUtils.findMethodWithSuperclasses(mainClass,
                    paramGetter,
                    strType).isPresent();
            if (hasConfigMethod) {
                arguments.add(new CallExpression(new GetVariable(mainClass, paramGetter),
                        Collections.emptyList()));
            } else {
                Optional<ExecutableElement> getterMethod = JavacUtils.findMethodWithSuperclasses(mainClass, null,
                        constructor.getParameters().get(i).getSimpleName().toString());

                if (getterMethod.isPresent()) {
                    arguments.add(new GetVariable(registering, getterMethod.get().getSimpleName().toString()));
                    break;
                }
                messager.printMessage(Diagnostic.Kind.ERROR, "Could not find required param " + param + " as " +
                        "a field, getter, nor could any getter with the same type be found!");
                return;
            }
        }

        registered.add(getRegisterStatement(element, arguments, registering));
    }
}
