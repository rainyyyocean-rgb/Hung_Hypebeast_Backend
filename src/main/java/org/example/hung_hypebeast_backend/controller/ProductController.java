package org.example.hung_hypebeast_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.response.ProductResponse;
import org.example.hung_hypebeast_backend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "API quản lí sản phẩm")
public class ProductController {

    // Inject Interface, Spring sẽ tự tìm class Impl tương ứng
    private final ProductService productService;

    @GetMapping
    @Operation(
            summary = "Lấy danh sách sản phẩm với phân trang và lọc",
            description = "Trả về danh sách sản phẩm có hỗ trợ lọc theo category, khoảng giá, phân trang và sắp xếp"
    )
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ResponseEntity.ok(productService.getProducts(categoryId, minPrice, maxPrice, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy chi tiết sản phẩm theo ID",
            description = "Trả về thông tin chi tiết của một sản phẩm bao gồm tất cả SKUs (màu sắc, size, giá, tồn kho)"
    )
    public ResponseEntity<ProductResponse> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}
