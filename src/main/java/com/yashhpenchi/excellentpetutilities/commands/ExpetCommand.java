package com.yashhpenchi.excellentpetutilities.commands;

import com.yashhpenchi.excellentpetutilities.items.UpgradeCardItem;
import com.yashhpenchi.excellentpetutilities.managers.PetManager;
import com.yashhpenchi.excellentpetutilities.models.PetInstance;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// stub only - real Pet Selection GUI comes in the GUI system phase.
// for now this lists pets as text and handles the admin "upgrade give" command.
public class ExpetCommand implements BasicCommand {

    private final PetManager petManager;
    private final UpgradeCardItem upgradeCardItem;

    public ExpetCommand(PetManager petManager, UpgradeCardItem upgradeCardItem) {
        this.petManager = petManager;
        this.upgradeCardItem = upgradeCardItem;
    }

    @Override
    public void execute(@NotNull CommandSourceStack source, String @NotNull [] args) {
        if (args.length >= 2 && args[0].equalsIgnoreCase("upgrade") && args[1].equalsIgnoreCase("give")) {
            handleUpgradeGive(source, args);
            return;
        }

        if (!(source.getSender() instanceof Player player)) {
            source.getSender().sendPlainMessage("This command can only be used by players.");
            return;
        }

        List<PetInstance> pets = petManager.getPets(player.getUniqueId());
        if (pets.isEmpty()) {
            source.getSender().sendPlainMessage("You don't have any pets yet.");
            return;
        }

        source.getSender().sendPlainMessage("Your pets:");
        for (PetInstance pet : pets) {
            source.getSender().sendPlainMessage("- " + pet.getType() + " #" + pet.getSlotIndex()
                    + " (level " + pet.getLevel() + ", " + pet.getState() + ")");
        }
    }

    private void handleUpgradeGive(CommandSourceStack source, String[] args) {
        if (!source.getSender().hasPermission("expet.give")) {
            source.getSender().sendMessage(Component.text("You don't have permission for this.").color(NamedTextColor.RED));
            return;
        }

        if (args.length < 4) {
            source.getSender().sendPlainMessage("Usage: /expet upgrade give <player> <1|2|3|all> [amount]");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            source.getSender().sendMessage(Component.text("Player not found or offline.").color(NamedTextColor.RED));
            return;
        }

        String levelArg = args[3];
        if (levelArg.equalsIgnoreCase("all")) {
            for (int level = 1; level <= 3; level++) {
                target.getInventory().addItem(upgradeCardItem.create(level));
            }
            source.getSender().sendMessage(Component.text("Gave 1x Level 1/2/3 Upgrade Card to " + target.getName())
                    .color(NamedTextColor.GREEN));
            return;
        }

        int level;
        try {
            level = Integer.parseInt(levelArg);
        } catch (NumberFormatException e) {
            source.getSender().sendPlainMessage("Level must be 1, 2, 3, or 'all'.");
            return;
        }
        if (level < 1 || level > 3) {
            source.getSender().sendPlainMessage("Level must be 1, 2, 3, or 'all'.");
            return;
        }

        int amount = 1;
        if (args.length >= 5) {
            try {
                amount = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                source.getSender().sendPlainMessage("Amount must be a number.");
                return;
            }
        }

        ItemStack card = upgradeCardItem.create(level);
        card.setAmount(Math.min(amount, 64));
        target.getInventory().addItem(card);
        source.getSender().sendMessage(Component.text("Gave " + amount + "x Level " + level + " Upgrade Card to " + target.getName())
                .color(NamedTextColor.GREEN));
    }
}

