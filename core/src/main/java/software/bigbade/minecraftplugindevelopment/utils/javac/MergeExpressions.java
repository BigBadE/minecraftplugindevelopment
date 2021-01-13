package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import software.bigbade.minecraftplugindevelopment.api.ICallableStatement;
import software.bigbade.minecraftplugindevelopment.utils.JavacUtils;

import java.util.Arrays;
import java.util.List;

public class MergeExpressions implements ICallableStatement<JCTree.JCBlock> {
    private final List<ICallableStatement<?>> statements;

    public MergeExpressions(ICallableStatement<?>... statements) {
        this.statements = Arrays.asList(statements);
    }

    public MergeExpressions(List<ICallableStatement<?>> statements) {
        this.statements = statements;
    }

    @Override
    public JCTree.JCBlock call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().Block(0, JavacUtils.convertList(wrapper, statements));
    }
}
