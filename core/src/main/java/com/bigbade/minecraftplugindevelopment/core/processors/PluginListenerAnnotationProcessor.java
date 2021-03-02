package com.bigbade.minecraftplugindevelopment.core.processors;

import com.bigbade.minecraftplugindevelopment.api.code.IClassType;
import com.bigbade.minecraftplugindevelopment.api.code.parameter.IParameterType;
import com.bigbade.minecraftplugindevelopment.api.expressions.IBasicExpression;
import com.bigbade.minecraftplugindevelopment.api.expressions.IExpressionReference;
import com.bigbade.minecraftplugindevelopment.api.factories.ICodeFactory;
import com.bigbade.minecraftplugindevelopment.api.factories.INodeFactory;
import com.bigbade.minecraftplugindevelopment.api.nodes.IClassNode;
import com.bigbade.minecraftplugindevelopment.api.nodes.IMethodNode;
import com.bigbade.minecraftplugindevelopment.api.nodes.builder.IMethodNodeBuilder;
import com.bigbade.minecraftplugindevelopment.core.PluginYMLManager;
import com.bigbade.minecraftplugindevelopment.core.annotations.PluginListener;
import com.bigbade.processorcodeapi.NodeFactoryCreator;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("com.bigbade.minecraftplugindevelopment.core.annotations.PluginListener")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PluginListenerAnnotationProcessor extends AbstractProcessor {
    private static final String LISTENER_METHOD_NAME = "registerListeners";

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

        IClassNode mainClass = factory.getClassNode(PluginYMLManager.getMainClass());
        List<IMethodNode> found = mainClass.findMethods(LISTENER_METHOD_NAME, null, factory.getVoidType(),
                null);
        IMethodNodeBuilder registerListener = found.isEmpty() ? mainClass.getMethodBuilder(LISTENER_METHOD_NAME)
                : mainClass.getMethodBuilder(found.get(0));
        ICodeFactory coder = factory.getCodeFactory();
        IClassNode mainNode = factory.getClassNode(PluginYMLManager.getMainClass());
        for (Element annotated : roundEnv.getElementsAnnotatedWith(PluginListener.class)) {
            IClassType thisType = factory.getClassType((TypeElement) annotated);
            mainNode.importClass(thisType);
            IExpressionReference serverReference = coder.createReference(null,
                    thisType.getMethod("getServer",
                            factory.getParameterType(factory.getClassType("org.bukkit.Server"))));
            IExpressionReference pluginManagerReference = coder.createReference(null,
                    coder.getVariable(serverReference, "getPluginManager"));
            List<IBasicExpression> params = getParams(annotated);

            registerListener.getCodeBlock().addStatement(coder.callReference(coder.createReference(null,
                    coder.getVariable(pluginManagerReference, "registerEvents"),
                    coder.instantiateClass(null, thisType, params.toArray(new IBasicExpression[0])),
                    coder.thisReference(thisType))));
        }
        if(found.isEmpty()) {
            IMethodNodeBuilder builder = mainClass.getMethodBuilder(
                    mainNode.findMethods("onEnable",
                    new IParameterType[0], factory.getVoidType(), Modifier.PUBLIC).get(0));
            builder.getCodeBlock().addStatement(coder.callReference(coder.createReference(null,
                    factory.getClassType(PluginYMLManager.getMainClass()).getMethod(LISTENER_METHOD_NAME,
                            factory.getVoidType()))));
            builder.build();
        }
        registerListener.build();
        return true;
    }

    @SuppressWarnings("unchecked")
    private List<IBasicExpression> getParams(Element annotated) {
        List<IBasicExpression> params = new ArrayList<>();
        List<IMethodNode> constructors =
                factory.getClassNode((TypeElement) annotated).findMethods("<init>", null,
                        null, Modifier.PUBLIC);
        List<VariableElement> found;
        if (constructors.isEmpty()) {
            factory.getMessenger().printMessage(Diagnostic.Kind.ERROR, "No public constructor for "
                    + annotated.getSimpleName());
            return params;
        } else if (constructors.size() > 1) {
            factory.getMessenger().printMessage(Diagnostic.Kind.WARNING, "Too many constructors on " +
                    annotated.getSimpleName() + ", trying a random one!");
            found = (List<VariableElement>) constructors.get(0).getMethodElement().getParameters();
        } else {
            if (annotated.getAnnotation(RequiredArgsConstructor.class) != null) {
                found = new ArrayList<>();
                for (Element contained : annotated.getEnclosedElements()) {
                    if (contained instanceof VariableElement) {
                        VariableElement variable = (VariableElement) contained;
                        if (variable.getModifiers().contains(javax.lang.model.element.Modifier.FINAL)) {
                            found.add(variable);
                        }
                    }
                }
            } else {
                found = (List<VariableElement>) constructors.get(0).getMethodElement().getParameters();
            }
        }
        List<VariableElement> mainVariables = new ArrayList<>();
        List<ExecutableElement> methods = new ArrayList<>();
        for (TypeMirror mirror : factory.getTypeUtils().getSuperclasses(PluginYMLManager.getMainClass())) {
            for (Element element : factory.getTypeUtils().getElement(mirror).getEnclosedElements()) {
                if (element instanceof VariableElement
                        && element.getModifiers().contains(javax.lang.model.element.Modifier.PUBLIC)) {
                    mainVariables.add((VariableElement) element);
                } else if (element instanceof ExecutableElement
                        && element.getModifiers().contains(javax.lang.model.element.Modifier.PUBLIC)) {
                    methods.add((ExecutableElement) element);
                }
            }
        }
        ICodeFactory coder = factory.getCodeFactory();
        for (VariableElement element : found) {
            VariableElement foundVariable = null;
            for (VariableElement testing : mainVariables) {
                if (factory.getTypeUtils().isType(testing, element)
                        && (foundVariable == null || element.getSimpleName().equals(testing.getSimpleName()))) {
                    foundVariable = testing;
                }
            }
            if (foundVariable != null) {
                params.add(coder.getVariable(factory.getClassType(PluginYMLManager.getMainClass()),
                        foundVariable.getSimpleName().toString()));
                continue;
            }
            ExecutableElement foundMethod = null;
            for (ExecutableElement method : methods) {
                if (method.getParameters().isEmpty() && factory.getTypeUtils().isType(method.getReturnType(), element.asType())
                        && (foundMethod == null || method.getSimpleName().toString()
                        .equalsIgnoreCase("get" + element.getSimpleName()))) {
                    foundMethod = method;
                }
            }
            if (foundMethod == null) {
                factory.getMessenger().printMessage(Diagnostic.Kind.ERROR, "Could not find way to get param " +
                        element.getSimpleName() + ", make sure you have a variable or getter of that type " +
                        "(or supertype) in the main class");
                continue;
            }
            params.add(coder.createReference(null,
                    factory.getVariableType(element).getMethod(foundMethod)));
        }
        return params;
    }
}
