package software.bigbade.minecraftplugindevelopment.manager;

import lombok.Getter;
import software.bigbade.minecraftplugindevelopment.annotations.command.MinecraftCommand;
import software.bigbade.minecraftplugindevelopment.annotations.PluginMain;

import java.util.ArrayList;
import java.util.List;

public class PluginYMLManager {
    public static final PluginYMLManager INSTANCE = new PluginYMLManager();

    @Getter
    private final List<MinecraftCommand> commands = new ArrayList<>();

    @Getter
    private String mainClassPath = null;
    @Getter
    private PluginMain main = null;

    public void addCommand(MinecraftCommand command) {
        commands.add(command);
    }

    public void setMain(PluginMain main, String classPath) {
        if(this.main != null || this.mainClassPath != null) {
            throw new IllegalArgumentException("Two main classes have been set!");
        }
        this.main = main;
        this.mainClassPath = classPath;
    }
}
