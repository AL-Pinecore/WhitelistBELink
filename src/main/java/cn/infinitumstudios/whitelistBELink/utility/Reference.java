package cn.infinitumstudios.whitelistBELink.utility;

import cn.infinitumstudios.whitelistBELink.WhitelistBELink;
import org.bukkit.Bukkit;

import java.nio.file.Path;

public class Reference {
    public static final Path DATABASE_PATH = Path.of(WhitelistBELink.getPlugin(WhitelistBELink.class).getDataFolder().getAbsolutePath(), "database.db");
    public static final String JAVA_CLIENT = "java";
    public static final String BEDROCK_CLIENT = "bedrock";
    public static final String NULL_STRING = "null";
}
