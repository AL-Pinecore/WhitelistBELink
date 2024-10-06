package cn.infinitumstudios.whitelistBELink.listeners;

import cn.infinitumstudios.whitelistBELink.WhitelistBELink;
import cn.infinitumstudios.whitelistBELink.foundation.database.DatabaseManager;
import cn.infinitumstudios.whitelistBELink.utility.Reference;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.geysermc.geyser.api.GeyserApi;

import java.sql.SQLException;

public class PlayerJoinEventListener implements Listener {
    DatabaseManager databaseManager;

    public PlayerJoinEventListener() throws SQLException {
        databaseManager = new DatabaseManager(WhitelistBELink.getPlugin(WhitelistBELink.class), Reference.DATABASE_PATH.toString());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        // See is the player using Bedrock client or Java client
        if (!databaseManager.isWhitelistedUUID(event.getPlayer().getUniqueId(), DatabaseManager.clientTypeFromAPI(event.getPlayer().getUniqueId()))){
            if (GeyserApi.api().isBedrockPlayer(event.getPlayer().getUniqueId())){
                databaseManager.addToDatabase(event.getPlayer().getUniqueId(), event.getPlayer().getName(),null, Reference.BEDROCK_CLIENT);
            } else {
                databaseManager.addToDatabase(event.getPlayer().getUniqueId(), event.getPlayer().getName(),null, Reference.JAVA_CLIENT);
            }
        }
    }
}
