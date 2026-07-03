package com.yashhpenchi.excellentpetutilities.storage;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// opens the sqlite file and makes sure the schema exists - nothing else
public class DatabaseManager {

    private final Connection connection;

    public DatabaseManager(Path dataFolder) throws SQLException {
        Path dbFile = dataFolder.resolve("pets.db");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
        createSchema();
    }

    private void createSchema() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS pets (
                    pet_uuid TEXT PRIMARY KEY,
                    owner_uuid TEXT NOT NULL,
                    entity_uuid TEXT,
                    type TEXT NOT NULL,
                    slot_index INTEGER NOT NULL,
                    level INTEGER NOT NULL,
                    hunger REAL NOT NULL,
                    state TEXT NOT NULL,
                    personality TEXT NOT NULL,
                    last_feed_timestamp INTEGER NOT NULL,
                    display_name TEXT,
                    ability_cooldowns TEXT
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_pets_owner ON pets(owner_uuid)");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
