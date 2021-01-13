package software.bigbade.minecraftplugindevelopment.utils;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import software.bigbade.minecraftplugindevelopment.api.ICallable;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;
import software.bigbade.minecraftplugindevelopment.api.ICallableStatement;
import software.bigbade.minecraftplugindevelopment.utils.javac.GetVariable;
import software.bigbade.minecraftplugindevelopment.utils.javac.IdentifyExpression;
import software.bigbade.minecraftplugindevelopment.utils.javac.MethodWrapper;
import software.bigbade.minecraftplugindevelopment.utils.javac.TreeWrapper;
import software.bigbade.minecraftplugindevelopment.utils.javac.VariableStatement;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public final class JavacUtils {
    private static final Pattern PACKAGE_REGEX = Pattern.compile("\\.");

    private JavacUtils() {
    }

    public static void importClass(TreeWrapper wrapper, Element classElement, String clazz) {
        JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) wrapper.getTrees().getTree(classElement);
        List<JCTree> defs = new ArrayList<>(classDecl.defs);
        defs.add(0,
                wrapper.getTreeMaker().Import(new GetVariable(
                        new IdentifyExpression(clazz.substring(0, clazz.lastIndexOf("."))),
                        clazz.substring(clazz.lastIndexOf(".") + 1)).call(wrapper), false));
        classDecl.defs = com.sun.tools.javac.util.List.from(defs);
    }

    public static boolean isPrimitive(TreeWrapper wrapper, Element field) {
        return ((JCTree.JCVariableDecl) wrapper.getTrees().getTree(field)).getType().type.isPrimitive();
    }

    public static String getPrimitiveType(TreeWrapper wrapper, Element field) {
        return ((JCTree.JCVariableDecl) wrapper.getTrees().getTree(field)).getType().type.toString();
    }

    public static ICallableExpression<JCTree.JCIdent> getFieldType(Element field) {
        return wrapper ->
                (JCTree.JCIdent) ((JCTree.JCVariableDecl) wrapper.getTrees().getTree(field)).getType();
    }

    public static void setFieldInit(TreeWrapper wrapper, Element field, ICallableExpression<?> init) {
        JCTree.JCVariableDecl fieldInit = (JCTree.JCVariableDecl) wrapper.getTrees().getTree(field);
        fieldInit.init = init.call(wrapper);
    }

    public static String getType(JCTree.JCExpression expression) {
        JCTree.JCIdent ident = (JCTree.JCIdent) expression;
        return ident.getName() == null ? ident.sym.toString() : ident.name.toString();
    }

    public static List<String> getField(Element classElement, String fieldType) {
        List<String> fields = new ArrayList<>();
        for (Element child : classElement.getEnclosedElements()) {
            if (!(child instanceof VariableElement)) {
                continue;
            }
            VariableElement variableElement = (VariableElement) child;
            String strType = variableElement.asType().toString();
            if (!PACKAGE_REGEX.matcher(strType).matches()) {
                String[] returning = PACKAGE_REGEX.split(strType);
                strType = returning[returning.length - 1];
            }
            if (strType.equals(fieldType)) {
                fields.add(variableElement.getSimpleName().toString());
            }
        }
        return fields;
    }

    public static ICallableExpression<?> getFieldInit(Element fieldElement) {
        return wrapper -> ((JCTree.JCVariableDecl) wrapper.getTrees().getTree(fieldElement)).init;
    }

    public static Optional<ExecutableElement> findMethodWithSuperclasses(Element classElement, @Nullable String name, String returnType, String... args) {
        if (returnType == null) {
            returnType = "void";
        }
        Optional<ExecutableElement> found = Optional.empty();
        for (Element element : getSuperclasses(classElement)) {
            found = checkClassForMethod(element, name, returnType, args);
            if (found.isPresent()) {
                break;
            }
        }
        return found;
    }

    public static List<Element> getSuperclasses(Element element) {
        List<Element> elements = new ArrayList<>();
        Type superClass = (Type) element.asType();
        while (superClass.asElement() != null) {
            elements.add(superClass.asElement());
            superClass = (Type) ((TypeElement) superClass.asElement()).getSuperclass();
        }
        return elements;
    }

    private static Optional<ExecutableElement> checkClassForMethod(Element classElement, @Nullable String name, String returnType, String[] args) {
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (!(enclosed instanceof ExecutableElement)) {
                continue;
            }

            ExecutableElement executableElement = (ExecutableElement) enclosed;
            if ((name != null && !executableElement.getSimpleName().contentEquals(name))
                    || !(((Type) executableElement.getReturnType()).asElement() instanceof Symbol.ClassSymbol)) {
                continue;
            }

            boolean found = args.length == 0;
            for (Element element : getSuperclasses(((Type) executableElement.getReturnType()).asElement())) {
                for (TypeMirror interfaceType : ((TypeElement) element).getInterfaces()) {
                    if(testName(((Type) interfaceType).asElement(), returnType)) {
                        found = true;
                    }
                }
                if(found) {
                    break;
                }
                if(testName(element, returnType)) {
                    found = true;
                }
            }

            if (!found) {
                continue;
            }

            int i = 0;
            for (VariableElement param : executableElement.getParameters()) {
                if (!param.getSimpleName().contentEquals(args[i])) {
                    found = false;
                    break;
                }
                i++;
            }
            if (found) {
                return Optional.of(executableElement);
            }
        }
        return Optional.empty();
    }

    private static boolean testName(Element element, String returnType) {
        String foundReturn = ((Type) element.asType()).tsym.toString();

        if (!PACKAGE_REGEX.matcher(returnType).matches()) {
            String[] returning = PACKAGE_REGEX.split(foundReturn);
            foundReturn = returning[returning.length - 1];
        }

        return foundReturn.equals(returnType);
    }

    public static Optional<MethodWrapper> findMethod(TreeWrapper wrapper, Element classElement, @Nullable String name, @Nullable String returnType, String... args) {
        MethodWrapper found = checkClassForMethod(wrapper, classElement, name, returnType, args);
        return Optional.ofNullable(found);
    }

    @Nullable
    private static MethodWrapper checkClassForMethod(TreeWrapper wrapper, Element classElement, @Nullable String name, @Nullable String returnType, String[] args) {
        if (returnType == null) {
            returnType = "void";
        }
        Tree tree = wrapper.getTrees().getTree(classElement);
        for (JCTree def : ((JCTree.JCClassDecl) tree).defs) {
            if (!(def instanceof JCTree.JCMethodDecl) || !checkMethod((JCTree.JCMethodDecl) def, name, returnType, args)) {
                continue;
            }
            return new MethodWrapper((JCTree.JCMethodDecl) def);
        }
        return null;
    }

    private static boolean checkMethod(JCTree.JCMethodDecl methodDecl, String name, String returnType, String[] args) {
        if ((name == null || methodDecl.getName().contentEquals(name)) && methodDecl.getParameters().size() == args.length
                && ((methodDecl.getReturnType() == null && args.length == 0)
                || (methodDecl.getReturnType() != null && methodDecl.getReturnType().type.tsym.toString().equals(returnType)))) {
            boolean passed = true;
            for (int i = 0; i < args.length; i++) {
                if (!args[i].equals(methodDecl.getParameters().get(i).getType().type.toString())) {
                    passed = false;
                    break;
                }
            }
            return passed;
        }
        return false;
    }

    public static MethodWrapper getWrapper(TreeWrapper wrapper, ExecutableElement element) {
        return new MethodWrapper((JCTree.JCMethodDecl) wrapper.getTrees().getTree(element));
    }

    public static void addFieldToClass(TreeWrapper wrapper, Element classElement, long modifier,
                                       String name, ICallableExpression<?> type) {
        JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) wrapper.getTrees().getTree(classElement);
        List<JCTree> defs = new ArrayList<>(classDecl.defs);
        defs.add(0, new VariableStatement(modifier, name, type).call(wrapper));
        classDecl.defs = com.sun.tools.javac.util.List.from(defs);
    }

    public static MethodWrapper addMethodToClass(TreeWrapper wrapper, Element classElement, String name, long modifiers,
                                                 List<ICallableStatement<?>> params, List<ICallableStatement<?>> statements) {
        JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) wrapper.getTrees().getTree(classElement);
        List<JCTree> defs = new ArrayList<>(classDecl.defs);

        JCTree.JCMethodDecl methodDecl = wrapper.getTreeMaker().MethodDef(wrapper.getTreeMaker().Modifiers(modifiers,
                com.sun.tools.javac.util.List.nil()),
                wrapper.getNames().fromString(name), null, com.sun.tools.javac.util.List.nil(),
                JavacUtils.convertList(wrapper, params), com.sun.tools.javac.util.List.nil(),
                wrapper.getTreeMaker().Block(0, convertList(wrapper, statements)),
                null);
        defs.add(methodDecl);
        classDecl.defs = com.sun.tools.javac.util.List.from(defs);
        return new MethodWrapper(methodDecl);
    }

    public static void setPrivate(TreeWrapper wrapper, Element constructor) {
        JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) wrapper.getTrees().getTree(constructor);
        methodDecl.mods = wrapper.getTreeMaker().Modifiers(2L);
    }

    @SuppressWarnings("unchecked")
    public static <T extends JCTree> com.sun.tools.javac.util.List<T> convertList(TreeWrapper wrapper, List<? extends ICallable<?>> callables) {
        JCTree[] array = new JCTree[callables.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = callables.get(i).call(wrapper);
        }
        return (com.sun.tools.javac.util.List<T>) com.sun.tools.javac.util.List.from(array);
    }
}
