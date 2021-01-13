package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;

@RequiredArgsConstructor
public class CastExpression implements ICallableExpression<JCTree.JCTypeCast> {
    private final ICallableExpression<?> type;
    private final ICallableExpression<?> from;

    @Override
    public JCTree.JCTypeCast call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().TypeCast(type.call(wrapper), from.call(wrapper));
    }
}
