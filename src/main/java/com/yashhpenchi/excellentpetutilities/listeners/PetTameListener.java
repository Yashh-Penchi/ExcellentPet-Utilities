package com.yashhpenchi.excellentpetutilities.listeners;

import com.yashhpenchi.excellentpetutilities.managers.PetManager;
import com.yashhpenchi.excellentpetutilities.models.PetInstance;
import com.yashhpenchi.excellentpetutilities.models.PetType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

import java.util.UUID;

public class PetTameListener implements Listener {

    private final PetManager petManager;

    public PetTameListener(PetManager petManager) {
        this.petManager = petManager;
    }

    @EventHandler
    public void onTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) {
            return;
        }

        PetType type = mapToPetType(event.getEntityType());
        if (type == null) {
            return; // not one of our 5 supported pets (e.g. a horse)
        }

        UUID ownerUUID = player.getUniqueId();
        int slotIndex = petManager.nextSlotIndex(ownerUUID, type);

        PetInstance pet = PetInstance.builder(UUID.randomUUID(), ownerUUID, type, slotIndex)
                .entityUUID(event.getEntity().getUniqueId())
                .build(); // level defaults to 0 - see PetInstance.Builder

        petManager.registerNewPet(pet);
    }

    private PetType mapToPetType(EntityType entityType) {
        return switch (entityType) {
            case WOLF -> PetType.WOLF;
            case CAT -> PetType.CAT;
            case PARROT -> PetType.PARROT;
            case LLAMA, TRADER_LLAMA -> PetType.LLAMA;
            default -> null; // fox intentionally excluded - not Tameable, see chat
        };
    }
}
