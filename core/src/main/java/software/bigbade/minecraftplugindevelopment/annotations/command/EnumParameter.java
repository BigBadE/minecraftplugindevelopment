package software.bigbade.minecraftplugindevelopment.annotations.command;

public @interface EnumParameter {
    Class<? extends Enum<?>> enumClass();

    String unmatchingError() default "NOT_AN_ENUM";
}
