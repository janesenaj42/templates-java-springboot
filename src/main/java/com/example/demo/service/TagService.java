package com.example.demo.service;

import com.example.demo.model.Tag;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class TagService extends AbstractCrudService<Tag, Long> {

    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    protected Long getId(Tag entity) {
        return entity.getId();
    }

    @Override
    protected Tag setId(Tag entity, Long id) {
        entity.setId(id);
        return entity;
    }

    @Override
    protected Long generateId() {
        return idGenerator.getAndIncrement();
    }

    @Override
    protected Tag mergeEntity(Tag existing, Tag updates) {
        if (updates.getName() != null) {
            existing.setName(updates.getName());
        }
        return existing;
    }
}
