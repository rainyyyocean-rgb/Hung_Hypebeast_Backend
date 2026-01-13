package org.example.hung_hypebeast_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.response.CategoryResponse;
import org.example.hung_hypebeast_backend.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        // Convert List<Entity> -> List<DTO>
        List<CategoryResponse> categories = categoryRepository.findAll().stream()
                .map(cat -> CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .build())
                .toList(); // Java 16+ có toList(), nếu lỗi dùng .collect(Collectors.toList())

        return ResponseEntity.ok(categories);
    }
}
