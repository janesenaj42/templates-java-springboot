package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.CrudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

/**
 * Abstract REST controller providing standard CRUD endpoints.
 * Extend this class and annotate with @RestController and @RequestMapping.
 *
 * @param <T> Entity type
 * @param <ID> ID type
 */
@Slf4j
public abstract class CrudController<T, ID> {

    protected abstract CrudService<T, ID> getService();

    /**
     * Convert path variable string to ID type. Override for non-Long IDs.
     */
    @SuppressWarnings("unchecked")
    protected ID parseId(String id) {
        return (ID) Long.valueOf(id);
    }

    /**
     * GET / - Retrieve all entities
     */
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<java.util.List<T>>>> findAll() {
        log.debug("GET request to find all entities");
        return getService().findAll()
                .collectList()
                .map(entities -> ResponseEntity.ok(ApiResponse.success(entities)))
                .onErrorResume(e -> {
                    log.error("Error fetching all entities", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("Failed to fetch entities: " + e.getMessage())));
                });
    }

    /**
     * GET /{id} - Retrieve entity by ID
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<T>>> findById(@PathVariable String id) {
        log.debug("GET request to find entity with ID: {}", id);
        ID parsedId = parseId(id);
        return getService().findById(parsedId)
                .map(entity -> ResponseEntity.ok(ApiResponse.success(entity)))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Entity not found with ID: " + id)))
                .onErrorResume(e -> {
                    log.error("Error fetching entity with ID: {}", id, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("Failed to fetch entity: " + e.getMessage())));
                });
    }

    /**
     * POST / - Create new entity
     */
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<T>>> create(@RequestBody T entity) {
        log.debug("POST request to create entity");
        return getService().create(entity)
                .map(created -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(created, "Entity created successfully")))
                .onErrorResume(e -> {
                    log.error("Error creating entity", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("Failed to create entity: " + e.getMessage())));
                });
    }

    /**
     * PUT /{id} - Update existing entity
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<T>>> update(@PathVariable String id, @RequestBody T entity) {
        log.debug("PUT request to update entity with ID: {}", id);
        ID parsedId = parseId(id);
        return getService().update(parsedId, entity)
                .map(updated -> ResponseEntity.ok(ApiResponse.success(updated, "Entity updated successfully")))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Entity not found with ID: " + id)))
                .onErrorResume(e -> {
                    log.error("Error updating entity with ID: {}", id, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("Failed to update entity: " + e.getMessage())));
                });
    }

    /**
     * DELETE /{id} - Delete entity by ID
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteById(@PathVariable String id) {
        log.debug("DELETE request to delete entity with ID: {}", id);
        ID parsedId = parseId(id);
        return getService().existsById(parsedId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.<Void>error("Entity not found with ID: " + id)));
                    }
                    return getService().deleteById(parsedId)
                            .then(Mono.just(ResponseEntity.ok(
                                    ApiResponse.<Void>success(null, "Entity deleted successfully"))));
                })
                .onErrorResume(e -> {
                    log.error("Error deleting entity with ID: {}", id, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("Failed to delete entity: " + e.getMessage())));
                });
    }

    /**
     * GET /count - Get total count of entities
     */
    @GetMapping("/count")
    public Mono<ResponseEntity<ApiResponse<Long>>> count() {
        log.debug("GET request to count all entities");
        return getService().count()
                .map(count -> ResponseEntity.ok(ApiResponse.success(count)))
                .onErrorResume(e -> {
                    log.error("Error counting entities", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.error("Failed to count entities: " + e.getMessage())));
                });
    }
}
