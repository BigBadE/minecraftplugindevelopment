package software.bigbade.minecraftplugindevelopment.example;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import software.bigbade.minecraftplugindevelopment.annotations.EventListener;
import software.bigbade.minecraftplugindevelopment.api.EventCaller;

@EventListener(permission = "example.permission", caller = EventCaller.PLAYER)
public class ExampleListener implements Listener {
    @EventHandler
    public void onPlayerHit(EntityDamageEvent event, Player player) {
        player.sendMessage("Test!");
    }
}
