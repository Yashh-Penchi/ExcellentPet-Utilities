package com.yashhpenchi.excellentpetutilities.storage;

import com.yashhpenchi.excellentpetutilities.models.PetInstance;
import com.yashhpenchi.excellentpetutilities.models.PetState;
import com.yashhpenchi.excellentpetutilities.models.PetType;
import com.yashhpenchi.excellentpetutilities.models.Personality;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * SQLite only handles one writer at a time, so every DB call here runs on
 * ONE dedicated thread, in order. Do not switch this to a pooled executor -
 * concurrent writes will throw SQLITE_BUSY under load.
 */
public class SqlitePetRepository implements PetRepository {

    private final DatabaseManager databaseManager;
    private final ExecutorService dbExecutor;

    public SqlitePetRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.dbExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "expet-db-writer");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public CompletableFuture<Void> save(PetInstance pet) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                    INSERT INTO pets (pet_uuid, owner_uuid, entity_uuid, type, slot_index, level, hunger,
                                       state, personality, last_feed_timestamp, display_name, ability_cooldowns)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(pet_uuid) DO UPDATE SET
                        entity_uuid = excluded.entity_uuid,
                        level = excluded.level,
                        hunger = excluded.hunger,
                        state = excluded.state,
                        last_feed_timestamp = excluded.last_feed_timestamp,
                        display_name = excluded.display_name,
                        ability_cooldowns = excluded.ability_cooldowns
                    """;
            Connection connection = databaseManager.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, pet.getPetUUID().toString());
                statement.setString(2, pet.getOwnerUUID().toString());
                statement.setString(3, pet.getEntityUUID() != null ? pet.getEntityUUID().toString() : null);
                statement.setString(4, pet.getType().name());
                statement.setInt(5, pet.getSlotIndex());
                statement.setInt(6, pet.getLevel());
                statement.setDouble(7, pet.getHunger());
                statement.setString(8, pet.getState().name());
                statement.setString(9, pet.getPersonality().name());
                statement.setLong(10, pet.getLastFeedTimestamp());
                statement.setString(11, pet.getDisplayName());
                statement.setString(12, encodeCooldowns(pet.getAbilityCooldowns()));
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save pet " + pet.getPetUUID(), e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<Optional<PetInstance>> findByUUID(UUID petUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM pets WHERE pet_uuid = ?";
            Connection connection = databaseManager.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, petUUID.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return Optional.<PetInstance>empty();
                    }
                    return Optional.of(mapRow(resultSet));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load pet " + petUUID, e);
            }
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<List<PetInstance>> findByOwner(UUID ownerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM pets WHERE owner_uuid = ? ORDER BY slot_index ASC";
            Connection connection = databaseManager.getConnection();
            List<PetInstance> pets = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, ownerUUID.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        pets.add(mapRow(resultSet));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load pets for owner " + ownerUUID, e);
            }
            return pets;
        }, dbExecutor);
    }

    @Override
    public CompletableFuture<Void> delete(UUID petUUID) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM pets WHERE pet_uuid = ?";
            Connection connection = databaseManager.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, petUUID.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete pet " + petUUID, e);
            }
        }, dbExecutor);
    }

    @Override
    public void close() {
        dbExecutor.shutdown();
        try {
            if (!dbExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                dbExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            dbExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        databaseManager.close();
    }

    private PetInstance mapRow(ResultSet resultSet) throws SQLException {
        UUID petUUID = UUID.fromString(resultSet.getString("pet_uuid"));
        UUID ownerUUID = UUID.fromString(resultSet.getString("owner_uuid"));
        PetType type = PetType.valueOf(resultSet.getString("type"));
        int slotIndex = resultSet.getInt("slot_index");

        String entityUUIDRaw = resultSet.getString("entity_uuid");

        return PetInstance.builder(petUUID, ownerUUID, type, slotIndex)
                .level(resultSet.getInt("level"))
                .hunger(resultSet.getDouble("hunger"))
                .state(PetState.valueOf(resultSet.getString("state")))
                .personality(Personality.valueOf(resultSet.getString("personality")))
                .lastFeedTimestamp(resultSet.getLong("last_feed_timestamp"))
                .displayName(resultSet.getString("display_name"))
                .abilityCooldowns(decodeCooldowns(resultSet.getString("ability_cooldowns")))
                .entityUUID(entityUUIDRaw != null ? UUID.fromString(entityUUIDRaw) : null)
                .build();
    }

    // simple "key=value,key=value" encoding - no need for a JSON dependency for this
    private String encodeCooldowns(Map<String, Long> cooldowns) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return builder.toString();
    }

    private Map<String, Long> decodeCooldowns(String raw) {
        Map<String, Long> cooldowns = new HashMap<>();
        if (raw == null || raw.isBlank()) {
            return cooldowns;
        }
        for (String part : raw.split(",")) {
            String[] kv = part.split("=");
            if (kv.length == 2) {
                cooldowns.put(kv[0], Long.parseLong(kv[1]));
            }
        }
        return cooldowns;
    }
}
