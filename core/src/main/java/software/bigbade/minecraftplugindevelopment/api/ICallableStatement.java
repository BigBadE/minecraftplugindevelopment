package software.bigbade.minecraftplugindevelopment.api;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import software.bigbade.minecraftplugindevelopment.utils.javac.TreeWrapper;

public interface ICallableStatement<T extends JCTree.JCStatement> extends ICallable<T> {

}
