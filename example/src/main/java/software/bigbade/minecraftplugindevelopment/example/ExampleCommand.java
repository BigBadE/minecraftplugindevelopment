package software.bigbade.minecraftplugindevelopment.example;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import software.bigbade.minecraftplugindevelopment.annotations.ConfigValue;
import software.bigbade.minecraftplugindevelopment.annotations.command.MinecraftCommand;
import software.bigbade.minecraftplugindevelopment.annotations.SpigotPermission;

import javax.annotation.Nonnull;

@MinecraftCommand(name = "example",
        aliases = { "test", "testing" },
        description = "An example command",
        permission = @SpigotPermission(permission = "example.permission"),
        permissionError = "\u00A7cYou do not have permission to run this command!")
public class ExampleCommand implements CommandExecutor {
    private final ConfigurationSection configurationSection;

    public ExampleCommand(ConfigurationSection config, String myAwesomeValue) {
        this.configurationSection = config;
    }

    @ConfigValue(path = "example.value")
    private final int testNumb = 2;

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        sender.sendMessage("Test!");
        return true;
    }
}
