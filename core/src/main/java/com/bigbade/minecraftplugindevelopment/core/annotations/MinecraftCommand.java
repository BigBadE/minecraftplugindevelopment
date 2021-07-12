package com.bigbade.minecraftplugindevelopment.core.annotations;

public @interface MinecraftCommand {
    //Command description for the /help menu
    String name();
    //All aliases of the command. If the aliases are "hi" and "hello", running /hi or /hello runs the command
    String[] aliases();
    //If the command runner should be a player. If true, don't override and replace CommandSender with Player.
    boolean playerRunner();
    //see SpigotPermission. If no SpigotPermission annotation is there for this permission, defaults are used.
    String permission();
    //Message if the user doesn't have permission to run the command.
    String permissionError();
    //Message if the wrong type of user uses the message (The console if playerRunner is true)
    String wrongUserError();
    //Message sent if false is returned from the command
    String usage();
}
