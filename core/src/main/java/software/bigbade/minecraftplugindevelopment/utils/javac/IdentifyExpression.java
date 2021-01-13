package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;

@RequiredArgsConstructor
public class IdentifyExpression implements ICallableExpression<JCTree.JCIdent> {
    private final String name;

    @Override
    public JCTree.JCIdent call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().Ident(wrapper.getNames().fromString(name));
    }
}
