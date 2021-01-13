package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;
import software.bigbade.minecraftplugindevelopment.api.ICallableStatement;

@RequiredArgsConstructor
public class ReturnExpression implements ICallableStatement<JCTree.JCReturn> {
    private final ICallableExpression<?> returned;

    @Override
    public JCTree.JCReturn call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().Return(returned.call(wrapper));
    }
}
