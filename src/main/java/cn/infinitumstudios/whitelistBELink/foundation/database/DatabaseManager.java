package cn.infinitumstudios.whitelistBELink.foundation.database;

import cn.infinitumstudios.whitelistBELink.WhitelistBELink;
import cn.infinitumstudios.whitelistBELink.utility.Reference;
import org.bukkit.Bukkit;
import org.geysermc.geyser.api.GeyserApi;

import java.sql.*;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class DatabaseManager{
    protected final WhitelistBELink plugin;
    private static Connection connection = null;

    public DatabaseManager(WhitelistBELink plugin, String databasePath) throws SQLException {
        this.plugin = plugin;
        initialize(databasePath);
    }

    public static void addToWhitelist(String Nickname) throws ExecutionException, InterruptedException {
        Bukkit.getScheduler().callSyncMethod(WhitelistBELink.getPlugin(WhitelistBELink.class), ()
                -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),
                "whitelist add "+Nickname)).get();
        Bukkit.reloadWhitelist();
    }

    public static void initialize(String databasePath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS accounts(
                    Nickname TEXT NOT NULL,
                    AccountUUID TEXT NOT NULL,
                    LinkedAccountUUID TEXT NOT NULL,
                    ClientType TEXT NOT NULL 
                )
            """);
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Is a player's whitelist data exists in the database
     * @param accountUUID a player's UUID
     * @param clientType client type of player, can be seen in {@link Reference}
     * @return true if founded data, false if not
     */
    public boolean isWhitelistedUUID(UUID accountUUID, String clientType){
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM accounts WHERE AccountUUID = ? AND ClientType = ?")){
            preparedStatement.setString(1, accountUUID.toString());
            preparedStatement.setString(2, clientType);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e){
            return false;
        }
    }

    /**
     * Is a player's whitelist data exists in the database
     * @param name a player's nickname
     * @param clientType client type of player, can be seen in {@link Reference}
     * @return true if founded data, false if not
     */
    public boolean isWhitelistedName(String name, String clientType){
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM accounts WHERE Nickname = ? AND ClientType = ?")){
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, clientType);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e){
            return false;
        }
    }

    /**
     * The client type of the player's account
     * @param accountUUID a player's UUID
     * @return return these two value: {@link Reference#BEDROCK_CLIENT}, or {@link Reference#JAVA_CLIENT}
     */
    public String clientTypeFromDatabase(UUID accountUUID){
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ClientType FROM accounts WHERE AccountUUID = ?")){
            preparedStatement.setString(1, accountUUID.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                return String.valueOf(resultSet.getString("ClientType"));
            } else {
                return null;
            }
        } catch (SQLException e){
            return null;
        }
    }

    /**
     * Is a player's account been linked by other player
     * @param accountUUID a player's UUID
     * @return true if the account has been linked
     */
    public boolean isBeenLinkedUUID(UUID accountUUID){
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT LinkedAccountUUID FROM accounts WHERE AccountUUID = ?")){
            preparedStatement.setString(1, accountUUID.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e){
            return false;
        }
    }

    /**
     * Is a player's account been linked by other player
     * @param nickname a player's name
     * @return true if the account has been linked
     */
    public boolean isBeenLinkedName(String nickname){
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT LinkedAccountUUID FROM accounts WHERE Nickname = ?")){
            preparedStatement.setString(1, nickname);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e){
            return false;
        }
    }

    /**
     * Is two account been linked
     * @param p1 first account's UUID (Player UUID)
     * @param p2 second account's UUID (Player UUID)
     * @return true if they are linked, false if they are not
     */
    public boolean isLinkedAccount(UUID p1, UUID p2){
        if (p1 == null || p2 == null) return false;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT LinkedAccountUUID FROM accounts WHERE AccountUUID = ?")){
            preparedStatement.setString(1, p1.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                return Objects.equals(String.valueOf(resultSet.getString("LinkedAccountUUID")), String.valueOf(p2));
            } else {
                return false;
            }
        } catch (SQLException e){
            return false;
        }
    }

    /**
     * Link a BE account and a JAVA account, also update command sender's account
     * @param accountUUID the target player's UUID
     * @param nickname the target player's nickname
     * @param senderUUID the command sender's UUID
     * @param clientType the target player account client type, can only be {@link Reference#JAVA_CLIENT} or {@link Reference#BEDROCK_CLIENT}
     * @return true if the action executed successfully
     */
    public boolean addToDatabase(UUID accountUUID, String nickname, UUID senderUUID, String clientType){
        if (accountUUID == null) return false;
        if (nickname == null || nickname.isEmpty()) return false;

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO accounts (Nickname, AccountUUID, LinkedAccountUUID, ClientType) VALUES (?,?,?,?)")){
            preparedStatement.setString(1,nickname);
            preparedStatement.setString(2,accountUUID.toString());
            if (senderUUID == null){
                preparedStatement.setString(3,Reference.NULL_STRING);
            } else {
                preparedStatement.setString(3,senderUUID.toString());
            }
            preparedStatement.setString(4, clientTypeFromAPI(accountUUID));
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            return false;
        }

        if (senderUUID != null){
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET LinkedAccountUUID = ? WHERE AccountUUID = ?")){
                preparedStatement.setString(1,accountUUID.toString());
                preparedStatement.setString(2,senderUUID.toString());
                preparedStatement.executeUpdate();
            } catch (SQLException e){
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the account's client type
     * @param playerUUID the account's UUID
     * @return {@link Reference#JAVA_CLIENT} or {@link Reference#BEDROCK_CLIENT}
     */
    public static String clientTypeFromAPI(UUID playerUUID){
        if (GeyserApi.api().isBedrockPlayer(playerUUID)){
            // Bedrock behavior
            return Reference.BEDROCK_CLIENT;
        } else {
            // Java behavior
            return Reference.JAVA_CLIENT;
        }
    }

    protected boolean closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()){
            connection.close();
            return true;
        } else {
            return false;
        }
    }
}
