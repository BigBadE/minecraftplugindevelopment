package software.bigbade.minecraftplugindevelopment;

import groovy.lang.Closure;
import org.gradle.api.Task;
import org.gradle.api.tasks.JavaExec;

import javax.annotation.Nonnull;

public class MinecraftPluginTask extends JavaExec {
    @Nonnull
    @Override
    public Task configure(@Nonnull Closure closure) {
        setMain("compileOnly");
        return super.configure(closure);
    }

    @Override
    public void exec() {
        // Configure JavaExec
        setIgnoreExitValue(true);
        setClasspath(getProject().getConfigurations().getByName("compileOnly"));
        super.exec();
    }
}
