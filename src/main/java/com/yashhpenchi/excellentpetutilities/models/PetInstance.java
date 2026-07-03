package com.yashhpenchi.excellentpetutilities.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * One tamed pet owned by one player.
 *
 * NOTE: the original spec lists "Pet Level" and "Upgrade Level" as two
 * separate stored fields. The Level System section only describes one
 * progression track (1 -> 2 -> 3 via upgrade cards), so this class merges
 * them into a single `level` field. Flag if you actually meant two
 * different numbers (e.g. ability tier vs. cosmetic upgrade tier) and
 * I'll split it back out.
 */
public class PetInstance {

    private final UUID petUUID;
    private final UUID ownerUUID;
    private final PetType type;
    private final int slotIndex;

    private int level;
    private double hunger;
    private PetState state;
    private Personality personality;
    private long lastFeedTimestamp;
    private String displayName;
    private final Map<String, Long> abilityCooldowns;
    // the live Bukkit entity currently representing this pet, if any.
    // null when despawned (dead, or owner offline / entity not summoned yet).
    // NOT the same as petUUID - petUUID is permanent, this changes every
    // time the entity is despawned and re-summoned.
    private UUID entityUUID;

    private PetInstance(Builder builder) {
        this.petUUID = builder.petUUID;
        this.ownerUUID = builder.ownerUUID;
        this.type = builder.type;
        this.slotIndex = builder.slotIndex;
        this.level = builder.level;
        this.hunger = builder.hunger;
        this.state = builder.state;
        this.personality = builder.personality;
        this.lastFeedTimestamp = builder.lastFeedTimestamp;
        this.displayName = builder.displayName;
        this.abilityCooldowns = builder.abilityCooldowns;
        this.entityUUID = builder.entityUUID;
    }

    public UUID getPetUUID() {
        return petUUID;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public PetType getType() {
        return type;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getHunger() {
        return hunger;
    }

    public void setHunger(double hunger) {
        this.hunger = hunger;
    }

    public PetState getState() {
        return state;
    }

    public void setState(PetState state) {
        this.state = state;
    }

    public Personality getPersonality() {
        return personality;
    }

    public long getLastFeedTimestamp() {
        return lastFeedTimestamp;
    }

    public void setLastFeedTimestamp(long lastFeedTimestamp) {
        this.lastFeedTimestamp = lastFeedTimestamp;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, Long> getAbilityCooldowns() {
        return abilityCooldowns;
    }

    public UUID getEntityUUID() {
        return entityUUID;
    }

    public void setEntityUUID(UUID entityUUID) {
        this.entityUUID = entityUUID;
    }

    public static Builder builder(UUID petUUID, UUID ownerUUID, PetType type, int slotIndex) {
        return new Builder(petUUID, ownerUUID, type, slotIndex);
    }

    public static class Builder {
        private final UUID petUUID;
        private final UUID ownerUUID;
        private final PetType type;
        private final int slotIndex;

        private int level = 1;
        private double hunger = 100.0;
        private PetState state = PetState.ACTIVE;
        private Personality personality = Personality.LOYAL;
        private long lastFeedTimestamp = System.currentTimeMillis();
        private String displayName = null;
        private Map<String, Long> abilityCooldowns = new HashMap<>();
        private UUID entityUUID = null;

        private Builder(UUID petUUID, UUID ownerUUID, PetType type, int slotIndex) {
            this.petUUID = petUUID;
            this.ownerUUID = ownerUUID;
            this.type = type;
            this.slotIndex = slotIndex;
        }

        public Builder level(int level) {
            this.level = level;
            return this;
        }

        public Builder hunger(double hunger) {
            this.hunger = hunger;
            return this;
        }

        public Builder state(PetState state) {
            this.state = state;
            return this;
        }

        public Builder personality(Personality personality) {
            this.personality = personality;
            return this;
        }

        public Builder lastFeedTimestamp(long lastFeedTimestamp) {
            this.lastFeedTimestamp = lastFeedTimestamp;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder abilityCooldowns(Map<String, Long> abilityCooldowns) {
            this.abilityCooldowns = abilityCooldowns;
            return this;
        }

        public Builder entityUUID(UUID entityUUID) {
            this.entityUUID = entityUUID;
            return this;
        }

        public PetInstance build() {
            return new PetInstance(this);
        }
    }
}
