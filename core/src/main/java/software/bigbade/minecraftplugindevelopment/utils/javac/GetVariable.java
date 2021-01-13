package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.tools.javac.tree.JCTree;
import software.bigbade.minecraftplugindevelopment.api.ICallableExpression;

import javax.lang.model.element.Element;

public class GetVariable implements ICallableExpression<JCTree.JCFieldAccess> {
    private final ICallableExpression<?> calling;
    private final String name;

    public GetVariable(Element thisMethod, String name) {
        calling = new ThisCall(thisMethod);
        this.name = name;
    }

    public GetVariable(ICallableExpression<?> provider, String name) {
        this.calling = provider;
        this.name = name;
    }

    public GetVariable(String varName, String name) {
        this.calling = new IdentifyExpression(varName);
        this.name = name;
    }

    @Override
    public JCTree.JCFieldAccess call(TreeWrapper wrapper) {
        return wrapper.getTreeMaker().Select(calling.call(wrapper), wrapper.getNames().fromString(name));
    }
}
