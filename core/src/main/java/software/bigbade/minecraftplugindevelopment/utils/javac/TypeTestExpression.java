package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;

@RequiredArgsConstructor
public class TypeTestExpression implements ICallableExpression<JCTree.JCInstanceOf> {
    private final ICallableExpression<?> testType;
    private final ICallableExpression<?> testing;

    @Override
    public JCTree.JCInstanceOf call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().TypeTest(testType.call(wrapper), testing.call(wrapper));
    }
}
