package org.example.hung_hypebeast_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Category", description = "API quản lí category")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả categories", description = "Trả về danh sách tất cả danh mục sản phẩm trong hệ thống")
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
