package software.bigbade.minecraftplugindevelopment.example;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;
import software.bigbade.minecraftplugindevelopment.annotations.ConfigValue;
import software.bigbade.minecraftplugindevelopment.annotations.PluginMain;

@PluginMain(name = "ExamplePlugin", version = "1.0")
public class ExamplePlugin extends JavaPlugin {
    @ConfigValue(path = "example.value")
    private final String testValue = "test";

    private final String myAwesomeValue = "Yay!!!";

    private String testing;

    public ExamplePlugin() {
        String testing = "test";
        this.testing = testing;
    }

    @Override
    public void onEnable() {
        System.out.println(String.class);
        saveDefaultConfig();
        System.out.println("Test value: " + getConfig().getObject("test.value", Integer.class, 1));
    }
}
