package software.bigbade.minecraftplugindevelopment.processors;

import software.bigbade.minecraftplugindevelopment.annotations.command.MinecraftCommand;
import software.bigbade.minecraftplugindevelopment.api.CodeGeneratorProcessor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;
import software.bigbade.minecraftplugindevelopment.api.ICallableStatement;
import software.bigbade.minecraftplugindevelopment.manager.PluginYMLManager;
import software.bigbade.minecraftplugindevelopment.utils.JavacUtils;
import software.bigbade.minecraftplugindevelopment.utils.javac.CallExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.ClassCreator;
import software.bigbade.minecraftplugindevelopment.utils.javac.CompareExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.ExecutedExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.GetVariable;
import software.bigbade.minecraftplugindevelopment.utils.javac.IfStatement;
import software.bigbade.minecraftplugindevelopment.utils.javac.LiteralValue;
import software.bigbade.minecraftplugindevelopment.utils.javac.MergeExpressions;
import software.bigbade.minecraftplugindevelopment.utils.javac.MethodWrapper;
import software.bigbade.minecraftplugindevelopment.utils.javac.ReturnExpression;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SupportedAnnotationTypes("software.bigbade.minecraftplugindevelopment.annotations.command.MinecraftCommand")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MinecraftCommandAnnotationProcessor extends CodeGeneratorProcessor<MinecraftCommand> {
    private static final String REGISTER_METHOD = "registerCommands";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty() || roundEnv.processingOver()) {
            return false;
        }

        addPermissionCheck(roundEnv);

        register(roundEnv, MinecraftCommand.class);

        roundEnv.getElementsAnnotatedWith(annotations.iterator().next()).stream()
                .map(element -> element.getAnnotation(MinecraftCommand.class))
                .forEach(PluginYMLManager.INSTANCE::addCommand);

        return true;
    }

    @Override
    public ICallableStatement<?> getRegisterStatement(MinecraftCommand annotation, List<ICallableExpression<?>> params, Element method) {
        ICallableExpression<?> newClass = new ClassCreator(method.getSimpleName() + "", params);
        return new ExecutedExpression(new GetVariable(new CallExpression(new GetVariable(method, "getCommand"),
                Collections.singletonList(new LiteralValue(annotation.name()))), "setExecutor"),
                Collections.singletonList(newClass));
    }

    @Override
    public String getRegisterMethod() {
        return REGISTER_METHOD;
    }

    private void addPermissionCheck(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(MinecraftCommand.class)) {
            MethodWrapper methodWrapper = JavacUtils.findMethod(wrapper, element,  "onCommand", "boolean","org.bukkit.command.CommandSender",
                    "org.bukkit.command.Command", "java.lang.String", "java.lang.String[]").orElseThrow(() ->
                    new IllegalStateException("No onCommand method of command!"));

            Optional<ICallableStatement<?>> jcStatement = addPermissionMessage(methodWrapper, element.getAnnotation(MinecraftCommand.class));
            jcStatement.ifPresent(statement -> methodWrapper.setBody(statement, wrapper));
        }
    }

    private Optional<ICallableStatement<?>> addPermissionMessage(MethodWrapper methodWrapper, MinecraftCommand annotation) {
        ICallableStatement<?> permissionMessage = new ExecutedExpression(
                new GetVariable(methodWrapper.getParamName(0), "sendMessage"),
                //TODO Localize this also
                Collections.singletonList(new LiteralValue(annotation.permissionError())));
        if(!annotation.permission().permission().isEmpty()) {
            ICallableExpression<?> callCommandSender =
                    new CallExpression(
                            new GetVariable(methodWrapper.getParamName(0), "hasPermission"),
                            Collections.singletonList(new LiteralValue(annotation.permission().permission())));
            ICallableStatement<?> statement = new IfStatement(new CompareExpression(true, callCommandSender),
                    new MergeExpressions(permissionMessage, new ReturnExpression(new LiteralValue(true))),
                    methodWrapper.getStatements());

            return Optional.of(statement);
        }
        return Optional.empty();
    }
}
