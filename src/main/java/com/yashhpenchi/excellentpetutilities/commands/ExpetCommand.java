package com.yashhpenchi.excellentpetutilities.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

// stub only - opens the real Pet Selection GUI once the GUI system phase is built
public class ExpetCommand implements BasicCommand {

    @Override
    public void execute(@NotNull CommandSourceStack source, String @NotNull [] args) {
        if (!(source.getSender() instanceof org.bukkit.entity.Player)) {
            source.getSender().sendPlainMessage("This command can only be used by players.");
            return;
        }
        source.getSender().sendRichMessage("<gray>ExcellentPetUtilities is running. GUI system not built yet.");
    }
}
