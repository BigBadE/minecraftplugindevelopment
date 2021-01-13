package software.bigbade.minecraftplugindevelopment.api;

import com.sun.tools.javac.tree.JCTree;
import software.bigbade.minecraftplugindevelopment.utils.javac.TreeWrapper;

public interface ICallable<T extends JCTree> {
    T call(TreeWrapper wrapper);
}
