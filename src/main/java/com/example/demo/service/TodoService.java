package com.example.demo.service;

import com.example.demo.model.Todo;
import com.example.demo.model.TodoStatus;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class TodoService extends AbstractCrudService<Todo, Long> {

    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    protected Long getId(Todo entity) {
        return entity.getId();
    }

    @Override
    protected Todo setId(Todo entity, Long id) {
        entity.setId(id);
        return entity;
    }

    @Override
    protected Long generateId() {
        return idGenerator.getAndIncrement();
    }

    @Override
    protected Todo mergeEntity(Todo existing, Todo updates) {
        if (updates.getTitle() != null) {
            existing.setTitle(updates.getTitle());
        }
        if (updates.getDescription() != null) {
            existing.setDescription(updates.getDescription());
        }
        if (updates.getStatus() != null) {
            existing.setStatus(updates.getStatus());
        }
        if (updates.getDeadline() != null) {
            existing.setDeadline(updates.getDeadline());
        }
        if (updates.getTagIds() != null) {
            existing.setTagIds(new HashSet<>(updates.getTagIds()));
        }
        return existing;
    }

    public Flux<Todo> findByStatus(TodoStatus status) {
        return Flux.fromIterable(store.values())
                .filter(todo -> todo.getStatus() == status);
    }

    public Flux<Todo> findByTagId(Long tagId) {
        return Flux.fromIterable(store.values())
                .filter(todo -> todo.getTagIds() != null && todo.getTagIds().contains(tagId));
    }
}
