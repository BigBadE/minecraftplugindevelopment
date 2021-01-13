package software.bigbade.minecraftplugindevelopment.utils.javac;

import com.sun.source.util.Trees;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import lombok.Getter;
import sun.tools.tree.Statement;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

public class TreeWrapper {
    @Getter
    private final TreeMaker treeMaker;
    @Getter
    private final Names names;
    @Getter
    private final Trees trees;

    public TreeWrapper(ProcessingEnvironment processingEnv) {
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        treeMaker = TreeMaker.instance(context);
        trees = Trees.instance(processingEnv);
        names = Names.instance(context);
    }

    public void setMethodBody(JCTree.JCMethodDecl methodDecl, List<JCTree.JCStatement> statements) {
        methodDecl.body = treeMaker.Block(0, statements);
    }

    public void visitClass(Element element, JCTree.Visitor visitor) {
        ((JCTree.JCClassDecl) trees.getTree(element)).accept(visitor);
    }

    public boolean startsWith(ExecutableElement element, int index, String starter) {
        return ((JCTree.JCMethodDecl) trees.getTree(element)).params.get(index).getType().type.toString().startsWith(starter);
    }
}
