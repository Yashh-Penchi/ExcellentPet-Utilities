package com.yashhpenchi.excellentpetutilities.listeners;

import com.yashhpenchi.excellentpetutilities.items.UpgradeCardItem;
import com.yashhpenchi.excellentpetutilities.managers.PetManager;
import com.yashhpenchi.excellentpetutilities.models.PetInstance;
import com.yashhpenchi.excellentpetutilities.models.PetType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;

public class PetUpgradeListener implements Listener {

    private final PetManager petManager;
    private final UpgradeCardItem upgradeCardItem;

    public PetUpgradeListener(PetManager petManager, UpgradeCardItem upgradeCardItem) {
        this.petManager = petManager;
        this.upgradeCardItem = upgradeCardItem;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return; // avoid double-firing for main hand + off hand
        }

        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        Integer cardLevel = upgradeCardItem.getLevel(heldItem);
        if (cardLevel == null) {
            return; // not holding a card, let vanilla interaction happen
        }

        PetType type = mapToPetType(event.getRightClicked().getType());
        if (type == null) {
            player.sendMessage(Component.text("This mob can't be a pet.").color(NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true); // it's a valid pet target, suppress vanilla interaction from here on

        if (!(event.getRightClicked() instanceof Tameable tameable) || !tameable.isTamed()) {
            player.sendMessage(Component.text("This pet must be tamed first.").color(NamedTextColor.RED));
            return;
        }

        if (tameable.getOwner() == null || !tameable.getOwner().getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("This isn't your pet.").color(NamedTextColor.RED));
            return;
        }

        UUID entityUUID = event.getRightClicked().getUniqueId();
        UUID ownerUUID = player.getUniqueId();
        Optional<PetInstance> existing = petManager.getPets(ownerUUID).stream()
                .filter(pet -> entityUUID.equals(pet.getEntityUUID()))
                .findFirst();

        if (existing.isPresent()) {
            upgradeExistingPet(player, existing.get(), cardLevel);
        } else {
            trackNewPet(player, entityUUID, type, cardLevel);
        }

        consumeOneCard(player, heldItem);
    }

    private void upgradeExistingPet(Player player, PetInstance pet, int cardLevel) {
        if (cardLevel <= pet.getLevel()) {
            player.sendMessage(Component.text("This pet is already level " + pet.getLevel() + " or higher.")
                    .color(NamedTextColor.RED));
            return;
        }
        pet.setLevel(cardLevel);
        petManager.savePet(pet);
        player.sendMessage(Component.text("Your pet is now level " + cardLevel + "!").color(NamedTextColor.GREEN));
    }

    private void trackNewPet(Player player, UUID entityUUID, PetType type, int cardLevel) {
        UUID ownerUUID = player.getUniqueId();
        int slotIndex = petManager.nextSlotIndex(ownerUUID, type);
        PetInstance pet = PetInstance.builder(UUID.randomUUID(), ownerUUID, type, slotIndex)
                .level(cardLevel)
                .entityUUID(entityUUID)
                .build();
        petManager.registerNewPet(pet);
        player.sendMessage(Component.text("Your pet is now tracked at level " + cardLevel + "!")
                .color(NamedTextColor.GREEN));
    }

    private void consumeOneCard(Player player, ItemStack heldItem) {
        heldItem.subtract(1);
        player.getInventory().setItemInMainHand(heldItem);
    }

    private PetType mapToPetType(EntityType entityType) {
        return switch (entityType) {
            case WOLF -> PetType.WOLF;
            case CAT -> PetType.CAT;
            case PARROT -> PetType.PARROT;
            case LLAMA, TRADER_LLAMA -> PetType.LLAMA;
            default -> null; // fox intentionally excluded - still pending your decision
        };
    }
}
