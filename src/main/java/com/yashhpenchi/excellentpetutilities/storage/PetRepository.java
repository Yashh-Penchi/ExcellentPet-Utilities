package com.yashhpenchi.excellentpetutilities.storage;

import com.yashhpenchi.excellentpetutilities.models.PetInstance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * All methods run off the main server thread. Callers must hop back to the
 * main thread (Bukkit scheduler) before touching any entity/world/inventory
 * API with the result - this repository only ever touches the database.
 */
public interface PetRepository {

    CompletableFuture<Void> save(PetInstance pet);

    CompletableFuture<Optional<PetInstance>> findByUUID(UUID petUUID);

    CompletableFuture<List<PetInstance>> findByOwner(UUID ownerUUID);

    CompletableFuture<Void> delete(UUID petUUID);

    void close();
}
