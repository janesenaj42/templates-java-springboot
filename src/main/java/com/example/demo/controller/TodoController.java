package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.model.Todo;
import com.example.demo.model.TodoStatus;
import com.example.demo.service.CrudService;
import com.example.demo.service.TodoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/todos")
public class TodoController extends CrudController<Todo, Long> {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @Override
    protected CrudService<Todo, Long> getService() {
        return todoService;
    }

    @GetMapping("/status/{status}")
    public Mono<ResponseEntity<ApiResponse<List<Todo>>>> findByStatus(@PathVariable TodoStatus status) {
        return todoService.findByStatus(status)
                .collectList()
                .map(todos -> ResponseEntity.ok(ApiResponse.success(todos)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to fetch todos: " + e.getMessage()))));
    }

    @GetMapping("/tag/{tagId}")
    public Mono<ResponseEntity<ApiResponse<List<Todo>>>> findByTagId(@PathVariable Long tagId) {
        return todoService.findByTagId(tagId)
                .collectList()
                .map(todos -> ResponseEntity.ok(ApiResponse.success(todos)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to fetch todos: " + e.getMessage()))));
    }
}
