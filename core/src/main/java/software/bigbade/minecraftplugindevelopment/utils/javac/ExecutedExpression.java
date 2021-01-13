package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;
import software.bigbade.minecraftplugindevelopment.api.ICallableStatement;
import software.bigbade.minecraftplugindevelopment.utils.JavacUtils;

@RequiredArgsConstructor
public class ExecutedExpression implements ICallableStatement<JCTree.JCExpressionStatement> {
    private final ICallableExpression<?> calling;
    private final java.util.List<ICallableExpression<?>> args;

    @Override
    public JCTree.JCExpressionStatement call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().Exec(wrapper.getTreeMaker().Apply(null, calling.call(wrapper),
                JavacUtils.convertList(wrapper, args)));
    }
}
