package com.yashhpenchi.excellentpetutilities.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

// creates/reads Upgrade Card items. Appearance is hardcoded for now - no
// items.yml yet, that's a config-system decision for later, not this pass.
public class UpgradeCardItem {

    private final NamespacedKey levelKey;

    public UpgradeCardItem(JavaPlugin plugin) {
        this.levelKey = new NamespacedKey(plugin, "upgrade_card_level");
    }

    public ItemStack create(int level) {
        ItemStack item = new ItemStack(Material.PAPER);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Level " + level + " Upgrade Card")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);
        });
        return item;
    }

    // returns null if the item isn't an Upgrade Card
    public Integer getLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
    }
}
