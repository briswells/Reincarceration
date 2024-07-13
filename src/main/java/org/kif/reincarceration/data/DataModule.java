package org.kif.reincarceration.data;

import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.core.Module;
import org.kif.reincarceration.util.ConsoleUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataModule implements Module {
    private final Reincarceration plugin;
    private Connection connection;
    private DataManager dataManager;

    public DataModule(Reincarceration plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() throws SQLException {
        initializeDatabase();
        this.dataManager = new DataManager(this);
        ConsoleUtil.sendSuccess("Data Module enabled");
    }

    @Override
    public void onDisable() {
        closeConnection();
        ConsoleUtil.sendSuccess("Data Module disabled");
    }

    private void initializeDatabase() throws SQLException {
        String dbName = plugin.getModuleManager().getConfigManager().getDatabaseFilename();
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdir()) {
                plugin.getLogger().severe("Failed to create data folder: " + dataFolder.getAbsolutePath());
                throw new SQLException("Failed to create data folder");
            }
        }

        String url = "jdbc:sqlite:" + new File(dataFolder, dbName).getAbsolutePath();
        connection = DriverManager.getConnection(url);
        createTables();
    }

    private void createTables() throws SQLException {
        String playerDataTable = "CREATE TABLE IF NOT EXISTS player_data (" +
                "uuid TEXT PRIMARY KEY," +
                "name TEXT," +
                "current_rank INTEGER," +
                "cycle_count INTEGER," +
                "in_cycle BOOLEAN," +
                "stored_balance REAL" + // Stores the player's balance when entering a cycle
                ");";

        String cycleHistoryTable = "CREATE TABLE IF NOT EXISTS cycle_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_uuid TEXT," +
                "modifier_id TEXT," +
                "start_time TIMESTAMP," +
                "end_time TIMESTAMP," +
                "completed BOOLEAN" +
                ");";

        String completedModifiersTable = "CREATE TABLE IF NOT EXISTS completed_modifiers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_uuid TEXT," +
                "modifier_id TEXT," +
                "UNIQUE(player_uuid, modifier_id)" +
                ");";

        String activeModifiersTable = "CREATE TABLE IF NOT EXISTS active_modifiers (" +
                "player_uuid TEXT PRIMARY KEY," +
                "modifier_id TEXT" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(playerDataTable);
            stmt.execute(cycleHistoryTable);
            stmt.execute(completedModifiersTable);
            stmt.execute(activeModifiersTable);
        }
    }

    private void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing database connection: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            initializeDatabase();
        }
        return connection;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public Reincarceration getPlugin() {
        return plugin;
    }
}