package com.yashhpenchi.excellentpetutilities.listeners;

import com.yashhpenchi.excellentpetutilities.managers.PetManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final PetManager petManager;

    public PlayerConnectionListener(PetManager petManager) {
        this.petManager = petManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        petManager.loadPets(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        petManager.unloadPets(event.getPlayer().getUniqueId());
    }
}
