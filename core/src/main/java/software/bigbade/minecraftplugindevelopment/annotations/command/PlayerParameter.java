package software.bigbade.minecraftplugindevelopment.annotations.command;

public @interface PlayerParameter {
    //Allows the @r and @p selectors
    boolean allowSelectors() default true;

    //Allows the @a selector. Requires an Array of players instead of a single one.
    boolean allowMultiple() default false;

    String notPlayerError() default "NOT_A_PLAYER";
}
