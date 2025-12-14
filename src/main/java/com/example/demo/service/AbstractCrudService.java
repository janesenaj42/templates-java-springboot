package com.example.demo.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Abstract implementation of CrudService with in-memory storage.
 * Extend this class and override methods to integrate with actual data stores
 * (e.g., R2DBC, MongoDB Reactive, etc.).
 *
 * @param <T> Entity type
 * @param <ID> ID type
 */
@Slf4j
public abstract class AbstractCrudService<T, ID> implements CrudService<T, ID> {

    protected final Map<ID, T> store = new ConcurrentHashMap<>();

    /**
     * Extract ID from entity. Must be implemented by subclasses.
     */
    protected abstract ID getId(T entity);

    /**
     * Set ID on entity. Must be implemented by subclasses.
     */
    protected abstract T setId(T entity, ID id);

    /**
     * Generate a new ID. Must be implemented by subclasses.
     */
    protected abstract ID generateId();

    /**
     * Merge updates from source entity to target. Override for custom merge logic.
     */
    protected abstract T mergeEntity(T existing, T updates);

    @Override
    public Mono<T> create(T entity) {
        return Mono.fromCallable(() -> {
            ID id = generateId();
            T entityWithId = setId(entity, id);
            store.put(id, entityWithId);
            log.debug("Created entity with ID: {}", id);
            return entityWithId;
        });
    }

    @Override
    public Mono<T> findById(ID id) {
        return Mono.justOrEmpty(store.get(id));
    }

    @Override
    public Flux<T> findAll() {
        return Flux.fromIterable(store.values());
    }

    @Override
    public Mono<T> update(ID id, T entity) {
        return Mono.fromCallable(() -> {
            AtomicReference<T> result = new AtomicReference<>();
            store.computeIfPresent(id, (key, existing) -> {
                T merged = mergeEntity(existing, entity);
                T updated = setId(merged, id);
                result.set(updated);
                return updated;
            });
            if (result.get() == null) {
                log.warn("Entity not found for update with ID: {}", id);
            } else {
                log.debug("Updated entity with ID: {}", id);
            }
            return result.get();
        });
    }

    @Override
    public Mono<Void> deleteById(ID id) {
        return Mono.fromRunnable(() -> {
            T removed = store.remove(id);
            if (removed != null) {
                log.debug("Deleted entity with ID: {}", id);
            } else {
                log.warn("Entity not found for deletion with ID: {}", id);
            }
        });
    }

    @Override
    public Mono<Boolean> existsById(ID id) {
        return Mono.just(store.containsKey(id));
    }

    @Override
    public Mono<Long> count() {
        return Mono.just((long) store.size());
    }
}
