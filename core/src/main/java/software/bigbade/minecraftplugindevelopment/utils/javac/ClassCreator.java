package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.RequiredArgsConstructor;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;
import software.bigbade.minecraftplugindevelopment.utils.JavacUtils;

@RequiredArgsConstructor
public class ClassCreator implements ICallableExpression<JCTree.JCNewClass> {
    private final String name;
    private final java.util.List<ICallableExpression<?>> statements;

    @Override
    public JCTree.JCNewClass call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().NewClass(null, null,
                wrapper.getTreeMaker().Ident(wrapper.getNames().fromString(name)),
                JavacUtils.convertList(wrapper, statements), null);
    }
}
