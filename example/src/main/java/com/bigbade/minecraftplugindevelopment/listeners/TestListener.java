package com.bigbade.minecraftplugindevelopment.listeners;

import com.bigbade.minecraftplugindevelopment.core.annotations.PluginListener;
import com.bigbade.minecraftplugindevelopment.core.annotations.Test;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
@PluginListener
public class TestListener implements Listener {
    private final ConfigurationSection config;

    @Test
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(ChatColor.GREEN + "Welcome to the server!");
    }
}
