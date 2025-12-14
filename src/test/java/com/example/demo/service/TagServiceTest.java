package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class TagServiceTest {

    private TagService tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagService();
    }

    @Test
    void create_shouldGenerateIdAndStoreTag() {
        Tag tag = Tag.builder().name("Work").build();

        StepVerifier.create(tagService.create(tag))
                .assertNext(created -> {
                    assertNotNull(created.getId());
                    assertEquals("Work", created.getName());
                })
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnTag_whenExists() {
        Tag tag = Tag.builder().name("Personal").build();
        Tag created = tagService.create(tag).block();

        StepVerifier.create(tagService.findById(created.getId()))
                .assertNext(found -> assertEquals("Personal", found.getName()))
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        StepVerifier.create(tagService.findById(999L))
                .verifyComplete();
    }

    @Test
    void findAll_shouldReturnAllTags() {
        tagService.create(Tag.builder().name("Tag 1").build()).block();
        tagService.create(Tag.builder().name("Tag 2").build()).block();

        StepVerifier.create(tagService.findAll().collectList())
                .assertNext(tags -> assertEquals(2, tags.size()))
                .verifyComplete();
    }

    @Test
    void update_shouldMergeName() {
        Tag tag = Tag.builder().name("Original").build();
        Tag created = tagService.create(tag).block();

        Tag updates = Tag.builder().name("Updated").build();

        StepVerifier.create(tagService.update(created.getId(), updates))
                .assertNext(updated -> assertEquals("Updated", updated.getName()))
                .verifyComplete();
    }

    @Test
    void update_shouldReturnNull_whenNotExists() {
        Tag updates = Tag.builder().name("Updated").build();

        StepVerifier.create(tagService.update(999L, updates).filter(result -> result != null))
                .verifyComplete();
    }

    @Test
    void deleteById_shouldRemoveTag() {
        Tag created = tagService.create(Tag.builder().name("Delete Me").build()).block();

        StepVerifier.create(tagService.deleteById(created.getId()))
                .verifyComplete();

        StepVerifier.create(tagService.findById(created.getId()))
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        Tag created = tagService.create(Tag.builder().name("Exists").build()).block();

        StepVerifier.create(tagService.existsById(created.getId()))
                .assertNext(exists -> assertTrue(exists))
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        StepVerifier.create(tagService.existsById(999L))
                .assertNext(exists -> assertFalse(exists))
                .verifyComplete();
    }

    @Test
    void count_shouldReturnNumberOfTags() {
        tagService.create(Tag.builder().name("Tag 1").build()).block();
        tagService.create(Tag.builder().name("Tag 2").build()).block();
        tagService.create(Tag.builder().name("Tag 3").build()).block();

        StepVerifier.create(tagService.count())
                .assertNext(count -> assertEquals(3L, count))
                .verifyComplete();
    }
}
