package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallable;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;

@RequiredArgsConstructor
public class CompareExpression implements ICallable<JCTree.JCUnary> {
    private final boolean not;
    private final ICallableExpression<?> checking;

    @Override
    public JCTree.JCUnary call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().Unary(not ? JCTree.Tag.NOT : JCTree.Tag.EQ, checking.call(wrapper));
    }
}
