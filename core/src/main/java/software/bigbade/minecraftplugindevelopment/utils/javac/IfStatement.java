package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableStatement;

@RequiredArgsConstructor
public class IfStatement implements ICallableStatement<JCTree.JCIf> {
    private final CompareExpression comparison;
    private final ICallableStatement<?> ifCode;
    private final ICallableStatement<?> elseStatement;

    @Override
    public JCTree.JCIf call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().If(comparison.call(wrapper),
                ifCode == null ? wrapper.getTreeMaker().Block(0, List.nil()) : ifCode.call(wrapper),
                elseStatement == null ? wrapper.getTreeMaker().Block(0, List.nil()) : elseStatement.call(wrapper));
    }
}
