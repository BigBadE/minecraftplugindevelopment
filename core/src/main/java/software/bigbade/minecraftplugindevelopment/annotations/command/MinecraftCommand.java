package software.bigbade.minecraftplugindevelopment.annotations.command;

import software.bigbade.minecraftplugindevelopment.annotations.SpigotPermission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Automatically registers the command and adds the given restrictions to it.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface MinecraftCommand {
    //Command name
    String name();

    //Command description for the /help menu
    String description();

    //All aliases of the command. If the aliases are "hi" and "hello", running /hi or /hello runs the command
    String[] aliases();

    //If the command runner should be a player. If true, don't override and replace CommandSender with Player.
    boolean playerRunner() default false;

    //Permissions required. The permission will be automatically added to the plugin.yml.
    SpigotPermission permission() default @SpigotPermission(permission="");

    //Message if the user doesn't have permission to run the command.
    String permissionError() default "NO_PERMISSION_ERROR";

    //Message if the wrong type of user uses the message (The console if playerRunner is true)
    String wrongUserError() default "WRONG_USER_ERROR";

    //Message sent if false is returned from the command
    String usage() default "COMMAND_USAGE";
}
