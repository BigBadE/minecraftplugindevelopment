package software.bigbade.minecraftplugindevelopment.annotations.command;

public @interface IntegerParameter {
    int max() default Integer.MAX_VALUE;

    int min() default 0;

    String notIntegerErrorMessage() default "NOT_AN_INTEGER";

    String rangeErrorMessage() default "INTEGER_OUT_OF_RANGE";
}
