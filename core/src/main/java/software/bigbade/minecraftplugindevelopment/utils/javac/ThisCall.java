package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;

import javax.lang.model.element.Element;

@RequiredArgsConstructor
public class ThisCall implements ICallableExpression<JCTree.JCExpression> {
    private final Element thisMethod;

    @Override
    public JCTree.JCExpression call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().This(((Symbol.ClassSymbol) thisMethod).type);
    }
}
