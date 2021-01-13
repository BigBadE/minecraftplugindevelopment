package software.bigbade.minecraftplugindevelopment.annotations;

import software.bigbade.minecraftplugindevelopment.api.EventCaller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Automatically registers the class, and adds the given permission/caller to any methods with an event parameter
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface EventListener {
    String permission() default "";

    EventCaller caller() default EventCaller.ENTITY;
}
