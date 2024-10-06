package cn.infinitumstudios.whitelistBELink.commands;

import cn.infinitumstudios.whitelistBELink.WhitelistBELink;
import cn.infinitumstudios.whitelistBELink.foundation.database.DatabaseManager;
import cn.infinitumstudios.whitelistBELink.utility.Reference;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class WLinkCommand implements CommandExecutor, TabCompleter {
    DatabaseManager db;
    public WLinkCommand() throws SQLException {
        db = new DatabaseManager(WhitelistBELink.getPlugin(WhitelistBELink.class), Reference.DATABASE_PATH.toString());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) commandSender;

        if (args.length < 2){
            commandSender.sendMessage(ChatColor.YELLOW + "该指令用法：/wlist bedrock|java 玩家游戏名称");
            return true;
        }

        switch(args[0]){
            case "bedrock":
                if (!db.isWhitelistedName(args[1], Reference.BEDROCK_CLIENT)){
                    if (!db.isBeenLinkedName(player.getName())){
                        try {
                            DatabaseManager.addToWhitelist(args[1]);
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        for (OfflinePlayer getWhitelistedPlayer : Bukkit.getWhitelistedPlayers()){
                            if (Objects.equals(getWhitelistedPlayer.getName(), args[1])){
                                db.addToDatabase(getWhitelistedPlayer.getUniqueId(), getWhitelistedPlayer.getName(), player.getUniqueId(), Reference.BEDROCK_CLIENT);
                            }
                        }

                        commandSender.sendMessage(ChatColor.GREEN + "执行成功：已成功绑定Bedrock账户 " + ChatColor.AQUA + args[1]);
                    } else {
                        commandSender.sendMessage(ChatColor.YELLOW + "执行失败：你的帐户已经绑定过Bedrock Edition帐户了！");
                        return true;
                    }
                } else {
                    commandSender.sendMessage(ChatColor.YELLOW + "执行失败：你请求绑定的帐户已经被其他玩家绑定了！");
                    return true;
                }
                break;
            case "java":
                if (!db.isWhitelistedName(args[1], Reference.JAVA_CLIENT)){
                    if (!db.isBeenLinkedName(player.getName())){
                        try {
                            DatabaseManager.addToWhitelist(args[1]);
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        for (OfflinePlayer getWhitelistedPlayer : Bukkit.getWhitelistedPlayers()){
                            if (Objects.equals(getWhitelistedPlayer.getName(), args[1])){
                                db.addToDatabase(getWhitelistedPlayer.getUniqueId(), getWhitelistedPlayer.getName(), player.getUniqueId(), Reference.JAVA_CLIENT);
                            }
                        }
                        commandSender.sendMessage(ChatColor.GREEN + "执行成功：已成功绑定Java账户 " + ChatColor.AQUA + args[1]);
                    } else {
                        commandSender.sendMessage(ChatColor.YELLOW + "执行失败：你的帐户已经绑定过Java Edition帐户了！");
                        return true;
                    }
                } else {
                    commandSender.sendMessage(ChatColor.YELLOW + "执行失败：你请求绑定的帐户已经被其他玩家绑定了！");
                    return true;
                }
                break;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1){
            return Arrays.asList("bedrock", "java");
        } else if(args.length == 2){
            return List.of("名称");
        } else {
            return null;
        }
    }
}
