package software.bigbade.minecraftplugindevelopment.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Calls the getObject method, with a default of the field value.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
public @interface ConfigValue {
    //Takes the path to get the given value.
    //Ex: section.value = "Value"
    //Field would have a value of "Value", would be gotten from the section "section" value "value".
    String path();
}
