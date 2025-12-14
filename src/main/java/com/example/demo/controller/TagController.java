package com.example.demo.controller;

import com.example.demo.model.Tag;
import com.example.demo.service.CrudService;
import com.example.demo.service.TagService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
public class TagController extends CrudController<Tag, Long> {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @Override
    protected CrudService<Tag, Long> getService() {
        return tagService;
    }
}
