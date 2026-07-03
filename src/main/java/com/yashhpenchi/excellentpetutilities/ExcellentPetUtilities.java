package com.yashhpenchi.excellentpetutilities;

import com.yashhpenchi.excellentpetutilities.commands.ExpetCommand;
import com.yashhpenchi.excellentpetutilities.items.UpgradeCardItem;
import com.yashhpenchi.excellentpetutilities.listeners.PetUpgradeListener;
import com.yashhpenchi.excellentpetutilities.listeners.PlayerConnectionListener;
import com.yashhpenchi.excellentpetutilities.managers.PetManager;
import com.yashhpenchi.excellentpetutilities.services.PetLifecycleService;
import com.yashhpenchi.excellentpetutilities.services.PetLifecycleServiceImpl;
import com.yashhpenchi.excellentpetutilities.storage.DatabaseManager;
import com.yashhpenchi.excellentpetutilities.storage.PetRepository;
import com.yashhpenchi.excellentpetutilities.storage.SqlitePetRepository;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class ExcellentPetUtilities extends JavaPlugin {

    private DatabaseManager databaseManager;
    private PetRepository petRepository;
    private PetLifecycleService petLifecycleService;
    private PetManager petManager;
    private UpgradeCardItem upgradeCardItem;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        try {
            databaseManager = new DatabaseManager(getDataFolder().toPath());
        } catch (SQLException e) {
            getLogger().severe("Failed to open pets.db - disabling plugin.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        petRepository = new SqlitePetRepository(databaseManager);
        petLifecycleService = new PetLifecycleServiceImpl(petRepository);
        petManager = new PetManager(petRepository);
        upgradeCardItem = new UpgradeCardItem(this);

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(petManager), this);
        getServer().getPluginManager().registerEvents(new PetUpgradeListener(petManager, upgradeCardItem), this);
        registerCommands();

        // covers the case where the plugin is enabled while players are
        // already online (e.g. /reload, even though Paper tells you not to
        // use that command) - PlayerJoinEvent won't fire for them
        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            petManager.loadPets(player.getUniqueId());
        }

        getLogger().info("ExcellentPetUtilities enabled.");
    }

    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            commands.register("expet", "ExcellentPetUtilities main command", new ExpetCommand(petManager, upgradeCardItem));
        });
    }

    @Override
    public void onDisable() {
        if (petRepository != null) {
            petRepository.close(); // blocks until queued writes finish
        }
        if (petManager != null) {
            petManager.clearAll();
        }
        getLogger().info("ExcellentPetUtilities disabled.");
    }

    public PetRepository getPetRepository() {
        return petRepository;
    }

    public PetLifecycleService getPetLifecycleService() {
        return petLifecycleService;
    }

    public PetManager getPetManager() {
        return petManager;
    }
}
