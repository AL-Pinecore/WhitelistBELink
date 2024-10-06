package cn.infinitumstudios.whitelistBELink;

import cn.infinitumstudios.whitelistBELink.commands.WLinkCommand;
import cn.infinitumstudios.whitelistBELink.foundation.database.DatabaseManager;
import cn.infinitumstudios.whitelistBELink.listeners.PlayerJoinEventListener;
import cn.infinitumstudios.whitelistBELink.utility.Reference;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.api.util.BedrockPlatform;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public final class WhitelistBELink extends JavaPlugin {
    DatabaseManager dbmanager;
    WLinkCommand linkCommand;
    PlayerJoinEventListener playerJoinEventListener;

    @Override
    public void onEnable() {

        if (!getDataFolder().exists()){
            if (!getDataFolder().mkdirs()){
                getLogger().severe("Disabled due to failed to create plugin data folder!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        if (!Bukkit.getServer().hasWhitelist()){
            getLogger().severe("Disabled due to failed to whitelist is not enabled!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            dbmanager = new DatabaseManager(this, Reference.DATABASE_PATH.toString());
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            linkCommand = new WLinkCommand();
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            playerJoinEventListener = new PlayerJoinEventListener();
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }

        Bukkit.getPluginManager().registerEvents(playerJoinEventListener, this);
        Objects.requireNonNull(this.getCommand("wlink")).setExecutor(linkCommand);

        getLogger().info("Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
