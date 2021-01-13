package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;
import software.bigbade.minecraftplugindevelopment.api.ICallableStatement;

import java.util.ArrayList;

@RequiredArgsConstructor
public class MethodWrapper {
    private final JCTree.JCMethodDecl methodDecl;

    public void setBody(ICallableStatement<?> expression, TreeWrapper wrapper) {
        methodDecl.body = wrapper.getTreeMaker().Block(0, List.of(expression.call(wrapper)));
    }

    public String getParamName(int index) {
        return methodDecl.params.get(index).getName().toString();
    }

    public int getTotalParams() { return methodDecl.params.size(); }

    public ICallableStatement<JCTree.JCBlock> getStatements() {
        return wrapper -> methodDecl.body;
    }

    public ICallableExpression<?> getParamType(int index) {
        return wrapper -> (JCTree.JCExpression) methodDecl.getParameters().get(index).getType();
    }

    public void removeParameter(int index) {
        java.util.List<JCTree.JCVariableDecl> parameters = new ArrayList<>(methodDecl.getParameters());
        parameters.remove(index);
        methodDecl.params = com.sun.tools.javac.util.List.from(parameters);
    }
}
