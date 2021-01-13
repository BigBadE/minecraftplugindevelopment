package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;
import software.bigbade.minecraftplugindevelopment.utils.JavacUtils;

import java.util.List;

@RequiredArgsConstructor
public class CallExpression implements ICallableExpression<JCTree.JCMethodInvocation> {
    private final ICallableExpression<?> calling;
    private final List<ICallableExpression<?>> args;

    @Override
    public JCTree.JCMethodInvocation call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().Apply(null, calling.call(wrapper),
                JavacUtils.convertList(wrapper, args));
    }
}
