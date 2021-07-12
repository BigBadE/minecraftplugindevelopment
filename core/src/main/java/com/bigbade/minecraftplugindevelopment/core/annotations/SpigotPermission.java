package com.bigbade.minecraftplugindevelopment.core.annotations;

public @interface SpigotPermission {
    //A short description of what this permission allows.
    //Allows programmatic access, and helps server administrators.
    String description();
    //The default value of the permission
    Default defaultValue();
    //Allows you to set children for the permission.
    PermissionChildren[] children();

    @interface PermissionChildren {
        //The permission of the child
        String permission();
        //A child node of true inherits the parent permission.
        //A child node of false inherits the inverse parent permission.
        boolean doesInherit();
    }

    enum Default {
        //All players have start with this permission
        TRUE,
        //No players start with this permission
        FALSE,
        //Operators have this permission
        OP,
        //Non-operators have this permission
        NOT_OP
    }
}
