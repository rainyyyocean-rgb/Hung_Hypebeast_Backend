package org.example.hung_hypebeast_backend.service;

import org.example.hung_hypebeast_backend.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    Page<ProductResponse> getProducts(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    ProductResponse getProductById(Long id);

    /**
     * Lấy danh sách sản phẩm sử dụng Specification để filter
     * @param categoryId - ID của category (có thể null)
     * @param minPrice - Giá tối thiểu (có thể null)
     * @param maxPrice - Giá tối đa (có thể null)
     * @param keyword - Từ khóa tìm kiếm trong tên sản phẩm (có thể null)
     * @param pageable - Thông tin phân trang và sắp xếp
     * @return Page<ProductResponse>
     */
    Page<ProductResponse> getProductsWithSpecification(Long categoryId, BigDecimal minPrice,
                                                        BigDecimal maxPrice, String keyword, Pageable pageable);
}
