package com.yashhpenchi.excellentpetutilities.managers;

import com.yashhpenchi.excellentpetutilities.models.PetInstance;
import com.yashhpenchi.excellentpetutilities.storage.PetRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches pets for ONLINE players only. Loaded on join, dropped on quit -
 * this is a bounded cache by design, not a full-database mirror, so it
 * stays cheap even at "thousands of pets" scale.
 *
 * GUI and gameplay code should go through this class, never PetRepository
 * directly. That keeps every SQLite call off the GUI click path.
 */
public class PetManager {

    private final PetRepository petRepository;
    private final Map<UUID, List<PetInstance>> petsByOwner = new ConcurrentHashMap<>();
    private final Map<UUID, PetInstance> petsByUUID = new ConcurrentHashMap<>();

    public PetManager(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    // call on PlayerJoinEvent
    public CompletableFuture<Void> loadPets(UUID ownerUUID) {
        return petRepository.findByOwner(ownerUUID).thenAccept(pets -> {
            petsByOwner.put(ownerUUID, pets);
            for (PetInstance pet : pets) {
                petsByUUID.put(pet.getPetUUID(), pet);
            }
        });
    }

    // call on PlayerQuitEvent - drops the cache entry so memory doesn't grow forever
    public void unloadPets(UUID ownerUUID) {
        List<PetInstance> pets = petsByOwner.remove(ownerUUID);
        if (pets != null) {
            for (PetInstance pet : pets) {
                petsByUUID.remove(pet.getPetUUID());
            }
        }
    }

    // cache read only, safe to call from the main thread / inside click handlers
    public List<PetInstance> getPets(UUID ownerUUID) {
        return petsByOwner.getOrDefault(ownerUUID, List.of());
    }

    public Optional<PetInstance> getPet(UUID petUUID) {
        return Optional.ofNullable(petsByUUID.get(petUUID));
    }

    // persists to SQLite; the passed-in instance is already the cached
    // object (same reference), so the cache reflects the change immediately
    public CompletableFuture<Void> savePet(PetInstance pet) {
        return petRepository.save(pet);
    }

    // call on plugin disable, after pending writes are flushed
    public void clearAll() {
        petsByOwner.clear();
        petsByUUID.clear();
    }
}
