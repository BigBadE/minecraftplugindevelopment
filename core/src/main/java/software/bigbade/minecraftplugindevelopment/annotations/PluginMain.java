package software.bigbade.minecraftplugindevelopment.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to automatically generate the plugin.yml
 * All values with a default are optional, and empty values will not be added to the plugin.yml
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PluginMain {
    //Name of the plugin. The plugin name should be like this: ExamplePlugin. Keep it short and sweet.
    String name();
    //Plugin version, in the format: major version.minor version.patch
    //Major version changes add/remove features, minor version changes may change features or some API points, patches are bug fixes with no features.
    String version();
    //A quick plugin description
    String description() default "";
    //Author shown, sometimes used in error messages. An email address is recommended (if not, Discord ID or Spigot forum username works)
    String author() default "";
    //Spigot API version. None makes Spigot load the plugin with backwards-compatibility.
    String apiVersion() default "";
    //Loading time of the plugin. Default is POSTWORLD (after the world is loaded), you can also use STARTUP (server start)
    String load() default "";
    //List of authors, an email address is recommended (if not, Discord ID or Spigot forum username works)
    String[] authors() default {};
    //Website of the project
    String website() default "";
    //Names of plugins needed, for example: ProtocolLib, Vault, etc...
    String[] depend() default {};
    //Prefix added to the logger of the plugin
    String prefix() default "";
    //Plugins that aren't necessary to use the plugin, but may be used by the plugin.
    String[] softDepend() default {};
    //Any plugins that should be loaded AFTER your plugin loads.
    String[] loadBefore() default {};
}
