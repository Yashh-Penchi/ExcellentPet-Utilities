package com.yashhpenchi.excellentpetutilities.services;

import com.yashhpenchi.excellentpetutilities.models.PetInstance;
import com.yashhpenchi.excellentpetutilities.models.PetState;
import com.yashhpenchi.excellentpetutilities.storage.PetRepository;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PetLifecycleServiceImpl implements PetLifecycleService {

    private final PetRepository petRepository;

    public PetLifecycleServiceImpl(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Override
    public CompletableFuture<Void> killPet(UUID petUUID) {
        return setState(petUUID, PetState.DEAD);
    }

    @Override
    public CompletableFuture<Void> revivePet(UUID petUUID) {
        return setState(petUUID, PetState.ACTIVE);
    }

    private CompletableFuture<Void> setState(UUID petUUID, PetState newState) {
        return petRepository.findByUUID(petUUID)
                .thenCompose(optionalPet -> {
                    PetInstance pet = optionalPet.orElseThrow(() ->
                            new NoSuchElementException("No pet found with UUID " + petUUID));
                    pet.setState(newState);
                    return petRepository.save(pet);
                });
    }

    @Override
    public void spawnPetEntity(PetInstance pet) {
        // TODO: implemented in the revive-system phase (see interface javadoc
        // for the chosen despawn/re-summon approach). Not needed until the
        // GUI and hunger systems exist first.
        throw new UnsupportedOperationException("spawnPetEntity not implemented yet - revive system phase");
    }

    @Override
    public void despawnPetEntity(PetInstance pet) {
        // TODO: implemented in the revive-system phase.
        throw new UnsupportedOperationException("despawnPetEntity not implemented yet - revive system phase");
    }
}
