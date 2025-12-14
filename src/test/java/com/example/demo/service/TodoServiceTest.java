package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.model.Todo;
import com.example.demo.model.TodoStatus;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class TodoServiceTest {

    private TodoService todoService;

    @BeforeEach
    void setUp() {
        todoService = new TodoService();
    }

    @Test
    void create_shouldGenerateIdAndStoreTodo() {
        Todo todo = Todo.builder()
                .title("Test Todo")
                .description("Test Description")
                .status(TodoStatus.TODO)
                .build();

        StepVerifier.create(todoService.create(todo))
                .assertNext(created -> {
                    assertNotNull(created.getId());
                    assertEquals("Test Todo", created.getTitle());
                    assertEquals("Test Description", created.getDescription());
                    assertEquals(TodoStatus.TODO, created.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnTodo_whenExists() {
        Todo todo = Todo.builder().title("Find Me").status(TodoStatus.TODO).build();

        Todo created = todoService.create(todo).block();

        StepVerifier.create(todoService.findById(created.getId()))
                .assertNext(found -> assertEquals("Find Me", found.getTitle()))
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        StepVerifier.create(todoService.findById(999L))
                .verifyComplete();
    }

    @Test
    void findAll_shouldReturnAllTodos() {
        todoService.create(Todo.builder().title("Todo 1").status(TodoStatus.TODO).build()).block();
        todoService.create(Todo.builder().title("Todo 2").status(TodoStatus.IN_PROGRESS).build()).block();

        StepVerifier.create(todoService.findAll().collectList())
                .assertNext(todos -> assertEquals(2, todos.size()))
                .verifyComplete();
    }

    @Test
    void update_shouldMergeFields() {
        Todo todo = Todo.builder()
                .title("Original")
                .description("Original Desc")
                .status(TodoStatus.TODO)
                .build();
        Todo created = todoService.create(todo).block();

        Todo updates = Todo.builder()
                .title("Updated")
                .status(TodoStatus.IN_PROGRESS)
                .build();

        StepVerifier.create(todoService.update(created.getId(), updates))
                .assertNext(updated -> {
                    assertEquals("Updated", updated.getTitle());
                    assertEquals("Original Desc", updated.getDescription());
                    assertEquals(TodoStatus.IN_PROGRESS, updated.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void update_shouldReturnNull_whenNotExists() {
        Todo updates = Todo.builder().title("Updated").build();

        StepVerifier.create(todoService.update(999L, updates).filter(result -> result != null))
                .verifyComplete();
    }

    @Test
    void deleteById_shouldRemoveTodo() {
        Todo created = todoService.create(
                Todo.builder().title("Delete Me").status(TodoStatus.TODO).build()
        ).block();

        StepVerifier.create(todoService.deleteById(created.getId()))
                .verifyComplete();

        StepVerifier.create(todoService.findById(created.getId()))
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        Todo created = todoService.create(
                Todo.builder().title("Exists").status(TodoStatus.TODO).build()
        ).block();

        StepVerifier.create(todoService.existsById(created.getId()))
                .assertNext(exists -> assertTrue(exists))
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        StepVerifier.create(todoService.existsById(999L))
                .assertNext(exists -> assertFalse(exists))
                .verifyComplete();
    }

    @Test
    void count_shouldReturnNumberOfTodos() {
        todoService.create(Todo.builder().title("Todo 1").status(TodoStatus.TODO).build()).block();
        todoService.create(Todo.builder().title("Todo 2").status(TodoStatus.TODO).build()).block();

        StepVerifier.create(todoService.count())
                .assertNext(count -> assertEquals(2L, count))
                .verifyComplete();
    }

    @Test
    void findByStatus_shouldFilterByStatus() {
        todoService.create(Todo.builder().title("Todo 1").status(TodoStatus.TODO).build()).block();
        todoService.create(Todo.builder().title("Todo 2").status(TodoStatus.IN_PROGRESS).build()).block();
        todoService.create(Todo.builder().title("Todo 3").status(TodoStatus.TODO).build()).block();

        StepVerifier.create(todoService.findByStatus(TodoStatus.TODO).collectList())
                .assertNext(todos -> {
                    assertEquals(2, todos.size());
                    todos.forEach(todo -> assertEquals(TodoStatus.TODO, todo.getStatus()));
                })
                .verifyComplete();
    }

    @Test
    void findByTagId_shouldFilterByTag() {
        todoService.create(Todo.builder().title("Todo 1").status(TodoStatus.TODO).tagIds(Set.of(1L, 2L)).build()).block();
        todoService.create(Todo.builder().title("Todo 2").status(TodoStatus.TODO).tagIds(Set.of(2L, 3L)).build()).block();
        todoService.create(Todo.builder().title("Todo 3").status(TodoStatus.TODO).tagIds(Set.of(3L)).build()).block();

        StepVerifier.create(todoService.findByTagId(2L).collectList())
                .assertNext(todos -> {
                    assertEquals(2, todos.size());
                    todos.forEach(todo -> assertTrue(todo.getTagIds().contains(2L)));
                })
                .verifyComplete();
    }

    @Test
    void create_shouldHandleDeadline() {
        Instant deadline = Instant.now().plusSeconds(3600);
        Todo todo = Todo.builder()
                .title("With Deadline")
                .status(TodoStatus.TODO)
                .deadline(deadline)
                .build();

        StepVerifier.create(todoService.create(todo))
                .assertNext(created -> assertEquals(deadline, created.getDeadline()))
                .verifyComplete();
    }
}
