package com.example.demo.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Abstract base service providing generic CRUD operations.
 * Supports both reactive (Mono/Flux) and blocking (Optional/List) patterns.
 *
 * @param <T> Entity type
 * @param <ID> ID type (e.g., Long, UUID, String)
 */
public interface CrudService<T, ID> {

    /**
     * Create a new entity.
     */
    Mono<T> create(T entity);

    /**
     * Find entity by ID.
     */
    Mono<T> findById(ID id);

    /**
     * Find all entities.
     */
    Flux<T> findAll();

    /**
     * Update an existing entity.
     */
    Mono<T> update(ID id, T entity);

    /**
     * Delete entity by ID.
     */
    Mono<Void> deleteById(ID id);

    /**
     * Check if entity exists by ID.
     */
    Mono<Boolean> existsById(ID id);

    /**
     * Count all entities.
     */
    Mono<Long> count();
}
