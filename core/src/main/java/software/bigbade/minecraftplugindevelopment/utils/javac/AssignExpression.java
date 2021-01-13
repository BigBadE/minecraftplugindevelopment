package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;

@RequiredArgsConstructor
public class AssignExpression implements ICallableExpression<JCTree.JCAssign> {
    private final ICallableExpression<?> assigning;
    private final ICallableExpression<?> value;

    @Override
    public JCTree.JCAssign call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().Assign(assigning.call(wrapper), value.call(wrapper));
    }
}
