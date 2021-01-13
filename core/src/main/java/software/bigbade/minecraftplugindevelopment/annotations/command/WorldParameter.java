package software.bigbade.minecraftplugindevelopment.annotations.command;

public @interface WorldParameter {
    //World environment of the argument
    WorldEnvironment environment() default WorldEnvironment.ALL;

    String worldErrorMessage() default "WORLD_NOT_FOUND";

    String environmentErrorMessage() default "WORLD_WRONG_ENVIRONMENT";
}
