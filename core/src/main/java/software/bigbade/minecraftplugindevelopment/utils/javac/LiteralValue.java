package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;

@RequiredArgsConstructor
public class LiteralValue implements ICallableExpression<JCTree.JCLiteral> {
    private final Object literal;

    @Override
    public JCTree.JCLiteral call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().Literal(literal);
    }
}
