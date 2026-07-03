package com.yashhpenchi.excellentpetutilities.services;

import com.yashhpenchi.excellentpetutilities.models.PetInstance;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Owns pet state transitions (alive <-> dead) and, later, the actual
 * entity spawning/despawning that goes with them.
 *
 * killPet/revivePet only flip PetState and persist it for now - they do NOT
 * touch any Bukkit entity yet. spawnPetEntity/despawnPetEntity are stubbed
 * (see PetLifecycleServiceImpl) until the revive system phase, where we're
 * going with despawn-on-death + re-summon-on-revive (chosen over freezing a
 * corpse entity in the world - far less risk of chunk-unload / entity-leak
 * bugs at the "thousands of pets" scale this plugin is meant to support).
 */
public interface PetLifecycleService {

    CompletableFuture<Void> killPet(UUID petUUID);

    CompletableFuture<Void> revivePet(UUID petUUID);

    // must be called on the main thread - touches the Bukkit entity API
    void spawnPetEntity(PetInstance pet);

    // must be called on the main thread - touches the Bukkit entity API
    void despawnPetEntity(PetInstance pet);
}
