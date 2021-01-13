package software.bigbade.minecraftplugindevelopment.processors;


import software.bigbade.minecraftplugindevelopment.annotations.EventListener;
import software.bigbade.minecraftplugindevelopment.api.CodeGeneratorProcessor;
import software.bigbade.minecraftplugindevelopment.api.EventCaller;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;
import software.bigbade.minecraftplugindevelopment.api.ICallableStatement;
import software.bigbade.minecraftplugindevelopment.utils.JavacUtils;
import software.bigbade.minecraftplugindevelopment.utils.javac.CallExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.CastExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.ClassCreator;
import software.bigbade.minecraftplugindevelopment.utils.javac.CompareExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.CreateVariableStatement;
import software.bigbade.minecraftplugindevelopment.utils.javac.ExecutedExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.GetVariable;
import software.bigbade.minecraftplugindevelopment.utils.javac.IdentifyExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.IfStatement;
import software.bigbade.minecraftplugindevelopment.utils.javac.LiteralValue;
import software.bigbade.minecraftplugindevelopment.utils.javac.MergeExpressions;
import software.bigbade.minecraftplugindevelopment.utils.javac.MethodWrapper;
import software.bigbade.minecraftplugindevelopment.utils.javac.ThisCall;
import software.bigbade.minecraftplugindevelopment.utils.javac.TypeTestExpression;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("software.bigbade.minecraftplugindevelopment.annotations.EventListener")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class EventListenerAnnotationProcessor extends CodeGeneratorProcessor<EventListener> {
    private static final String REGISTER_METHOD = "registerListeners";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty() || roundEnv.processingOver()) {
            return false;
        }

        register(roundEnv, EventListener.class);

        addTypeCheck(roundEnv);

        return true;
    }

    private void addTypeCheck(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(EventListener.class)) {
            for (Element testing : element.getEnclosedElements()) {
                if (!(testing instanceof ExecutableElement)) {
                    continue;
                }
                ExecutableElement executable = (ExecutableElement) testing;

                if (executable.getParameters().size() > 2
                        || executable.getParameters().isEmpty()
                        || !wrapper.startsWith(executable, 0, "org.bukkit.event.entity")) {
                    continue;
                }
                EventListener annotation = element.getAnnotation(EventListener.class);
                if (annotation.caller() != EventCaller.PLAYER) {
                    continue;
                }
                if (executable.getParameters().size() != 2 && annotation.caller() == EventCaller.PLAYER) {
                    messager.printMessage(Diagnostic.Kind.WARNING, "Incorrect parameters for event listener! Requires " +
                            "an EntityEvent and a Player!");
                    return;
                }
                addListenerCheck(JavacUtils.getWrapper(wrapper, executable), annotation);
            }
        }
    }

    private void addListenerCheck(MethodWrapper methodWrapper, EventListener annotation) {
        String playerClassName = methodWrapper.getParamName(1);
        ICallableExpression<?> playerType = methodWrapper.getParamType(1);
        ICallableExpression<?> eventExpression = new IdentifyExpression(methodWrapper.getParamName(0));
        ICallableStatement<?> playerExpression = new CreateVariableStatement(0, playerClassName,
                playerType,
                new CastExpression(playerType,
                        new CallExpression(new GetVariable(eventExpression, "getEntity"), Collections.emptyList())));
        if (!annotation.permission().isEmpty() && annotation.caller() == EventCaller.PLAYER) {
            ICallableExpression<?> callCommandSender =
                    new CallExpression(
                            new GetVariable("player", "hasPermission"),
                            Collections.singletonList(new LiteralValue(annotation.permission())));
            methodWrapper.setBody(new IfStatement(new CompareExpression(true, callCommandSender), null,
                    methodWrapper.getStatements()), wrapper);
        }
        if (annotation.caller() == EventCaller.PLAYER) {
            ICallableExpression<?> isPlayer = new TypeTestExpression(
                    new CallExpression(new GetVariable(eventExpression, "getEntity"),
                            Collections.emptyList()), playerType);
            methodWrapper.setBody(new IfStatement(new CompareExpression(true, isPlayer), null,
                    new MergeExpressions(playerExpression, methodWrapper.getStatements())), wrapper);
        }
        methodWrapper.removeParameter(1);
    }

    @Override
    public ICallableStatement<?> getRegisterStatement(EventListener annotation, List<ICallableExpression<?>> params, Element method) {
        ClassCreator newClass = new ClassCreator(method.getSimpleName() + "", params);

        return new ExecutedExpression(new GetVariable(new CallExpression(
                new GetVariable(new CallExpression(new GetVariable(new ThisCall(method), "getServer"),
                        Collections.emptyList()), "getPluginManager"), Collections.emptyList()), "registerEvents"),
                Arrays.asList(newClass, new ThisCall(method)));
    }

    @Override
    public String getRegisterMethod() {
        return REGISTER_METHOD;
    }
}
