package software.bigbade.minecraftplugindevelopment.manager;

import lombok.Getter;
import software.bigbade.minecraftplugindevelopment.annotations.SpigotPermission;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    public static final PermissionManager INSTANCE = new PermissionManager();

    @Getter
    private final List<SpigotPermission> permissions = new ArrayList<>();

    public void addPermission(SpigotPermission permission) {
        permissions.add(permission);
    }
}
