package software.bigbade.minecraftplugindevelopment.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SpigotPermission {
    //Permission node of the permission. Example: "my.plugin.permission"
    //Setting this to "" will require no permission.
    String permission();
    //Description of the permission.
    String description() default "";
    //If players have this permission by default. Values are "true", "false", "op", and "not op"
    String defaultValue() default "true";
    //All child nodes. Useful for things like wildcards, where having this permission gives all the children permissions.
    String[] children() default {};
    //Inheritance of the child nodes, if the list isn't long enough, defaults to true.
    //True means having the parent node WILL give the permission, FALSE removes the permission.
    boolean[] childrenInheritance() default {};
}
