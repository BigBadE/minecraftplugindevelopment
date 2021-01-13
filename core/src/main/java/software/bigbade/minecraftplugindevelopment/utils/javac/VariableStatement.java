package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;
import software.bigbade.minecraftplugindevelopment.api.ICallableStatement;

@RequiredArgsConstructor
public class VariableStatement implements ICallableStatement<JCTree.JCVariableDecl> {
    private final long modifiers;
    private final String name;
    private final ICallableExpression<?> type;

    @Override
    public JCTree.JCVariableDecl call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().VarDef(wrapper.getTreeMaker().Modifiers(modifiers),
                wrapper.getNames().fromString(name), type.call(wrapper), null);
    }
}
